#!/bin/bash

# Script para encontrar linhas longas em arquivos Java
# Uso: ./find-long-lines.sh [limite]

LIMIT=${1:-120}
COLOR_RED='\033[0;31m'
COLOR_GREEN='\033[0;32m'
COLOR_YELLOW='\033[1;33m'
COLOR_BLUE='\033[0;34m'
COLOR_RESET='\033[0m'

echo -e "${COLOR_BLUE}🔍 Procurando linhas com mais de ${LIMIT} caracteres...${COLOR_RESET}"
echo ""

# Encontra e formata as linhas longas
FOUND=0
find src/main/java -name "*.java" | while read file; do
    # Processa cada arquivo
    awk -v file="$file" -v limit="$LIMIT" '
        length > limit {
            # Remove indentação para exibição
            line = $0
            gsub(/^[ \t]+/, "", line)

            # Trunca linha se muito longa
            if (length(line) > 100) {
                line = substr(line, 1, 100) "..."
            }

            printf "%-70s %sLinha %4d%s (%s%3d chars%s): %s\n",
                file, "\033[1;33m", NR, "\033[0m",
                "\033[0;31m", length, "\033[0m", line
            found = 1
        }
        END {
            if (found) exit 1
        }
    ' "$file"

    if [ $? -eq 1 ]; then
        FOUND=$((FOUND + 1))
    fi
done | sort -t: -k1,1 -k2n,2

echo ""

# Resumo
TOTAL_FILES=$(find src/main/java -name "*.java" | wc -l)
TOTAL_LONG_LINES=$(find src/main/java -name "*.java" -exec awk -v limit="$LIMIT" 'length > limit' {} \; | wc -l)

if [ $TOTAL_LONG_LINES -eq 0 ]; then
    echo -e "${COLOR_GREEN}✅ Nenhuma linha encontrada com mais de ${LIMIT} caracteres!${COLOR_RESET}"
else
    echo -e "${COLOR_YELLOW}📊 Resumo:${COLOR_RESET}"
    echo -e "   Total de arquivos Java: ${COLOR_BLUE}${TOTAL_FILES}${COLOR_RESET}"
    echo -e "   Total de linhas longas: ${COLOR_RED}${TOTAL_LONG_LINES}${COLOR_RESET}"
fi

echo ""
echo -e "${COLOR_GREEN}✅ Busca concluída!${COLOR_RESET}"
