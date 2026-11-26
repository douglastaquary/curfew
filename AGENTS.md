# Agentes neste repositório

O corte atual é a fila. Sem variável de leitura, ela é o exemplo. Com `CURFEW_GITHUB_TOKEN` ou o par `CURFEW_AWS_ACCESS_KEY_ID` e `CURFEW_AWS_SECRET_ACCESS_KEY`, a varredura lista metadado e descarta o valor do segredo. Não avance para fechamento, modelo ou deploy sem um sim de quem mantém o projeto.

Correção vira teste. O ciclo de desenvolver, corrigir, publicar e aprender está em `.cursor/skills/curfew-aprender/SKILL.md`. O plugin pstack está em `plugins/pstack`. Os playbooks estão em `plugins/pstack/skills/poteto-mode/playbooks/`. Quando um playbook e o limite deste corte discordam, o limite ganha. Cada ajuste em `docs/aprendizado.md` cita um método que `LearningLedgerTest` confere. A fábrica de agentes, com merge sozinho ou varredura a cada release, continua fechada.

Fora deste corte, até esse sim:

- Credencial gravada no repositório
- Deploy (`sam deploy`)
- Modelo de linguagem
- Fechar, revogar ou apagar qualquer acesso

## Onde publicar

O push vai para o GitHub, no repositório `douglastaquary/curfew`. Não envie este projeto para o repositório do Cursor.

## Como rodar

```bash
./mvnw quarkus:dev
```

A porta é 8473. Os testes são `./mvnw test`. A prova no ar é `python3 .cursor/skills/verify-curfew/verify-curfew.py prove`.

O mock está em `src/main/java/curfew/scan/MockAccessSource.java`. As regras estão em `ScanRules`. Os templates estão em `src/main/resources/templates/Pages/`.

## Segurança

Não coloque valor de token, chave ou senha em código, teste, log, URL ou template. A fila guarda idade, dono e ação. As chaves `dev-not-a-secret` em `application.properties` só existem para o SDK subir em dev e teste. Não as troque por uma chave real.
