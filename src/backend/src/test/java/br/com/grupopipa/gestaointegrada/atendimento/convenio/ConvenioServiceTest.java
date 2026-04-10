package br.com.grupopipa.gestaointegrada.atendimento.convenio;

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

import br.com.grupopipa.gestaointegrada.atendimento.convenio.dto.ConvenioDTO;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.dto.ConvenioGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.DeletedEntityException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;

@DisplayName("ConvenioService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class ConvenioServiceTest {

    @Mock
    private ConvenioRepository repository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private Specifications<Convenio> specifications;

    @InjectMocks
    private ConvenioServiceImpl service;

    private ConvenioDTO dtoValido;
    private Convenio entidadeValida;
    private Pessoa pessoa;
    private UUID convenioId;
    private UUID pessoaId;

    @BeforeEach
    void setup() {
        convenioId = UUID.randomUUID();
        pessoaId = UUID.randomUUID();

        pessoa = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.JURIDICA)
                .nome("Plano Saúde Ltda")
                .email("contato@planosaude.com.br")
                .telefone("1133334444")
                .cnpj("06158095000152")
                .razaoSocial("Plano Saúde Comércio Ltda")
                .build();

        try {
            java.lang.reflect.Field idField = Pessoa.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(pessoa, pessoaId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        dtoValido = ConvenioDTO.builder()
                .nome("Unimed")
                .pessoaId(pessoaId)
                .registroAns("123456")
                .ativo(true)
                .build();

        entidadeValida = new Convenio.Builder()
                .nome("Unimed")
                .pessoa(pessoa)
                .registroAns("123456")
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve criar novo convênio")
    void deveCriarNovoConvenio() {
        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));
        when(repository.save(any(Convenio.class))).thenAnswer(inv -> inv.getArgument(0));

        ConvenioDTO resultado = service.save(dtoValido);

        assertNotNull(resultado);
        assertEquals("Unimed", resultado.getNome());
        assertEquals(pessoaId, resultado.getPessoaId());
        assertEquals("Plano Saúde Ltda", resultado.getPessoaNome());
        assertEquals("123456", resultado.getRegistroAns());
        assertTrue(resultado.getAtivo());

        verify(pessoaRepository, times(1)).findById(pessoaId);
        verify(repository, times(1)).save(any(Convenio.class));
    }

    @Test
    @DisplayName("Deve criar convênio sem registro ANS")
    void deveCriarConvenioSemRegistroAns() {
        dtoValido.setRegistroAns(null);
        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));
        when(repository.save(any(Convenio.class))).thenAnswer(inv -> inv.getArgument(0));

        ConvenioDTO resultado = service.save(dtoValido);

        assertNotNull(resultado);
        assertNull(resultado.getRegistroAns());
        verify(repository, times(1)).save(any(Convenio.class));
    }

    @Test
    @DisplayName("Deve atualizar convênio existente")
    void deveAtualizarConvenioExistente() {
        dtoValido.setId(convenioId);
        dtoValido.setNome("Amil");
        dtoValido.setRegistroAns("654321");

        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));
        when(repository.findById(convenioId)).thenReturn(Optional.of(entidadeValida));
        when(repository.save(any(Convenio.class))).thenAnswer(inv -> inv.getArgument(0));

        ConvenioDTO resultado = service.save(dtoValido);

        assertNotNull(resultado);
        assertEquals("Amil", resultado.getNome());
        assertEquals("654321", resultado.getRegistroAns());

        verify(repository, times(1)).findById(convenioId);
        verify(repository, times(1)).save(any(Convenio.class));
    }

    @Test
    @DisplayName("Deve buscar convênio por id")
    void deveBuscarConvenioPorId() {
        when(repository.findById(convenioId)).thenReturn(Optional.of(entidadeValida));

        ConvenioDTO resultado = service.findById(convenioId);

        assertNotNull(resultado);
        assertEquals("Unimed", resultado.getNome());
        assertEquals("123456", resultado.getRegistroAns());
        assertEquals(pessoaId, resultado.getPessoaId());
        assertEquals("Plano Saúde Ltda", resultado.getPessoaNome());
        assertTrue(resultado.getAtivo());

        verify(repository, times(1)).findById(convenioId);
    }

    @Test
    @DisplayName("Deve realizar soft delete do convênio")
    void deveRealizarSoftDeleteDoConvenio() {
        when(repository.findById(convenioId)).thenReturn(Optional.of(entidadeValida));
        when(repository.save(any(Convenio.class))).thenReturn(entidadeValida);

        UUID resultado = service.delete(convenioId);

        assertEquals(convenioId, resultado);
        verify(repository, times(1)).findById(convenioId);
        verify(repository, times(1)).save(any(Convenio.class));
        assertTrue(entidadeValida.getDeleted());
        assertNotNull(entidadeValida.getDeletedAt());
    }

    @Test
    @DisplayName("Deve construir DTO corretamente da entidade")
    void deveConstruirDTOCorretamenteDaEntidade() {
        ConvenioDTO dto = service.buildDTOFromEntity(entidadeValida);

        assertNotNull(dto);
        assertEquals("Unimed", dto.getNome());
        assertEquals("123456", dto.getRegistroAns());
        assertEquals(pessoaId, dto.getPessoaId());
        assertEquals("Plano Saúde Ltda", dto.getPessoaNome());
        assertTrue(dto.getAtivo());
    }

    @Test
    @DisplayName("Deve construir GridDTO corretamente da entidade")
    void deveConstruirGridDTOCorretamenteDaEntidade() {
        ConvenioGridDTO gridDTO = service.buildGridDTOFromEntity(entidadeValida);

        assertNotNull(gridDTO);
        assertEquals("Unimed", gridDTO.getNome());
        assertEquals("123456", gridDTO.getRegistroAns());
        assertEquals("Plano Saúde Ltda", gridDTO.getPessoaNome());
        assertTrue(gridDTO.getAtivo());
    }

    @Test
    @DisplayName("Deve incluir campo deleted no GridDTO")
    void deveIncluirCampoDeletedNoGridDTO() {
        ConvenioGridDTO gridDTO = service.buildGridDTOFromEntity(entidadeValida);
        assertNotNull(gridDTO);
        assertFalse(gridDTO.getDeleted());

        entidadeValida.markAsDeleted("admin");
        ConvenioGridDTO gridDTOExcluido = service.buildGridDTOFromEntity(entidadeValida);
        assertNotNull(gridDTOExcluido);
        assertTrue(gridDTOExcluido.getDeleted());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar editar convênio excluído")
    void deveLancarExcecaoAoTentarEditarConvenioExcluido() {
        Convenio convenioExcluido = new Convenio.Builder()
                .nome("Bradesco Saúde")
                .pessoa(pessoa)
                .ativo(true)
                .build();

        convenioExcluido.markAsDeleted("admin");

        try {
            java.lang.reflect.Field idField = Convenio.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(convenioExcluido, convenioId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(repository.findById(convenioId)).thenReturn(Optional.of(convenioExcluido));

        ConvenioDTO dtoAtualizado = ConvenioDTO.builder()
                .id(convenioId)
                .nome("Bradesco Saúde Atualizado")
                .pessoaId(pessoaId)
                .ativo(true)
                .build();

        assertThrows(DeletedEntityException.class, () -> service.save(dtoAtualizado));
        verify(repository, times(1)).findById(convenioId);
        verify(repository, never()).save(any(Convenio.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar convênio sem nome")
    void deveLancarExcecaoAoCriarConvenioSemNome() {
        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));

        ConvenioDTO dtoSemNome = ConvenioDTO.builder()
                .nome(null)
                .pessoaId(pessoaId)
                .ativo(true)
                .build();

        assertThrows(BeanValidationException.class, () -> service.save(dtoSemNome));
        verify(repository, never()).save(any(Convenio.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar convênio sem pessoa")
    void deveLancarExcecaoAoCriarConvenioSemPessoa() {
        ConvenioDTO dtoSemPessoa = ConvenioDTO.builder()
                .nome("Unimed")
                .pessoaId(null)
                .ativo(true)
                .build();

        assertThrows(BeanValidationException.class, () -> service.save(dtoSemPessoa));
        verify(repository, never()).save(any(Convenio.class));
    }

    @Test
    @DisplayName("Deve retornar pessoaNome nulo quando convênio não tem pessoa")
    void deveRetornarPessoaNomeNuloQuandoSemPessoa() {
        Convenio convenioSemPessoa = new Convenio.Builder()
                .nome("Unimed")
                .pessoa(pessoa)
                .ativo(true)
                .build();

        try {
            java.lang.reflect.Field pessoaField = Convenio.class.getDeclaredField("pessoa");
            pessoaField.setAccessible(true);
            pessoaField.set(convenioSemPessoa, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ConvenioDTO dto = service.buildDTOFromEntity(convenioSemPessoa);

        assertNull(dto.getPessoaId());
        assertNull(dto.getPessoaNome());
    }
}
