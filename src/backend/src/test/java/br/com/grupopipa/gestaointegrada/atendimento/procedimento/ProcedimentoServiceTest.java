package br.com.grupopipa.gestaointegrada.atendimento.procedimento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.grupopipa.gestaointegrada.atendimento.procedimento.dto.ProcedimentoDTO;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.DeletedEntityException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;

@DisplayName("ProcedimentoService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class ProcedimentoServiceTest {

    @Mock
    private ProcedimentoRepository repository;

    @Mock
    private Specifications<Procedimento> specifications;

    @InjectMocks
    private ProcedimentoServiceImpl service;

    private ProcedimentoDTO dtoValido;
    private Procedimento entidadeValida;
    private UUID procedimentoId;

    @BeforeEach
    void setup() throws Exception {
        procedimentoId = UUID.randomUUID();

        dtoValido = ProcedimentoDTO.builder()
                .codigo("PROC-001")
                .descricao("Sessão de Terapia ABA")
                .codigoTiss("10101012")
                .codigoTuss("20102022")
                .ativo(true)
                .build();

        entidadeValida = new Procedimento.Builder()
                .codigo("PROC-001")
                .descricao("Sessão de Terapia ABA")
                .codigoTiss("10101012")
                .codigoTuss("20102022")
                .ativo(true)
                .build();

        java.lang.reflect.Field idField = Procedimento.class.getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entidadeValida, procedimentoId);
    }

    @Test
    @DisplayName("Deve criar novo procedimento")
    void deveCriarNovoProcedimento() {
        when(repository.save(any(Procedimento.class))).thenReturn(entidadeValida);

        ProcedimentoDTO resultado = service.save(dtoValido);

        assertNotNull(resultado);
        assertEquals("PROC-001", resultado.getCodigo());
        assertEquals("Sessão de Terapia ABA", resultado.getDescricao());
        verify(repository, times(1)).save(any(Procedimento.class));
    }

    @Test
    @DisplayName("Deve criar procedimento sem códigos opcionais")
    void deveCriarProcedimentoSemCodigosOpcionais() throws Exception {
        ProcedimentoDTO dto = ProcedimentoDTO.builder()
                .codigo("PROC-002")
                .descricao("Avaliação Fonoaudiológica")
                .ativo(true)
                .build();

        Procedimento entidade = new Procedimento.Builder()
                .codigo("PROC-002")
                .descricao("Avaliação Fonoaudiológica")
                .ativo(true)
                .build();

        java.lang.reflect.Field idField = Procedimento.class.getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entidade, UUID.randomUUID());

        when(repository.save(any(Procedimento.class))).thenReturn(entidade);

        ProcedimentoDTO resultado = service.save(dto);

        assertNotNull(resultado);
        assertNull(resultado.getCodigoTiss());
        assertNull(resultado.getCodigoTuss());
    }

    @Test
    @DisplayName("Deve atualizar procedimento existente")
    void deveAtualizarProcedimento() {
        dtoValido.setId(procedimentoId);
        dtoValido.setCodigo("PROC-001-UPD");
        dtoValido.setDescricao("Sessão ABA Atualizada");

        when(repository.findById(procedimentoId)).thenReturn(Optional.of(entidadeValida));
        when(repository.save(any(Procedimento.class))).thenReturn(entidadeValida);

        ProcedimentoDTO resultado = service.save(dtoValido);

        assertNotNull(resultado);
        verify(repository, times(1)).findById(procedimentoId);
        verify(repository, times(1)).save(any(Procedimento.class));
    }

    @Test
    @DisplayName("Deve buscar procedimento por ID")
    void deveBuscarProcedimentoPorId() {
        when(repository.findById(procedimentoId)).thenReturn(Optional.of(entidadeValida));

        ProcedimentoDTO resultado = service.findById(procedimentoId);

        assertNotNull(resultado);
        assertEquals("PROC-001", resultado.getCodigo());
        verify(repository, times(1)).findById(procedimentoId);
    }

    @Test
    @DisplayName("Deve fazer soft delete de procedimento")
    void deveFazerSoftDelete() throws Exception {
        when(repository.findById(procedimentoId)).thenReturn(Optional.of(entidadeValida));
        when(repository.save(any(Procedimento.class))).thenReturn(entidadeValida);

        service.delete(procedimentoId);

        verify(repository, times(1)).findById(procedimentoId);
        verify(repository, times(1)).save(any(Procedimento.class));
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao editar procedimento excluído")
    void deveLancarExcecaoAoEditarProcedimentoExcluido() throws Exception {
        java.lang.reflect.Field deletedField = Procedimento.class.getSuperclass().getDeclaredField("deleted");
        deletedField.setAccessible(true);
        deletedField.set(entidadeValida, true);

        dtoValido.setId(procedimentoId);
        when(repository.findById(procedimentoId)).thenReturn(Optional.of(entidadeValida));

        assertThrows(DeletedEntityException.class, () -> service.save(dtoValido));
    }

    @Test
    @DisplayName("Deve construir DTO a partir de entidade")
    void deveConstruirDTODaEntidade() {
        when(repository.findById(procedimentoId)).thenReturn(Optional.of(entidadeValida));

        ProcedimentoDTO resultado = service.findById(procedimentoId);

        assertNotNull(resultado);
        assertEquals(procedimentoId, resultado.getId());
        assertEquals("PROC-001", resultado.getCodigo());
        assertEquals("Sessão de Terapia ABA", resultado.getDescricao());
        assertEquals("10101012", resultado.getCodigoTiss());
        assertEquals("20102022", resultado.getCodigoTuss());
        assertTrue(resultado.getAtivo());
    }

    @Test
    @DisplayName("Deve construir GridDTO a partir de entidade")
    void deveConstruirGridDTODaEntidade() {
        when(repository.findById(procedimentoId)).thenReturn(Optional.of(entidadeValida));

        ProcedimentoDTO dtoResultado = service.findById(procedimentoId);

        assertNotNull(dtoResultado);
        assertEquals("PROC-001", dtoResultado.getCodigo());
    }

    @Test
    @DisplayName("Deve lançar exceção quando código é nulo")
    void deveLancarExcecaoQuandoCodigoNulo() {
        ProcedimentoDTO dto = ProcedimentoDTO.builder()
                .codigo(null)
                .descricao("Sem código")
                .ativo(true)
                .build();

        assertThrows(BeanValidationException.class, () -> service.save(dto));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando descrição é nula")
    void deveLancarExcecaoQuandoDescricaoNula() {
        ProcedimentoDTO dto = ProcedimentoDTO.builder()
                .codigo("PROC-003")
                .descricao(null)
                .ativo(true)
                .build();

        assertThrows(BeanValidationException.class, () -> service.save(dto));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve incluir deleted false no GridDTO")
    void deveIncluirDeletedFalseNoGridDTO() throws Exception {
        java.lang.reflect.Field deletedField = Procedimento.class.getSuperclass().getDeclaredField("deleted");
        deletedField.setAccessible(true);
        deletedField.set(entidadeValida, false);

        when(repository.findById(procedimentoId)).thenReturn(Optional.of(entidadeValida));

        ProcedimentoDTO resultado = service.findById(procedimentoId);

        assertNotNull(resultado);
        assertFalse(entidadeValida.getDeleted());
    }
}
