# Agentes neste repositório

O corte atual é a fila e a passagem da noite. Sem `CURFEW_CLOSE=sim`, executar a passagem só tira da fila local o que não pede humano e não chama a conta. Com `CURFEW_CLOSE=sim` e `CURFEW_GITHUB_TOKEN`, a passagem apaga webhook, chave de deploy e nome de segredo no GitHub. Produção continua pedindo um humano. Sem variável de leitura, a fila é o exemplo. Não avance para modelo ou deploy, e não defina `CURFEW_CLOSE`, sem um sim que diga isso.

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
