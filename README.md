# Curfew

Fila dos acessos que um agente de IA deixou valendo. Este corte só mostra dados fictícios. Nada é lido de uma conta real e nada é fechado.

O plano de produto está em [docs/plano.md](docs/plano.md).

## Instalação

É preciso Java 21. O Maven vem no wrapper do repositório. Não é preciso instalar Maven à parte.

```bash
java -version
./mvnw -version
```

AWS CLI e SAM só entram num deploy futuro. Não rode `sam deploy` neste corte.

## Build e execução

```bash
./mvnw test
./mvnw package
./mvnw quarkus:dev
```

O dev sobe em [http://127.0.0.1:8473/](http://127.0.0.1:8473/).

O pacote Lambda, quando o build termina, fica em `target/function.zip`. O script `./build-lambda.sh` faz esse pacote e falha se o zip não aparecer. Gerar o zip não publica nada.

Telas:

- `/` fila
- `/acessos/gh-token-18` detalhe
- `/varredura` contagem de itens e tokens evitados
- `/conexoes` GitHub e AWS ainda sem leitura
- `/?estado=vazio`, `/?estado=carregando`, `/?estado=erro`

## Erros comuns

**Java ausente ou antigo.** `java -version` precisa mostrar 21. Sem isso, o `mvnw` para na compilação.

**Porta 8473 ocupada.** Outro processo já escuta essa porta. Encerre esse processo ou mude `quarkus.http.port` em `src/main/resources/application.properties`.

**`./mvnw` sem permissão.** Rode `chmod +x mvnw` e tente de novo.

**SAM ausente.** Só importa se alguém tentar `sam build` ou `sam deploy`. O dev e os testes não usam SAM. Deploy fica para depois da autenticação.

**Wrapper sem rede na primeira vez.** O `mvnw` baixa o Maven na primeira execução. Sem rede, essa etapa falha. As seguintes usam o que já foi baixado.

## Troubleshooting

| Sintoma | O que fazer |
| --- | --- |
| `release version 21 not supported` | Instale o JDK 21 e confira `JAVA_HOME`. |
| `Address already in use` | A porta 8473 está presa. Veja o erro de porta acima. |
| `target/function.zip não foi gerado` | Rode `./mvnw package` e leia o erro do Quarkus antes do script. |
| Página em branco | Confira se o dev ainda está de pé e se a URL é a porta 8473. |
| Teste de página falha procurando texto | O mock mudou. Ajuste `MockAccessSource` e o teste juntos. |

## Desenvolvimento

A fila fictícia está em `src/main/java/curfew/scan/MockAccessSource.java`. As regras estão em `ScanRules`. A engine limita quantas classificações correm ao mesmo tempo. `TokenLedger` conta tokens que um modelo teria gasto. Neste corte as chamadas de modelo são zero.

Em dev e teste a fila fica na memória. O perfil de produção usa DynamoDB, tabela `curfew-access`, descrita em `template.yaml`. O contrato é o mesmo. Não há campo para o valor de um segredo.

As telas são templates Qute em `src/main/resources/templates/Pages/` e o visual está em `src/main/resources/META-INF/resources/app.css`. O `app.js` só filtra a tabela no navegador. Ele não chama a rede.

Para um agente, a skill em `.cursor/skills/curfew/SKILL.md` e o `AGENTS.md` repetem o limite deste corte.

## O que não entra aqui

Conta real, conector, credencial de cliente, deploy e modelo. As chaves `dev-not-a-secret` no `application.properties` são fictícias para o SDK subir em dev e teste. Não as troque por uma chave real. Não escreva token, chave ou senha em log, URL, teste ou template.
