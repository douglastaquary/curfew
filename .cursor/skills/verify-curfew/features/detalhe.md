# Detalhe

O detalhe mostra um acesso da fila. Um id que não existe responde que o acesso não está na fila.

## Sub-features

- `detalhe-token`: token do GitHub, sem dono, ação revogar.
- `detalhe-ausente`: id fora da fila, resposta 404.

## How to get to it (user POV)

- Na fila, abra o acesso `gh-token-18`.
- Digite `/acessos/gh-token-18`.
- Um id desconhecido cai em `/acessos/nao-existe`.

## Driving it with verify-curfew

Preconditions:

- `doctor` devolve `"h1": "Fila"`.
- A instância é o exemplo, então `gh-token-18` existe.

- **Token.** Abra o acesso. Rode `python3 .cursor/skills/verify-curfew/verify-curfew.py get /acessos/gh-token-18`. Status 200, h1 `Token`, marcador `Revogar`, `secret` false.
- **Ausente.** Abra um id que não existe. Rode `python3 .cursor/skills/verify-curfew/verify-curfew.py get /acessos/nao-existe`. Status 404 e h1 `Este acesso não está na fila`.

## Gotchas

- 404 nesta rota é o comportamento. Não trate como falha de `up`.
- A página não mostra o valor do token. A prova é o h1 e o marcador `Revogar`.
