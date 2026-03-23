---
name: enforce-architecture
description: "Constraint-skill: valida fronteiras de Clean Architecture no fixkitty. Use SEMPRE como último passo ao criar ou alterar qualquer artefato, por camada ou por feature. Detecta importações proibidas, responsabilidades vazadas e acoplamentos indevidos. Não gera código — apenas valida."
---

# Skill: Enforcer de Arquitetura (Constraint-Skill)

## Quando usar

- Como último passo de qualquer outra skill (obrigatório)
- Ao revisar código de qualquer camada
- Antes de commit de código novo
- Quando suspeitar de vazamento de responsabilidade entre camadas

## Fronteiras proibidas

| De (quem importa) | Proibido importar |
|---|---|
| `core/` (domain) | `application/`, `infrastructure/`, `interfaces/` |
| `application/` (use cases) | `infrastructure/`, `interfaces/gui/`, `interfaces/tui/` |
| `infrastructure/` | `interfaces/gui/`, `interfaces/tui/` |
| `interfaces/gui/` | `interfaces/tui/` (e vice-versa) |

Permitido:
- `core/` pode ser importado por qualquer camada
- `application/` pode importar `core/`
- `infrastructure/` pode importar `core/` e implementar interfaces de `core/ports/`
- `interfaces/` pode importar `application/` e tipos de resposta de `core/`

## Instruções

### 1. Verificar imports proibidos nos arquivos criados/alterados

- [ ] Se estiver em `core/`: não importa nada fora de `core/`
- [ ] Se estiver em `application/`: não importa `infrastructure/` nem `interfaces/`
- [ ] Se estiver em `infrastructure/`: não importa `interfaces/`
- [ ] Se estiver em `interfaces/gui/`: não importa `interfaces/tui/` (e vice-versa)
- [ ] Nenhuma camada importa `javafx.*` exceto `interfaces/gui/`
- [ ] `System.out` direto apenas em `interfaces/` via `TuiPrinter`

### 2. Verificar responsabilidades indevidas

- [ ] Controllers (gui/tui) sem `if` de distro, retry ou construção de comandos
- [ ] Use cases sem `ProcessBuilder` nem strings de comando shell
- [ ] Domain entities sem referência a serviços Linux específicos (PipeWire, systemctl etc.)
- [ ] Environment profiles sem execução de comandos — apenas declarações de strings
- [ ] Infrastructure sem redefinição de regras que pertencem ao domain/application

### 3. Verificar estrutura de pacotes

- [ ] `org.fixkitty.core.*` — domain apenas
- [ ] `org.fixkitty.application.*` — use cases apenas
- [ ] `org.fixkitty.infrastructure.*` — implementações, profiles, CommandRunner
- [ ] `org.fixkitty.interfaces.gui.*` — JavaFX controllers e FXML
- [ ] `org.fixkitty.interfaces.tui.*` — componentes terminal

## Script de verificação rápida

```bash
# Domain importando infrastructure ou interfaces?
grep -rn "import org.fixkitty.infrastructure" src/main/java/org/fixkitty/core/
grep -rn "import org.fixkitty.interfaces" src/main/java/org/fixkitty/core/

# Application importando infrastructure ou interfaces?
grep -rn "import org.fixkitty.infrastructure" src/main/java/org/fixkitty/application/
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

## Critical

- Esta skill não gera código — apenas valida
- Violação encontrada: parar e corrigir antes de continuar
- Nenhuma violação é tolerada como "temporária" — cria precedente difícil de reverter

## Quando reportar violação

Se qualquer grep retornar resultado, reportar:
1. Arquivo infrator e import proibido encontrado
2. Camada correta onde a lógica deveria estar
3. Sugestão de correção (mover para use case / criar port / injetar dependência)

## Exemplos

### Violação: Controller com lógica

```java
// interfaces/gui/AudioController.java — VIOLAÇÃO
@FXML void onFixAudio() {
    ProcessBuilder pb = new ProcessBuilder("systemctl", "--user", "restart", "pipewire");
    pb.start(); // lógica de negócio no controller
}
```

Correção: Mover lógica para `FixAudioUseCase.execute()`, controller apenas chama o use case.

### Violação: Use case importando infrastructure

```java
// application/FixAudioUseCase.java — VIOLAÇÃO
import org.fixkitty.infrastructure.linux.SystemctlRunner;
```

Correção: Criar interface `CommandRunner` em `core/ports/`, injetar via construtor.

## Consulte também

- [layer-domain](../layer-domain/SKILL.md)
- [layer-application](../layer-application/SKILL.md)
- [layer-infrastructure](../layer-infrastructure/SKILL.md)
- [layer-interface](../layer-interface/SKILL.md)
- [write-tests](../write-tests/SKILL.md)
- [implement-feature](../implement-feature/SKILL.md)
