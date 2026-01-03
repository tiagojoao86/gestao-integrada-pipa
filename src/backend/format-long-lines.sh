#!/bin/bash

# Script para quebrar linhas longas em arquivos Java
# Uso: ./format-long-lines.sh [arquivo.java]

FILE="${1}"

if [ -z "$FILE" ]; then
    echo "Uso: $0 <arquivo.java>"
    exit 1
fi

if [ ! -f "$FILE" ]; then
    echo "Erro: Arquivo '$FILE' não encontrado"
    exit 1
fi

echo "Formatando linhas longas em: $FILE"

# Backup do arquivo original
cp "$FILE" "$FILE.bak"

# Processa o arquivo linha por linha
while IFS= read -r line; do
    # Conta o comprimento da linha
    length=${#line}

    if [ $length -gt 120 ]; then
        echo "  Linha longa encontrada (${length} caracteres)"

        # Se a linha contém extends com generics, quebra adequadamente
        if echo "$line" | grep -q "extends.*<.*>"; then
            # Quebra antes do último generic
            echo "$line" | sed -E 's/(.*),\s*([A-Z].*JpaRepository.*>)/\1,\n        \2/'
        else
            echo "$line"
        fi
    else
        echo "$line"
    fi
done < "$FILE.bak" > "$FILE"

echo "Formatação concluída! Backup salvo em: $FILE.bak"
