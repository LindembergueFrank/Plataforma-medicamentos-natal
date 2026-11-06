package br.com.vianux.medicamentos;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.TreeSet;

/** Oráculo independente para entradas pequenas. Não chama WagnerFischer nem usa sua recorrência. */
public final class OraculoEdicoesBfs {
    private record Estado(String texto, int edicoes) {}

    public int distancia(String origem, String destino) {
        Objects.requireNonNull(origem);
        Objects.requireNonNull(destino);
        int m = origem.codePointCount(0, origem.length());
        int n = destino.codePointCount(0, destino.length());
        if (m > 4 || n > 4) {
            throw new IllegalArgumentException("O oráculo BFS aceita até quatro pontos de código por texto.");
        }
        if (origem.equals(destino)) return 0;
        var simbolos = new TreeSet<Integer>();
        origem.codePoints().forEach(simbolos::add);
        destino.codePoints().forEach(simbolos::add);
        int[] alfabeto = simbolos.stream().mapToInt(Integer::intValue).toArray();
        // Substituir o trecho comum e inserir/excluir a diferença custa no máximo max(m,n).
        int limiteEdicoes = Math.max(m, n);
        // Qualquer caminho com até U edições tem comprimento intermediário de até m+U.
        int limiteComprimento = m + limiteEdicoes;
        var fila = new ArrayDeque<Estado>();
        var visitados = new HashSet<String>();
        fila.addLast(new Estado(origem, 0));
        visitados.add(origem);
        while (!fila.isEmpty()) {
            Estado estado = fila.removeFirst();
            if (estado.edicoes() == limiteEdicoes) continue;
            int[] atual = estado.texto().codePoints().toArray();
            var vizinhos = new HashSet<String>();
            // Arestas de exclusão.
            for (int i = 0; i < atual.length; i++) {
                int[] proximo = new int[atual.length - 1];
                System.arraycopy(atual, 0, proximo, 0, i);
                System.arraycopy(atual, i + 1, proximo, i, atual.length - i - 1);
                vizinhos.add(new String(proximo, 0, proximo.length));
            }
            // Arestas de substituição.
            for (int i = 0; i < atual.length; i++) {
                for (int simbolo : alfabeto) {
                    if (atual[i] == simbolo) continue;
                    int[] proximo = Arrays.copyOf(atual, atual.length);
                    proximo[i] = simbolo;
                    vizinhos.add(new String(proximo, 0, proximo.length));
                }
            }
            // Arestas de inserção, inclusive antes e depois do texto.
            if (atual.length < limiteComprimento) {
                for (int i = 0; i <= atual.length; i++) {
                    for (int simbolo : alfabeto) {
                        int[] proximo = new int[atual.length + 1];
                        System.arraycopy(atual, 0, proximo, 0, i);
                        proximo[i] = simbolo;
                        System.arraycopy(atual, i, proximo, i + 1, atual.length - i);
                        vizinhos.add(new String(proximo, 0, proximo.length));
                    }
                }
            }
            for (String proximo : vizinhos) {
                int custo = estado.edicoes() + 1;
                if (proximo.equals(destino)) return custo;
                if (visitados.add(proximo)) fila.addLast(new Estado(proximo, custo));
            }
        }
        throw new AssertionError("Nenhum caminho encontrado dentro de uma cota superior válida.");
    }
}
