---
name: layer-interface
description: "Construindo a camada interfaces. Use para criar ou alterar GUI JavaFX, TUI textual ou bootstrap de entrada. Não use para regra de negócio (use layer-application), adapters (use layer-infrastructure) ou modelagem de domínio (use layer-domain)."
---

# Skill: Camada Interface

## Quando usar

- Criar ou alterar controller JavaFX
- Criar ou alterar componente TUI/TTY
- Adicionar menu, botão, tela, painel de log ou fluxo de interação
- Ajustar bootstrap entre GUI e TUI

## Responsabilidade da camada

Interfaces recebem input e exibem output.

Esta camada:
- chama use cases
- apresenta progresso e resultado
- transforma input do usuário em comandos de aplicação
- decide forma de renderização, não regra de recuperação

## Instruções

### 1. Delegar corretamente

- [ ] Classe em `src/main/java/org/fixkitty/interfaces/...`
- [ ] Dependência principal é um use case, não um adapter concreto
- [ ] Converte eventos de UI em chamada de aplicação
- [ ] Renderiza resposta sem reinventar regra de negócio

### 2. Respeitar a interface alvo

- [ ] GUI: botão, feedback visual, estado de execução, mensagem amigável
- [ ] TUI: menu, navegação simples, saída legível em terminal/TTY
- [ ] Bootstrap: argumento explícito ou fallback conforme ambiente

### 3. Não vazar responsabilidades

- [ ] Nenhum `ProcessBuilder` ou `systemctl` aqui
- [ ] Nenhuma seleção de profile ou regra de negócio duplicada
- [ ] Output de `System.out` somente via `TuiPrinter` port — nunca direto

## Critical

- GUI e TUI devem consumir os mesmos use cases — comportamento divergente indica lógica duplicada em algum dos lados
- Controller com `if` sobre distro ou serviço está fazendo trabalho do use case — mova a lógica
- Política de retry, timeout ou fallback técnico pertence ao use case, não ao controller
- Mensagens ao usuário podem ser adaptadas por interface, mas a verdade do resultado vem do use case

## Exemplos

### Exemplo 1: Controller JavaFX

1. Botão chama `executeFixActionUseCase.execute(AUDIO)`
2. Desabilita interação durante execução
3. Mostra resultado amigável e logs estruturados do `RecoveryResult`
4. Reabilita ao final — sucesso ou falha

### Exemplo 2: Menu TUI

1. Lista ações suportadas
2. Usuário escolhe opção por número/tecla
3. Componente chama o mesmo use case da GUI
4. Saída renderizada no terminal via `TuiPrinter`

## Troubleshooting

**GUI e TUI têm comportamento diferente para a mesma ação**
- Causa: lógica duplicada em ambas as interfaces
- Solução: Consolidar no use case; ambas as interfaces chamam o mesmo use case

**TUI exibe stacktrace ao usuário**
- Causa: exception não tratada no componente
- Solução: Capturar exception, transformar em `RecoveryResult` com `success=false` e mensagem legível

**Controller cresce com múltiplos `if` condicionais**
- Causa: decisões que pertencem ao use case estão vazando para a interface
- Solução: Mover decisões para o use case correspondente; controller apenas renderiza o resultado

## Consulte também

- [layer-application](../layer-application/SKILL.md) — camada que a interface deve chamar
- [write-tests](../write-tests/SKILL.md) — estratégia de testes para controllers e fluxos
- [enforce-architecture](../enforce-architecture/SKILL.md) — validação final obrigatória
