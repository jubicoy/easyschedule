package fi.jubic.easyschedule.liquibase;

import fi.jubic.easyconfig.db.SqlDatabaseConfig;
import fi.jubic.easyschedule.StartupScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LiquibaseH2Test {
    private Connection connection;

    @BeforeEach
    void setup() throws SQLException {
        connection = DriverManager.getConnection(
                "jdbc:h2:file:./target/liquibase/liquibase-test-db",
                "SA",
                ""
        );

        try {
            connection.createStatement()
                    .execute("DROP TABLE DATABASECHANGELOG");
        } catch (SQLException ignore) {}
        try {
            connection.createStatement()
                    .execute("DROP TABLE DATABASECHANGELOGLOCK");
        } catch (SQLException ignore) {}
        try {
            connection.createStatement()
                    .execute("DROP TABLE users");
        } catch (SQLException ignore) {}
    }

    @Test
    void testMigrations() throws SQLException {
        SqlDatabaseConfig config = new SqlDatabaseConfig() {
            @Override
            public void withConnection(ConnectionConsumer connectionConsumer) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T withConnection(ConnectionFunction<T> connectionFunction) throws SQLException {
                return connectionFunction.apply(connection);
            }
        };

        ResultSet preResults = connection.createStatement()
                .executeQuery("SHOW COLUMNS FROM users;");
        assertFalse(preResults.next());


        new StartupScheduler()
                .registerStartupTask(
                        new LiquibaseTask(config, "migrations.xml")
                )
                .start();

        ResultSet results = connection.createStatement()
                .executeQuery("SHOW COLUMNS FROM users;");

        results.first();
        assertEquals("ID", results.getString("field"));

        results.last();
        assertEquals(2, results.getRow());
        assertEquals("NAME", results.getString("field"));

    }
}
