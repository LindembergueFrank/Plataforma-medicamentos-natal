package br.com.vianux.medicamentos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Leitor do formato didático delimitado por ponto e vírgula, sem aspas. */
public final class CatalogoCsv {
    private CatalogoCsv() { }

    public static List<Medicamento> ler(Path arquivo) throws IOException {
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        if (linhas.isEmpty() || !linhas.get(0).equals("id;nome;concentracao;forma")) {
            throw new IllegalArgumentException("Cabeçalho esperado: id;nome;concentracao;forma");
        }
        List<Medicamento> medicamentos = new ArrayList<>();
        for (int i = 1; i < linhas.size(); i++) {
            if (linhas.get(i).isBlank()) {
                continue;
            }
            String[] campos = linhas.get(i).split(";", -1);
            if (campos.length != 4 || linhas.get(i).contains("\"")) {
                throw new IllegalArgumentException("Linha " + (i + 1) + ": use quatro campos, sem aspas.");
            }
            medicamentos.add(new Medicamento(campos[0], campos[1], campos[2], campos[3]));
        }
        return List.copyOf(medicamentos);
    }
}
