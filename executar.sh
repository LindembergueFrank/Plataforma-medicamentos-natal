#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"
mkdir -p build/classes
find src/main/java src/test/java -name '*.java' -print > build/fontes.txt
java -m jdk.compiler/com.sun.tools.javac.Main --release 17 -encoding UTF-8 -d build/classes @build/fontes.txt
if [[ "${1:-}" == "testar" ]]; then
    java -cp build/classes br.com.vianux.medicamentos.Testes
elif [[ "${1:-}" == "validar" ]]; then
    java -cp build/classes br.com.vianux.medicamentos.ValidacaoIndependente
elif [[ "${1:-}" == "medir" ]]; then
    java -cp build/classes br.com.vianux.medicamentos.MedicaoDesempenho
elif [[ $# -eq 0 ]]; then
    java -cp build/classes br.com.vianux.medicamentos.Main comparar dipirna dipirona
else
    java -cp build/classes br.com.vianux.medicamentos.Main "$@"
fi
