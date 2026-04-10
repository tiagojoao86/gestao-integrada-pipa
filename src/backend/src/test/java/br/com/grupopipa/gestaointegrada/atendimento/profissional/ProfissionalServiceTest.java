package br.com.grupopipa.gestaointegrada.atendimento.profissional;

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

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.grupopipa.gestaointegrada.atendimento.profissional.dto.ProfissionalDTO;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.dto.ProfissionalGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.entity.Profissional;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.DeletedEntityException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;

@DisplayName("ProfissionalService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class ProfissionalServiceTest {

    @Mock
    private ProfissionalRepository repository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private Specifications<Profissional> specifications;

    @InjectMocks
    private ProfissionalServiceImpl service;

    private ProfissionalDTO dtoValido;
    private Profissional entidadeValida;
    private Pessoa pessoa;
    private UUID profissionalId;
    private UUID pessoaId;

    @BeforeEach
    void setup() {
        profissionalId = UUID.randomUUID();
        pessoaId = UUID.randomUUID();

        pessoa = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.FISICA)
                .nome("Ana Paula Ferreira")
                .email("ana@example.com")
                .telefone("11987654321")
                .cpf("12345678909")
                .dataNascimento(LocalDate.of(1985, 3, 20))
                .build();

        try {
            java.lang.reflect.Field idField = Pessoa.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(pessoa, pessoaId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        dtoValido = ProfissionalDTO.builder()
                .pessoaId(pessoaId)
                .conselho("CRP")
                .codigoConselho("CRP-06/12345")
                .tipoRemuneracao("CLT")
                .banco("Nubank")
                .conta("12345-6")
                .chavePix("ana@example.com")
                .ativo(true)
                .build();

        entidadeValida = new Profissional.Builder()
                .pessoa(pessoa)
                .conselho("CRP")
                .codigoConselho("CRP-06/12345")
                .tipoRemuneracao(TipoRemuneracao.CLT)
                .banco("Nubank")
                .conta("12345-6")
                .chavePix("ana@example.com")
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve criar novo profissional")
    void deveCriarNovoProfissional() {
        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));
        when(repository.save(any(Profissional.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfissionalDTO resultado = service.save(dtoValido);

        assertNotNull(resultado);
        assertEquals(pessoaId, resultado.getPessoaId());
        assertEquals("Ana Paula Ferreira", resultado.getPessoaNome());
        assertEquals("CRP", resultado.getConselho());
        assertEquals("CRP-06/12345", resultado.getCodigoConselho());
        assertEquals("CLT", resultado.getTipoRemuneracao());
        assertEquals("Nubank", resultado.getBanco());
        assertTrue(resultado.getAtivo());

        verify(pessoaRepository, times(1)).findById(pessoaId);
        verify(repository, times(1)).save(any(Profissional.class));
    }

    @Test
    @DisplayName("Deve atualizar profissional existente")
    void deveAtualizarProfissionalExistente() {
        dtoValido.setId(profissionalId);
        dtoValido.setConselho("CRM");
        dtoValido.setCodigoConselho("CRM-SP/99999");

        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));
        when(repository.findById(profissionalId)).thenReturn(Optional.of(entidadeValida));
        when(repository.save(any(Profissional.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfissionalDTO resultado = service.save(dtoValido);

        assertNotNull(resultado);
        assertEquals("CRM", resultado.getConselho());
        assertEquals("CRM-SP/99999", resultado.getCodigoConselho());

        verify(repository, times(1)).findById(profissionalId);
        verify(repository, times(1)).save(any(Profissional.class));
    }

    @Test
    @DisplayName("Deve buscar profissional por id")
    void deveBuscarProfissionalPorId() {
        when(repository.findById(profissionalId)).thenReturn(Optional.of(entidadeValida));

        ProfissionalDTO resultado = service.findById(profissionalId);

        assertNotNull(resultado);
        assertEquals("CRP", resultado.getConselho());
        assertEquals("CRP-06/12345", resultado.getCodigoConselho());
        assertEquals("CLT", resultado.getTipoRemuneracao());
        assertEquals(pessoaId, resultado.getPessoaId());
        assertEquals("Ana Paula Ferreira", resultado.getPessoaNome());

        verify(repository, times(1)).findById(profissionalId);
    }

    @Test
    @DisplayName("Deve realizar soft delete do profissional")
    void deveRealizarSoftDeleteDoProfissional() {
        when(repository.findById(profissionalId)).thenReturn(Optional.of(entidadeValida));
        when(repository.save(any(Profissional.class))).thenReturn(entidadeValida);

        UUID resultado = service.delete(profissionalId);

        assertEquals(profissionalId, resultado);
        verify(repository, times(1)).findById(profissionalId);
        verify(repository, times(1)).save(any(Profissional.class));
        assertTrue(entidadeValida.getDeleted());
        assertNotNull(entidadeValida.getDeletedAt());
    }

    @Test
    @DisplayName("Deve construir DTO corretamente da entidade")
    void deveConstruirDTOCorretamenteDaEntidade() {
        ProfissionalDTO dto = service.buildDTOFromEntity(entidadeValida);

        assertNotNull(dto);
        assertEquals("CRP", dto.getConselho());
        assertEquals("CRP-06/12345", dto.getCodigoConselho());
        assertEquals("CLT", dto.getTipoRemuneracao());
        assertEquals("Nubank", dto.getBanco());
        assertEquals("12345-6", dto.getConta());
        assertEquals("ana@example.com", dto.getChavePix());
        assertEquals(pessoaId, dto.getPessoaId());
        assertEquals("Ana Paula Ferreira", dto.getPessoaNome());
        assertTrue(dto.getAtivo());
    }

    @Test
    @DisplayName("Deve construir GridDTO corretamente da entidade")
    void deveConstruirGridDTOCorretamenteDaEntidade() {
        ProfissionalGridDTO gridDTO = service.buildGridDTOFromEntity(entidadeValida);

        assertNotNull(gridDTO);
        assertEquals("Ana Paula Ferreira", gridDTO.getPessoaNome());
        assertEquals("CRP", gridDTO.getConselho());
        assertEquals("CRP-06/12345", gridDTO.getCodigoConselho());
        assertEquals("CLT", gridDTO.getTipoRemuneracao());
        assertTrue(gridDTO.getAtivo());
    }

    @Test
    @DisplayName("Deve incluir campo deleted no GridDTO")
    void deveIncluirCampoDeletedNoGridDTO() {
        ProfissionalGridDTO gridDTO = service.buildGridDTOFromEntity(entidadeValida);
        assertNotNull(gridDTO);
        assertFalse(gridDTO.getDeleted());

        entidadeValida.markAsDeleted("admin");
        ProfissionalGridDTO gridDTOExcluido = service.buildGridDTOFromEntity(entidadeValida);
        assertNotNull(gridDTOExcluido);
        assertTrue(gridDTOExcluido.getDeleted());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar editar profissional excluído")
    void deveLancarExcecaoAoTentarEditarProfissionalExcluido() {
        Profissional profissionalExcluido = new Profissional.Builder()
                .pessoa(pessoa)
                .conselho("CRP")
                .codigoConselho("CRP-06/99999")
                .tipoRemuneracao(TipoRemuneracao.CLT)
                .ativo(true)
                .build();

        profissionalExcluido.markAsDeleted("admin");

        try {
            java.lang.reflect.Field idField = Profissional.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(profissionalExcluido, profissionalId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(repository.findById(profissionalId)).thenReturn(Optional.of(profissionalExcluido));

        ProfissionalDTO dtoAtualizado = ProfissionalDTO.builder()
                .id(profissionalId)
                .pessoaId(pessoaId)
                .conselho("CRM")
                .codigoConselho("CRM-SP/12345")
                .tipoRemuneracao("CLT")
                .ativo(true)
                .build();

        assertThrows(DeletedEntityException.class, () -> service.save(dtoAtualizado));
        verify(repository, times(1)).findById(profissionalId);
        verify(repository, never()).save(any(Profissional.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar profissional sem pessoa")
    void deveLancarExcecaoAoCriarProfissionalSemPessoa() {
        ProfissionalDTO dtoSemPessoa = ProfissionalDTO.builder()
                .pessoaId(null)
                .conselho("CRP")
                .codigoConselho("CRP-06/12345")
                .tipoRemuneracao("CLT")
                .ativo(true)
                .build();

        assertThrows(BeanValidationException.class, () -> service.save(dtoSemPessoa));
        verify(repository, never()).save(any(Profissional.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar profissional sem conselho")
    void deveLancarExcecaoAoCriarProfissionalSemConselho() {
        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));

        ProfissionalDTO dtoSemConselho = ProfissionalDTO.builder()
                .pessoaId(pessoaId)
                .conselho(null)
                .codigoConselho("CRP-06/12345")
                .tipoRemuneracao("CLT")
                .ativo(true)
                .build();

        assertThrows(BeanValidationException.class, () -> service.save(dtoSemConselho));
        verify(repository, never()).save(any(Profissional.class));
    }

    @Test
    @DisplayName("Deve retornar pessoaNome nulo quando profissional não tem pessoa")
    void deveRetornarPessoaNomeNuloQuandoSemPessoa() {
        // Construir entidade sem pessoa via reflexão (simula estado inconsistente)
        Profissional profissionalSemPessoa = new Profissional.Builder()
                .pessoa(pessoa)
                .conselho("CRP")
                .codigoConselho("CRP-06/12345")
                .tipoRemuneracao(TipoRemuneracao.CLT)
                .ativo(true)
                .build();

        // Setar pessoa como null via reflexão
        try {
            java.lang.reflect.Field pessoaField = Profissional.class.getDeclaredField("pessoa");
            pessoaField.setAccessible(true);
            pessoaField.set(profissionalSemPessoa, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ProfissionalDTO dto = service.buildDTOFromEntity(profissionalSemPessoa);

        assertNull(dto.getPessoaId());
        assertNull(dto.getPessoaNome());
    }
}
