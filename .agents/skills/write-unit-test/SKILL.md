---
name: write-unit-test
description: "Escrevendo testes unitários para Use Cases e Domain. Use para testar orquestração de recovery, comportamento de RecoveryResult e mocks de PrivilegeManager e CommandRunner. Não use para testar GUI/TUI (headless JavaFX) nem para testes de integração com shell real."
---

# Skill: Escrever Teste Unitário (Micro-Skill)

## Quando usar

- Criar testes para novo Use Case
- Testar entidades de domain (`RecoveryAction`, `EnvironmentProfile`)
- Mockar `CommandRunner` e `PrivilegeManager` para isolar testes
- Validar que `RecoveryResult` contém steps e status corretos

## Contexto do projeto

- **Framework**: JUnit 5 (`org.junit.jupiter`)
- **Mocks**: Mockito (adicionar dependência em `build.gradle.kts` quando necessário)
- **Path**: `src/test/java/org/fixkitty/...`
- **Convenção**: `{Classe}Test.java` no mesmo subpackage da classe testada

## Instruções

### 1. Criar classe de teste

- [ ] `src/test/java/org/fixkitty/application/usecases/{acao}/Fix{Acao}UseCaseTest.java`
- [ ] Anotada com `@ExtendWith(MockitoExtension.class)` se usar mocks
- [ ] `@BeforeEach` configura mocks comuns

### 2. Definir cenários (mínimo 3)

- [ ] **Happy path**: execução bem-sucedida, `RecoveryResult.isSuccess() == true`
- [ ] **Falha parcial**: um step falha, outros continuam (ou param, conforme política)
- [ ] **Ação não suportada**: `profile.isSupported(actionId)` retorna `false` → resultado com erro claro

### 3. Mockar dependências externas

- [ ] `CommandRunner runner = mock(CommandRunner.class)` — nunca chame shell real
- [ ] `EnvironmentProfile profile = mock(EnvironmentProfile.class)` — controle de `isSupported()`
- [ ] `PrivilegeManager privilege = mock(PrivilegeManager.class)` — conforme concern do MEMORY.md

### 4. Assertions

- [ ] `assertNotNull(result)` — result nunca nulo
- [ ] `assertTrue/assertFalse(result.isSuccess())`
- [ ] `assertEquals(expectedSteps, result.steps().size())`
- [ ] Para erros: `assertNotNull(result.errorMessage())`

## Critical

- Chamar `ProcessBuilder` ou `Runtime.exec()` em teste de unidade invalida o isolamento — use mock
- Testar GUI (JavaFX/TUI) nesta skill foge do escopo — use teste de comportamento separado
- Testes de unidade devem passar sem `--module-path` de JavaFX runtime
- Estado compartilhado entre testes causa falhas por ordem de execução — reset em `@BeforeEach`

## Exemplos

### Exemplo 1: Happy path — Fix Audio

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

### Exemplo 2: Acao nao suportada

```java
@Test
@DisplayName("Fix Audio: should fail gracefully when not supported")
void shouldFailWhenNotSupported() {
    when(profile.isSupported(ActionId.AUDIO)).thenReturn(false);

    RecoveryResult result = useCase.execute();

    assertFalse(result.isSuccess());
    assertNotNull(result.errorMessage());
}
```

## Troubleshooting

**Teste acidentalmente executa comando real no sistema**
- Causa: mock não configurado ou `CommandRunner` instanciado diretamente no use case
- Solução: Verificar que `CommandRunner` foi `@Mock`, não `new ProcessBuilderCommandRunner()`; checar construtor do use case

**Teste depende da ordem de execução**
- Causa: estado compartilhado entre testes
- Solução: Mover setup para `@BeforeEach`; garantir que mocks são reiniciados por teste

**`JavaFX HeadlessException` em teste**
- Causa: teste acidentalmente instancia controller GUI
- Solução: Remover import de GUI do teste; testar apenas camada application/domain

## Consulte também

- [create-use-case](../create-use-case/SKILL.md) — o que está sendo testado
- [write-tests](../write-tests/SKILL.md) — estratégia de cobertura por camada
- [enforce-architecture](../enforce-architecture/SKILL.md) — validação final obrigatória
