# Aprendizado

Cada linha cita um teste que trava a correção. `LearningLedgerTest` falha se a classe ou o método não existirem. Não escreva aqui uma regra que o teste já cobre.

| Correção | Teste |
| --- | --- |
| A fila mostra o mock e nenhum segredo | `QueuePageTest.queueShowsTheMockAndNoSecret` |
| Vazio, carregando e erro são explícitos | `QueuePageTest.emptyLoadingAndErrorAreExplicit` |
| Detalhe, varredura e conexões renderizam, e id ausente é 404 | `QueuePageTest.detailScanAndConnectionsRender` |
| O log ao vivo não leva segredo e fecha em 720 | `QueuePageTest.scanLogStreamsEachStepWithoutSecrets` |
| A linha do log nomeia o acesso e pula o segredo | `ScanLogTest.lineNamesTheAccessAndSkipsAnySecret` |
| Token sem dono é revogado pela regra | `ScanRulesTest.tokenWithoutOwnerIsRevokedByRule` |
| Escopo largo demais é reduzido | `ScanRulesTest.wideScopeIsReduced` |
| Recurso que pode ser de produção pede um humano | `ScanRulesTest.productionResourceAsksForAHuman` |
| Senha no log sai só do texto | `ScanRulesTest.secretInLogIsRemovedFromTextOnly` |
| A engine classifica cada item uma vez, no máximo quatro ao mesmo tempo | `ScanEngineTest.classifiesEveryItemOnceUnderTheConcurrencyCap` |
| Regra evita um orçamento fixo e não chama modelo | `TokenLedgerTest.rulesAvoidAFixedBudgetAndNeverCallAModel` |
| O registro da decisão não tem campo de segredo | `TokenLedgerTest.recordsHaveNoSecretField` |
| A leitura real descarta URL, material de chave e valor de segredo | `MetadataInventoryTest.dropsUrlKeyMaterialAndSecretValues` |
| O mapa de verificação cobre as quatro telas | `VerifyCurfewTest.featureMapKeepsTheFourScreens` |
| O dry-run da verificação não sobe o app nem imprime segredo | `VerifyCurfewTest.dryRunDoesNotStartTheAppOrPrintASecret` |
| O plano exige prova no ar e recusa deploy ou fechamento | `VerifyCurfewTest.planRequiresLiveProofAndRejectsDeploy` |
| A passagem da noite pede um humano na produção e não fecha nada | `NightlyCloseTest.productionAsksAndNothingCloses` |
| A passagem desarmada tira a fila local e não chama a conta | `NightPageTest.passageRemovesLocalRowsAndDoesNotCallTheAccount` |
| O token do GitHub não entra na URL de exclusão | `GithubCloseTest.tokenStaysOutOfTheUrl` |
| A chave da tarefa acaba antes de 24 horas e não guarda segredo | `LeaseTermTest.rejectsAFullDayAndKeepsNoSecret` |
| A fila mostra a chave de exemplo e 24 horas não entra | `LeasePageTest.exampleLeaseEndsInsideADay` |
