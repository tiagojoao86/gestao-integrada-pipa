package br.com.grupopipa.gestaointegrada.cadastro.cep;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

/**
 * Proxy público para consulta de endereço.
 * Endpoints acessíveis sem autenticação e sem X-Tenant-ID.
 */
@RestController
@RequestMapping("/cep")
public class CepController {

    private static final Logger LOG = LoggerFactory.getLogger(CepController.class);

    private static final String VIA_CEP_URL = "https://viacep.com.br/ws/{cep}/json/";
    private static final String VIA_CEP_LOGRADOURO_URL =
            "https://viacep.com.br/ws/{uf}/{cidade}/{logradouro}/json/";
    private static final String IBGE_MUNICIPIOS_URL =
            "https://servicodados.ibge.gov.br/api/v1/localidades/estados/{uf}/municipios?orderBy=nome";

    private static final int CEP_LENGTH = 8;
    private static final int MIN_LOGRADOURO_LENGTH = 3;

    private final RestClient restClient;

    public CepController() {
        this.restClient = RestClient.create();
    }

    /**
     * Consulta CEP via ViaCEP. Preenche logradouro, bairro, cidade e UF.
     */
    @GetMapping("/{cep}")
    public ResponseEntity<CepDTO> consultarCep(@PathVariable String cep) {
        String cepNormalizado = cep.replaceAll("[^0-9]", "");

        if (cepNormalizado.length() != CEP_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP deve conter 8 dígitos.");
        }

        CepDTO resultado = restClient.get()
                .uri(VIA_CEP_URL, cepNormalizado)
                .retrieve()
                .body(CepDTO.class);

        if (resultado == null || resultado.isErro()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CEP não encontrado.");
        }

        if (resultado.getCep() != null) {
            resultado.setCep(resultado.getCep().replaceAll("[^0-9]", ""));
        }

        return ResponseEntity.ok(resultado);
    }

    /**
     * Lista municípios de um estado via API do IBGE.
     * Retorna lista de nomes ordenada alfabeticamente.
     */
    @GetMapping("/cidades/{uf}")
    public ResponseEntity<List<String>> listarCidades(@PathVariable String uf) {
        try {
            IbgeMunicipioDTO[] municipios = restClient.get()
                    .uri(IBGE_MUNICIPIOS_URL, uf.toUpperCase())
                    .retrieve()
                    .body(IbgeMunicipioDTO[].class);

            if (municipios == null) {
                return ResponseEntity.ok(List.of());
            }

            return ResponseEntity.ok(
                    Arrays.stream(municipios)
                            .map(IbgeMunicipioDTO::getNome)
                            .toList());
        } catch (Exception e) {
            LOG.warn("Erro ao consultar municípios do IBGE para UF {}: {}", uf, e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Busca logradouros via ViaCEP (busca por endereço).
     * Requer UF, cidade e pelo menos 3 caracteres no logradouro.
     * Ao selecionar um resultado, o frontend obtém CEP, bairro e complemento.
     */
    @GetMapping("/logradouros/{uf}")
    public ResponseEntity<List<LogradouroDTO>> buscarLogradouros(
            @PathVariable String uf,
            @RequestParam String cidade,
            @RequestParam(defaultValue = "") String q) {

        if (q.length() < MIN_LOGRADOURO_LENGTH) {
            return ResponseEntity.ok(List.of());
        }

        try {
            LogradouroDTO[] resultados = restClient.get()
                    .uri(VIA_CEP_LOGRADOURO_URL, uf.toUpperCase(), cidade, q)
                    .retrieve()
                    .body(LogradouroDTO[].class);

            if (resultados == null) {
                return ResponseEntity.ok(List.of());
            }

            List<LogradouroDTO> lista = Arrays.stream(resultados)
                    .filter(r -> r.getCep() != null)
                    .peek(r -> r.setCep(r.getCep().replaceAll("[^0-9]", "")))
                    .toList();

            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            LOG.debug("ViaCEP logradouro search sem resultados para {}/{}/{}: {}", uf, cidade, q, e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }
}
