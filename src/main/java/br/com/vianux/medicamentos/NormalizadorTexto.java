package br.com.vianux.medicamentos;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/** Política de comparação textual da aplicação, separada do algoritmo. */
public final class NormalizadorTexto {
    private NormalizadorTexto() { }

    public static String normalizar(String texto) {
        Objects.requireNonNull(texto, "O texto é obrigatório.");
        String minusculo = texto.strip().toLowerCase(Locale.ROOT);
        return Normalizer.normalize(minusculo, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[\\p{Z}\\s]+", " ");
    }
}
