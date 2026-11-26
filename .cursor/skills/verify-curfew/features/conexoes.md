# Conexões

Conexões diz se a leitura de GitHub e AWS está ligada. Nesta instância de verificação as duas ficam desligadas.

## Sub-features

- `conexoes-desligadas`: GitHub e AWS sem leitura nesta execução.

## How to get to it (user POV)

- Use o link Conexões no topo.
- Abra `http://127.0.0.1:8473/conexoes`.

## Driving it with verify-curfew

Preconditions:

- `doctor` devolve `"h1": "Fila"`.
- O processo foi iniciado por `up`, que tira as variáveis de conta do ambiente.

- **Desligadas.** Abra conexões. Rode `python3 .cursor/skills/verify-curfew/verify-curfew.py get /conexoes`. Status 200, h1 `Conexões`, marcador `Leitura ainda não ligada`.

## Gotchas

- Uma Curfew subida fora deste comando pode estar com conta ligada. Não use essa instância como prova.
- O marcador não mostra token nem chave. Se o corpo tiver valor de segredo, o comando falha sem imprimir.
