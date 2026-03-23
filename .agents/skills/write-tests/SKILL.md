---
name: write-tests
description: "Escrevendo testes para o ciclo de desenvolvimento inteiro. Use para cobrir domain, application e adapters com JUnit 5, Mockito e testes focados por camada. Não use para empurrar shell real ou dependências frágeis para a suíte."
---

# Skill: Escrever Testes

## Quando usar

- Criar testes de domain
- Criar testes de use cases
- Criar testes de adapters de infrastructure com isolamento adequado
- Definir estratégia de cobertura por camada

## Estratégia por camada

### Domain

- Testes puros e rápidos
- Sem mocks quando não houver dependência externa
- Foco em invariantes, composição e estados válidos/ inválidos

### Application

- Mockar ports
- Validar orquestração, política de erro e composição de resultados
- Nunca chamar shell real

### Infrastructure

- Testar mapeamentos, parsing e adaptação técnica
- Isolar processos quando possível
- Se houver integração mais pesada, separar claramente de unit tests

### Interface

- Priorizar testes de comportamento fino e wiring mínimo
- Não usar testes GUI pesados cedo demais
- Garantir que interface delega corretamente ao use case

## Checklist

- [ ] Classe de teste no pacote espelho da classe testada
- [ ] Nome claro de cenários
- [ ] Happy path coberto
- [ ] Falha relevante coberta
- [ ] Caso de borda relevante coberto
- [ ] Nenhum teste depende do ambiente Linux real sem necessidade explícita

## Critical

- NUNCA use shell real para testar application/domain
- NUNCA misture teste de unidade com validação de ambiente local
- NUNCA use GUI/TUI como único ponto de validação da regra
- Se o teste for frágil demais, a arquitetura provavelmente está vazando

## Exemplos

### Exemplo 1: Teste de domain

Validar que `ExecutionPlan` rejeita step vazio ou ordem inválida.

### Exemplo 2: Teste de use case

Mockar `EnvironmentDetector`, `ActionCatalog` e `CommandRunner` para validar `ExecuteFixActionUseCase`.

### Exemplo 3: Teste de adapter

Validar que `Ubuntu24Profile` monta corretamente a lista de ações suportadas e requisitos de privilégio.

## Consulte também

- [../layer-domain/SKILL.md](../layer-domain/SKILL.md)
- [../layer-application/SKILL.md](../layer-application/SKILL.md)
- [../layer-infrastructure/SKILL.md](../layer-infrastructure/SKILL.md)
- [../enforce-architecture/SKILL.md](../enforce-architecture/SKILL.md)