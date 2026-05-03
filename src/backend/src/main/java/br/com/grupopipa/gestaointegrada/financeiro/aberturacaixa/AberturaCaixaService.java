package br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AberturaCaixaService {

    AberturaCaixaDTO abrir(AbrirCaixaRequest request);

    AberturaCaixaDTO fechar(UUID id, FecharCaixaRequest request);

    Optional<AberturaCaixaDTO> findAtivaByCaixaId(UUID caixaId);

    List<CaixaComStatusDTO> listarMeusCaixas();
}
