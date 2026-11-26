---
name: verify-curfew
description: Sobe a Curfew na porta 8473, dirige fila, detalhe, varredura e conexões e guarda a prova. Use ao mudar uma tela ou ao precisar provar que a rota no ar funciona.
---

# Verificar a Curfew

Uma instância só, a que este comando subiu. A prova é o exemplo: o processo filho não recebe `CURFEW_GITHUB_TOKEN` nem o par `CURFEW_AWS_*`. Nada é fechado e o valor de um segredo não entra na saída.

## Launch

Na raiz do repositório:

```bash
python3 .cursor/skills/verify-curfew/verify-curfew.py up
```

Pronto quando o JSON traz `"ok": true` e a fila responde em `http://127.0.0.1:8473/`. O limite é 120 segundos. `--dry-run` mostra o comando e não sobe nada.

## Doctor

```bash
python3 .cursor/skills/verify-curfew/verify-curfew.py doctor
```

Passa quando o pid desta execução está vivo, a porta 8473 pertence a essa árvore e `GET /` devolve `<h1>Fila</h1>` sem marcador de segredo. Rode doctor antes da primeira rota e de novo depois de uma falha.

## Drive

`get` devolve status, h1 e marcadores conhecidos. Não devolve o HTML.

```bash
python3 .cursor/skills/verify-curfew/verify-curfew.py get /
python3 .cursor/skills/verify-curfew/verify-curfew.py get /acessos/gh-token-18
python3 .cursor/skills/verify-curfew/verify-curfew.py get /acessos/nao-existe
python3 .cursor/skills/verify-curfew/verify-curfew.py get /varredura
python3 .cursor/skills/verify-curfew/verify-curfew.py log
python3 .cursor/skills/verify-curfew/verify-curfew.py get /conexoes
```

O mapa está em `features/`. Cada arquivo diz a rota, o comando e o resultado que prova a tela.

## Evidence

O print vai para `.cursor/skills/verify-curfew/artifacts/`. O fluxo da varredura prova `6 6 0 720`, com início, Revogar e "Nada foi fechado".

```bash
python3 .cursor/skills/verify-curfew/verify-curfew.py screenshot /varredura
```

`prove` sobe, lê a varredura, grava o print e encerra:

```bash
python3 .cursor/skills/verify-curfew/verify-curfew.py prove
```

## Cleanup

```bash
python3 .cursor/skills/verify-curfew/verify-curfew.py down
```

`down` encerra o grupo que `up` criou. Não mata pelo nome do processo. Não apaga `artifacts/`. `--dry-run` mostra o pid e não encerra.

Rode `down` antes de `./mvnw test`. Os dois disputam `target/`.

## Helpers

O comando é `.cursor/skills/verify-curfew/verify-curfew.py`. `python3 .cursor/skills/verify-curfew/verify-curfew.py --help` lista os subcomandos. Saída JSON em uma linha. Falha traz `error` e `next`.
