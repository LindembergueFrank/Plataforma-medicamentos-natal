# Wagner–Fischer na busca de medicamentos

Implementação em Java do algoritmo que calcula a distância de Levenshtein. Este demonstrador faz parte do projeto acadêmico de consulta de medicamentos na rede pública de Natal/RN.

## Contexto da plataforma

A proposta evolui um dashboard em Looker Studio para uma plataforma de consulta de medicamentos na rede pública de Natal/RN. O cidadão poderá pesquisar pelo nome e pela apresentação, consultar quantidades registradas em UBS com fonte e referência e verificar a cobertura informada do Farmácia Popular e os estabelecimentos credenciados. O responsável pelos dados poderá importar, revisar e publicar lotes validados.

A entrega deste repositório é o demonstrador Java de busca. A integração com API, banco, bases públicas e interface está prevista no projeto. A quantidade de UBS corresponde à referência do registro e o credenciamento no Farmácia Popular, por si só, não confirma estoque.

## Como executar

Use um JDK 17 ou superior e Bash (no Windows, pode usar o WSL). O projeto não exige dependências externas.

```bash
bash executar.sh comparar dipirna dipirona
bash executar.sh buscar dipirna 1 "500 mg" comprimido
bash executar.sh testar
bash executar.sh validar
```

O primeiro comando imprime a matriz e a distância **1**. O segundo pesquisa no catálogo demonstrativo e sugere **dipirona, 500 mg, comprimido**, com uma edição.

## Implementação

- [WagnerFischer.java](src/main/java/br/com/vianux/medicamentos/WagnerFischer.java): usa programação dinâmica e uma matriz `int[][]` para obter o menor número de inserções, exclusões e substituições. Para comprimentos `m` e `n`, usa tempo e memória O(mn) para entradas não vazias.
- [BuscaMedicamentos.java](src/main/java/br/com/vianux/medicamentos/BuscaMedicamentos.java): normaliza a pesquisa, aplica os filtros exatos de concentração e forma, prioriza o nome exato e retorna até cinco sugestões aproximadas.
- [Catálogo demonstrativo](dados/catalogo-demonstracao.csv): dez registros para executar os exemplos. Não representa estoque nem substituição terapêutica.

## Validação

O comando `testar` executa **26 verificações funcionais**. O comando `validar` compara o resultado com uma [busca em largura (BFS)](src/test/java/br/com/vianux/medicamentos/OraculoEdicoesBfs.java) que encontra o caminho com menos edições em um grafo de textos. Foram **967 comparações sem divergências**: 961 pares de textos curtos sobre o alfabeto `{a,b}` e seis casos adicionais. A BFS é usada somente na validação.

Veja [como a validação foi feita](docs/VALIDACAO.md). Para medir o núcleo em seu ambiente, execute `bash executar.sh medir`. A medição é exploratória e não mede a latência de uma API.
