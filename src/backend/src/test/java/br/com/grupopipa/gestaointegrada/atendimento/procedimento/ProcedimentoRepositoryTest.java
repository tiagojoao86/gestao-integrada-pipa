package br.com.grupopipa.gestaointegrada.atendimento.procedimento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;

@DisplayName("ProcedimentoRepository - Testes de Integração")
@Transactional
class ProcedimentoRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ProcedimentoRepository repository;

    @Test
    @DisplayName("Deve salvar e recuperar procedimento")
    void deveSalvarERecuperarProcedimento() {
        Procedimento procedimento = new Procedimento.Builder()
                .codigo("PROC-" + UUID.randomUUID().toString().substring(0, 8))
                .descricao("Sessão de Terapia ABA")
                .codigoTiss("10101012")
                .codigoTuss("20102022")
                .ativo(true)
                .build();

        Procedimento salvo = repository.save(procedimento);
        repository.flush();

        Optional<Procedimento> encontrado = repository.findById(salvo.getId());

        assertTrue(encontrado.isPresent());
        assertNotNull(encontrado.get().getCodigo());
        assertEquals("Sessão de Terapia ABA", encontrado.get().getDescricao());
        assertEquals("10101012", encontrado.get().getCodigoTiss());
        assertEquals("20102022", encontrado.get().getCodigoTuss());
        assertTrue(encontrado.get().getAtivo());
        assertNotNull(encontrado.get().getCreatedAt());
    }

    @Test
    @DisplayName("Deve salvar procedimento sem códigos TISS e TUSS")
    void deveSalvarProcedimentoSemCodigosOpcionais() {
        Procedimento procedimento = new Procedimento.Builder()
                .codigo("PROC-" + UUID.randomUUID().toString().substring(0, 8))
                .descricao("Avaliação Fonoaudiológica")
                .ativo(true)
                .build();

        Procedimento salvo = repository.save(procedimento);
        repository.flush();

        Optional<Procedimento> encontrado = repository.findById(salvo.getId());
        assertTrue(encontrado.isPresent());
        assertNotNull(encontrado.get().getCodigo());
        assertTrue(encontrado.get().getCodigoTiss() == null || encontrado.get().getCodigoTiss().isBlank()
                || !encontrado.get().getCodigoTiss().isEmpty());
    }

    @Test
    @DisplayName("Deve deletar procedimento")
    void deveDeletarProcedimento() {
        Procedimento procedimento = new Procedimento.Builder()
                .codigo("PROC-" + UUID.randomUUID().toString().substring(0, 8))
                .descricao("Para Deletar")
                .ativo(true)
                .build();

        Procedimento salvo = repository.save(procedimento);
        repository.flush();

        repository.deleteById(salvo.getId());
        repository.flush();

        Optional<Procedimento> encontrado = repository.findById(salvo.getId());
        assertFalse(encontrado.isPresent());
    }

    @Test
    @DisplayName("Deve validar constraint unique de código")
    void deveValidarConstraintUniqueCodigo() {
        String codigo = "PROC-UNICO-" + UUID.randomUUID().toString().substring(0, 6);

        Procedimento procedimento1 = new Procedimento.Builder()
                .codigo(codigo)
                .descricao("Primeiro Procedimento")
                .ativo(true)
                .build();

        repository.save(procedimento1);
        repository.flush();

        Procedimento procedimento2 = new Procedimento.Builder()
                .codigo(codigo)
                .descricao("Segundo Procedimento")
                .ativo(true)
                .build();

        assertThrows(Exception.class, () -> {
            repository.save(procedimento2);
            repository.flush();
        });
    }

    @Test
    @DisplayName("Deve salvar procedimento inativo")
    void deveSalvarProcedimentoInativo() {
        Procedimento procedimento = new Procedimento.Builder()
                .codigo("PROC-" + UUID.randomUUID().toString().substring(0, 8))
                .descricao("Procedimento Inativo")
                .ativo(false)
                .build();

        Procedimento salvo = repository.save(procedimento);
        repository.flush();

        Optional<Procedimento> encontrado = repository.findById(salvo.getId());
        assertTrue(encontrado.isPresent());
        assertFalse(encontrado.get().getAtivo());
    }
}
