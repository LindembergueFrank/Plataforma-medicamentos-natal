package br.com.vianux.medicamentos;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Compara os resultados de dois algoritmos com estratégias distintas. */
public final class ValidacaoIndependente {
    public static void main(String[] args) throws Exception {
        Path pasta = Path.of("evidencias");
        Files.createDirectories(pasta);
        var dominio = new ArrayList<String>();
        gerar("", 4, dominio);
        var linhas = new ArrayList<String>();
        linhas.add("origem;destino;wagner_fischer;bfs;igual");
        int pares = 0;
        long inicio = System.nanoTime();
        for (String a : dominio) {
            for (String b : dominio) {
                comparar(a, b, linhas);
                pares++;
            }
        }
        List<String[]> adicionais = List.of(
                new String[]{"", "😀"}, new String[]{"a😀", "a"},
                new String[]{"é", "e\u0301"}, new String[]{"A", "a"},
                new String[]{"a ", "a"}, new String[]{"sol", "sal"});
        for (String[] par : adicionais) comparar(par[0], par[1], linhas);
        double decorridoMs = (System.nanoTime() - inicio) / 1_000_000.0;
        Files.write(pasta.resolve("validacao-independente.csv"), linhas, StandardCharsets.UTF_8);
        String resumo = String.format(java.util.Locale.ROOT,
                "{\"dominio\":31,\"pares_exaustivos\":%d,\"casos_adicionais\":%d,\"comparacoes\":%d,\"divergencias\":0,\"tempo_ms\":%.3f}%n",
                pares, adicionais.size(), pares + adicionais.size(), decorridoMs);
        Files.writeString(pasta.resolve("validacao-resumo.json"), resumo, StandardCharsets.UTF_8);
        System.out.println("Domínio: " + dominio.size() + " textos sobre {a,b}, de comprimento 0 a 4.");
        System.out.println("Pares exaustivos: " + pares + "; adicionais: " + adicionais.size());
        System.out.println("RESULTADO: " + (pares + adicionais.size()) + " comparações concordantes; zero divergências.");
    }

    private static void gerar(String prefixo, int restantes, List<String> dominio) {
        dominio.add(prefixo);
        if (restantes > 0) {
            gerar(prefixo + "a", restantes - 1, dominio);
            gerar(prefixo + "b", restantes - 1, dominio);
        }
    }

    private static void comparar(String a, String b, List<String> linhas) {
        int dinamico = new WagnerFischer().distancia(a, b);
        int bfs = new OraculoEdicoesBfs().distancia(a, b);
        if (dinamico != bfs) throw new AssertionError("Divergência para [" + a + "] e [" + b + "]");
        linhas.add(exibir(a) + ";" + exibir(b) + ";" + dinamico + ";" + bfs + ";true");
    }

    private static String exibir(String s) { return s.isEmpty() ? "<vazio>" : s; }
}
