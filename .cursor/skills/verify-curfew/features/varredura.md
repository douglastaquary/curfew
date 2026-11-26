# Varredura

A varredura acompanha a leitura. O HTML abre com contadores em zero. Sem conta, o fluxo termina em 0, 0, 0 e 0.

## Sub-features

- `varredura-pagina`: título, acompanhamento e contadores iniciais em zero.
- `varredura-fluxo`: cada acesso vira uma linha e o fim declara que nada foi fechado.
- `varredura-print`: o arquivo em `artifacts/` mostra a tela.
- `varredura-noite`: a passagem anota a ação. Produção pede um humano. O botão executa a passagem na fila. A conta fica de fora neste comando.

## How to get to it (user POV)

- Use o link Varredura no topo.
- Abra `http://127.0.0.1:8473/varredura`.

## Driving it with verify-curfew

Preconditions:

- `doctor` devolve `"h1": "Fila"`.
- A instância não leva conta. Conta real não entra neste processo.

- **Página.** Abra a varredura. Rode `python3 .cursor/skills/verify-curfew/verify-curfew.py get /varredura`. Status 200, h1 `Varredura`, marcadores `Acompanhamento`, `Passagem da noite` e `Executar a passagem`. Não envie o formulário. Esse comando só lê.
- **Noite vazia.** Abra a varredura vazia. Rode `python3 .cursor/skills/verify-curfew/verify-curfew.py get '/varredura?estado=vazio'`. Status 200, sem o marcador `Passagem da noite`.
- **Fluxo.** Leia até o fim. Rode `python3 .cursor/skills/verify-curfew/verify-curfew.py log`. `done` é `0 0 0 0`, com início e nada fechado. `secret` false.
- **Print.** Grave a tela. Rode `python3 .cursor/skills/verify-curfew/verify-curfew.py screenshot /varredura`. O JSON traz o caminho em `artifacts/varredura.png` e o arquivo continua lá depois de `down`.

## Gotchas

- O HTML inicial tem os contadores em zero. A prova dos números é o comando `log`, não o `get`.
- O print pode mostrar o log ainda enchendo. O número que decide a prova é o `done` do `log`.
- `./mvnw test` não roda enquanto esta instância segura `target/`. Rode `down` antes.
