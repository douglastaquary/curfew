# Agentes neste repositório

O corte atual é a fila fictícia. Não avance para o próximo corte sem um sim de quem mantém o projeto.

Correção vira teste. O ciclo de desenvolver, corrigir, publicar e aprender está em `.cursor/skills/curfew-aprender/SKILL.md`. As regras aplicáveis da skill poteto-mode estão nessa mesma página. Cada ajuste em `docs/aprendizado.md` cita um método que `LearningLedgerTest` confere. A fábrica de agentes, com merge sozinho ou varredura a cada release, continua fechada.

Fora deste corte, até esse sim:

- Conector real de GitHub ou AWS
- Credencial de cliente
- Deploy (`sam deploy`)
- Modelo de linguagem
- Fechar, revogar ou apagar qualquer acesso

## Onde publicar

O push vai para o GitHub, no repositório `douglastaquary/curfew`. Não envie este projeto para o repositório do Cursor.

## Como rodar

```bash
./mvnw quarkus:dev
```

A porta é 8473. Os testes são `./mvnw test`.

O mock está em `src/main/java/curfew/scan/MockAccessSource.java`. As regras estão em `ScanRules`. Os templates estão em `src/main/resources/templates/Pages/`.

## Segurança

Não coloque valor de token, chave ou senha em código, teste, log, URL ou template. A fila guarda idade, dono e ação. As chaves `dev-not-a-secret` em `application.properties` só existem para o SDK subir em dev e teste. Não as troque por uma chave real.
