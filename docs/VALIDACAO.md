# Registro da validação executada em 26 de outubro de 2026

Ambiente: Linux amd64, OpenJDK 17.0.20, compilação UTF-8 com --release 17.

## Verificações funcionais

`bash executar.sh testar`: 26 verificações aprovadas, com término do processo em código zero. Cobrem valores conhecidos, inserção, exclusão, substituição, transposição como duas edições, textos vazios, null, limite de entrada, Unicode, normalização, propriedades métricas em domínio pequeno, exatas, filtros de apresentação, dados CSV e empates. A saída está em `evidencias/testes-saida.txt`.

## Algoritmo de validação independente

`bash executar.sh validar`: BFS sobre um grafo implícito de textos. Não usa a recorrência do núcleo e não chama WagnerFischer. A fila visita camadas de edições e um conjunto impede revisitas. O limite de profundidade é max(m,n), com comprimento intermediário permitido até m+max(m,n). O oráculo fica restrito a textos de até quatro pontos de código.

Foram comparados todos os 961 pares ordenados dos 31 textos sobre {a,b} com comprimentos de 0 a 4, além de seis casos de Unicode, caixa, espaço e substituição. Resultado: 967 comparações concordantes e zero divergências. A contagem não deve ser confundida com o número de verificações funcionais.

Evidências: `evidencias/validacao-independente.csv`, `evidencias/validacao-resumo.json` e `evidencias/validacao-saida.txt`.

## Desempenho

`bash executar.sh medir`: comprimentos 8, 16, 32, 64, 128 e 200. Para cada comprimento, 24 pares sintéticos previamente construídos, 500 chamadas de aquecimento e 21 rodadas de 100 comparações. Mediana e p95 são calculados sobre as médias por rodada. A região medida inclui conversão Unicode e alocação, sem compilação, CSV, rede ou banco.

Os valores observados estão em `evidencias/desempenho.csv`. O registro de 16 foi maior que o de 32 nesta execução. O ensaio não isola as causas; JIT, alocação, GC e compartilhamento do ambiente afetam as medidas. Os dados não foram ajustados para formar uma curva crescente. Detalhes em `evidencias/ambiente-medicao.txt`.

## Limites da evidência

O domínio finito de validação e os testes são evidências, não uma prova exaustiva para todas as entradas. O microensaio não substitui JMH, avaliação com catálogo e pesquisas reais ou teste de carga da API. Não houve integração com banco e fontes oficiais, implantação em produção, depuração gráfica na máquina do aluno ou validação da futura área restrita.
