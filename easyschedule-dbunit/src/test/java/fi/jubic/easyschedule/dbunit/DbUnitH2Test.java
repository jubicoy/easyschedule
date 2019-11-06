package fi.jubic.easyschedule.dbunit;

import fi.jubic.easyconfig.db.SqlDatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DbUnitH2Test {
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
                    .execute("DROP TABLE user");
            connection.createStatement()
                    .execute("DROP TABLE message");
        }
        catch (SQLException ignore) {}

        connection.createStatement()
                .execute("CREATE TABLE user (id IDENTITY PRIMARY KEY, name VARCHAR(255))");
        connection.createStatement()
                .execute("CREATE TABLE message (id IDENTITY PRIMARY KEY, user_id BIGINT, text VARCHAR(255))");
        connection.createStatement()
                .execute("ALTER TABLE message ADD FOREIGN KEY (user_id) REFERENCES user(id)");
    }

    @Test
    void testPopulation() throws SQLException {
        SqlDatabaseConfig config = new SqlDatabaseConfig() {
            @Override
            public void withConnection(ConnectionConsumer connectionConsumer) throws SQLException {
                connectionConsumer.accept(connection);
            }

            @Override
            public <T> T withConnection(ConnectionFunction<T> connectionFunction) {
                throw new UnsupportedOperationException();
            }
        };

        System.out.println(this.getClass().getResource(".").getPath());

        new DbUnitTask(
                config,
                DbUnitH2Test.class.getClassLoader(),
                "dataset.xml",
                "dataset.dtd"
        ).run();

        ResultSet user1Results = connection.createStatement()
                .executeQuery("SELECT * FROM user WHERE id=1");
        user1Results.next();
        assertEquals(1, user1Results.getInt("ID"));
        assertEquals("User 1", user1Results.getString("NAME"));
        assertFalse(user1Results.next());

        ResultSet user2Results = connection.createStatement()
                .executeQuery("SELECT * FROM user WHERE id=2");
        user2Results.next();
        assertEquals(2, user2Results.getInt("ID"));
        assertEquals("User 2", user2Results.getString("NAME"));
        assertFalse(user2Results.next());

        ResultSet countResultSet = connection.createStatement()
                .executeQuery("SELECT COUNT(*) AS count FROM user");
        countResultSet.next();
        assertEquals(2, countResultSet.getInt("COUNT"));

        ResultSet message1ResultSet = connection.createStatement()
                .executeQuery("SELECT * FROM message WHERE id=1");
        message1ResultSet.next();
        assertEquals(1, message1ResultSet.getInt("ID"));
        assertEquals(1, message1ResultSet.getInt("USER_ID"));
        assertEquals("Hello there", message1ResultSet.getString("TEXT"));

        ResultSet message2ResultSet = connection.createStatement()
                .executeQuery("SELECT * FROM message WHERE id=2");
        message2ResultSet.next();
        assertEquals(2, message2ResultSet.getInt("ID"));
        assertEquals(1, message2ResultSet.getInt("USER_ID"));
        assertEquals("Hello there again", message2ResultSet.getString("TEXT"));

        ResultSet messageCountResultSet = connection.createStatement()
                .executeQuery("SELECT COUNT(*) AS count FROM message");
        messageCountResultSet.next();
        assertEquals(2, messageCountResultSet.getInt("COUNT"));
    }
}
