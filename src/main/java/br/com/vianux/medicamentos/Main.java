package br.com.vianux.medicamentos;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class Main {
    public static void main(String[] args) {
        try {
            executar(args);
        } catch (IllegalArgumentException | IOException erro) {
            System.err.println("Erro: " + erro.getMessage());
            System.exit(2);
        }
    }

    private static void executar(String[] args) throws IOException {
        if (args.length == 3 && args[0].equals("comparar")) {
            comparar(args[1], args[2]);
        } else if (args.length >= 2 && args.length <= 5 && args[0].equals("buscar")) {
            int limite = args.length >= 3 ? Integer.parseInt(args[2]) : 2;
            String concentracao = args.length >= 4 ? args[3] : null;
            String forma = args.length >= 5 ? args[4] : null;
            buscar(args[1], limite, concentracao, forma);
        } else {
            throw new IllegalArgumentException("Use: comparar texto1 texto2 | buscar nome [limite] [concentração] [forma]");
        }
    }

    private static void comparar(String origem, String destino) {
        int[][] matriz = new WagnerFischer().calcularMatriz(origem, destino);
        System.out.println("Comparação literal: distingue maiúsculas, acentos e espaços.");
        System.out.println("Origem: " + origem + " | Destino: " + destino);
        int[] a = origem.codePoints().toArray();
        int[] b = destino.codePoints().toArray();
        System.out.printf("%5s%5s", "", "∅");
        for (int cp : b) {
            System.out.printf("%5s", new String(Character.toChars(cp)));
        }
        System.out.println();
        for (int i = 0; i < matriz.length; i++) {
            String rotulo = i == 0 ? "∅" : new String(Character.toChars(a[i - 1]));
            System.out.printf("%5s", rotulo);
            for (int valor : matriz[i]) {
                System.out.printf("%5d", valor);
            }
            System.out.println();
        }
        System.out.println("Distância: " + matriz[a.length][b.length]);
    }

    private static void buscar(String nome, int limite, String concentracao, String forma) throws IOException {
        List<Medicamento> catalogo = CatalogoCsv.ler(Path.of("dados/catalogo-demonstracao.csv"));
        List<BuscaMedicamentos.Sugestao> sugestoes =
                new BuscaMedicamentos(catalogo).buscar(nome, limite, concentracao, forma);
        System.out.println("CATÁLOGO DIDÁTICO: não informa estoque nem disponibilidade real.");
        System.out.println("Consulta normalizada: " + NormalizadorTexto.normalizar(nome));
        if (sugestoes.isEmpty()) {
            System.out.println("Nenhuma sugestão dentro dos critérios informados.");
            return;
        }
        for (BuscaMedicamentos.Sugestao sugestao : sugestoes) {
            Medicamento m = sugestao.medicamento();
            System.out.printf("%s | %s | %s | %s | distância=%d%n",
                    m.id(), m.nome(), m.concentracao(), m.forma(), sugestao.distancia());
        }
        System.out.println("A seleção do nome e da apresentação deve ser confirmada pelo usuário.");
    }
}
