# Como conectar

A varredura é leitura. Quatro chamadas HTTP no GitHub e uma chamada IAM. A Curfew não instala um GitHub App, não assume uma role e não recebe webhook de CI.

O valor de token, chave e secret não entra na fila, na página, no log nem na tabela.

## GitHub

1. No GitHub, em Settings, Developer settings, Fine-grained tokens, crie um token.
2. Em Repository access, marque só os repositórios que a Curfew pode ver.
3. Em Permissions, deixe leitura em Metadata, Administration e Secrets. Administration cobre webhook e deploy key. Secrets cobre o nome do secret de Actions. O valor do secret não vem nessa listagem.
4. Copie o token para o ambiente do processo, no nome `CURFEW_GITHUB_TOKEN`. Não cole no git, no template nem na página.

A Curfew faz, com `Authorization: Bearer` e sem seguir redirect:

- `GET https://api.github.com/user/repos?per_page=5&sort=pushed`
- `GET https://api.github.com/repos/{dono}/{repo}/hooks`
- `GET https://api.github.com/repos/{dono}/{repo}/actions/secrets`
- `GET https://api.github.com/repos/{dono}/{repo}/keys`

Entram no máximo cinco repositórios, os mais recentemente atualizados que esse token enxerga. A empresa inteira não entra de uma vez. Para outro conjunto, gere outro token com outros repositórios e troque a variável.

## AWS

A leitura não percorre a conta. Ela chama `iam:ListAccessKeys` na identidade da access key que você colocou no ambiente. O resultado são as chaves daquele usuário, com status, data e nome. A fila mostra quatro caracteres finais.

1. Crie um usuário IAM, por exemplo `curfew-leitura`.
2. Anexe só esta política, trocando `CONTA` pelo número da conta:

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Action": "iam:ListAccessKeys",
    "Resource": "arn:aws:iam::CONTA:user/curfew-leitura"
  }]
}
```

3. Crie uma access key desse usuário.
4. Coloque o par no ambiente: `CURFEW_AWS_ACCESS_KEY_ID` e `CURFEW_AWS_SECRET_ACCESS_KEY`.

Chave de outro usuário, role e segredo do Secrets Manager ficam de fora desta chamada.

## CI

Não há passo de workflow para “conectar o CI”. Um job não envia log para a Curfew.

O que aparece de CI são os nomes dos secrets de GitHub Actions nos cinco repositórios do token. O valor não é pedido.

## Servidor

Onde a Curfew roda, as três variáveis acima ficam no ambiente do processo.

- Máquina local: exporte no shell que executa `./mvnw quarkus:dev`. Sem as variáveis, a fila é o exemplo.
- Função publicada: Configuration, Environment variables. O template não leva token de cliente.

Abra `/conexoes`. Com a variável presente, a linha diz que a leitura está ligada. Abra `/varredura` para ver o log. Nada é fechado.

Para desligar, apague as variáveis, revogue o token no GitHub e desative a access key.

## Porta

A função publicada exige `CURFEW_GATE`, com 16 caracteres ou mais. Sem ela, a resposta é “A porta não foi configurada.” Com ela, abra `/porta`, digite o segredo e o navegador guarda um cookie de uma hora. Cliente HTTP manda `Authorization: Bearer` com o mesmo segredo.

`CURFEW_CLOSE` e `CURFEW_ISSUE` não entram neste deploy. A passagem da noite não apaga nada na conta, e o formulário não emite sessão.
