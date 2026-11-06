package br.com.vianux.medicamentos;

import java.util.Objects;

/** Entrada do catálogo didático. Não contém informação de estoque. */
public record Medicamento(String id, String nome, String concentracao, String forma) {
    public Medicamento {
        id = obrigatorio(id, "id");
        nome = obrigatorio(nome, "nome");
        concentracao = obrigatorio(concentracao, "concentração");
        forma = obrigatorio(forma, "forma");
    }

    private static String obrigatorio(String valor, String campo) {
        Objects.requireNonNull(valor, "Campo obrigatório: " + campo);
        if (valor.isBlank()) {
            throw new IllegalArgumentException("Campo vazio: " + campo);
        }
        return valor.strip();
    }
}
