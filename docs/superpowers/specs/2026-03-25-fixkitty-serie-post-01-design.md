# Spec: Blog Post — FixKitty Série, Post 1

**Data:** 2026-03-25
**Tipo:** Blog post de abertura de série (build in public)
**Destino:** `/home/josenaldo/repos/personal/josenaldo.github.io/content/blog/fixkitty-01-o-inicio.md`
**Status:** spec aprovado pelo autor

---

## Objetivo

Primeiro post de uma série de build-in-public sobre o FixKitty. Situar o leitor no início da aventura: o problema real, a ideia, as decisões técnicas, a Fase 1 concluída, e o convite para acompanhar.

---

## Audiência

Desenvolvedores brasileiros com experiência em programação. Conhecem Java, podem não conhecer JavaFX ou Clean Architecture. Curiosos sobre uso prático de IA no desenvolvimento.

---

## Tom

Mistura de:
- **Especialista humilde:** "sei Java, mas JavaFX é terra nova pra mim — última vez foi 2008"
- **Entusiasta com gosto pelo caos:** "resolvi construir um app desktop Linux em 2026 porque sim"

Casual, bem-humorado, primeira pessoa. Referências culturais BR. Honesto sobre o que sabia e o que estava aprendendo.

---

## Estrutura do Post

### Frontmatter

```yaml
---
title: "FixKitty: quando o Linux quebra e você resolve construir o conserto"
description: "Como uma frustração com áudio, bluetooth e wifi virou um projeto Java 25 com Clean Architecture, JavaFX e IA. Post 1 de uma série build-in-public."
date: 2026-03-25 09:00:00 -0300
author: Josenaldo Matos
image: /images/blog/fixkitty-01-o-inicio.png
category: Engenharia de Software
status: draft
language: pt
---
```

### Seção 1: Abertura — A Dor Real (~200 palavras)

Cenário: desenvolvedor Linux, à noite, áudio parando no meio de uma call, bluetooth sumindo, wifi reclamando. Abrindo o terminal, pesquisando o mesmo comando pela quinta vez na semana. Cansaço de `systemctl restart` na memória muscular.

Tom: humor com identificação. O leitor que usa Linux reconhece. Não é reclamação — é contextualização.

Termina com a faísca: "e se eu tivesse um botão pra isso?"

### Seção 2: A Ideia (~150 palavras)

O conceito: um app simples, à mão, onde você clica num botão ou digita `fix audio` e o sistema resolve. Sem pesquisar, sem lembrar flag, sem sudo na cabeça às 23h.

Não é um substituto para entender Linux — é uma ferramenta de conveniência para quem já entende e só quer se livrar do trabalho braçal de lembrar o comando certo.

Mencionar brevemente o nome: FixKitty. De onde veio (deixar leve, não precisa de origem profunda — pode ser inventado/aleatório/gatinho fixando problemas).

### Seção 3: Decisões Técnicas — "Por Que Tudo Isso?" (~300 palavras)

Honestidade é o tom. Três decisões principais:

**Java 25:** conheço bem, me sinto em casa, não quero aprender linguagem nova enquanto aprendo JavaFX. Escolha pragmática.

**JavaFX + AtlantaFX:** última vez que fiz app desktop Java foi 2008 com Swing. O mundo mudou. JavaFX existe, tem tema Dracula via AtlantaFX, tem Ikonli para ícones. Parece divertido de aprender.

**Clean Architecture:** honestidade máxima — "não consigo fazer diferente". Quando o projeto tem domínio, casos de uso e infraestrutura, a separação de camadas vira instinto. Plus: facilita testar com mocks sem subir o sistema real.

**TUI (Lanterna):** fallback para quando não tem display gráfico, ou para quem prefere terminal.

Não precisa ser tutorial de nada — só explicar as escolhas de forma conversacional.

### Seção 4: A IA Entrou (~250 palavras)

Transparência total sobre o processo.

Explicar o que é Subagent-Driven Development em termos simples: em vez de deixar um agente de IA trabalhar horas seguidas no mesmo contexto (onde ele acumula erros e esquece coisas), você divide o trabalho em tarefas pequenas. Cada tarefa vai para um subagente fresco, que faz só aquilo. Depois, um revisor checa se o que foi implementado é o que foi pedido. Outro revisor checa a qualidade do código.

Mencionar o momento real: o teste do WARN policy que encontrou um bug de lógica real em `aggregate()`. Não para se gabar — para mostrar que a revisão funciona.

Ser claro: você (o autor) ainda tomou todas as decisões. A IA executou. A diferença é que, com boas guardrails, o que a IA executou foi revisado antes de você aceitar.

### Seção 5: Fase 1 Concluída (~200 palavras)

O que existe hoje:
- GUI funcional (JavaFX, tema Dracula) com 6 botões de ação
- TUI para quem prefere terminal
- 6 ações: Fix Audio, Fix Bluetooth, Fix Network, Fix GNOME Shell, Fix All, Check Environment
- 34 testes passando
- Clean Architecture completa (domínio → aplicação → infraestrutura → interfaces)

Nada está no ar ainda — é um projeto local, em construção. A Fase 1 é o MVP: a estrutura existe, os botões existem, mas os comandos Linux reais ainda precisam ser validados no sistema.

Tom: empolgado, mas honesto sobre onde está.

### Seção 6: Próximos Passos + Convite (~150 palavras)

O que vem a seguir:
- Testar os comandos Linux reais (e descobrir o que quebra)
- Testes de integração com TestFX
- Empacotamento para distribuição

Convite para a série: "vou postar conforme construo, com o bem e o mal". Links para repo (se público) ou promessa de que vai publicar quando estiver pronto.

Encerramento no tom do autor — algo entre "vamos ver no que dá" e "isso vai ser divertido".

---

## Restrições

- Sem screenshots (Fase 1 é MVP local, sem visual polido ainda) — ou mencionar brevemente que vem nas próximas partes
- Sem código inline longo — no máximo uma linha de exemplo para contextualizar
- Não virar tutorial de JavaFX ou Clean Architecture
- Não prometer datas ou número de posts

---

## Métricas de Sucesso

- Leitor entende o que é o FixKitty e por que existe
- Leitor entende as escolhas técnicas sem precisar ser expert
- Leitor tem vontade de acompanhar a série
- Leitor sabe que IA foi usada e como, sem mistério
