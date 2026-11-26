---
name: curfew-deploy
description: Publicar a Curfew na AWS quando a pessoa pediu o deploy. Use ao ver aws login, NoCredentials, Access Key credentials, sam deploy ou a stack curfew.
---

# Deploy da Curfew

Rode na raiz do clone. Não imprima access key, token nem `CURFEW_GATE`. Não apague credencial que já existe.

## Identidade

1. Rode `aws sts get-caller-identity`.
2. Se sair 0, use esse perfil. Não rode `aws login`.
3. Se o erro for `Profile 'default' is already configured with Access Key credentials`, pare. Não apague a credencial do perfil default. Não crie outro perfil. Volte ao passo 1.
4. Se sair `NoCredentials` ou `Unable to locate credentials`, pare. Essa máquina não tem conta. Não invente chave.
5. Se o ARN contiver `curfew-leitura`, pare. Esse usuário só tem `iam:ListAccessKeys` e não publica a stack. A publicação pede outro perfil. Esse usuário entra depois, nas variáveis da função.

## Publicar

Antes do `sam deploy`, rode `sam --version`. A versão 1.132.0 já passou nesse Mac. Se o erro for `No module named 'pygments'`, o CLI está incompleto no Python 3.12 de `/usr/local`. Rode `python3.12 -m pip install --break-system-packages pygments` e confira `sam --version` de novo. Se o `pip` disser `externally-managed-environment`, a flag é o caminho. Não crie um venv: o `sam` já instalado não entra nele. Não é falha da conta AWS. No macOS 12, não rode `brew reinstall aws-sam-cli` e não apague as Command Line Tools. O Homebrew recusa essas ferramentas nesse sistema. Quando `sam --version` imprimir a versão, não reinstale. Siga para o pacote.

Antes do pacote, rode `./mvnw -version`. A linha `Java version` precisa ser 21 ou mais nova. Se o runtime contiver `jbr-17`, é o Java 17 do IntelliJ. Não empacote com ele. No Mac, instale com `brew install --cask temurin@21`, depois `export JAVA_HOME="$(/usr/libexec/java_home -v 21)"`. Se `generate-code` falhar com `NoSuchElementException`, é esse JDK antigo. Não mexa no código.

`CURFEW_GATE` tem 16 caracteres ou mais e fica só no ambiente desta execução. Não grave em `samconfig.toml`.

```bash
./mvnw -q package
sam validate --template-file template.yaml --lint
sam deploy \
  --template-file template.yaml \
  --stack-name curfew \
  --resolve-s3 \
  --capabilities CAPABILITY_IAM \
  --region us-east-1 \
  --parameter-overrides "GateSecret=${CURFEW_GATE}" \
  --no-confirm-changeset
```

Não defina `CURFEW_CLOSE` nem `CURFEW_ISSUE`.

A saída `ApiUrl` abre em `/porta`. A stack já criada está em `https://f3nqvezln1.execute-api.us-east-1.amazonaws.com`. Se a resposta for `Internal Server Error`, não faça outro deploy. Leia o log:

```bash
sam logs -n CurfewFunction --stack-name curfew --region us-east-1 -s '30min ago'
```

Token de GitHub e o par de leitura AWS entram depois, no ambiente da função, não no template. Não atualize o SAM 1.132.0 neste Mac.

## Aprender

Sintoma novo vai para `docs/deploy.md`. `DeployGuideTest.accessKeyProfileIsUsedAsIs` trava a decisão desta skill.
