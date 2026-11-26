# Detalhe

O detalhe mostra um acesso da fila. Um id que não existe responde que o acesso não está na fila.

## Sub-features

- `detalhe-token`: token do GitHub, sem dono, ação revogar.
- `detalhe-ausente`: id fora da fila, resposta 404.

## How to get to it (user POV)

- Um id que a leitura não trouxe cai em `/acessos/nao-existe`.
- `gh-token-18` só existe quando o teste planta a fila. Na instalação sem conta, esse id responde 404.

## Driving it with verify-curfew

Preconditions:

- `doctor` devolve `"h1": "Fila"`.
- A instância sem conta não tem `gh-token-18`.

- **Ausente na instalação.** Abra o id do antigo exemplo. Rode `python3 .cursor/skills/verify-curfew/verify-curfew.py get /acessos/gh-token-18`. Status 404.
- **Ausente.** Abra um id que não existe. Rode `python3 .cursor/skills/verify-curfew/verify-curfew.py get /acessos/nao-existe`. Status 404 e h1 `Este acesso não está na fila`.

## Gotchas

- 404 nesta rota é o comportamento. Não trate como falha de `up`.
- A página não mostra o valor do token. Na instalação sem conta a prova é o 404.
