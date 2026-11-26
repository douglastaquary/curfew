# Deploy

Sobe uma função Java 21, uma HTTP API e a tabela DynamoDB `curfew-access`. Sem VPC. A tabela fica com criptografia e não é apagada com a stack. O segredo da porta entra na hora do deploy, com `NoEcho`. Token de GitHub, access key de leitura, `CURFEW_CLOSE` e `CURFEW_ISSUE` não estão no template.

Custo neste tamanho, us-east-1, uso ocasional: HTTP API a US$ 1,00 por milhão de requisições, Lambda na maior parte do free tier, DynamoDB sob demanda em centavos. Abaixo de US$ 5 no mês. Não é layout multi-AZ.

## Como publicar

Na raiz do clone, primeiro `aws sts get-caller-identity`. Se sair 0, use esse perfil. Não rode `aws login`.

Se o erro for `Profile 'default' is already configured with Access Key credentials`, pare. Não apague a credencial do perfil default. Não crie outro perfil. O perfil default já tem access key. Rode `aws sts get-caller-identity`.

Se o ARN contiver `curfew-leitura`, pare. Esse usuário só tem `iam:ListAccessKeys` e não publica a stack.

Com a identidade de publicação confirmada, confira o Java que o Maven usa. A linha `Java version` precisa ser 21 ou mais nova. `java -version` sozinho não basta: o `./mvnw` segue o `JAVA_HOME`.

```bash
./mvnw -version
```

No Mac, se essa linha for 17 e o runtime contiver `jbr-17`, esse é o Java do IntelliJ. Ele não serve. Instale o Temurin 21 e só então aponte o `JAVA_HOME`. Sem esse JDK, `java_home -v 21` não acha nada.

```bash
brew install --cask temurin@21
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
./mvnw -version
```

A linha `Java version` tem de começar com 21. O runtime não pode continuar em `jbr-17`.

Se `generate-code` falhar com `NoSuchElementException`, é esse JDK antigo. Não é o código da Curfew.

Antes do deploy, `sam --version` precisa imprimir a versão. Se aparecer `No module named 'pygments'`, o CLI foi instalado sem essa biblioteca. No Mac, com o Python 3.12 de `/usr/local`:

```bash
python3.12 -m pip install pygments
sam --version
```

```bash
./mvnw -q package
export CURFEW_GATE='um-segredo-de-16-ou-mais'
sam deploy \
  --template-file template.yaml \
  --stack-name curfew \
  --resolve-s3 \
  --capabilities CAPABILITY_IAM \
  --region us-east-1 \
  --parameter-overrides "GateSecret=${CURFEW_GATE}" \
  --no-confirm-changeset
```

A saída `ApiUrl` é a base. Abra `ApiUrl/porta`.

O zip é `target/function.zip`. Gerar o zip não publica.

## O que esta tentativa encontrou

| Quando | Sintoma | O que ficou |
| --- | --- | --- |
| 26 nov 2026, 00:12, Brasília | `aws` ausente, exit 127, `command not found` | Não havia `~/.aws` nem variável `AWS_*`. A conta do GitHub também não tinha secret de deploy. |
| 26 nov 2026, 00:20, Brasília | CLI 2.42.4 instalada. `aws sts get-caller-identity` saiu 253, `NoCredentials` | A mensagem pede `aws login`. Não há chave para publicar a stack. A função não foi criada. |
| 26 nov 2026, 00:21, Brasília | SAM CLI 1.174.1. `sam validate` aceitou o template. O zip ficou em 34 MB. `sam deploy` saiu 1, `Unable to locate credentials` | O artefato está pronto. A conta AWS é o que falta. |
| 26 nov 2026, 08:07, Brasília | `aws login` recusou: `Profile 'default' is already configured with Access Key credentials` | O perfil default já tem access key. Não apague a credencial do perfil default. O passo é `aws sts get-caller-identity`, não um login novo. |
| 26 nov 2026, 08:19, Brasília | `./mvnw -q package` falhou em `generate-code` com `NoSuchElementException` | O Maven não estava no Java 21. Confira `./mvnw -version` e o `JAVA_HOME`. |
| 26 nov 2026, 08:25, Brasília | `./mvnw -version` mostrou Java 17.0.14, runtime `jbr-17` | É o JDK do IntelliJ. Instale o Temurin 21 e exporte o `JAVA_HOME` para ele. |
| 26 nov 2026, 08:34, Brasília | `sam` morreu com `No module named 'pygments'` | O CLI está no Python 3.12 de `/usr/local` sem essa biblioteca. `python3.12 -m pip install pygments` e depois `sam --version`. |

Não cole a access key no chat. A skill que o próximo agente segue é `.cursor/skills/curfew-deploy/SKILL.md`.
