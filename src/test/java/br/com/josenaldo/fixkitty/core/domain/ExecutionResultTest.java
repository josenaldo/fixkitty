package br.com.josenaldo.fixkitty.core.domain;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ExecutionResultTest {

    @Test
    void isSuccess_whenStatusSuccess_returnsTrue() {
        ExecutionResult result = new ExecutionResult(
            RecoveryAction.FIX_AUDIO, ResultStatus.SUCCESS, List.of(),
            Instant.now(), Instant.now(), null);
        assertTrue(result.isSuccess());
    }

    @Test
    void isSuccess_whenStatusFailed_returnsFalse() {
        ExecutionResult result = new ExecutionResult(
            RecoveryAction.FIX_AUDIO, ResultStatus.FAILED, List.of(),
            Instant.now(), Instant.now(), "Try again.");
        assertFalse(result.isSuccess());
    }

    @Test
    void recommendation_canBeNull() {
        ExecutionResult result = new ExecutionResult(
            RecoveryAction.FIX_AUDIO, ResultStatus.SUCCESS, List.of(),
            Instant.now(), Instant.now(), null);
        assertNull(result.recommendation());
    }
}
