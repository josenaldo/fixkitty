---
name: layer-domain
description: "Modelando a camada domain/core. Use quando criar ou alterar entidades, value objects, enums, resultados de execução ou ports centrais do problema. Não use para orquestração de use cases (use layer-application), adapters concretos (use layer-infrastructure) ou componentes de UI (use layer-interface)."
---

# Skill: Camada Domain

## Quando usar

- Criar ou alterar entidades em `org.fixkitty.core.*`
- Modelar `ActionId`, `ExecutionPlan`, `ExecutionStep`, `RecoveryResult`, `StepResult`
- Criar value objects, enums e contratos centrais do domínio
- Criar ports que representam necessidades do domínio ou da aplicação

## Responsabilidade da camada

Domain descreve o problema, não a implementação.

Pode definir:
- conceitos do sistema, regras de consistência
- tipos de entrada e saída centrais
- interfaces/ports que outras camadas implementarão

Não pode conhecer:
- JavaFX, TUI/TTY, `ProcessBuilder`
- `systemctl`, `journalctl`, `sudo`
- Ubuntu, Fedora ou GNOME como implementação concreta

## Instruções

### 1. Modelar conceitos puros

- [ ] Arquivo em `src/main/java/org/fixkitty/core/...`
- [ ] Nome do tipo expressa conceito do problema, não da tecnologia
- [ ] API pequena e explícita, sem efeitos colaterais externos

### 2. Proteger invariantes

- [ ] Validações no construtor/factory quando necessário
- [ ] Estados inválidos impedidos cedo
- [ ] `equals/hashCode/toString` só quando fizer sentido semântico

### 3. Definir ports quando necessário

- [ ] Se o caso de uso precisar de IO externo, definir interface em `core/ports/`
- [ ] Port descreve intenção (`CommandRunner`, `EnvironmentDetector`), não tecnologia concreta

## Critical

- Imports de `application`, `infrastructure` ou `interfaces` aqui indicam design errado — mova a dependência para a camada correta
- Comandos shell construídos no domain quebram isolamento — delegue ao port correspondente
- Nomes de serviços Linux hardcoded como constantes criam acoplamento invisível — use abstrações semânticas
- Domain deve compilar e testar sem nenhuma dependência de infra ou UI

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
3. Infra implementa depois; domain não sabe como

## Troubleshooting

**Domain acumula lógica de distro específica**
- Causa: strings de serviço (`pipewire`, `NetworkManager`) hardcoded no domínio
- Solução: Criar enum ou abstração semântica; implementação concreta vai no profile de infra

**Tipo de domínio cresce descontroladamente**
- Causa: mistura de conceito de domínio com detalhe de infraestrutura
- Solução: Separar — domain mantém o contrato, infra mantém o detalhe

## Consulte também

- [layer-application](../layer-application/SKILL.md) — quando começar a orquestração
- [write-tests](../write-tests/SKILL.md) — testes de domain e application
- [enforce-architecture](../enforce-architecture/SKILL.md) — validação final obrigatória
