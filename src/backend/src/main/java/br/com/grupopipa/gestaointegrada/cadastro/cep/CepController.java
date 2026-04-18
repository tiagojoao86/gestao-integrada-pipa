package br.com.grupopipa.gestaointegrada.cadastro.cep;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Proxy público para consulta de CEP via ViaCEP.
 * Endpoint acessível sem autenticação e sem X-Tenant-ID.
 */
@RestController
@RequestMapping("/cep")
public class CepController {

    private static final String VIA_CEP_URL = "https://viacep.com.br/ws/{cep}/json/";
    private static final int CEP_LENGTH = 8;

    private final RestClient restClient;

    public CepController() {
        this.restClient = RestClient.create();
    }

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

        // Normaliza o CEP retornado (remove hífen)
        if (resultado.getCep() != null) {
            resultado.setCep(resultado.getCep().replaceAll("[^0-9]", ""));
        }

        return ResponseEntity.ok(resultado);
    }
}
