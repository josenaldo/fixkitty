---
name: layer-infrastructure
description: "Implementando a camada infrastructure. Use para implementar ports, integrar com Linux real, definir profiles por distro, command runners, privilege strategies e logging. Não use para regra de negócio (use layer-application) nem para controllers (use layer-interface)."
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
- implementa ports definidos em `core/`
- conhece Linux real
- encapsula diferenças entre Ubuntu/Fedora/Mint
- isola detalhes de execução, timeout e saída de processo

## Instruções

### 1. Implementar port existente

- [ ] Classe em `src/main/java/org/fixkitty/infrastructure/...`
- [ ] Implementa interface definida em `core/ports/`
- [ ] Nome concreto e técnico (`ProcessBuilderCommandRunner`, `Ubuntu24Profile`)

### 2. Isolar detalhes operacionais

- [ ] Timeout configurável
- [ ] stdout/stderr capturados e mapeados
- [ ] exit code convertido para tipo estruturado do domínio
- [ ] Erros retornados como resultado previsível, não exceção genérica

### 3. Preparar crescimento multi-ambiente

- [ ] Profiles separados por distro/desktop
- [ ] Comandos concretos centralizados no profile, não espalhados em adapters
- [ ] Cada implementação substituível por mock em teste

## Critical

- Importar controllers GUI/TUI aqui cria acoplamento proibido — infrastructure não conhece interfaces
- Decisões de negócio movidas para o adapter por conveniência criam lógica duplicada invisível — mantenha no use case via port
- `ProcessBuilder` espalhado em múltiplas classes sem encapsulamento dificulta troca de estratégia — centralize
- Toda implementação concreta deve permanecer substituível por mock em teste unitário

## Exemplos

### Exemplo 1: `ProcessBuilderCommandRunner`

1. Implementa `CommandRunner`
2. Recebe `CommandRequest`
3. Executa processo com timeout configurável
4. Retorna `CommandExecutionResult` com stdout, stderr e exit code mapeados

### Exemplo 2: `Ubuntu24Profile`

1. Implementa `EnvironmentProfile`
2. Expõe ações suportadas (`AUDIO`, `NETWORK`, `BLUETOOTH`)
3. Mapeia ação para steps concretos com comando shell e flag de privilégio
4. Declara limites do ambiente (ex: PipeWire em user-space, bluetooth em root)

## Troubleshooting

**Adapter cresceu com lógica de negócio**
- Causa: decisão de fluxo colocada aqui por conveniência
- Solução: Mover decisão para o use case; adapter só executa e retorna resultado

**Teste falha por chamar shell real**
- Causa: mock de `CommandRunner` não configurado corretamente
- Solução: Verificar que o teste usa mock via construtor; nunca instanciar `ProcessBuilderCommandRunner` em teste unitário

**Profile diferente, mesmo bug**
- Causa: lógica de detecção espalhada em vários adapters
- Solução: Centralizar em `ProfileDetector`; cada profile apenas declara o que suporta

## Consulte também

- [layer-domain](../layer-domain/SKILL.md) — ports e tipos implementados aqui
- [layer-application](../layer-application/SKILL.md) — quem consome esta camada
- [enforce-architecture](../enforce-architecture/SKILL.md) — validação final obrigatória
