package br.com.grupopipa.gestaointegrada.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    public static final String POSTGRES_DRIVER = "org.postgresql.Driver";

    private static final String[] ENTITY_PACKAGES = new String[] {
            "br.com.grupopipa.gestaointegrada.core.entity",
            "br.com.grupopipa.gestaointegrada.cadastro.usuario.entity",
            "br.com.grupopipa.gestaointegrada.cadastro.perfil.entity",
            "br.com.grupopipa.gestaointegrada.cadastro.modulo.entity",
            "br.com.grupopipa.gestaointegrada.tenant.entity"
        };

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.jpa.show-sql}")
    private boolean showSql;

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder
                .create()
                .driverClassName(POSTGRES_DRIVER)
                .url(dbUrl)
                .username(dbUsername)
                .password(dbPassword)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            br.com.grupopipa.gestaointegrada.tenant.config.TenantConnectionProvider tenantConnectionProvider,
            br.com.grupopipa.gestaointegrada.tenant.config.TenantIdentifierResolver tenantIdentifierResolver) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        //vendorAdapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");
        vendorAdapter.setGenerateDdl(false);
        vendorAdapter.setShowSql(showSql);

        HashMap<String, Object> properties = new HashMap<>();
        // properties.put("hibernate.physical_naming_strategy",
        // "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        // properties.put("hibernate.implicit_naming_strategy",
        // "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.type.descriptor.sql.BasicBinder", "TRACE");
        properties.put("logging.level.org.hibernate.SQL", "DEBUG");
        properties.put("logging.level.org.hibernate.orm.jdbc.bind", "TRACE");
        
        // ⭐ CONFIGURAÇÃO DE MULTI-TENANCY ⭐
        properties.put("hibernate.multiTenancy", "SCHEMA");
        properties.put("hibernate.multi_tenant_connection_provider", tenantConnectionProvider);
        properties.put("hibernate.tenant_identifier_resolver", tenantIdentifierResolver);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setJpaPropertyMap(properties);
        factory.setPackagesToScan(ENTITY_PACKAGES);
        factory.setDataSource(dataSource());
        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }

    @Bean
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .baselineOnMigrate(true)
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
    }
}