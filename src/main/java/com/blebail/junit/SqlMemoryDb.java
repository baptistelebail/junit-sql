package com.blebail.junit;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Provides a memory DataSource, and loads the initial SQL schema from an optional path if there is one,
 * or from the /db/schema.sql resource if it exists
 *
 * @see DataSource
 */
public final class SqlMemoryDb implements BeforeAllCallback {

    private static final String DATASOURCE_SCHEMA_PATH = "/db/schema.sql";

    private static final String DATASOURCE_URL = "jdbc:h2:mem:test;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1";

    private static final String DATASOURCE_USERNAME = "sa";

    private static final String DATASOURCE_PASSWORD = "";

    private final Path schemaPath;

    private final DataSource dataSource;

    public SqlMemoryDb() {
        this(null);
    }

    public SqlMemoryDb(Path schemaPath) {
        this.dataSource = buildDataSource();
        this.schemaPath = schemaPath;
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        resolveSchemaPath().ifPresent(this::createSchema);
    }

    private DataSource buildDataSource() {
        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setURL(DATASOURCE_URL);
        jdbcDataSource.setUser(DATASOURCE_USERNAME);
        jdbcDataSource.setPassword(DATASOURCE_PASSWORD);

        return jdbcDataSource;
    }

    private Optional<Path> resolveSchemaPath() {
        return Optional.ofNullable(schemaPath)
                .or(this::defaultSchemaPath);
    }

    private Optional<Path> defaultSchemaPath() {
        return Optional.ofNullable(getClass().getResource(DATASOURCE_SCHEMA_PATH))
                .flatMap(this::asUriUncheked)
                .map(Paths::get);
    }

    private Optional<URI> asUriUncheked(URL url) {
        try {
            return Optional.of(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void createSchema(Path path) {
        try {
            String createSchemaSql = Files.readString(path);
            new DbSetup(new DataSourceDestination(dataSource), Operations.sql(createSchemaSql)).launch();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the underlying DataSource
     *
     * @return the underlying DataSource
     */
    public DataSource dataSource() {
        return dataSource;
    }
}
