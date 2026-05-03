package br.com.grupopipa.gestaointegrada.financeiro.caixa;

import java.util.List;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.service.CrudService;

public interface CaixaService extends CrudService<CaixaDTO, CaixaGridDTO> {

    List<UsuarioCaixaDTO> listarUsuarios(UUID caixaId);

    void atualizarUsuarios(UUID caixaId, List<UUID> usuarioIds);

    List<CaixaGridDTO> listarTodosAtivos();

    List<UUID> listarCaixasPorUsuario(UUID usuarioId);

    void atualizarCaixasDoUsuario(UUID usuarioId, List<UUID> caixaIds);
}
