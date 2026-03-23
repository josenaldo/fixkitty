---
name: create-recovery-action
description: "Criando ação de recuperação de subsistema Linux (audio, network, bluetooth, GNOME). Use para adicionar nova ação de fix, definir seus passos e registrá-la no profile de ambiente. Não use para criar o Use Case que orquestra a ação (use create-use-case) nem para criar um novo profile de distro (use create-environment-profile)."
---

# Skill: Criar Ação de Recuperação (Micro-Skill)

## Quando usar

- Adicionar nova ação fixável (ex: "Fix Audio", "Fix Display", "Fix Bluetooth")
- Definir os passos que a ação executa
- Especificar quais ambientes suportam essa ação
- Atualizar lista de serviços/comandos existentes num profile

## Instruções

### 1. Criar entidade RecoveryAction

- [ ] `src/main/java/org/fixkitty/core/recovery/{dominio}/entities/{Acao}Action.java` estendendo `RecoveryAction`
- [ ] Define `actionId` único (ex: `AUDIO`, `NETWORK`)
- [ ] Método `describe()` retorna string legível (ex: `"Restart audio with PipeWire and WirePlumber"`)
- [ ] Sem imports de infrastructure ou adapters

### 2. Criar comando estruturado

- [ ] `src/main/java/org/fixkitty/core/recovery/{dominio}/commands/{Acao}Command.java` estendendo `Command`
- [ ] Define sequência de passos (`List<CommandStep>`)
- [ ] Cada passo com: comando lógico, timeout e flag de criticidade

### 3. Registrar em EnvironmentProfile

- [ ] `src/main/java/org/fixkitty/infrastructure/profiles/ubuntu/Ubuntu24Profile.java`
- [ ] Adicionar suporte: `actionId` presente no mapa de ações suportadas
- [ ] Mapear `actionId` → comando shell real com flag de privilégio e timeout

## Critical

- A ação define **o quê**, não **como** — comandos shell concretos ficam exclusivamente no profile de infra
- Ação é agnóstica a distro — profiles resolvem diferenças; domain não sabe o que é `systemctl`
- Marcar `isSupported=false` por padrão; só marcar `true` após teste manual confirmado no ambiente

## Exemplos

### Exemplo 1: Nova ação "Fix Audio"

Usuário diz: "Adiciona ação Fix Audio que reinicia PipeWire e WirePlumber"

Ações:
1. Cria `AudioAction` em `core/recovery/audio/entities/`, define `actionId = AUDIO`
2. Cria `AudioCommand` com dois passos: PipeWire, WirePlumber
3. Em `Ubuntu24Profile`, mapeia `AUDIO` → `"systemctl --user restart pipewire wireplumber"`
4. Resultado: ação pronta para ser usada por use cases, sem conhecer Linux ou systemctl

## Troubleshooting

**`ActionNotSupportedError` em runtime**
- Causa: `Ubuntu24Profile` (ou profile ativo) não suporta essa ação
- Solução: Adicionar suporte no profile correspondente ou criar novo profile

**Campo `timeout` em `CommandStep` não definido**
- Causa: passo crítico sem timeout
- Solução: Definir timeout baseado na expectativa do serviço (ex: audio = 5s, network = 10s)

**Ordem dos passos causa falha intermitente**
- Causa: dependência entre passos não mapeada
- Solução: Validar dependências e documentar a ordem com comentário no Command

## Consulte também

- [create-use-case](../create-use-case/SKILL.md) — próximo passo: orquestrar a ação num use case
- [create-environment-profile](../create-environment-profile/SKILL.md) — registrar a ação num novo profile de distro
- [enforce-architecture](../enforce-architecture/SKILL.md) — validação final obrigatória
