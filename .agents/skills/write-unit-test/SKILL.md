---
name: write-unit-test
description: "Escrevendo testes unitários para Use Cases e Domain. Use para testar orquestração de recovery, comportamento de RecoveryResult, mocks de PrivilegeManager e CommandRunner. Não use para testar GUI/TUI diretamente."
---

# Skill: Escrever Teste Unitário (Micro-Skill)

## Quando usar

- Criar testes para novo Use Case
- Testar entidades de domain (RecoveryAction, EnvironmentProfile)
- Mockar `CommandRunner` e `PrivilegeManager` para isolar testes
- Validar que `RecoveryResult` contém steps e status corretos

## Contexto do projeto

- **Framework**: JUnit 5 (`org.junit.jupiter`)
- **Mocks**: Mockito (adicionar dependência em `build.gradle.kts` quando necessário)
- **Path**: `src/test/java/org/fixkitty/...`
- **Convenção de nome**: `{Classe}Test.java` no mesmo subpackage da classe testada

---

## Checklist

### 1. Criar classe de teste

- [ ] `src/test/java/org/fixkitty/application/usecases/{acao}/Fix{Acao}UseCaseTest.java`
- [ ] Anotada com `@ExtendWith(MockitoExtension.class)` se usar mocks
- [ ] `@BeforeEach` configura mocks comuns

### 2. Definir cenários (mínimo 3)

- [ ] **Happy path**: execução bem-sucedida, `RecoveryResult.isSuccess() == true`
- [ ] **Falha parcial**: um step falha, outros continuam (ou param, conforme política)
- [ ] **Ação não suportada**: `profile.isSupported(actionId)` retorna false → resultado com erro claro

### 3. Mockar dependências externas

- [ ] `CommandRunner runner = mock(CommandRunner.class)` — nunca chame shell real
- [ ] `EnvironmentProfile profile = mock(EnvironmentProfile.class)` — controle de `isSupported()`
- [ ] `PrivilegeManager privilege = mock(PrivilegeManager.class)` — conforme MEMORY.md concern

### 4. Assertions

- [ ] `assertNotNull(result)` — result nunca nulo
- [ ] `assertTrue/assertFalse(result.isSuccess())`
- [ ] `assertEquals(expectedSteps, result.steps().size())`
- [ ] Para erros: `assertNotNull(result.errorMessage())`

---

## Critical

- NUNCA chame `ProcessBuilder`, `Runtime.exec()` ou qualquer shell em testes
- NUNCA teste GUI (JavaFX/TUI) nesta skill — esses testes são de unidade da camada application/domain
- Testes DEVEM ser reproduzíveis sem Java FX runtime (sem `--module-path`)
- Se precisar de `@DisplayName`, use — melhora legibilidade no relatório

## Exemplos

### Exemplo 1: Teste de FixAudioUseCase — happy path

```java
@Test
@DisplayName("Fix Audio: should return success when all steps complete")
void shouldReturnSuccessWhenAudioRestored() {
    when(profile.isSupported(ActionId.AUDIO)).thenReturn(true);
    when(runner.run(any())).thenReturn(StepResult.ok());

    RecoveryResult result = useCase.execute();

    assertTrue(result.isSuccess());
    assertFalse(result.steps().isEmpty());
}
```

### Exemplo 2: Teste de ação não suportada

```java
@Test
@DisplayName("Fix Audio: should fail gracefully when not supported")
void shouldFailWhenNotSupported() {
    when(profile.isSupported(ActionId.AUDIO)).thenReturn(false);

    RecoveryResult result = useCase.execute();

    assertFalse(result.isSuccess());
    assertNotNull(result.errorMessage());
    assertTrue(result.errorMessage().contains("not supported"));
}
```

## Troubleshooting

**Teste acidentalmente executa comando real no sistema**
- Causa: mock não configurado ou injeção errada no use case
- Solução: Verificar que `CommandRunner` foi `@Mock`, não `new CommandRunner()`; checar construtor do use case

**Teste depende da ordem de execução**
- Causa: estado compartilhado entre testes
- Solução: Mover setup para `@BeforeEach`; garantir que mocks são reiniciados por teste

**JavaFX HeadlessException em teste**
- Causa: teste acidentalmente instancia controller GUI
- Solução: Remover import de GUI do teste; testar apenas camada application/domain

## Consulte também

- [create-use-case](../create-use-case/SKILL.md) — O que está sendo testado
- [enforce-architecture](../enforce-architecture/SKILL.md) — Validação final obrigatória
