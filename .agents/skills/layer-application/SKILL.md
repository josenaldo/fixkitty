---
name: layer-application
description: "Trabalhando na camada application. Use para criar ou alterar use cases, fluxos de execução, políticas de orquestração e coordenação entre domain e ports. Não use para modelagem pura nem para adapters concretos."
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

## Checklist

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

- Use case não é lugar para string de shell
- Use case não é lugar para parsing de `/etc/os-release`
- Use case não é lugar para lógica de widget, botão ou teclado
- Se a classe começar a conhecer tecnologia concreta, ela está vazando responsabilidade

## Exemplos

### Exemplo 1: `ExecuteFixActionUseCase`

1. Recebe `EnvironmentDetector`, `ActionCatalog` e `CommandRunner`
2. Detecta perfil atual
3. Obtém plano aplicável
4. Executa passos segundo política definida
5. Retorna `RecoveryResult`

### Exemplo 2: `CheckEnvironmentUseCase`

1. Detecta ambiente
2. Lista ações suportadas
3. Retorna resumo para GUI e TUI renderizarem

## Consulte também

- [../layer-domain/SKILL.md](../layer-domain/SKILL.md) — modelagem dos tipos usados aqui
- [../layer-infrastructure/SKILL.md](../layer-infrastructure/SKILL.md) — implementação concreta dos ports
- [../write-tests/SKILL.md](../write-tests/SKILL.md) — cobertura obrigatória de use cases
- [../enforce-architecture/SKILL.md](../enforce-architecture/SKILL.md) — validação final obrigatória