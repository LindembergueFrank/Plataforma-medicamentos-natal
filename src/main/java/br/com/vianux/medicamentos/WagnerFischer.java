package br.com.vianux.medicamentos;

import java.util.Objects;

/** Calcula a distância de Levenshtein por programação dinâmica. */
public final class WagnerFischer {
    // Limite da demonstração: impede construir matrizes excessivamente grandes.
    public static final int MAX_CARACTERES = 200;

    public int distancia(String origem, String destino) {
        int[][] matriz = calcularMatriz(origem, destino);
        return matriz[matriz.length - 1][matriz[0].length - 1];
    }

    public int[][] calcularMatriz(String origem, String destino) {
        Objects.requireNonNull(origem, "A origem é obrigatória.");
        Objects.requireNonNull(destino, "O destino é obrigatório.");
        // Code points evitam contar um caractere suplementar como dois chars.
        int[] a = origem.codePoints().toArray();
        int[] b = destino.codePoints().toArray();
        if (a.length > MAX_CARACTERES || b.length > MAX_CARACTERES) {
            throw new IllegalArgumentException("Cada texto deve ter até 200 caracteres Unicode.");
        }
        int[][] matriz = new int[a.length + 1][b.length + 1];

        // Transformar um prefixo em vazio exige excluir todos os seus caracteres.
        for (int i = 0; i <= a.length; i++) {
            matriz[i][0] = i;
        }
        // Transformar vazio em um prefixo exige inserir todos os seus caracteres.
        for (int j = 0; j <= b.length; j++) {
            matriz[0][j] = j;
        }

        for (int i = 1; i <= a.length; i++) {
            for (int j = 1; j <= b.length; j++) {
                int custoSubstituicao = a[i - 1] == b[j - 1] ? 0 : 1;
                int excluir = matriz[i - 1][j] + 1;
                int inserir = matriz[i][j - 1] + 1;
                int substituirOuManter = matriz[i - 1][j - 1] + custoSubstituicao;
                matriz[i][j] = Math.min(Math.min(excluir, inserir), substituirOuManter);
            }
        }
        return matriz;
    }
}
