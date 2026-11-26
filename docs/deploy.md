# Deploy

Sobe uma função Java 21, uma HTTP API e a tabela DynamoDB `curfew-access`. Sem VPC. A tabela fica com criptografia e não é apagada com a stack. O segredo da porta entra na hora do deploy, com `NoEcho`. Token de GitHub, access key de leitura, `CURFEW_CLOSE` e `CURFEW_ISSUE` não estão no template.

Custo neste tamanho, us-east-1, uso ocasional: HTTP API a US$ 1,00 por milhão de requisições, Lambda na maior parte do free tier, DynamoDB sob demanda em centavos. Abaixo de US$ 5 no mês. Não é layout multi-AZ.

## Como publicar

Na máquina que já tem identidade AWS, na raiz do repositório:

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

A publicação para aqui até existir uma identidade AWS nesta máquina. O passo seguinte, na conta que vai receber a Curfew, é `aws login` ou um perfil já configurado. Não cole a access key no chat. Com a identidade pronta, rode o comando de publicar acima.
