package br.com.grupopipa.gestaointegrada.tenant.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provider de conexões para multi-tenancy
 * Altera o search_path do PostgreSQL dinamicamente
 */
@Slf4j
@Component
public class TenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    @Autowired
    private DataSource dataSource;

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        final Connection connection = getAnyConnection();
        try {
            if (tenantIdentifier != null && !tenantIdentifier.equals("public")) {
                // Define o search_path para o schema do tenant
                String searchPath = String.format("SET search_path TO %s, public", tenantIdentifier);
                log.debug("Configurando search_path: {}", searchPath);
                connection.createStatement().execute(searchPath);
            } else {
                // Schema público
                log.debug("Usando schema public");
                connection.createStatement().execute("SET search_path TO public");
            }
        } catch (SQLException e) {
            log.error("Erro ao configurar search_path para tenant [{}]: {}", tenantIdentifier, e.getMessage());
            throw new SQLException("Could not alter JDBC connection to specified schema [" + tenantIdentifier + "]", e);
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try {
            // Reset search_path antes de devolver a conexão ao pool
            connection.createStatement().execute("SET search_path TO public");
        } catch (SQLException e) {
            // Log error but don't fail
            e.printStackTrace();
        }
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}
