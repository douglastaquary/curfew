# Mapa de verificação da Curfew

Este diretório é a fonte da verificação das telas. Leia o índice e siga o arquivo da tela. A instância é a que `verify-curfew.py up` subiu, em `http://127.0.0.1:8473`.

## Baseline preconditions

- Uma execução só. Não dirija uma Curfew que já estava na porta.
- Rode `python3 .cursor/skills/verify-curfew/verify-curfew.py doctor` e exija `"h1": "Fila"`.
- A prova usa o exemplo. O processo não leva token de GitHub nem chave de AWS.
- Não apague `artifacts/` ao encerrar.

## Driving conventions

- Comece pelo doctor.
- Use o caminho da rota, não a posição no HTML.
- Copie o comando do arquivo da tela como está.
- 404 em `/acessos/nao-existe` é o resultado certo, não falha de subida.

## Proof and skip reporting

- A prova junta a ação e o estado, não só a tela final.
- `get` prova status, h1 e marcadores. `log` prova o fluxo. `screenshot` prova o arquivo em `artifacts/`.
- Se uma rota não abrir, registre o comando e a condição que faltou. Não marque como verificado por outra rota.

## Features

- [Fila](./fila.md) cobre o exemplo, vazio, carregando e erro.
- [Detalhe](./detalhe.md) cobre o token sem dono e o id que não existe.
- [Varredura](./varredura.md) cobre o log ao vivo e os contadores do exemplo.
- [Conexões](./conexoes.md) cobre GitHub e AWS desligados nesta instância.
