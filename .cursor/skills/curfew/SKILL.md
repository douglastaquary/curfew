---
name: curfew
description: Trabalhar no repositório Curfew sem sair do corte aprovado. Use quando for subir a aplicação, alterar a fila, as regras de varredura ou os templates.
---

# Curfew

A aplicação é Quarkus, Java 21, HTML Qute e JavaScript puro. Um JAR só. Lambda, HTTP API e DynamoDB estão em `template.yaml`. A função de produção só atende com `CURFEW_GATE`. O guia de conexão é `docs/conectar.md`. O deploy é `docs/deploy.md`.

## Subir

```bash
./mvnw quarkus:dev
```

Abra `http://127.0.0.1:8473/`. Estados da fila: `/?estado=vazio`, `/?estado=carregando`, `/?estado=erro`.

## Testar

```bash
./mvnw test
```

O limite da chave e da noite, que os exemplos não percorrem inteiro, é `./mvnw -q -Dtest=LeaseAndNightPropertyTest test`. A skill é `.cursor/skills/verify-lease-bound/SKILL.md`.

## Onde mexer

- Fila de teste: `src/main/java/curfew/scan/MockAccessSource.java`. A aplicação sem variável começa vazia.
- Regras: `src/main/java/curfew/scan/ScanRules.java`
- Teto de concorrência: `ScanEngine`
- Tokens evitados: `TokenLedger` (o contador de chamadas de modelo permanece zero)
- Log ao vivo: `GET /varredura/eventos`. Cada linha leva sistema, idade, dono e ação. Sem segredo.
- Passagem da noite: `POST /noite` com `confirmo=sim`. Sem `CURFEW_CLOSE=sim`, só muda a fila local. Com essa variável e `CURFEW_GITHUB_TOKEN`, apaga webhook, chave de deploy e nome de segredo. Produção pede um humano. Não defina `CURFEW_CLOSE` sem um sim explícito.
- Chave com prazo: `POST /chaves`. No máximo 23 horas. O formulário não emite segredo. Com `Accept: application/json` e `CURFEW_ISSUE=sim`, a API pede uma sessão AWS e devolve o valor uma vez. A fila guarda só os quatro finais. Não defina `CURFEW_ISSUE` sem um sim explícito.
- Prova no ar: `python3 .cursor/skills/verify-curfew/verify-curfew.py prove`. O comando sobe a instância, lê a varredura e guarda o print.
- Telas: `src/main/resources/templates/Pages/` e `src/main/resources/META-INF/resources/app.css`
- Filtro no navegador: `app.js`. O log da varredura lê `GET /varredura/eventos`.

## Onde publicar

O push vai para o GitHub, `douglastaquary/curfew`. Não envie este projeto para o repositório do Cursor.

A leitura real está em `curfew.read`. Sem variável, a fila fica vazia. Com `CURFEW_GITHUB_TOKEN` ou o par `CURFEW_AWS_ACCESS_KEY_ID` e `CURFEW_AWS_SECRET_ACCESS_KEY`, a varredura lê webhook, nome de segredo, chave de deploy e chaves IAM ativas. O valor não entra na fila. O perfil de teste força `curfew.read.mode=mock`. Uma instalação por cliente. Sem login. A contagem fica em `/contagem`.

## Parar

A publicação segue `.cursor/skills/curfew-deploy/SKILL.md`. Comece por `aws sts get-caller-identity`. Se o perfil default já tem access key, não rode `aws login` e não apague a credencial. Sem identidade ou sem `CURFEW_GATE`, pare e registre o erro em `docs/deploy.md`. Não defina `CURFEW_CLOSE`, `CURFEW_ISSUE` ou modelo sem um sim explícito. Não grave credencial no repositório. Se o layout dos prints for reprovado, o corte seguinte pode testar TypeScript compilado para um `app.js` dentro do mesmo JAR.
