---
name: curfew-aprender
description: Desenvolver, corrigir, publicar e aprender na Curfew. Use ao mudar código, consertar um bug, fazer push ou registrar um ajuste.
---

# Aprender na Curfew

O limite do produto continua em `.cursor/skills/curfew/SKILL.md`. Esta skill é o ciclo. Correção vira teste. A skill guarda só o julgamento que o teste não alcança.

Não abra a fábrica: vários agentes, merge sozinho ou varredura a cada release. Esse passo espera o ciclo abaixo acertar sozinho.

## Referência poteto-mode

O plugin está em `plugins/pstack`, com a licença MIT de Lauren Tan. A skill é `plugins/pstack/skills/poteto-mode/SKILL.md` e os playbooks estão em `plugins/pstack/skills/poteto-mode/playbooks/`. O Cursor carrega esse diretório ao abrir o projeto. As mesmas skills também aparecem em `.cursor/skills`.

- Laziness Protocol e Subtract Before You Add. A menor mudança. Apague o peso morto antes de acrescentar.
- Encode Lessons in Structure. A segunda vez que a mesma instrução aparece, ela vai para um teste, não para mais texto.
- Prove It Works e Test Behavior, Not Implementation. O teste olha o que a página ou o evento mostram.
- Fix Root Causes. O conserto é na primeira causa, não num comentário em volta.
- Sequence Work into Verifiable Units. Cada passo termina em `./mvnw test` ou na rota no ar antes do próximo.
- Build the Lever. `LearningLedgerTest` é a ferramenta que o próximo agente roda de novo.
- Never Block on the Human para trabalho reversível. Pare em `sam deploy`, apagar dado e fechar acesso.
- No is an acceptable answer. Se o pedido sai do corte, diga isso.

Quando um playbook e o limite da Curfew discordam, o limite ganha. Não rode `sam deploy`, não feche acesso e não grave credencial de cliente. O merge automático dos playbooks de fábrica continua fechado até um sim explícito.

## Mapa

Quem usa a aplicação encontra isto:

- `/` fila. Também `/?estado=vazio`, `/?estado=carregando` e `/?estado=erro`.
- `/acessos/gh-token-18` detalhe. Id que não existe responde 404.
- `/varredura` e `GET /varredura/eventos`. O log não leva segredo. A leitura de exemplo fecha em 6 itens, 6 por regra, 0 chamadas de modelo e 720 tokens evitados.
- `/conexoes` mostra se GitHub e AWS estão ligados. Sem variável, os dois ficam desligados e a fila é o exemplo.
- `docs/index.html` é a landing. No ar: https://douglastaquary.github.io/curfew/

## Decidir

Antes do código, escreva em um parágrafo o problema nas suas palavras. Se o parágrafo pede deploy, modelo, enxame ou fechar acesso, pare.

Mecânica: `plugins/pstack/skills/how/SKILL.md`. Motivo: `plugins/pstack/skills/why/SKILL.md`, a partir do git e de `docs/aprendizado.md`.

Se a dúvida é de tela ou de formato de dado, faça um rascunho em `scratch/` e um print. O rascunho não entra em `src/`. O playbook é `plugins/pstack/skills/poteto-mode/playbooks/prototype.md`.

Mudança óbvia de um ou dois arquivos não ganha plano. Plano maior só depois do desenho escolhido, e cada item passa em `python3 .cursor/skills/verify-curfew/verify-curfew.py plan <arquivo>`. Apague o arquivo quando o corte fechar. Arena de vários modelos e merge sozinho continuam fechados.

## Desenvolver

1. Diga o objetivo e a menor mudança que o cumpre. Nomeie o dado antes do código. Os tipos de hoje são `RawAccess` e `ClassifiedAccess`. Faça só essa mudança.
2. Rode `./mvnw test`.
3. Se a tela mudou, prove com `python3 .cursor/skills/verify-curfew/verify-curfew.py prove` e rode `down` antes de `./mvnw test`. O mapa das telas está em `.cursor/skills/verify-curfew/features/`.

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
