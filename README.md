# Curfew

Plataforma que lista e fecha os acessos que um agente de IA usou e deixou valendo depois que a tarefa acabou.

Token de GitHub, chave de AWS, webhook, permissão de admin, túnel da sessão e senha escrita no log do agente. O trabalho termina. Esses acessos continuam funcionando. A Curfew mostra a fila e fecha o que não tem dono.

## Um exemplo

1. Alguém pede ao agente: abre um pull request e confere o log de produção.
2. O agente recebe um token do GitHub e uma chave da AWS. Cria um webhook para ser avisado se o PR mudar.
3. O pull request entra. O time segue o dia. Ninguém recolhe o token, a chave e o webhook.
4. A Curfew lê GitHub, AWS, Slack e o registro da sessão. Mostra o que ainda está aberto.
5. Revoga o token, expira a chave, desliga o webhook. Se algo puder ser de produção, tranca e pede um humano para assumir.

## A tela

Uma fila. Cada linha é um acesso esquecido.

| O que sobrou | Ação |
| --- | --- |
| Token | Revogar |
| Chave de nuvem | Expirar |
| Webhook ou túnel da sessão | Desligar |
| Permissão larga demais | Reduzir |
| Senha no log do agente | Apagar do texto |
| Algo que pode ser de produção | Trancar e pedir um humano. Se a pessoa assumir, ela vira dono |

## Duas formas de usar

1. **Limpa o que ficou.** Conecta em GitHub, AWS e Slack, lê o registro do agente e fecha a fila toda noite. Esta já é a plataforma.
2. **Entrega a chave.** O agente pede acesso para uma tarefa e recebe um token que vale só enquanto a tarefa existe. Quando acaba, o acesso para de funcionar. É a mesma plataforma, entrando antes da tarefa.

Quem olha a fila é o time de plataforma. Quem assina é a pessoa de segurança.

## Preço

Números de novembro de 2026, premissas, não clientes reais.

| O que compram | Preço |
| --- | --- |
| A lista, uma vez por mês | US$ 18 mil por ano |
| Fechamento toda noite | US$ 48 por desenvolvedor com agente, por mês, mais US$ 12 mil de plataforma |
| Chave com prazo | US$ 96 por desenvolvedor com agente, por mês, mais US$ 24 mil de plataforma |

Um fechamento com 30 desenvolvedores fica perto de US$ 30 mil por ano. Uma chave com prazo para 120 desenvolvedores fica perto de US$ 160 mil por ano.

| Cenário | Receita anual no ano 3 | Receita anual no ano 5 |
| --- | --- | --- |
| Conservador, só a lista e um pouco de fechamento | US$ 1,6 mi | US$ 4 mi |
| Base, fechamento vira rotina e a chave com prazo entra no ano 3 | US$ 8,5 mi | US$ 32 mi |
| Teto, a empresa só libera agente pela chave com prazo | US$ 16 mi | US$ 72 mi |

## Ordem de construção

1. Primeiros 4 meses: a lista, lendo GitHub e AWS. Cinco empresas veem a fila.
2. Até o mês 9: fechamento toda noite. Produção é trancada e perguntada.
3. Até o mês 12: chave com prazo em um cliente. O acesso daquela tarefa acaba em menos de 24 horas.

A métrica é o tempo que um acesso esquecido continua valendo. Se esse tempo cai de semanas para menos de um dia, a plataforma está provada.

## Como rodar

Este repositório é a explicação da plataforma e o plano. Não há serviço para subir.
