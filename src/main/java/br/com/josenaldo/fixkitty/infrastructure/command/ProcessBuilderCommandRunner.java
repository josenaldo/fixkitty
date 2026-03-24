package br.com.josenaldo.fixkitty.infrastructure.command;

import br.com.josenaldo.fixkitty.core.domain.*;
import br.com.josenaldo.fixkitty.core.ports.CommandRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.concurrent.*;

/**
 * Executes OS commands using {@link ProcessBuilder}.
 *
 * <p>Captures stdout and stderr concurrently to avoid deadlocks.
 * Enforces the step's timeout and terminates the process forcibly on overflow.
 * Never throws for execution failures — all outcomes are captured in
 * the returned {@link StepResult}.
 */
public class ProcessBuilderCommandRunner implements CommandRunner {

    private static final Logger log = LoggerFactory.getLogger(ProcessBuilderCommandRunner.class);

    /**
     * Executes the command in the given step and returns a structured result.
     *
     * @param step the step to execute; the command array is used as-is
     * @return a fully populated result; never {@code null}
     */
    @Override
    public StepResult execute(ExecutionStep step) {
        long startMs = System.currentTimeMillis();
        try {
            ProcessBuilder pb = new ProcessBuilder(step.command());
            pb.redirectErrorStream(false);
            Process process = pb.start();

            // Drain streams concurrently to prevent deadlock
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
            Future<String> stdoutFuture = executor.submit(() -> drain(process.getInputStream()));
            Future<String> stderrFuture = executor.submit(() -> drain(process.getErrorStream()));

            boolean finished = process.waitFor(step.timeoutSeconds(), TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                executor.shutdownNow();
                long duration = System.currentTimeMillis() - startMs;
                log.warn("Command timed out after {}s: {}", step.timeoutSeconds(), step.id());
                return new StepResult(step, StepStatus.TIMEOUT, -1, "", "",
                        duration, "Command timed out after " + step.timeoutSeconds() + " seconds");
            }

            String stdout = stdoutFuture.get();
            String stderr = stderrFuture.get();
            executor.shutdown();

            int exitCode = process.exitValue();
            long duration = System.currentTimeMillis() - startMs;
            StepStatus status = exitCode == 0 ? StepStatus.SUCCESS : StepStatus.FAILED;
            String message = exitCode == 0
                    ? "Completed successfully"
                    : "Command exited with code " + exitCode;

            log.debug("Command {} finished with exit code {} in {}ms", step.id(), exitCode, duration);
            return new StepResult(step, status, exitCode, stdout, stderr, duration, message);

        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startMs;
            log.error("Failed to start command {}: {}", step.id(), e.getMessage());
            return new StepResult(step, StepStatus.FAILED, -1, "", e.getMessage(),
                    duration, "Failed to start command: " + e.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            long duration = System.currentTimeMillis() - startMs;
            return new StepResult(step, StepStatus.FAILED, -1, "", e.getMessage(),
                    duration, "Execution interrupted");
        }
    }

    private String drain(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            log.warn("Error reading process stream: {}", e.getMessage());
        }
        return sb.toString().stripTrailing();
    }
}
