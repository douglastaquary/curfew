# Agentes neste repositório

O corte atual é a fila, a passagem da noite, a chave com prazo e a porta de produção. A chave da tarefa acaba em menos de 24 horas. O formulário não emite segredo. A sessão AWS só sai em `POST /chaves` com `Accept: application/json` e `CURFEW_ISSUE=sim`, e o valor não fica na fila. Não defina `CURFEW_ISSUE` sem um sim que diga isso. Sem `CURFEW_CLOSE=sim`, executar a passagem só tira da fila local o que não pede humano e não chama a conta. Com `CURFEW_CLOSE=sim` e `CURFEW_GITHUB_TOKEN`, a passagem apaga webhook, chave de deploy e nome de segredo no GitHub. Produção continua pedindo um humano. Sem variável de leitura, a fila é o exemplo. A publicação usa `template.yaml`, exige `GateSecret` e segue `.cursor/skills/curfew-deploy/SKILL.md`. Se o perfil default já tem access key, não rode `aws login` e não apague a credencial. Sem identidade AWS, não invente uma conta. Não avance para modelo, e não defina `CURFEW_CLOSE`, sem um sim que diga isso.

Correção vira teste. O ciclo de desenvolver, corrigir, publicar e aprender está em `.cursor/skills/curfew-aprender/SKILL.md`. O plugin pstack está em `plugins/pstack`. Os playbooks estão em `plugins/pstack/skills/poteto-mode/playbooks/`. Quando um playbook e o limite deste corte discordam, o limite ganha. Cada ajuste em `docs/aprendizado.md` cita um método que `LearningLedgerTest` confere. A fábrica de agentes, com merge sozinho ou varredura a cada release, continua fechada.

Fora deste corte, até esse sim:

- Credencial gravada no repositório
- `CURFEW_CLOSE` e `CURFEW_ISSUE` no template ou no ambiente, sem um sim
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
