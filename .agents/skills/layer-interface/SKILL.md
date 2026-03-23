---
name: layer-interface
description: "Trabalhando na camada interfaces. Use para criar ou alterar GUI JavaFX, TUI textual ou bootstrap de entrada do usuário. A interface renderiza e delega; não implementa regras de negócio."
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

## Checklist

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

- [ ] Nada de `ProcessBuilder`
- [ ] Nada de `systemctl`
- [ ] Nada de seleção de profile ou regra de negócio duplicada

## Critical

- GUI e TUI devem consumir os mesmos use cases
- TUI não é desculpa para duplicar lógica de aplicação
- Controller não deve decidir política de retry, timeout ou fallback técnico
- Mensagens ao usuário podem ser adaptadas por interface, mas a verdade do resultado vem do use case

## Exemplos

### Exemplo 1: Controller JavaFX

1. Botão chama `executeFixActionUseCase.execute(AUDIO)`
2. Desabilita interação durante execução
3. Mostra resultado amigável e logs estruturados

### Exemplo 2: Menu TUI

1. Lista ações suportadas
2. Usuário escolhe opção por número/tecla
3. Componente chama o mesmo use case da GUI
4. Saída é renderizada no terminal

## Consulte também

- [../layer-application/SKILL.md](../layer-application/SKILL.md) — camada que a interface deve chamar
- [../write-tests/SKILL.md](../write-tests/SKILL.md) — estratégia de testes para controllers e fluxos
- [../enforce-architecture/SKILL.md](../enforce-architecture/SKILL.md) — validação final obrigatória