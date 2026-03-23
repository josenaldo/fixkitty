---
name: layer-domain
description: "Trabalhando na camada de domain/core. Use para criar ou alterar entidades, value objects, enums, resultados de execução e ports centrais do problema. Não use para orquestração, adapters ou UI."
---

# Skill: Camada Domain

## Quando usar

- Criar ou alterar entidades em `org.fixkitty.core.*`
- Modelar `ActionId`, `ExecutionPlan`, `ExecutionStep`, `RecoveryResult`, `StepResult`
- Criar value objects, enums e contratos centrais do domínio
- Criar ports que representam necessidades do domínio ou da aplicação

## Responsabilidade da camada

Domain descreve o problema, não a implementação.

Esta camada pode definir:
- conceitos do sistema
- regras de consistência
- tipos de entrada e saída centrais
- interfaces/ports que outras camadas implementarão

Esta camada não pode conhecer:
- JavaFX
- TUI/TTY
- `ProcessBuilder`
- `systemctl`, `journalctl`, `sudo`
- Ubuntu, Fedora ou GNOME como implementação concreta

## Checklist

### 1. Modelar conceitos puros

- [ ] Arquivo em `src/main/java/org/fixkitty/core/...`
- [ ] Nome do tipo expressa conceito do problema, não da tecnologia
- [ ] API pequena e explícita
- [ ] Sem efeitos colaterais externos

### 2. Proteger invariantes

- [ ] Validações ficam no construtor/factory quando necessário
- [ ] Estados inválidos são impedidos cedo
- [ ] `equals/hashCode/toString` só quando fizer sentido semântico

### 3. Definir ports quando necessário

- [ ] Se o caso de uso precisar de IO externo, definir interface em `core/ports/`
- [ ] Port descreve intenção (`CommandRunner`, `EnvironmentDetector`), não tecnologia concreta

## Critical

- NUNCA importar `org.fixkitty.application`, `org.fixkitty.infrastructure` ou `org.fixkitty.interfaces`
- NUNCA construir comandos shell aqui
- NUNCA colocar nomes de serviços Linux como regra fixa do domínio
- Domain deve continuar válido mesmo se a infra mudar completamente

## Exemplos

### Exemplo 1: Criar `RecoveryResult`

Use quando precisar padronizar retorno de execuções para GUI e TUI.

Ações:
1. Criar tipo em `core/results/RecoveryResult.java`
2. Incluir campos de status, steps, warnings e recommendation
3. Garantir que o tipo não conhece logger, terminal ou JavaFX

### Exemplo 2: Criar `CommandRunner` port

Use quando a aplicação precisar executar algo externo.

Ações:
1. Criar interface em `core/ports/CommandRunner.java`
2. Método recebe request do domínio e retorna resultado tipado
3. Infra implementa depois

## Consulte também

- [../layer-application/SKILL.md](../layer-application/SKILL.md) — quando começar a orquestração
- [../write-tests/SKILL.md](../write-tests/SKILL.md) — testes de domain e application
- [../enforce-architecture/SKILL.md](../enforce-architecture/SKILL.md) — validação final obrigatória