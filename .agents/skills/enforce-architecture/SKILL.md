---
name: enforce-architecture
description: "Constraint-skill: valida fronteiras de Clean Architecture no projeto fixkitty. Use SEMPRE como último passo ao criar qualquer artefato (ação, use case, UI, profile). Detecta importações proibidas entre camadas."
---

# Skill: Enforcer de Arquitetura (Constraint-Skill)

## Quando usar

- **SEMPRE** — como último passo de qualquer outra skill
- Ao revisar código de qualquer camada
- Antes de commit de código novo
- Quando suspeitar de vazamento de responsabilidade entre camadas

---

## Fronteiras proibidas (regras absolutas)

| De (quem importa) | Para (o que é proibido importar) |
|---|---|
| `core/` (domain) | `application/`, `infrastructure/`, `interfaces/` |
| `application/` (use cases) | `infrastructure/`, `interfaces/gui/`, `interfaces/tui/` |
| `infrastructure/` | `interfaces/gui/`, `interfaces/tui/` |
| `interfaces/gui/` | `interfaces/tui/` e vice-versa |

Permitido:
- `core/` pode ser importado por qualquer camada
- `application/` pode importar `core/`
- `infrastructure/` pode importar `core/` e implementar interfaces de `core/ports/`
- `interfaces/` pode importar `application/` e `core/` (tipos de resposta apenas)

---

## Checklist de validação

### 1. Verificar imports proibidos nos arquivos criados/alterados

Para cada arquivo Java novo ou modificado:

- [ ] Se estiver em `core/`: não importa nada fora de `core/`
- [ ] Se estiver em `application/`: não importa `infrastructure/` nem `interfaces/`
- [ ] Se estiver em `infrastructure/`: não importa `interfaces/`
- [ ] Se estiver em `interfaces/gui/`: não importa `interfaces/tui/` (e vice-versa)
- [ ] Nenhuma camada importa `javafx.*` exceto `interfaces/gui/`
- [ ] Nenhuma camada exceto `interfaces/` usa `System.out` diretamente

### 2. Verificar responsabilidades indevidas

- [ ] Controllers (gui/tui) NÃO contêm `if`, lógica de retry, ou construção de comandos
- [ ] Use cases NÃO constroem `ProcessBuilder` nem constroem strings de comando shell
- [ ] Domain entities NÃO têm referência a serviços Linux específicos (PipeWire, systemctl etc.)
- [ ] Environment profiles NÃO executam comandos — apenas declaram strings

### 3. Verificar estrutura de pacotes

- [ ] `org.fixkitty.core.*` — domain apenas
- [ ] `org.fixkitty.application.*` — use cases apenas
- [ ] `org.fixkitty.infrastructure.*` — implementações, profiles, CommandRunner
- [ ] `org.fixkitty.interfaces.gui.*` — JavaFX controllers e FXML
- [ ] `org.fixkitty.interfaces.tui.*` — componentes terminal

---

## Script de verificação rápida

Execute para detectar imports proibidos óbvios:

```bash
# Domain importando infrastructure?
grep -rn "import org.fixkitty.infrastructure" src/main/java/org/fixkitty/core/

# Domain importando interfaces?
grep -rn "import org.fixkitty.interfaces" src/main/java/org/fixkitty/core/

# Application importando infrastructure?
grep -rn "import org.fixkitty.infrastructure" src/main/java/org/fixkitty/application/

# Application importando GUI?
grep -rn "import org.fixkitty.interfaces" src/main/java/org/fixkitty/application/

# Infrastructure importando GUI/TUI?
grep -rn "import org.fixkitty.interfaces" src/main/java/org/fixkitty/infrastructure/

# GUI importando TUI (e vice-versa)?
grep -rn "import org.fixkitty.interfaces.tui" src/main/java/org/fixkitty/interfaces/gui/
grep -rn "import org.fixkitty.interfaces.gui" src/main/java/org/fixkitty/interfaces/tui/

# ProcessBuilder fora de infrastructure?
grep -rn "ProcessBuilder" src/main/java/org/fixkitty/core/
grep -rn "ProcessBuilder" src/main/java/org/fixkitty/application/
grep -rn "ProcessBuilder" src/main/java/org/fixkitty/interfaces/
```

Resultado esperado: **zero linhas** para cada grep acima.

---

## Critical

- Esta skill NÃO gera código — apenas valida
- Se uma violação for encontrada, PARAR e corrigir antes de continuar
- Exceção zero: nenhuma violação de fronteira é tolerada, nem "temporária"

## Quando reportar violação

Se qualquer grep retornar resultado, reportar:
1. Arquivo infrator
2. Import proibido encontrado
3. Camada correta onde a lógica deveria estar
4. Sugestão de correção (mover para use case / criar port / injetar dependência)

## Exemplos

### Violação clássica: Controller com lógica

```java
// interfaces/gui/AudioController.java — VIOLAÇÃO
@FXML
void onFixAudio() {
    // ❌ Lógica de negócio aqui — não pertence ao controller
    ProcessBuilder pb = new ProcessBuilder("systemctl", "--user", "restart", "pipewire");
    pb.start();
}
```

Correção: Mover lógica para `FixAudioUseCase.execute()`, controller apenas chama o use case.

### Violação clássica: Use case importando infrastructure

```java
// application/FixAudioUseCase.java — VIOLAÇÃO
import org.fixkitty.infrastructure.linux.SystemctlRunner; // ❌
```

Correção: Criar interface `CommandRunner` em `core/ports/`, injetar via construtor.

## Consulte também

- Todas as outras skills referenciam esta — é o passo final obrigatório
- `memory/MEMORY.md` — Decisões arquiteturais registradas
- `AGENTS.md` — Regras de arquitetura (fonte de verdade)
