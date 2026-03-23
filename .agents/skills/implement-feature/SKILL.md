---
name: implement-feature
description: "Meta-skill para implementar uma feature ponta a ponta sem acoplar camadas. Use quando a tarefa exigir atravessar domain, application, infrastructure e interfaces em sequência controlada. Não use para mudanças isoladas a uma única camada — use a skill de camada correspondente diretamente."
---

# Skill: Implementar Feature (Meta-Skill)

## Quando usar

- A tarefa exige mudança em mais de uma camada
- Você está adicionando capacidade nova ao sistema, não só ajustando um adapter isolado
- Precisa de um fluxo disciplinado de implementação de ponta a ponta

## Fluxo obrigatório

### 1. Modelar no domain

Use [layer-domain](../layer-domain/SKILL.md) para:
- criar ou ajustar conceitos
- definir ports necessários
- manter o problema modelado sem tecnologia concreta

### 2. Orquestrar na application

Use [layer-application](../layer-application/SKILL.md) para:
- criar/alterar use cases
- definir fluxo, sequência e política de falha

### 3. Implementar adapters em infrastructure

Use [layer-infrastructure](../layer-infrastructure/SKILL.md) para:
- implementar ports
- integrar com Linux real
- encapsular diferenças de ambiente

### 4. Expor na interface adequada

Use [layer-interface](../layer-interface/SKILL.md) para:
- conectar GUI, TUI ou bootstrap
- apresentar resultado sem duplicar regra

### 5. Cobrir com testes

Use [write-tests](../write-tests/SKILL.md) para:
- validar domain
- cobrir use cases
- testar adapters e wiring essencial

### 6. Validar arquitetura

Use [enforce-architecture](../enforce-architecture/SKILL.md) por último — obrigatório.

## Regras de execução

- Não comece pela interface se a feature muda regra do sistema
- Não deixe adapter concreto ditar o modelo do domínio
- Não pule testes de application quando a feature muda fluxo
- Não encerre a tarefa sem passar pela validação arquitetural

## Exemplo

Tarefa: adicionar nova capacidade de recuperação.

Sequência:
1. Definir tipos e ports necessários no domain
2. Criar use case correspondente
3. Implementar profile/runner/adapters necessários
4. Expor em TUI e GUI
5. Escrever testes por camada
6. Rodar a validação de arquitetura

## Consulte também

- [layer-domain](../layer-domain/SKILL.md)
- [layer-application](../layer-application/SKILL.md)
- [layer-infrastructure](../layer-infrastructure/SKILL.md)
- [layer-interface](../layer-interface/SKILL.md)
- [write-tests](../write-tests/SKILL.md)
- [enforce-architecture](../enforce-architecture/SKILL.md)
