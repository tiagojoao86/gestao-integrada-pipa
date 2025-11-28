package br.com.grupopipa.gestaointegrada.core.dao;

public enum DatabaseConstraintsEnum {

    DEFAULT("errors.internalServerError"),
    UK_USUARIO_LOGIN("usuario.login.unique"),
    UK_PERFIL_NOME("perfil.nome.unique");
    
    String userMessageKey;

    DatabaseConstraintsEnum(String userMessageKey) {
        this.userMessageKey = userMessageKey;
    }

    public String getUserMessageKey() {
        return userMessageKey;
    }

    public static DatabaseConstraintsEnum getByKey(String key) {
        for (DatabaseConstraintsEnum constraint : values()) {
            if (constraint.name().equalsIgnoreCase(key)) {
                return constraint;
            }
        }
        return DEFAULT;
    }
}
