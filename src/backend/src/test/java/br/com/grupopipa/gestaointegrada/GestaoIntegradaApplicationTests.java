package br.com.grupopipa.gestaointegrada;

import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Teste básico para verificar se o contexto da aplicação carrega corretamente
 * com multi-tenancy e Testcontainers configurados.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class GestaoIntegradaApplicationTests extends AbstractIntegrationTest {

	@Test
	void contextLoads() {
		// Se chegou aqui, o contexto carregou com sucesso!
		// - Testcontainers PostgreSQL rodando
		// - Tenant 'teste_tenant' criado e configurado
		// - Todas as migrations executadas
	}

}
