---
name: create-use-case
description: "Criando Use Case na camada application para orquestrar ações de recovery. Use para criar novo orquestrador de execução, coordenar RecoveryActions e portas de infraestrutura. Não use para criar a ação em si (use create-recovery-action) nem para componente de UI (use create-ui-component)."
---

# Skill: Criar Use Case (Micro-Skill)

## Quando usar

- Criar novo fluxo de execução de recovery (ex: "Fix Audio", "Fix All")
- Coordenar múltiplas RecoveryActions em sequência definida
- Precisar de orquestração com controle de erro, log e rollback
- Adicionar ponto de entrada para GUI ou TUI chamar

## Instruções

### 1. Criar o Use Case

- [ ] `src/main/java/org/fixkitty/application/usecases/{acao}/Fix{Acao}UseCase.java`
- [ ] Recebe `EnvironmentProfile` e `PrivilegeManager` via injeção no construtor (não estático)
- [ ] Método principal: `execute() → RecoveryResult`
- [ ] `RecoveryResult` contém: `success`, `steps[]`, `errorMessage` (se falhar)
- [ ] Sem imports de `javafx.*`, `tui.*`, `ProcessBuilder`, ou comandos Linux

### 2. Definir interações com domain

- [ ] Varre ações relevantes do `EnvironmentProfile.getSupportedActions()`
- [ ] Para cada ação: delega execução ao `CommandRunner` via port (interface, não impl)
- [ ] Em caso de falha parcial: registra no `RecoveryResult` e decide explicitamente: continuar ou abortar

### 3. Criar port se necessário

- [ ] Se Use Case precisar de IO externo (ex: logs, notificações), criar interface em `core/ports/`
- [ ] Nunca introduzir dependência direta de infra no Use Case

## Critical

- Use Case que chama `ProcessBuilder` ou `Runtime.exec()` está executando o que deveria ser do adapter — crie port e delegue
- `System.out` ou logger de infra aqui acumula preocupações que pertencem à infra — use `RecoveryResult.addStep()`
- Use Case orquestra — toda lógica de comando fica em infra via ports; lógica de regra fica em domain
- Inputs e outputs devem ser tipos do domain (`RecoveryResult`, `RecoveryAction`, etc.)

## Exemplos

### Exemplo 1: Fix Audio Use Case

Usuário diz: "Cria o use case que executa Fix Audio"

Ações:
1. Cria `FixAudioUseCase` em `application/usecases/audio/`
2. Construtor recebe `EnvironmentProfile profile, CommandRunner runner`
3. `execute()`: busca `AudioAction`, valida suporte, executa cada step via `runner.run(cmd)`
4. Retorna `RecoveryResult` com passos executados e status final
5. GUI e TUI chamam este use case — nunca executam comandos por conta própria

### Exemplo 2: Fix All Use Case

Ações:
1. Cria `FixAllUseCase` que delega para `FixAudioUseCase`, `FixNetworkUseCase`, etc.
2. Coleta todos os `RecoveryResult` individuais
3. Retorna `RecoveryResult` composto (`success = todos ok`)

## Troubleshooting

**Use Case importa classe de infrastructure**
- Causa: violação de Clean Architecture
- Solução: Criar port (interface) em `core/ports/`, implementar em infra, injetar no use case

**`RecoveryResult` não carrega detalhes de erro**
- Causa: `addStep()` não chamado na falha
- Solução: Garantir que cada passo crítico registre resultado antes de retornar

**GUI chama `ProcessBuilder` diretamente**
- Causa: atalho indevido sem passar pelo use case
- Solução: Extrair lógica para o Use Case correspondente, GUI só chama `useCase.execute()`

## Consulte também

- [create-recovery-action](../create-recovery-action/SKILL.md) — pré-requisito: crie a ação antes
- [create-ui-component](../create-ui-component/SKILL.md) — próximo passo: conectar use case à UI
- [write-unit-test](../write-unit-test/SKILL.md) — escreva testes do use case antes do demo
- [enforce-architecture](../enforce-architecture/SKILL.md) — validação final obrigatória
