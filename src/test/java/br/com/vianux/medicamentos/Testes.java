package br.com.vianux.medicamentos;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Verificações executáveis sem dependências externas, com falha via exit != 0. */
public final class Testes {
    private static int aprovados;
    private static final WagnerFischer ALGORITMO = new WagnerFischer();

    public static void main(String[] args) throws Exception {
        verificar("textos iguais", () -> igual(0, ALGORITMO.distancia("dipirona", "dipirona")));
        verificar("inserção", () -> igual(1, ALGORITMO.distancia("dipirna", "dipirona")));
        verificar("exclusão", () -> igual(1, ALGORITMO.distancia("dipirona", "dipirna")));
        verificar("substituição", () -> igual(1, ALGORITMO.distancia("sol", "sal")));
        verificar("exemplo clássico", () -> igual(3, ALGORITMO.distancia("kitten", "sitting")));
        verificar("dois vazios", () -> igual(0, ALGORITMO.distancia("", "")));
        verificar("origem vazia", () -> igual(3, ALGORITMO.distancia("", "abc")));
        verificar("destino vazio", () -> igual(3, ALGORITMO.distancia("abc", "")));
        verificar("transposição não é operação primitiva", () -> igual(2, ALGORITMO.distancia("ab", "ba")));
        verificar("algoritmo literal distingue caixa", () -> igual(1, ALGORITMO.distancia("A", "a")));
        verificar("caractere Unicode suplementar", () -> igual(1, ALGORITMO.distancia("a😀", "a")));
        verificar("normalização fica fora do algoritmo", () ->
                igual("acido folico", NormalizadorTexto.normalizar("  ÁCIDO   FÓLICO  ")));
        verificar("limite de entrada", () -> espera(IllegalArgumentException.class,
                () -> ALGORITMO.distancia("a".repeat(201), "a")));
        verificar("null não vira texto vazio", () -> espera(NullPointerException.class,
                () -> ALGORITMO.distancia(null, "a")));
        verificar("propriedades em pequeno domínio", Testes::propriedades);
        verificar("busca prioriza exatas normalizadas", () -> {
            var resultados = busca().buscar(" DIPIRONA ", 2, null, null);
            igual(List.of("A", "B"), resultados.stream().map(s -> s.medicamento().id()).toList());
            verdadeiro(resultados.stream().allMatch(s -> s.distancia() == 0));
        });
        verificar("busca sugere erro de uma letra", () -> {
            var resultados = busca().buscar("dipirna", 1, "500 mg", "comprimido");
            igual(1, resultados.size());
            igual("A", resultados.get(0).medicamento().id());
            igual(1, resultados.get(0).distancia());
        });
        verificar("não aproxima concentração", () ->
                verdadeiro(busca().buscar("dipirna", 2, "50 mg", null).isEmpty()));
        verificar("não aproxima forma", () ->
                verdadeiro(busca().buscar("dipirna", 2, null, "cápsula").isEmpty()));
        verificar("consulta sem candidato", () ->
                verdadeiro(busca().buscar("zzzzzzzzzzzzzz", 1, null, null).isEmpty()));
        verificar("busca vazia rejeitada", () -> espera(IllegalArgumentException.class,
                () -> busca().buscar("  ", 2, null, null)));
        verificar("limite inválido rejeitado", () -> espera(IllegalArgumentException.class,
                () -> busca().buscar("dipirona", 4, null, null)));
        verificar("IDs duplicados rejeitados", () -> espera(IllegalArgumentException.class,
                () -> new BuscaMedicamentos(List.of(
                        new Medicamento("A", "nome a", "1 mg", "forma"),
                        new Medicamento("A", "nome b", "2 mg", "forma")))));
        verificar("carga CSV e busca integradas", () -> {
            try {
                var catalogo = CatalogoCsv.ler(Path.of("dados/catalogo-demonstracao.csv"));
                igual(10, catalogo.size());
                var resultados = new BuscaMedicamentos(catalogo).buscar("acido folico", 0, null, null);
                igual("DEMO-10", resultados.get(0).medicamento().id());
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        });
        verificar("CSV malformado rejeitado", () -> {
            Path arquivo = null;
            try {
                arquivo = Files.createTempFile("catalogo-teste", ".csv");
                Files.writeString(arquivo, "id;nome;concentracao;forma\nA;nome;5 mg\n", StandardCharsets.UTF_8);
                Path entrada = arquivo;
                espera(IllegalArgumentException.class, () -> {
                    try { CatalogoCsv.ler(entrada); }
                    catch (java.io.IOException e) { throw new AssertionError(e); }
                });
            } catch (Exception e) {
                throw new AssertionError(e);
            } finally {
                if (arquivo != null) {
                    try { Files.deleteIfExists(arquivo); }
                    catch (java.io.IOException e) { throw new AssertionError(e); }
                }
            }
        });
        verificar("empates têm ordem determinística", () -> {
            var resultados = new BuscaMedicamentos(List.of(
                    new Medicamento("Z", "sol", "1 mg", "forma"),
                    new Medicamento("A", "sal", "1 mg", "forma")))
                    .buscar("sil", 1, null, null);
            igual(List.of("A", "Z"), resultados.stream().map(s -> s.medicamento().id()).toList());
        });
        System.out.println("RESULTADO: " + aprovados + " verificações aprovadas.");
    }

    private static BuscaMedicamentos busca() {
        return new BuscaMedicamentos(List.of(
                new Medicamento("A", "dipirona", "500 mg", "comprimido"),
                new Medicamento("B", "dipirona", "500 mg/mL", "solução oral"),
                new Medicamento("C", "dipironal", "500 mg", "comprimido")));
    }

    private static void propriedades() {
        List<String> palavras = List.of("", "a", "b", "ab", "ba", "aaa", "😀");
        for (String a : palavras) {
            for (String b : palavras) {
                int d = ALGORITMO.distancia(a, b);
                int m = a.codePointCount(0, a.length());
                int n = b.codePointCount(0, b.length());
                igual(d, ALGORITMO.distancia(b, a));
                verdadeiro(d >= Math.abs(m - n) && d <= Math.max(m, n));
                for (String c : palavras) {
                    verdadeiro(d <= ALGORITMO.distancia(a, c) + ALGORITMO.distancia(c, b));
                }
            }
        }
    }

    private static void verificar(String descricao, Runnable teste) {
        teste.run();
        aprovados++;
        System.out.println("OK: " + descricao);
    }

    private static void igual(Object esperado, Object obtido) {
        if (!esperado.equals(obtido)) {
            throw new AssertionError("Esperado: " + esperado + "; obtido: " + obtido);
        }
    }

    private static void verdadeiro(boolean condicao) {
        if (!condicao) { throw new AssertionError("Condição esperada não atendida."); }
    }

    private static void espera(Class<? extends Throwable> tipo, Runnable acao) {
        try { acao.run(); }
        catch (Throwable erro) {
            if (tipo.isInstance(erro)) { return; }
            throw new AssertionError("Tipo inesperado de erro.", erro);
        }
        throw new AssertionError("Era esperado: " + tipo.getSimpleName());
    }
}
