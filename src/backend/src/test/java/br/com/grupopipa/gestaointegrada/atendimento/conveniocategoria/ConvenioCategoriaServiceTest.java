package br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria;

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

import br.com.grupopipa.gestaointegrada.atendimento.convenio.ConvenioRepository;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.dto.ConvenioCategoriaDTO;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.dto.ConvenioCategoriaGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity.ConvenioCategoria;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.DeletedEntityException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;

@DisplayName("ConvenioCategoriaService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class ConvenioCategoriaServiceTest {

    @Mock
    private ConvenioCategoriaRepository repository;

    @Mock
    private ConvenioRepository convenioRepository;

    @Mock
    private Specifications<ConvenioCategoria> specifications;

    @InjectMocks
    private ConvenioCategoriaServiceImpl service;

    private ConvenioCategoriaDTO dtoValido;
    private ConvenioCategoria entidadeValida;
    private Convenio convenio;
    private UUID categoriaId;
    private UUID convenioId;

    @BeforeEach
    void setup() {
        categoriaId = UUID.randomUUID();
        convenioId = UUID.randomUUID();

        Pessoa pessoa = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.JURIDICA)
                .nome("Plano Saúde Ltda")
                .email("contato@planosaude.com.br")
                .telefone("1133334444")
                .cnpj("06158095000152")
                .razaoSocial("Plano Saúde Ltda")
                .build();

        convenio = new Convenio.Builder()
                .nome("Unimed")
                .pessoa(pessoa)
                .ativo(true)
                .build();

        try {
            java.lang.reflect.Field idField = Convenio.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(convenio, convenioId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        dtoValido = ConvenioCategoriaDTO.builder()
                .convenioId(convenioId)
                .nome("Básico")
                .codigoAnsPlano("ANS001")
                .ativo(true)
                .build();

        entidadeValida = new ConvenioCategoria.Builder()
                .convenio(convenio)
                .nome("Básico")
                .codigoAnsPlano("ANS001")
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve criar nova categoria de convênio")
    void deveCriarNovaCategoria() {
        when(convenioRepository.findById(convenioId)).thenReturn(Optional.of(convenio));
        when(repository.save(any(ConvenioCategoria.class))).thenAnswer(inv -> inv.getArgument(0));

        ConvenioCategoriaDTO resultado = service.save(dtoValido);

        assertNotNull(resultado);
        assertEquals("Básico", resultado.getNome());
        assertEquals("ANS001", resultado.getCodigoAnsPlano());
        assertEquals(convenioId, resultado.getConvenioId());
        assertEquals("Unimed", resultado.getConvenioNome());
        assertTrue(resultado.getAtivo());

        verify(convenioRepository, times(1)).findById(convenioId);
        verify(repository, times(1)).save(any(ConvenioCategoria.class));
    }

    @Test
    @DisplayName("Deve criar categoria sem código ANS do plano")
    void deveCriarCategoriaSemCodigoAns() {
        dtoValido.setCodigoAnsPlano(null);
        when(convenioRepository.findById(convenioId)).thenReturn(Optional.of(convenio));
        when(repository.save(any(ConvenioCategoria.class))).thenAnswer(inv -> inv.getArgument(0));

        ConvenioCategoriaDTO resultado = service.save(dtoValido);

        assertNotNull(resultado);
        assertNull(resultado.getCodigoAnsPlano());
        verify(repository, times(1)).save(any(ConvenioCategoria.class));
    }

    @Test
    @DisplayName("Deve atualizar categoria existente")
    void deveAtualizarCategoriaExistente() {
        dtoValido.setId(categoriaId);
        dtoValido.setNome("Especial");
        dtoValido.setCodigoAnsPlano("ANS999");

        when(convenioRepository.findById(convenioId)).thenReturn(Optional.of(convenio));
        when(repository.findById(categoriaId)).thenReturn(Optional.of(entidadeValida));
        when(repository.save(any(ConvenioCategoria.class))).thenAnswer(inv -> inv.getArgument(0));

        ConvenioCategoriaDTO resultado = service.save(dtoValido);

        assertNotNull(resultado);
        assertEquals("Especial", resultado.getNome());
        assertEquals("ANS999", resultado.getCodigoAnsPlano());

        verify(repository, times(1)).findById(categoriaId);
        verify(repository, times(1)).save(any(ConvenioCategoria.class));
    }

    @Test
    @DisplayName("Deve buscar categoria por id")
    void deveBuscarCategoriaPorId() {
        when(repository.findById(categoriaId)).thenReturn(Optional.of(entidadeValida));

        ConvenioCategoriaDTO resultado = service.findById(categoriaId);

        assertNotNull(resultado);
        assertEquals("Básico", resultado.getNome());
        assertEquals("ANS001", resultado.getCodigoAnsPlano());
        assertEquals(convenioId, resultado.getConvenioId());
        assertEquals("Unimed", resultado.getConvenioNome());

        verify(repository, times(1)).findById(categoriaId);
    }

    @Test
    @DisplayName("Deve realizar soft delete da categoria")
    void deveRealizarSoftDeleteDaCategoria() {
        when(repository.findById(categoriaId)).thenReturn(Optional.of(entidadeValida));
        when(repository.save(any(ConvenioCategoria.class))).thenReturn(entidadeValida);

        UUID resultado = service.delete(categoriaId);

        assertEquals(categoriaId, resultado);
        verify(repository, times(1)).findById(categoriaId);
        verify(repository, times(1)).save(any(ConvenioCategoria.class));
        assertTrue(entidadeValida.getDeleted());
        assertNotNull(entidadeValida.getDeletedAt());
    }

    @Test
    @DisplayName("Deve construir DTO corretamente da entidade")
    void deveConstruirDTOCorretamenteDaEntidade() {
        ConvenioCategoriaDTO dto = service.buildDTOFromEntity(entidadeValida);

        assertNotNull(dto);
        assertEquals("Básico", dto.getNome());
        assertEquals("ANS001", dto.getCodigoAnsPlano());
        assertEquals(convenioId, dto.getConvenioId());
        assertEquals("Unimed", dto.getConvenioNome());
        assertTrue(dto.getAtivo());
    }

    @Test
    @DisplayName("Deve construir GridDTO corretamente da entidade")
    void deveConstruirGridDTOCorretamenteDaEntidade() {
        ConvenioCategoriaGridDTO gridDTO = service.buildGridDTOFromEntity(entidadeValida);

        assertNotNull(gridDTO);
        assertEquals("Básico", gridDTO.getNome());
        assertEquals("ANS001", gridDTO.getCodigoAnsPlano());
        assertEquals("Unimed", gridDTO.getConvenioNome());
        assertTrue(gridDTO.getAtivo());
    }

    @Test
    @DisplayName("Deve incluir campo deleted no GridDTO")
    void deveIncluirCampoDeletedNoGridDTO() {
        ConvenioCategoriaGridDTO gridDTO = service.buildGridDTOFromEntity(entidadeValida);
        assertNotNull(gridDTO);
        assertFalse(gridDTO.getDeleted());

        entidadeValida.markAsDeleted("admin");
        ConvenioCategoriaGridDTO gridDTOExcluido = service.buildGridDTOFromEntity(entidadeValida);
        assertNotNull(gridDTOExcluido);
        assertTrue(gridDTOExcluido.getDeleted());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar editar categoria excluída")
    void deveLancarExcecaoAoTentarEditarCategoriaExcluida() {
        ConvenioCategoria categoriaExcluida = new ConvenioCategoria.Builder()
                .convenio(convenio)
                .nome("Premium")
                .ativo(true)
                .build();

        categoriaExcluida.markAsDeleted("admin");

        try {
            java.lang.reflect.Field idField = ConvenioCategoria.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(categoriaExcluida, categoriaId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(repository.findById(categoriaId)).thenReturn(Optional.of(categoriaExcluida));

        ConvenioCategoriaDTO dtoAtualizado = ConvenioCategoriaDTO.builder()
                .id(categoriaId)
                .convenioId(convenioId)
                .nome("Premium Atualizado")
                .ativo(true)
                .build();

        assertThrows(DeletedEntityException.class, () -> service.save(dtoAtualizado));
        verify(repository, times(1)).findById(categoriaId);
        verify(repository, never()).save(any(ConvenioCategoria.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar categoria sem convênio")
    void deveLancarExcecaoAoCriarCategoriaSemConvenio() {
        ConvenioCategoriaDTO dtoSemConvenio = ConvenioCategoriaDTO.builder()
                .convenioId(null)
                .nome("Básico")
                .ativo(true)
                .build();

        assertThrows(BeanValidationException.class, () -> service.save(dtoSemConvenio));
        verify(repository, never()).save(any(ConvenioCategoria.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar categoria sem nome")
    void deveLancarExcecaoAoCriarCategoriaSemNome() {
        when(convenioRepository.findById(convenioId)).thenReturn(Optional.of(convenio));

        ConvenioCategoriaDTO dtoSemNome = ConvenioCategoriaDTO.builder()
                .convenioId(convenioId)
                .nome(null)
                .ativo(true)
                .build();

        assertThrows(BeanValidationException.class, () -> service.save(dtoSemNome));
        verify(repository, never()).save(any(ConvenioCategoria.class));
    }

    @Test
    @DisplayName("Deve retornar convenioNome nulo quando categoria não tem convênio")
    void deveRetornarConvenioNomeNuloQuandoSemConvenio() {
        ConvenioCategoria categoriaSemConvenio = new ConvenioCategoria.Builder()
                .convenio(convenio)
                .nome("Básico")
                .ativo(true)
                .build();

        try {
            java.lang.reflect.Field convenioField = ConvenioCategoria.class.getDeclaredField("convenio");
            convenioField.setAccessible(true);
            convenioField.set(categoriaSemConvenio, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ConvenioCategoriaDTO dto = service.buildDTOFromEntity(categoriaSemConvenio);

        assertNull(dto.getConvenioId());
        assertNull(dto.getConvenioNome());
    }
}
