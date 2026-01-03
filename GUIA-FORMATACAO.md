# Guia de Formatação de Código Java no Projeto

## 📋 Resumo da Configuração

O projeto está configurado com:
- ✅ **Indentação**: 4 espaços (padrão Java)
- ✅ **Limite de linha**: 120 caracteres (validado pelo Checkstyle)
- ✅ **EditorConfig**: Garante 4 espaços em todos os editores
- ✅ **Checkstyle**: Valida qualidade e estilo do código
- ❌ **Spotless**: Removido (causava conflitos com VSCode)

## 🎯 Formatação no VSCode

### Configuração Atual

O VSCode está configurado para:
- **Tab size**: 4 espaços
- **Régua visual**: Linha em 120 caracteres
- **Formatação automática ao salvar**: DESABILITADA (para evitar problemas)

### Atalhos Úteis

| Ação | Windows/Linux | Mac |
|------|---------------|-----|
| Formatar documento | `Shift+Alt+F` | `Shift+Option+F` |
| Formatar seleção | `Ctrl+K Ctrl+F` | `Cmd+K Cmd+F` |
| Organizar imports | `Shift+Alt+O` | `Shift+Option+O` |
| Salvar arquivo | `Ctrl+S` | `Cmd+S` |
| Command Palette | `Ctrl+Shift+P` | `Cmd+Shift+P` |

### Como Formatar Código

**1. Formatação básica (indentação, espaços)**
```
Shift+Alt+F
```
Isso formata:
- ✅ Indentação (4 espaços)
- ✅ Espaços após vírgulas
- ✅ Espaços ao redor de operadores
- ✅ Organização de chaves

**2. Quebra manual de linhas longas**

O formatador **NÃO quebra automaticamente** linhas complexas. Você precisa fazer manualmente.

**Exemplo prático:**

```java
// ❌ ERRADO (127 caracteres - excede 120)
public abstract class CrudServiceImpl<D extends DTO, G extends GridDTO, T extends BaseEntity, R extends JpaRepository<T, UUID>>

// ✅ CORRETO (93 + 72 caracteres)
public abstract class CrudServiceImpl<D extends DTO, G extends GridDTO, T extends BaseEntity,
        R extends JpaRepository<T, UUID>>
```

### Dicas de Quebra de Linha

**Para declarações de classe com generics:**
```java
// Quebre antes do último generic ou do implements
public class MinhaClasse<T extends Tipo1, U extends Tipo2,
        V extends TipoMuitoLongo> implements Interface {
```

**Para métodos com muitos parâmetros:**
```java
// Quebre após cada parâmetro
public void meuMetodo(
        String parametro1,
        Integer parametro2,
        List<String> parametro3) {
```

**Para chamadas de método com muitos argumentos:**
```java
// Quebre após cada argumento
resultado = service.metodoComplexo(
        argumento1,
        argumento2,
        argumento3
);
```

## 🔍 Identificar Linhas Longas

### Comando 1: Encontrar todas as linhas > 120 caracteres

```bash
cd src/backend
find src/main/java -name "*.java" -exec awk 'length>120 {print FILENAME":"NR": "length" caracteres"}' {} \;
```

### Comando 2: Ver as linhas longas com contexto

```bash
cd src/backend
find src/main/java -name "*.java" -exec awk 'length>120 {print FILENAME":"NR" ("length" chars): "$0}' {} \; | head -20
```

### Comando 3: Contar linhas longas por arquivo

```bash
cd src/backend
find src/main/java -name "*.java" -exec sh -c 'count=$(awk "length>120" "$1" | wc -l); [ $count -gt 0 ] && echo "$1: $count linhas"' _ {} \;
```

## ✅ Validar Código Antes do Commit

### 1. Executar Checkstyle

```bash
cd src/backend
./mvnw checkstyle:check
```

Se houver violações, você verá:
```
[WARNING] src/main/java/.../MeuArquivo.java:[42,5] (sizes) LineLength: ...
```

### 2. Executar Checkstyle em arquivo específico

```bash
cd src/backend
./mvnw checkstyle:check | grep "MeuArquivo.java"
```

### 3. Build completo (Checkstyle + Compilação)

```bash
cd src/backend
./mvnw clean verify
```

## 🛠️ Ferramentas de Ajuda

### Script: Encontrar Linhas Longas

Criamos um script em `src/backend/find-long-lines.sh`:

```bash
cd src/backend
./find-long-lines.sh
```

Crie o script com:
```bash
cat > src/backend/find-long-lines.sh << 'EOF'
#!/bin/bash
echo "🔍 Procurando linhas com mais de 120 caracteres..."
echo ""
find src/main/java -name "*.java" -exec awk '
    length>120 {
        gsub(/^[ \t]+/, "");
        printf "%-60s Linha %4d (%3d chars): %.80s...\n",
            FILENAME, NR, length, $0
    }
' {} \; | sort
echo ""
echo "✅ Busca concluída!"
EOF
chmod +x src/backend/find-long-lines.sh
```

## 📝 Workflow Recomendado

### Antes de Commitar

1. **Organize imports** (`Shift+Alt+O`)
2. **Formate o código** (`Shift+Alt+F`)
3. **Verifique linhas longas visualmente** (use a régua em 120)
4. **Quebre manualmente** linhas que excedem 120 caracteres
5. **Execute Checkstyle**:
   ```bash
   cd src/backend
   ./mvnw checkstyle:check
   ```
6. **Corrija violações** se houver
7. **Commit** quando tudo estiver OK

### Durante o Desenvolvimento

1. Use a **régua visual em 120** como guia
2. Quando uma linha ultrapassar 120:
   - Quebre imediatamente
   - Não deixe acumular
3. Formate regularmente com `Shift+Alt+F`

## 🎨 Padrões de Estilo do Projeto

### Indentação
- 4 espaços (NUNCA tabs)
- Continuação: +8 espaços (2x indentação)

### Chaves
- Estilo K&R (chaves no final da linha)
```java
public class MinhaClasse {
    public void meuMetodo() {
        if (condicao) {
            // código
        }
    }
}
```

### Espaços
- Após vírgulas: `metodo(a, b, c)`
- Ao redor de operadores: `x = a + b`
- NÃO antes de parênteses em métodos: `metodo()` não `metodo ()`

### Imports
- Sem wildcards: `import java.util.List;` não `import java.util.*;`
- Organize automaticamente: `Shift+Alt+O`

### Linhas em branco
- 1 linha antes de métodos
- 1 linha entre blocos lógicos
- Newline ao final de cada arquivo

## 🚨 Problemas Comuns

### "Meu código está formatando com 2 espaços!"

**Causa**: Resquício do Spotless

**Solução**:
1. Selecione o código
2. `Shift+Alt+F` para reformatar com 4 espaços
3. Salve

### "O formatador não quebra minhas linhas longas!"

**Causa**: Limitação do Eclipse Formatter

**Solução**: Quebre manualmente onde necessário

### "Checkstyle reclama de indentação"

**Causa**: Código ainda formatado com 2 espaços (Spotless)

**Solução**:
1. Selecione todo o arquivo (`Ctrl+A`)
2. Formate (`Shift+Alt+F`)
3. Verifique se mudou para 4 espaços

## 📚 Recursos Adicionais

### Arquivos de Configuração

- **`.editorconfig`**: `src/backend/.editorconfig`
- **Checkstyle**: `src/backend/checkstyle.xml`
- **VSCode**: `.vscode/settings.json`

### Comandos Maven Úteis

```bash
# Compilar sem testes
./mvnw clean compile

# Executar testes
./mvnw test

# Verificar tudo (Checkstyle + Testes)
./mvnw clean verify

# Pular testes
./mvnw clean package -DskipTests
```

### Recarregar Configurações do VSCode

Se mudou configurações:
1. `Ctrl+Shift+P`
2. Digite: `Developer: Reload Window`
3. Enter

Ou:
1. `Ctrl+Shift+P`
2. Digite: `Java: Clean Java Language Server Workspace`
3. Recarregue a janela

## ✨ Dicas Pro

### 1. Multi-cursor para quebrar parâmetros
```
Ctrl+Alt+Down (ou Up)  # Adiciona cursor abaixo/acima
Alt+Click              # Adiciona cursor onde clicar
```

### 2. Selecionar próxima ocorrência
```
Ctrl+D                 # Seleciona próxima ocorrência da palavra
```

### 3. Comentar/Descomentar
```
Ctrl+/                 # Comentário de linha
Shift+Alt+A            # Comentário de bloco
```

### 4. Mover linha para cima/baixo
```
Alt+Up                 # Move linha para cima
Alt+Down               # Move linha para baixo
```

### 5. Duplicar linha
```
Shift+Alt+Down         # Duplica linha abaixo
```

## 📊 Estatísticas do Projeto

Para ver quantas linhas precisam ser ajustadas:

```bash
cd src/backend
echo "Total de arquivos Java:"
find src/main/java -name "*.java" | wc -l

echo ""
echo "Arquivos com linhas > 120 caracteres:"
find src/main/java -name "*.java" -exec sh -c 'awk "length>120" "$1" | grep -q . && echo "$1"' _ {} \; | wc -l

echo ""
echo "Total de linhas > 120 caracteres:"
find src/main/java -name "*.java" -exec awk 'length>120' {} \; | wc -l
```

---

**Última atualização**: Janeiro 2026
**Versão**: 1.0
