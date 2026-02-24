package br.com.grupopipa.gestaointegrada.cadastro.usuario;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.valueobject.Login;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import br.com.grupopipa.gestaointegrada.core.valueobject.Senha;

/**
 * Validador responsável por centralizar as regras de criação de UsuarioEntity.
 * Chamado exclusivamente pelo {@code UsuarioEntity.Builder}.
 */
public class UsuarioValidator {

    private UsuarioValidator() {
    }

    public static ValidatedData validate(
            String nomeStr,
            String loginStr,
            String senhaStr,
            PasswordEncoder passwordEncoder,
            boolean isCreation) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);
        Login login = ValidationUtils.validateAndGet(() -> Login.of(loginStr), violations);

        Senha senha = null;
        if (isCreation) {
            senha = ValidationUtils.validateAndGet(() -> Senha.of(senhaStr, passwordEncoder), violations);
        } else if (senhaStr != null && !senhaStr.trim().isEmpty()) {
            senha = ValidationUtils.validateAndGet(() -> Senha.of(senhaStr, passwordEncoder), violations);
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("usuario", violations);
        }
        return new ValidatedData(nome, login, senha);
    }

    /**
     * Dados validados retornados por {@code validate()}.
     * Campos públicos para acesso direto pelo {@code UsuarioEntity.Builder}.
     */
    public static class ValidatedData {
        public final Nome nome;
        public final Login login;
        public final Senha senha;

        ValidatedData(Nome nome, Login login, Senha senha) {
            this.nome = nome;
            this.login = login;
            this.senha = senha;
        }
    }
}
