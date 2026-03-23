---
name: layer-application
description: "Orquestrando a camada application. Use quando criar ou alterar use cases, fluxos de execução, políticas de orquestração e coordenação entre domain e ports. Não use para modelagem pura de domínio (use layer-domain), adapters concretos (use layer-infrastructure) ou telas (use layer-interface)."
---

# Skill: Camada Application

## Quando usar

- Criar novo use case
- Alterar fluxo de execução de uma recuperação
- Encadear múltiplas ações em sequência definida
- Aplicar política de erro, cancelamento, continuidade ou composição

## Responsabilidade da camada

Application coordena o trabalho.

Esta camada:
- consome tipos do domain
- chama ports definidos no core
- decide sequência e política de execução
- entrega resposta estruturada para GUI e TUI

Esta camada não:
- executa shell diretamente
- conhece adapters concretos
- conhece JavaFX ou renderização de terminal

## Instruções

### 1. Criar o use case

- [ ] Arquivo em `src/main/java/org/fixkitty/application/...`
- [ ] Nome orientado a intenção (`ExecuteFixActionUseCase`, `CheckEnvironmentUseCase`)
- [ ] Dependências recebidas por construtor
- [ ] Retorno em tipos do domínio/aplicação, nunca widgets/UI

### 2. Orquestrar com clareza

- [ ] Ordem das etapas explícita
- [ ] Política de falha explícita: aborta, continua ou degrada
- [ ] Logs/resultados agregados em retorno estruturado

### 3. Manter isolamento

- [ ] Usa apenas interfaces/ports para dependências externas
- [ ] Não monta `ProcessBuilder`
- [ ] Não referencia classes de `infrastructure` ou `interfaces`

## Critical

- Use case que constrói strings de shell está com responsabilidade vazada — mova para o adapter correspondente via port
- Use case que parseia `/etc/os-release` ou detecta distro pertence à infrastructure — crie port e injete
- Use case que referencia widget, botão ou tecla pertence à interface — remova a referência e delegue output
- Se a classe começar a conhecer tecnologia concreta, ela está vazando responsabilidade

## Exemplos

### Exemplo 1: `ExecuteFixActionUseCase`

1. Recebe `EnvironmentDetector`, `ActionCatalog` e `CommandRunner`
2. Detecta perfil atual
3. Obtém plano aplicável
4. Executa passos segundo política definida
5. Retorna `RecoveryResult`

### Exemplo 2: `CheckEnvironmentUseCase`

1. Detecta ambiente via port
2. Lista ações suportadas
3. Retorna resumo para GUI e TUI renderizarem independentemente

## Troubleshooting

**Use case importa classe de infrastructure**
- Causa: violação de Clean Architecture
- Solução: Criar interface (port) em `core/ports/`, implementar em infra, injetar no use case

**Política de falha não documentada**
- Causa: `if` espalhado sem intenção clara
- Solução: Nomear explicitamente a política (`continueOnPartialFailure`, `abortOnFirstError`) e documentar no use case

**Use case cresce mais de ~100 linhas**
- Causa: orquestração misturada com detalhe de execução
- Solução: Extrair sub-use-cases ou delegar detalhes via ports novos

## Consulte também

- [layer-domain](../layer-domain/SKILL.md) — modelagem dos tipos usados aqui
- [layer-infrastructure](../layer-infrastructure/SKILL.md) — implementação concreta dos ports
- [write-tests](../write-tests/SKILL.md) — cobertura obrigatória de use cases
- [enforce-architecture](../enforce-architecture/SKILL.md) — validação final obrigatória
