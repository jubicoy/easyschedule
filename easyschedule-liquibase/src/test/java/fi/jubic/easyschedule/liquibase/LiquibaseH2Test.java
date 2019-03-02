package fi.jubic.easyschedule.liquibase;

import fi.jubic.easyconfig.db.SqlDatabaseConfig;
import fi.jubic.easyschedule.StartupScheduler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LiquibaseH2Test {
    Connection connection;

    @Before
    public void setup() throws SQLException {
        connection = DriverManager.getConnection(
                "jdbc:h2:/tmp/liquibase-test-db",
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
    public void testMigrations() throws SQLException {
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
        Assert.assertFalse(preResults.next());


        new StartupScheduler()
                .registerStartupTask(
                        new LiquibaseTask(config, "migrations.xml")
                )
                .start();

        ResultSet results = connection.createStatement()
                .executeQuery("SHOW COLUMNS FROM users;");

        results.first();
        Assert.assertEquals("ID", results.getString("field"));

        results.last();
        Assert.assertEquals(2, results.getRow());
        Assert.assertEquals("NAME", results.getString("field"));

    }
}
