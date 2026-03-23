---
name: create-recovery-action
description: "Criando ação de recuperação de subsistema Linux (audio, network, bluetooth). Use para adicionar nova ação de fix, definir comandos, especificar perfis suportados. Não use para criar Use Case — use create-use-case."
---

# Skill: Criar Ação de Recuperação (Micro-Skill)

## Quando usar

- Adicionar nova ação fixávavel (ex: "Fix Audio", "Fix Display")
- Definir os comandos/passos que a ação executa
- Especificar quais ambientes suportam essa ação
- Atualizar lista de serviços/comandos existentes

## Checklist

### 1. Criar entidade RecoveryAction

- [ ] `src/main/java/org/fixkitty/core/recovery/{dominio}/entities/{Acao}Action.java` estendendo `RecoveryAction`
- [ ] Defines `actionId` único (ex: `AUDIO`, `NETWORK`)
- [ ] Método `describe()` retorna string legível (ex: "Restart audio with PipeWire and WirePlumber")
- [ ] Sem imports de infrastructure ou adapters

### 2. Criar comando estruturado

- [ ] `src/main/java/org/fixkitty/core/recovery/{dominio}/commands/{Acao}Command.java` estendendo `Command`
- [ ] Define sequência de passos (List<CommandStep>)
- [ ] Cada passo com: comando lógico, timeout, criticidade

### 3. Registrar em EnvironmentProfile

- [ ] `src/main/java/org/fixkitty/infrastructure/profiles/ubuntu/Ubuntu24Profile.java`
- [ ] Adicionar suporte: `actionId.isSupported()` retorna true
- [ ] Mapear: `actionId` → comando shell real

## Critical

- Nunca importe `ProcessBuilder`, `systemctl`, ou comando Linux específico em domínio. A ação define O QUÊ, não COMO. O profile define COMO.
- Ação é agnóstica a distro — profiles resolvem diferenças

## Exemplos

### Exemplo 1: Nova ação "Fix Audio"

Usuário diz: "Adiciona ação Fix Audio que reinicia PipeWire e WirePlumber"

Ações:
1. Cria `AudioAction` em domain, define `actionId = AUDIO`
2. Cria `AudioCommand` com dois passos: PipeWire, WirePlumber
3. Em Ubuntu24Profile, mapeia AUDIO → `systemctl --user restart pipewire wireplumber`
4. Resultado: ação pronta para ser usada por Use Cases, sem conhecer Linux/systemctl

## Troubleshooting

**Erro: "ActionNotSupportedError" em ttY**
- Causa: Profile (Ubuntu24) não suporta essa ação
- Solução: Adicionar suporte no profile correspondente ou criar novo profile

**Erro: Campo `timeout` em CommandStep não definido**
- Causa: Passo crítico sem timeout
- Solução: Definir timeout baseado em experiência do serviço (ex: audio = 5s)

## Performance Notes

- A ordem dos passos importa — validar dependências
- Testes devem coverir: ação bem-sucedida, uma etapa falha, timeout

## Consulte também

- [create-use-case](../create-use-case/SKILL.md) — Próximo passo: orquestrar a ação
- [enforce-architecture](../enforce-architecture/SKILL.md) — Validação final obrigatória
