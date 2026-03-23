---
name: write-tests
description: "Escrevendo testes para o ciclo de desenvolvimento. Use para cobrir domain, application e adapters com JUnit 5 e Mockito, definindo estratégia por camada. Não use para testar GUI/TUI diretamente (prefira write-unit-test para application/domain) nem para empurrar shell real para a suíte."
---

# Skill: Escrever Testes

## Quando usar

- Definir estratégia de cobertura por camada
- Criar testes de domain
- Criar testes de use cases
- Criar testes de adapters de infrastructure com isolamento adequado

## Estratégia por camada

### Domain

- Testes puros e rápidos, sem mocks quando não houver dependência externa
- Foco em invariantes, composição e estados válidos/inválidos
- Devem compilar e passar sem JVM com módulos JavaFX ou runtime de processo

### Application

- Mockar todos os ports (`CommandRunner`, `EnvironmentProfile`, `PrivilegeManager`)
- Validar orquestração, política de erro e composição de resultados
- Sem chamada de shell real em nenhuma hipótese

### Infrastructure

- Testar mapeamentos, parsing e adaptação técnica (ex: parsing de exit code, stdout/stderr)
- Isolar `ProcessBuilder` quando possível; se não, separar em teste de integração
- Marcar testes que exigem Linux real com tag ou classe separada

### Interface

- Priorizar testes de wiring mínimo e delegação correta ao use case
- Evitar testes de GUI pesados (headless JavaFX) cedo demais no projeto
- Garantir que interface delega — não reimplementa — o comportamento esperado

## Instruções

- [ ] Classe de teste no pacote espelho da classe testada
- [ ] Nome claro de cenário (ex: `shouldReturnFailureWhenActionNotSupported`)
- [ ] Happy path coberto
- [ ] Falha relevante coberta (ex: ação não suportada, timeout, step falho)
- [ ] Caso de borda relevante coberto
- [ ] Nenhum teste depende do ambiente Linux real sem necessidade explícita e marcação

## Critical

- Shell real em teste de domain/application indica port não mockado — revise a injeção de dependência
- Misturar teste de unidade com validação de ambiente local torna o CI não reproduzível
- GUI/TUI como único ponto de validação de regra deixa domain e application sem cobertura real
- Teste frágil demais geralmente indica que a arquitetura está vazando entre camadas

## Exemplos

### Exemplo 1: Teste de domain

Validar que `ExecutionPlan` rejeita step vazio ou ordem inválida.

Ações:
1. Criar `ExecutionPlanTest` em `src/test/java/org/fixkitty/core/...`
2. Sem mocks — domain é puro
3. Testar construção válida, inválida e borda (plan vazio)

### Exemplo 2: Teste de use case

Mockar `EnvironmentDetector`, `ActionCatalog` e `CommandRunner` para validar `ExecuteFixActionUseCase`.

Ações:
1. `when(profile.isSupported(AUDIO)).thenReturn(true)`
2. `when(runner.run(any())).thenReturn(StepResult.ok())`
3. Validar `result.isSuccess()` e steps retornados

### Exemplo 3: Teste de adapter

Validar que `Ubuntu24Profile` monta corretamente a lista de ações suportadas e requisitos de privilégio.

Ações:
1. Instanciar `Ubuntu24Profile` diretamente (sem mock)
2. Verificar `isSupported(AUDIO) == true`
3. Verificar que o comando mapeado é o esperado

## Troubleshooting

**Teste quebra em CI mas passa localmente**
- Causa: dependência de ambiente Linux real (serviço, `/etc/os-release`, etc.)
- Solução: Separar em classe de integração; mockar environment no teste de unidade

**Teste acidentalmente executa comando real**
- Causa: `CommandRunner` não mockado — injeção via construtor não configurada
- Solução: Verificar `@Mock` e `@ExtendWith(MockitoExtension.class)`; checar construtor do use case

## Consulte também

- [layer-domain](../layer-domain/SKILL.md)
- [layer-application](../layer-application/SKILL.md)
- [layer-infrastructure](../layer-infrastructure/SKILL.md)
- [write-unit-test](../write-unit-test/SKILL.md) — checklist detalhado para testes de use case
- [enforce-architecture](../enforce-architecture/SKILL.md)
