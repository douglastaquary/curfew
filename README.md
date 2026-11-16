# Curfew

A porta fecha quando o agente sai.

A Curfew é uma empresa de manutenção para o acesso que agentes de IA deixam aberto. Cada execução nasce com acesso temporário. O que uma pessoa não promove, com o próprio nome e um motivo, é revogado, expirado, reduzido, redigido ou colocado em quarentena.

Pentest entrega um PDF e vai embora. Agente trabalha de novo amanhã. O contrato que ganha dinheiro é a ronda da noite, não o projeto.

Os números abaixo são premissas de novembro de 2026, não histórico de cliente. O modelo interativo, com cenários e simulador de contrato, está no canvas da conversa em que este plano foi escrito.

## A tese

Cada agente com acesso é uma porta nova: token, chave, app OAuth, webhook, túnel, role, servidor MCP, transcrição com segredo dentro. A porta era da tarefa. A tarefa acabou. A porta ficou.

Inventário de identidade não humana responde “essa conta existe?”. Postura de nuvem responde “esse recurso está exposto?”. Segurança de prompt responde “o que o agente fez enquanto rodava?”. A Curfew responde outra pergunta: esta porta sobreviveu à tarefa que a abriu?

A regra do produto: tudo o que o agente abre nasce temporário. Vira permanente só quando alguém promove.

## Um produto, três tempos

Três SKUs. O primeiro ano fala com GitHub, AWS, Slack e o rastro do agente.

| Tempo | O que o cliente compra | Preço |
| --- | --- | --- |
| Varredura | Leitura e relatório das portas ainda abertas, com dono, idade e ação | Grátis em uma org. US$ 18 mil por ano o relatório completo |
| Fechamento | Toda noite: revoga, expira, reduz escopo, redige log, quarentena o que não tem dono | US$ 48 por desenvolvedor com agente, por mês, mais US$ 12 mil de plataforma |
| Lease | O agente não recebe chave fixa. Recebe acesso com prazo, no escopo da tarefa | US$ 96 por desenvolvedor com agente, por mês, mais US$ 24 mil de plataforma |

Fechamento não apaga produção. Apagar fica restrito ao que nasceu dentro da lease e ninguém promoveu.

A âncora de preço é uma pessoa de segurança, na casa de US$ 180 mil por ano carregada. Ela não enxerga GitHub, AWS e a transcrição no mesmo dia.

Um fechamento com 30 desenvolvedores fica perto de US$ 30 mil por ano. Serve para provar o relatório. Um lease com 120 desenvolvedores fica perto de US$ 160 mil por ano. É esse o contrato que muda o ano.

## A conta

| Cenário | ARR ano 3 | ARR ano 5 | O que precisa ser verdade |
| --- | --- | --- | --- |
| Conservador | US$ 1,6 mi | US$ 4 mi | Fica no relatório. Empresa lucrativa e pequena |
| Base | US$ 8,5 mi | US$ 32 mi | Fechamento vira manutenção e o lease entra no ano 3 |
| Teto | US$ 16 mi | US$ 72 mi | Liberar agente passa a exigir o lease |

No cenário base, o ano 3 tem 70 clientes e ACV médio de US$ 121 mil. A composição desse ano é cerca de US$ 1,3 mi em varredura, US$ 3,8 mi em fechamento e US$ 3,4 mi em lease. Margem bruta alvo: 82%.

Dez milhões de ARR é empresa. O múltiplo de uma venda muda por estar no caminho da credencial. Relatório sozinho é funcionalidade, e funcionalidade se vende barato.

ARR não é o que cai na conta do fundador. Sem fundo, o caminho conservador fica com o lucro. Com pré-seed e seed, a fatia diminui e o ativo cresce se o lease retiver e expandir com o número de desenvolvedores.

## Os primeiros doze meses

1. Dias 1–45. Cinco conversas com CISO ou head de plataforma. A prova é uma frase que a pessoa repete sem slide.
2. Dias 45–120. Conector só de leitura em GitHub e AWS. Cinco auditorias, de graça ou a US$ 15–25 mil.
3. Dias 120–180. Três contratos de varredura. Slack e o rastro do agente entram no mesmo relatório. Alguém paga o segundo mês.
4. Dias 180–270. Fechamento em quarentena. Zero incidente de produção causado pela Curfew.
5. Dias 270–365. Lease de GitHub e uma sessão AWS num design partner. Nesse cliente, a mediana de vida da porta cai para menos de 24 horas.

A métrica que importa é essa mediana, medida depois que a execução acaba. A segunda é a parcela de portas com dono humano: ou fecha, ou alguém promove.

A partir do mês 9, serviço fica em no máximo 30% da receita. Auditoria paga o engenheiro. Não pode virar a empresa.

## Quem paga, e em que ordem

Quem assina é o CISO. Quem sente a dor é a pessoa de plataforma. O gatilho é o questionário de segurança do cliente: “como vocês governam o acesso dos agentes?”. Quando a transcrição guarda dado de cliente, o DPO entra na mesma reunião. LGPD é argumento de compra, não apêndice.

Os cinco primeiros logos podem ser brasileiros: fintech regulada, banco digital, scale-up que já responde questionário de enterprise, software house que entrega com agente, time com contratado rodando agente na própria máquina.

O contrato que muda o ano está no mid-market americano, 200 a 800 desenvolvedores. A empresa que assina esse contrato costuma ser uma Delaware C-Corp, com a operação no Brasil. Isso é desenho comum de mercado, não conselho jurídico.

Fundo entra tarde. Até cerca de US$ 300 mil de ARR, a auditoria sustenta o time. Pré-seed quando três logos pagam fechamento e um lease roda em produção. Seed quando o lease tem repetição. O cheque é para distribuição, não para descobrir o que vender.

Time até lá: quem funda, na narrativa e nos dez primeiros contratos. Um engenheiro que já sofreu IAM de GitHub e AWS. Um engenheiro no relatório e nos conectores. Vendedor quando duas vendas acontecerem sem o fundador na sala o tempo todo.

## O que já está ocupado

Astrix e Oasis inventariam identidade não humana. GitGuardian acha segredo em código. Wiz olha a nuvem. Zenity, Noma e Lakera olham o agente enquanto ele age. A fronteira que eles não usam é a execução. Essa janela é o primeiro ano.

O ativo que compõe, depois de muitas empresas, é a lista do que cada runtime costuma deixar aberto. A política de fechamento fica específica. O acervo não se vende cru. Vende-se a política que ficou mais esperta.

## O que fica de fora no começo

Firewall de prompt, seguro de resíduo, dezena de conectores, apagar recurso de produção, marketplace. Um índice público e anônimo, todo trimestre, serve para a categoria ganhar nome: mediana de vida da porta, tokens sem prazo, webhooks apontando para túnel. É geração de reunião, não um segundo produto. Corretor de MCP, sozinho, é pequeno. Dentro do lease, é o lugar certo, porque a lista da execução fica completa e a autorização cai com a run.

## O que mata a empresa

- Fechar produção. Um deploy quebrado expulsa o campeão interno. Resposta: quarentena e promoção.
- Virar consultoria. Teto de 30% da receita em serviço a partir do mês 9.
- Virar feature de um player maior. O lease no caminho da credencial é o que um módulo não faz no primeiro ano deles.
- Quarenta conectores. O quarto conector espera um cliente pagar por ele.
- Narrativa de pânico contra a plataforma de agentes. A Curfew é o motivo pelo qual a empresa libera o agente.

## Como rodar

Este repositório é o plano. Não há serviço para subir.
