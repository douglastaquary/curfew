# Clientes

Uma instalação por cliente. Sem login.

Cinco clientes ao mesmo tempo são cinco processos. Cada um tem o seu `CURFEW_GATE`, o seu `CURFEW_GITHUB_TOKEN`, o seu par AWS e a sua tabela. A porta abre a fila daquela instalação. Um login compartilhado mostraria a fila de uma empresa para outra.

A contagem em `/contagem` é desta instalação. Ela guarda hora, quantidades e as linhas já mostradas na varredura. Uma linha com marcador de segredo não entra. Nenhuma chamada de modelo acontece neste corte. O modelo fica para quando essas linhas forem de clientes reais e o teste continuar recusando o segredo.
