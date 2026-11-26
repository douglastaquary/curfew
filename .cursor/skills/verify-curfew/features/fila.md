# Fila

A fila mostra os acessos do exemplo e os três estados explícitos: vazio, carregando e erro.

## Sub-features

- `fila-exemplo`: seis acessos, com dono e ação, sem valor de segredo.
- `fila-vazia`: a leitura não achou acesso esquecido.
- `fila-carregando`: a leitura ainda está em curso.
- `fila-erro`: a leitura não completou e nada foi fechado.
- `fila-prazo`: a chave de exemplo acaba em menos de 24 horas. O comando não envia o formulário.

## How to get to it (user POV)

- Abra `http://127.0.0.1:8473/`.
- Abra `/?estado=vazio`, `/?estado=carregando` ou `/?estado=erro`.
- Use o link Fila no topo.

## Driving it with verify-curfew

Preconditions:

- `doctor` devolve `"h1": "Fila"`.

- **Exemplo.** Abra a fila. Rode `python3 .cursor/skills/verify-curfew/verify-curfew.py get /`. Status 200, h1 `Fila`, marcadores `Trancar e pedir um humano`, `Chave com prazo` e `menos de 24 horas`, `secret` false. Não envie o formulário da chave.
- **Vazio.** Abra o estado vazio. Rode `python3 .cursor/skills/verify-curfew/verify-curfew.py get '/?estado=vazio'`. Status 200 e marcador `Nenhum acesso esquecido`.
- **Carregando.** Abra o estado carregando. Rode `python3 .cursor/skills/verify-curfew/verify-curfew.py get '/?estado=carregando'`. Status 200 e marcador `Lendo GitHub e AWS`.
- **Erro.** Abra o estado erro. Rode `python3 .cursor/skills/verify-curfew/verify-curfew.py get '/?estado=erro'`. Status 200 e marcador `Nada foi fechado`.

## Gotchas

- Os botões de filtro só escondem linha no navegador. Este comando não clica. A prova HTTP é a fila completa.
- `secret` false não imprime o valor. Se a resposta tiver marcador de segredo, o comando falha sem mostrar o corpo.
