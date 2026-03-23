# Memória do Fixkitty

Decisões arquiteturais, padrões confirmados, bugs conhecidos e estado de features.

---

## Decisões Arquiteturais

### [2026-03] Clean Architecture com Multi-Interface (GUI + TUI)

**Contexto**: Sistema precisa funcionar tanto em GUI (JavaFX) quanto em TTY (terminal), porque o painel gráfico pode travar.

**Decisão**: Separar complemente a lógica de domínio (core) da interface. GUI e TUI são apenas adaptadores que chamam Use Cases.

**Consequência**: 
- Core não depende de nenhuma biblioteca de UI
- Use Cases retornam estruturas de domínio (RecoveryResult), não dados formatados
- GUI e TUI formatam a saída independentemente
- Mesma lógica de negócio para ambas as interfaces

---

### [2026-03] EnvironmentProfile para Multi-Distro Support

**Contexto**: Ubuntu 24, Fedora, Linux Mint têm diferenças em serviços, comandos e políticas de autenticação.

**Decisão**: Cada distro/desktop é um EnvironmentProfile. Nenhuma lógica de negócio conhece `systemctl` ou `sudo` específicos — apenas interfaces abstratas.

**Consequência**: 
- Comandos concretos vivem em profiles (Ubuntu24Profile, FedoraProfile, etc.)
- Core usa abstrações (ex: "restart audio service") que profiles resolvem
- Suporte multi-distro é "plug-in" — novo profile = novo suporte

---

### [2026-03] Privilege Escalation via PrivilegeManager Port

**Contexto**: Algumas ações exigem `sudo`/`pkexec`. Não sabemos ainda qual estratégia usar.

**Decisão**: Abstrata PrivilegeManager como port. Infrastructure implementará:
- Opção A: ProcessBuilder + `sudo`/`pkexec` com dialogue
- Opção B: Backend daemon separado com permissões (futuro)

**Consequência**: 
- Core não sabe como escalação acontece
- Teste é possível mockando PrivilegeManager
- Troca de estratégia não afeta lógica de negócio

---

### [2026-03] Logging Transparente para Usuário

**Contexto**: App é ferramenta de "recovery" — usuário precisa entender o que está acontecendo.

**Decisão**: Cada RecoveryAction retorna lista de ExecutionStep com status, comando, stderr, stdout, duration e recomendação.

**Consequência**:
- Logging é dado estruturado (JSON-like), não strings brutas
- GUI e TUI formatam e exibem de forma legível
- Teste pode validar que logs saem corretos

---

## Padrões Confirmados

### ExecutionResult Pattern

Toda operação retorna estrutura:
```
RecoveryResult {
  actionId: FixAction
  status: SUCCESS | PARTIAL | FAILED
  steps: List<ExecutionStep>
  startedAt: Instant
  finishedAt: Instant
  recommendation: String?
}
```

Não lança exceção para falhas de execução — retorna resultado estruturado. Exceção só para bugs.

### Domain Entities

Toda entidade estende `DomainEntity`:
- Sem setter (immutable)
- Value Objects para relacionamentos
- Método `validate()` que lança `DomainError`

### Use Case Pattern

Cada Use Case:
- Recebe `input` tipado
- Retorna `output` tipado
- Sem side effects — tudo via ports
- Testável sem mock complexo (inputs/outputs são simples)

---

## Estado de Features

Essas features estão planejadas. NÃO iniciar antes de avisar time:

| Feature | Status | Notas |
|---------|--------|-------|
| Fix Audio | ⏳ Fase 2 | MVP com PipeWire + WirePlumber (Ubuntu24) |
| Fix Bluetooth | ⏳ Fase 2 | systemctl restart bluetooth |
| Fix Network | ⏳ Fase 2 | systemctl restart NetworkManager |
| Fix GNOME | ⏳ Fase 2-3 | killall gnome-shell (sensível, depois) |
| Multi-Linux | ⏳ Fase 3 | Profiles para Fedora, Mint, etc. |
| TUI | ⏳ Fase 2 | Interface terminal com boxes/menus |
| Daemon Mode | ❌ Futuro | Backend privilegiado separado (considerado) |

---

## Bugs Conhecidos / Concerns

### Privilégios em Teste
- Teste unitário não pode fazer `sudo` sem prompt interativo
- Solução: Mock PrivilegeManager em teste; deixar integração para CI/manual

### Configuração de Serviços
- Diferentes Ubuntu/Fedora têm nomes diferentes (ex: NetworkManager vs network-manager)
- Solução: EnvironmentProfile.ServiceName enum por distro

### Changelog / Git Workflow

- Sempre usar branches feature/
- `main` sempre compilável, sempre testável
- PR review obrigatório antes de merge

---

## Dependências Conhecidas

- Java 25 (LTS-track)
- Gradle 8.x
- JUnit 5 (testes)
- JavaFX (GUI, adicionado depois)
- [TUI framework: decidir depois — options: Picocli, Lanterna, ...] (adicionado depois)

---

## Contatos / Referências

- Conversação design: [ChatGPT — Soluções para reset no Ubuntu](https://chatgpt.com/c/69c15458-0770-8326-970a-f5165e8d8141)
- Context Engineering: [Josenaldo — Context Engineering Guide](https://josenaldo.com.br/blog/context-engineering-guia-completo)
