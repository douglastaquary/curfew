---
name: curfew
description: Trabalhar no repositório Curfew sem sair do corte aprovado. Use quando for subir a aplicação, alterar a fila fictícia, as regras de varredura ou os templates.
---

# Curfew

A aplicação é Quarkus, Java 21, HTML Qute e JavaScript puro. Um JAR só. Lambda e DynamoDB estão descritos no `template.yaml` e não são usados neste corte.

## Subir

```bash
./mvnw quarkus:dev
```

Abra `http://127.0.0.1:8473/`. Estados da fila: `/?estado=vazio`, `/?estado=carregando`, `/?estado=erro`.

## Testar

```bash
./mvnw test
```

## Onde mexer

- Fila fictícia: `src/main/java/curfew/scan/MockAccessSource.java`
- Regras: `src/main/java/curfew/scan/ScanRules.java`
- Teto de concorrência: `ScanEngine`
- Tokens evitados: `TokenLedger` (o contador de chamadas de modelo permanece zero)
- Log ao vivo: `GET /varredura/eventos`. Cada linha leva sistema, idade, dono e ação. Sem segredo.
- Passagem da noite: `POST /noite` com `confirmo=sim`. Sem `CURFEW_CLOSE=sim`, só muda a fila local. Com essa variável e `CURFEW_GITHUB_TOKEN`, apaga webhook, chave de deploy e nome de segredo. Produção pede um humano. Não defina `CURFEW_CLOSE` sem um sim explícito.
- Chave com prazo: `POST /chaves`. No máximo 23 horas. O exemplo não emite segredo e não chama a conta.
- Prova no ar: `python3 .cursor/skills/verify-curfew/verify-curfew.py prove`. O comando sobe a instância, lê a varredura e guarda o print.
- Telas: `src/main/resources/templates/Pages/` e `src/main/resources/META-INF/resources/app.css`
- Filtro no navegador: `app.js`. O log da varredura lê `GET /varredura/eventos`.

## Onde publicar

O push vai para o GitHub, `douglastaquary/curfew`. Não envie este projeto para o repositório do Cursor.

A leitura real está em `curfew.read`. Sem variável, a fila continua o exemplo. Com `CURFEW_GITHUB_TOKEN` ou o par `CURFEW_AWS_ACCESS_KEY_ID` e `CURFEW_AWS_SECRET_ACCESS_KEY`, a varredura lê webhook, nome de segredo, chave de deploy e chaves IAM ativas. O valor não entra na fila. O perfil de teste força `curfew.read.mode=mock`.

## Parar

Não faça deploy, modelo ou fechamento de acesso sem um sim explícito de quem mantém o projeto. Não grave credencial no repositório. Se o layout dos prints for reprovado, o corte seguinte pode testar TypeScript compilado para um `app.js` dentro do mesmo JAR.
