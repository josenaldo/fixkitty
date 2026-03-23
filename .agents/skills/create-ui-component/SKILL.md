---
name: create-ui-component
description: "Criando componente de UI (Controller JavaFX ou componente TUI). Use para adicionar tela, botão, painel ou view de log. A UI apenas delega para Use Cases — sem lógica de negócio. Aplicável para interfaces/gui e interfaces/tui."
---

# Skill: Criar Componente de UI (Micro-Skill)

## Quando usar

- Adicionar nova tela ou painel na GUI (JavaFX)
- Adicionar novo componente ou menu na TUI (terminal)
- Conectar botão/ação ao Use Case correspondente
- Mostrar resultado de recovery na interface

## Regra de ouro

> **Controllers são passadores de mensagem.** Pegam input do usuário → chamam use case → exibem resultado. Zero lógica de negócio.

---

## Checklist: GUI (JavaFX)

### 1. Criar Controller

- [ ] `src/main/java/org/fixkitty/interfaces/gui/controllers/{Acao}Controller.java`
- [ ] Recebe Use Case via injeção no construtor (`Fix{Acao}UseCase`)
- [ ] Handler de botão: chama `useCase.execute()`, exibe `RecoveryResult`
- [ ] Sem imports de `core/`, `application/` além do Use Case e domain types de resposta
- [ ] Sem `ProcessBuilder`, `RuntimeException` swallowed, ou lógica condicional de distro

### 2. Criar FXML (se GUI)

- [ ] `src/main/resources/org/fixkitty/interfaces/gui/views/{acao}.fxml`
- [ ] Layout com: botão de ação, área de log, indicador de status
- [ ] `fx:controller` aponta para o controller criado

### 3. Feedback de execução

- [ ] Botão desabilitado durante execução (evitar cliques duplos)
- [ ] Área de log atualizada linha a linha com cada step do `RecoveryResult`
- [ ] Status final: ✓ (verde) ou ✗ (vermelho)
- [ ] Erros exibidos em mensagem legível — não stacktrace bruto

---

## Checklist: TUI (Terminal)

### 1. Criar componente TUI

- [ ] `src/main/java/org/fixkitty/interfaces/tui/components/{Acao}Component.java`
- [ ] Recebe Use Case via injeção no construtor
- [ ] `render()`: exibe menu/opção no terminal
- [ ] `handleInput(String key)`: responde a tecla, chama use case
- [ ] Output via `TuiPrinter` (port), nunca `System.out` direto

### 2. Integrar no menu principal TUI

- [ ] `MainMenuComponent` ou equivalente deve listar a nova opção
- [ ] Tecla/número atribuído documentado no componente

---

## Critical

- NUNCA coloque lógica de negócio em controllers ou componentes TUI
- NUNCA importe classes de `infrastructure/` na GUI/TUI
- GUI e TUI devem ser substituíveis — mesmos use cases, output diferente
- Se precisar de lógica antes de chamar use case, essa lógica pertence ao use case

## Exemplos

### Exemplo 1: Botão "Fix Audio" na GUI

Usuário diz: "Adicione botão Fix Audio na tela principal"

Ações:
1. Cria `AudioController` em `interfaces/gui/controllers/`
2. Injeta `FixAudioUseCase`
3. `onFixAudioClick()`: desabilita botão, chama `useCase.execute()`, exibe resultado, reabilita botão
4. Cria `audio.fxml` com Button e TextArea
5. Registra controller no FXML

### Exemplo 2: Opção "2 - Fix Audio" na TUI

Ações:
1. Cria `AudioTuiComponent` em `interfaces/tui/components/`
2. `render()`: imprime linha "2) Fix Audio"
3. `handleInput("2")`: chama `FixAudioUseCase.execute()`  
4. Imprime cada step via `tuiPrinter.println(step.message())`
5. Imprime `[OK]` ou `[FAIL]` ao final

## Troubleshooting

**Controller chama ProcessBuilder**
- Causa: lógica de negócio no controller — violação de Clean Architecture
- Solução: Mover lógica para Use Case, controller apenas chama `useCase.execute()`

**GUI e TUI com comportamento diferente para mesma ação**
- Causa: lógica duplicada em ambas as interfaces
- Solução: Consolidar no Use Case; ambas as interfaces chamam o mesmo use case

**TUI exibe stacktrace ao usuário**
- Causa: exception não tratada no componente
- Solução: Catching exception, transformar em `RecoveryResult` com `success=false` e mensagem legível

## Consulte também

- [create-use-case](../create-use-case/SKILL.md) — Pré-requisito: use case deve existir antes
- [enforce-architecture](../enforce-architecture/SKILL.md) — Validação final obrigatória
