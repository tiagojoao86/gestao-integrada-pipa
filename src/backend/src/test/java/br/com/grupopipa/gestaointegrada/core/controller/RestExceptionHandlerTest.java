package br.com.grupopipa.gestaointegrada.core.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.grupopipa.gestaointegrada.core.exception.DeletedEntityException;
import br.com.grupopipa.gestaointegrada.core.exception.EntityNotFoundException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;

@DisplayName("RestExceptionHandler - Testes Unitários")
class RestExceptionHandlerTest {

    private MockMvc mockMvc;

    /**
     * Controller mínimo que lança as exceções para exercitar o handler.
     */
    @RestController
    static class TestController {

        @GetMapping("/test/bean-validation")
        void throwBeanValidation(@RequestParam(defaultValue = "false") boolean withEntity) {
            Set<BeanValidationMessage> violations = Set.of(
                    new BeanValidationMessage("nome", "O nome é obrigatório."),
                    new BeanValidationMessage("codigo", "O código é obrigatório."));
            if (withEntity) {
                throw new BeanValidationException("centroCusto", violations);
            }
            throw new BeanValidationException(violations);
        }

        @GetMapping("/test/not-found")
        void throwNotFound() {
            throw new EntityNotFoundException("MinhaEntidade", java.util.UUID.randomUUID());
        }

        @GetMapping("/test/deleted-entity")
        void throwDeletedEntity() {
            throw new DeletedEntityException("MinhaEntidade", java.util.UUID.randomUUID());
        }

        @GetMapping("/test/bad-credentials")
        void throwBadCredentials() {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        @GetMapping("/test/authorization-denied")
        void throwAuthorizationDenied() {
            throw new AuthorizationDeniedException("Acesso negado");
        }

        @GetMapping("/test/unexpected")
        void throwUnexpected() {
            throw new RuntimeException("Erro inesperado");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    // =========================================================
    // BeanValidationException
    // =========================================================

    @Test
    @DisplayName("BeanValidationException sem entidade deve retornar 400 com messages diretas")
    void deveTratarBeanValidationSemEntidade() throws Exception {
        mockMvc.perform(get("/test/bean-validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.userMessageKey").doesNotExist());
    }

    @Test
    @DisplayName("BeanValidationException com entidade deve retornar mensagens humanas (não keys)")
    void deveTratarBeanValidationComEntidade() throws Exception {
        mockMvc.perform(get("/test/bean-validation").param("withEntity", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages.length()").value(2))
                // Garante que messages contém texto legível (não keys como "centroCusto.nome")
                .andExpect(jsonPath("$.messages[?(@=~/.+é obrigatório.*/)]").exists())
                .andExpect(jsonPath("$.userMessageKey").doesNotExist());
    }

    // =========================================================
    // EntityNotFoundException
    // =========================================================

    @Test
    @DisplayName("EntityNotFoundException deve retornar 404 com mensagem humana em português")
    void deveTratarEntityNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.messages[0]").value("Recurso não encontrado."))
                .andExpect(jsonPath("$.userMessageKey").doesNotExist());
    }

    // =========================================================
    // DeletedEntityException
    // =========================================================

    @Test
    @DisplayName("DeletedEntityException deve retornar 400 com mensagem humana em português")
    void deveTratarDeletedEntityException() throws Exception {
        mockMvc.perform(get("/test/deleted-entity"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.messages[0]")
                        .value("Não é possível alterar um registro que foi excluído."))
                .andExpect(jsonPath("$.userMessageKey").doesNotExist());
    }

    // =========================================================
    // BadCredentialsException
    // =========================================================

    @Test
    @DisplayName("BadCredentialsException deve retornar 401 com mensagem humana em português")
    void deveTratarBadCredentialsException() throws Exception {
        mockMvc.perform(get("/test/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.messages[0]").value("Credenciais inválidas."))
                .andExpect(jsonPath("$.userMessageKey").doesNotExist());
    }

    // =========================================================
    // Exceção genérica (500)
    // =========================================================

    @Test
    @DisplayName("Exceção não tratada deve retornar 500 com mensagem genérica")
    void deveTratarExcecaoGenerica() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.messages[0]").value("Erro interno do servidor."))
                .andExpect(jsonPath("$.userMessageKey").doesNotExist());
    }

    // =========================================================
    // DatabaseConstraintsEnum
    // =========================================================

    @Test
    @DisplayName("DatabaseConstraintsEnum DEFAULT deve retornar mensagem genérica")
    void deveTratarConstraintNaoMapeada() {
        String message = br.com.grupopipa.gestaointegrada.core.dao.DatabaseConstraintsEnum
                .getByKey("constraint_inexistente")
                .getMessage();
        org.assertj.core.api.Assertions.assertThat(message).isEqualTo("Erro interno do servidor.");
    }

    @Test
    @DisplayName("DatabaseConstraintsEnum UK_USUARIO_LOGIN deve retornar mensagem de login duplicado")
    void deveMappearConstraintLoginUnico() {
        String message = br.com.grupopipa.gestaointegrada.core.dao.DatabaseConstraintsEnum
                .getByKey("UK_USUARIO_LOGIN")
                .getMessage();
        org.assertj.core.api.Assertions.assertThat(message).isEqualTo("Este login já está cadastrado.");
    }

    @Test
    @DisplayName("DatabaseConstraintsEnum CHK_TITULO_DATAS deve retornar mensagem de datas")
    void deveMappearConstraintDatas() {
        String message = br.com.grupopipa.gestaointegrada.core.dao.DatabaseConstraintsEnum
                .getByKey("CHK_TITULO_DATAS")
                .getMessage();
        org.assertj.core.api.Assertions.assertThat(message)
                .isEqualTo("A data de vencimento deve ser posterior à data de emissão.");
    }
}
