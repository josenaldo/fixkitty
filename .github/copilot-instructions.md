Ver AGENTS.md para regras universais e lista de skills.

Skills em `.agents/skills/` carregadas por todas as ferramentas via:
- Claude Code: `.claude/skills/` (symlink → `.agents/skills/`)
- GitHub Copilot: `.github/skills/` (symlink → `.agents/skills/`)
- Cursor/OpenAI/Gemini: `.agents/skills/` (nativo)
