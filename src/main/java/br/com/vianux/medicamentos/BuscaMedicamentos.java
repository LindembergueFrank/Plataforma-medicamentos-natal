package br.com.vianux.medicamentos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Orquestra filtros e sugestões. Não modifica nem substitui medicamentos. */
public final class BuscaMedicamentos {
    public record Sugestao(Medicamento medicamento, int distancia) { }

    private final List<Medicamento> catalogo;
    private final WagnerFischer algoritmo;

    public BuscaMedicamentos(List<Medicamento> catalogo) {
        this.catalogo = List.copyOf(catalogo);
        this.algoritmo = new WagnerFischer();
        Set<String> ids = new HashSet<>();
        for (Medicamento medicamento : this.catalogo) {
            if (!ids.add(medicamento.id())) {
                throw new IllegalArgumentException("ID repetido: " + medicamento.id());
            }
        }
    }

    public List<Sugestao> buscar(String consulta, int maxDistancia,
                                 String concentracao, String forma) {
        Objects.requireNonNull(consulta, "Informe o nome pesquisado.");
        String nome = NormalizadorTexto.normalizar(consulta);
        if (nome.isBlank()) {
            throw new IllegalArgumentException("Informe um nome não vazio.");
        }
        int tamanho = nome.codePointCount(0, nome.length());
        if (tamanho > WagnerFischer.MAX_CARACTERES) {
            throw new IllegalArgumentException("Pesquisa limitada a 200 caracteres Unicode.");
        }
        if (maxDistancia < 0 || maxDistancia > 3) {
            throw new IllegalArgumentException("Nesta demonstração, a distância máxima deve ser de 0 a 3.");
        }

        List<Medicamento> candidatos = catalogo.stream()
                .filter(m -> correspondeFiltro(m.concentracao(), concentracao))
                .filter(m -> correspondeFiltro(m.forma(), forma))
                .toList();

        // Correspondência exata é exata após a normalização declarada acima.
        List<Sugestao> exatas = candidatos.stream()
                .filter(m -> NormalizadorTexto.normalizar(m.nome()).equals(nome))
                .map(m -> new Sugestao(m, 0))
                .sorted(ordenacao())
                .toList();
        if (!exatas.isEmpty()) {
            return exatas;
        }

        List<Sugestao> aproximadas = new ArrayList<>();
        for (Medicamento medicamento : candidatos) {
            String nomeCandidato = NormalizadorTexto.normalizar(medicamento.nome());
            int tamanhoCandidato = nomeCandidato.codePointCount(0, nomeCandidato.length());
            // Diferença de comprimentos já é um limite inferior da distância.
            if (Math.abs(tamanho - tamanhoCandidato) > maxDistancia) {
                continue;
            }
            int distancia = algoritmo.distancia(nome, nomeCandidato);
            if (distancia <= maxDistancia) {
                aproximadas.add(new Sugestao(medicamento, distancia));
            }
        }
        return aproximadas.stream().sorted(ordenacao()).limit(5).toList();
    }

    private boolean correspondeFiltro(String valor, String filtro) {
        return filtro == null || filtro.isBlank()
                || NormalizadorTexto.normalizar(valor).equals(NormalizadorTexto.normalizar(filtro));
    }

    private Comparator<Sugestao> ordenacao() {
        return Comparator.comparingInt(Sugestao::distancia)
                .thenComparing(s -> s.medicamento().nome())
                .thenComparing(s -> s.medicamento().id());
    }
}
