---
name: layer-infrastructure
description: "Trabalhando na camada infrastructure. Use para implementar ports, integrar com Linux real, definir profiles, command runners, privilege strategies e logging. Não use para regra de negócio nem para controllers."
---

# Skill: Camada Infrastructure

## Quando usar

- Implementar `CommandRunner`, `EnvironmentDetector`, `ActionCatalog`, logging e privilege gateways
- Integrar com `ProcessBuilder`, `systemctl`, `journalctl`, `pkexec` ou `sudo`
- Criar profiles concretos por distro/desktop
- Adaptar comandos para ambientes específicos

## Responsabilidade da camada

Infrastructure traduz intenção em execução concreta.

Esta camada:
- implementa ports
- conhece Linux real
- encapsula diferenças entre Ubuntu/Fedora/Mint
- isola detalhes de execução, timeout e saída de processo

## Checklist

### 1. Implementar port existente

- [ ] Classe em `src/main/java/org/fixkitty/infrastructure/...`
- [ ] Implementa interface definida em `core/ports/`
- [ ] Nome concreto e técnico (`ProcessBuilderCommandRunner`, `Ubuntu24Profile`)

### 2. Isolar detalhes operacionais

- [ ] Timeout configurável
- [ ] stdout/stderr capturados
- [ ] exit code mapeado para tipo estruturado
- [ ] erros convertidos em retorno previsível

### 3. Preparar crescimento multi-ambiente

- [ ] Profiles separados por distro/desktop
- [ ] Nada de `if` gigante espalhado por todo adapter
- [ ] Comandos concretos centralizados em local apropriado

## Critical

- NUNCA importar controllers GUI/TUI
- NUNCA mover decisão de negócio para o adapter só porque é mais fácil
- NUNCA deixar `ProcessBuilder` espalhado em várias classes sem encapsulamento
- Toda implementação concreta deve continuar substituível por mock em teste

## Exemplos

### Exemplo 1: `ProcessBuilderCommandRunner`

1. Implementa `CommandRunner`
2. Recebe `CommandRequest`
3. Executa processo com timeout
4. Retorna `CommandExecutionResult`

### Exemplo 2: `Ubuntu24Profile`

1. Expõe ações suportadas
2. Mapeia ações para steps concretos
3. Declara necessidades de privilégio e limites do ambiente

## Consulte também

- [../layer-domain/SKILL.md](../layer-domain/SKILL.md) — ports e tipos implementados aqui
- [../layer-application/SKILL.md](../layer-application/SKILL.md) — quem consome esta camada
- [../enforce-architecture/SKILL.md](../enforce-architecture/SKILL.md) — validação final obrigatória