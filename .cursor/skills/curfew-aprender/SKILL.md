---
name: curfew-aprender
description: Desenvolver, corrigir, publicar e aprender na Curfew. Use ao mudar código, consertar um bug, fazer push ou registrar um ajuste.
---

# Aprender na Curfew

O limite do produto continua em `.cursor/skills/curfew/SKILL.md`. Esta skill é o ciclo. Correção vira teste. A skill guarda só o julgamento que o teste não alcança.

Não abra a fábrica: vários agentes, merge sozinho ou varredura a cada release. Esse passo espera o ciclo abaixo acertar sozinho.

## Mapa

Quem usa a aplicação encontra isto:

- `/` fila. Também `/?estado=vazio`, `/?estado=carregando` e `/?estado=erro`.
- `/acessos/gh-token-18` detalhe. Id que não existe responde 404.
- `/varredura` e `GET /varredura/eventos`. O log não leva segredo. A leitura de exemplo fecha em 6 itens, 6 por regra, 0 chamadas de modelo e 720 tokens evitados.
- `/conexoes` leitura ainda desligada.
- `docs/index.html` é a landing. No ar: https://douglastaquary.github.io/curfew/

## Desenvolver

1. Leia o mapa e mude a menor superfície que cumpre o pedido.
2. Rode `./mvnw test`.
3. Se a tela mudou, suba `./mvnw quarkus:dev` na porta 8473 e percorra a rota do mapa. Confira vazio, carregando e erro quando a fila mudar.

## Corrigir

1. Escreva primeiro o teste que falha no comportamento errado.
2. Conserte até esse teste passar.
3. Não deixe comentário justificando atalho. Não copie um exemplo ruim que já está no repositório. Apague o exemplo se ele ensina o erro.

## Aprender

Acrescente uma linha em `docs/aprendizado.md` com a correção e o teste, no formato `` `Classe.metodo` ``. `LearningLedgerTest` falha se a classe ou o método não existirem.

Se a correção já cabe num assert que roda, não repita a regra nesta skill.

## Publicar

O push vai para `origin`, https://github.com/douglastaquary/curfew. Não envie para o repositório do Cursor. Não rode `sam deploy`.

A landing sobe por `.github/workflows/pages.yml` quando `docs/` muda. Os testes sobem por `.github/workflows/test.yml` em todo push na `main`.

Depois do push da landing, confirme que https://douglastaquary.github.io/curfew/ responde com o título da Curfew.
