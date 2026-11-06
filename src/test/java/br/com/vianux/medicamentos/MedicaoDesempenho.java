package br.com.vianux.medicamentos;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/** Medição exploratória reprodutível. Não substitui JMH ou teste de carga da API. */
public final class MedicaoDesempenho {
    private static volatile long consumo;

    public static void main(String[] args) throws Exception {
        Path pasta = Path.of("evidencias");
        Files.createDirectories(pasta);
        var linhas = new ArrayList<String>();
        linhas.add("comprimento;celulas;bytes_payload_int;mediana_us_por_comparacao;p95_medias_us_por_comparacao;rodadas;repeticoes");
        var algoritmo = new WagnerFischer();
        int rodadas = 21, repeticoes = 100, aquecimento = 500;
        for (int tamanho : new int[]{8, 16, 32, 64, 128, 200}) {
            var sorteio = new Random(20261026L + tamanho);
            String[] a = new String[24], b = new String[24];
            for (int i = 0; i < a.length; i++) {
                a[i] = texto(sorteio, tamanho);
                b[i] = texto(sorteio, tamanho);
            }
            long soma = 0;
            for (int i = 0; i < aquecimento; i++) soma += algoritmo.distancia(a[i % 24], b[i % 24]);
            consumo = soma;
            double[] micros = new double[rodadas];
            for (int r = 0; r < rodadas; r++) {
                soma = 0;
                long inicio = System.nanoTime();
                for (int i = 0; i < repeticoes; i++) soma += algoritmo.distancia(a[(i + r) % 24], b[(i + r) % 24]);
                long duracao = System.nanoTime() - inicio;
                consumo = soma;
                micros[r] = duracao / (1000.0 * repeticoes);
            }
            Arrays.sort(micros);
            long celulas = (long) (tamanho + 1) * (tamanho + 1);
            String linha = String.format(Locale.ROOT, "%d;%d;%d;%.3f;%.3f;%d;%d",
                    tamanho, celulas, 4 * celulas, micros[rodadas / 2],
                    micros[(int) Math.ceil(.95 * rodadas) - 1], rodadas, repeticoes);
            linhas.add(linha);
            System.out.println(linha);
        }
        Files.write(pasta.resolve("desempenho.csv"), linhas, StandardCharsets.UTF_8);
        String dataExecucao = java.time.LocalDate.now(java.time.ZoneId.of("America/Fortaleza"))
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu"));
        String ambiente = "Data de execução: " + dataExecucao + "\nJava: " + System.getProperty("java.runtime.version")
                + "\nVM: " + System.getProperty("java.vm.name") + "\nSO: " + System.getProperty("os.name")
                + "\nArquitetura: " + System.getProperty("os.arch")
                + "\nProcessadores disponíveis: " + Runtime.getRuntime().availableProcessors()
                + "\nEntradas sintéticas com alfabeto {a,b,c,d}, semente 20261026 + comprimento."
                + "\n24 pares por comprimento preparados fora da região medida."
                + "\n500 chamadas de aquecimento e 21 rodadas de 100 comparações por comprimento."
                + "\nMediana e p95 são calculados sobre as médias por rodada, não sobre latências individuais."
                + "\nSystem.nanoTime inclui o núcleo, vetores Unicode e alocação da matriz, sem compilação, CSV, rede ou banco."
                + "\nMedição exploratória em um único processo e ambiente virtualizado. JIT e GC podem afetar os resultados."
                + "\nPayload int = 4*(m+1)*(n+1). Não inclui cabeçalhos dos arrays, referências ou vetores auxiliares.\n";
        Files.writeString(pasta.resolve("ambiente-medicao.txt"), ambiente, StandardCharsets.UTF_8);
        System.out.println("RESULTADO: medição exploratória concluída; consumo=" + consumo);
    }

    private static String texto(Random sorteio, int tamanho) {
        StringBuilder s = new StringBuilder(tamanho);
        for (int i = 0; i < tamanho; i++) s.append((char) ('a' + sorteio.nextInt(4)));
        return s.toString();
    }
}
