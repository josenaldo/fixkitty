---
name: create-environment-profile
description: "Criando perfil de ambiente Linux/desktop (EnvironmentProfile) para nova distro ou DE. Use para adicionar suporte a Ubuntu 24, Fedora, Mint, KDE etc. Não use para criar ações de recovery (use create-recovery-action) nem para orquestrar execução (use create-use-case)."
---

# Skill: Criar Environment Profile (Micro-Skill)

## Quando usar

- Adicionar suporte a nova distro (ex: Fedora 40, Mint 22)
- Adicionar suporte a novo desktop environment (ex: KDE Plasma, Cinnamon)
- Adaptar comandos existentes para diferenças de serviços entre distros
- Testar o app num ambiente diferente do Ubuntu 24 (profile de referência)

## Instruções

### 1. Criar o profile

- [ ] `src/main/java/org/fixkitty/infrastructure/profiles/{distro}/{Distro}Profile.java`
- [ ] Implementa interface `EnvironmentProfile` (definida em `core/ports/`)
- [ ] `profileId` único (ex: `UBUNTU_24`, `FEDORA_40`)
- [ ] `description()` legível para logs

### 2. Declarar ações suportadas

- [ ] Implementar `isSupported(ActionId actionId)` — retorna `false` para ações não testadas
- [ ] Listar ações suportadas explicitamente via `Map<ActionId, ShellCommand>`
- [ ] Não assumir que o que funciona no Ubuntu funciona aqui

### 3. Mapear comandos por ação

Para cada `ActionId` suportado, definir:
- [ ] Comando shell concreto como string (ex: `"systemctl --user restart pipewire wireplumber"`)
- [ ] Flag `requiresPrivilege: boolean`
- [ ] Timeout recomendado

### 4. Detectar automaticamente (opcional, fase futura)

- [ ] `src/main/java/org/fixkitty/infrastructure/profiles/ProfileDetector.java`
- [ ] Lê `/etc/os-release` para inferir profile
- [ ] Fallback para `GenericProfile` se desconhecido

## Critical

- Profile não executa comandos — apenas os declara; quem executa é `CommandRunner`
- Sem imports de JavaFX ou TUI aqui
- Comandos devem ser testáveis como strings — sem lógica dinâmica no mapa
- Marcar `isSupported=false` por padrão; só mudar para `true` após teste manual confirmado
- Ubuntu24Profile é o profile de referência — consulte sua estrutura antes de criar um novo

## Exemplos

### Exemplo 1: Novo profile para Fedora 40

Usuário diz: "Quero que o app funcione no Fedora 40"

Ações:
1. Cria `Fedora40Profile` em `infrastructure/profiles/fedora/`
2. Audio: `"systemctl --user restart pipewire wireplumber"` (verificar nomes no Fedora)
3. Bluetooth: `"sudo systemctl restart bluetooth"`
4. Network: `"sudo systemctl restart NetworkManager"`
5. GNOME Shell: `"killall gnome-shell"` — `requiresPrivilege=false` (user-space)

### Exemplo 2: GenericProfile (fallback seguro)

Ações:
1. Cria `GenericProfile` que retorna `isSupported() = false` para tudo
2. Usado quando `ProfileDetector` não reconhece o ambiente
3. UI exibe: "Ambiente não suportado — operações indisponíveis"

## Troubleshooting

**Profile detectado errado em runtime**
- Causa: `/etc/os-release` tem formato diferente do esperado
- Solução: Atualizar `ProfileDetector` com novo padrão de parse; adicionar teste unitário

**Comando do profile lança exceção em runtime**
- Causa: serviço tem nome diferente nessa distro
- Solução: Verificar `systemctl list-units --type=service | grep <nome>`, atualizar profile

**Ação marcada como suportada mas falha**
- Causa: `isSupported=true` declarado antes de testar no ambiente real
- Solução: Reverter para `false`; reativar apenas após confirmação manual

## Consulte também

- [create-recovery-action](../create-recovery-action/SKILL.md) — ações que o profile vai suportar
- [create-use-case](../create-use-case/SKILL.md) — use case que usa o profile
- [enforce-architecture](../enforce-architecture/SKILL.md) — validação final obrigatória
