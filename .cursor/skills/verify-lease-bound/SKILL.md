---
name: verify-lease-bound
description: Confere que toda chave aceita acaba antes de 24 horas, que o segredo não entra na fila, e que a passagem desarmada conserva quem pede humano. Use ao mudar LeaseTerm, LeaseDelivery, LeaseBook, NightlyRun ou SessionJson.
---

# Limite da chave e da noite

A promessa: uma chave aceita acaba antes de 24 horas, o segredo da sessão não aparece na fila, e a passagem desarmada não tira quem pede humano.

O dano: uma chave de um dia inteiro, um segredo na página, ou produção saindo da fila sem pergunta.

## Quando rodar

Rode se a mudança toca `LeaseTerm`, `LeaseDelivery`, `LeaseBook`, `Minted`, `NightlyRun`, `SessionJson` ou `LeaseAndNightPropertyTest`.

## Comando

```bash
./mvnw -q -Dtest=LeaseAndNightPropertyTest test
```

O teste chama o código real. Horas de -1 a 48, tarefas seguras e tarefas que parecem segredo, GitHub, AWS e sistema desconhecido, passagem armada e desarmada. A noite percorre os 64 subconjuntos do exemplo. Não há modelo separado. Não chama AWS.

## O que a falha significa

Se o teste falha, a propriedade quebrou no código. Não afrouxe o teste para ele passar. Um erro de modelagem seria o teste rejeitar uma chave de 23 horas ou exigir chamada de rede. Isso não é o caso: 23 horas passa, e a rede não entra.

## O que fica de fora

Não cobre a resposta HTTP, o HTML da landing, nem o relógio do STS na conta. A prova da tela continua em `verify-curfew`.

## Cobertura

Arquivos: `src/main/java/curfew/scan/LeaseTerm.java`, `LeaseDelivery.java`, `LeaseBook.java`, `NightlyRun.java`, `src/main/java/curfew/read/SessionJson.java`.

Última conferência no mesmo commit que criou `LeaseAndNightPropertyTest`. Se esse teste mudar depois desta skill, rode o comando de novo e avance esta frase só quando o código e o teste ainda disserem a mesma coisa.
