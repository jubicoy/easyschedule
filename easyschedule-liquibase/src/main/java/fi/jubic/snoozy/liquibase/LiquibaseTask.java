package fi.jubic.snoozy.liquibase;

import fi.jubic.easyconfig.db.SqlDatabaseConfig;
import fi.jubic.snoozy.Task;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.SQLException;
import java.util.Optional;

public class LiquibaseTask implements Task {
    private final SqlDatabaseConfig config;
    private final String migrationsFile;

    public LiquibaseTask(
            SqlDatabaseConfig config,
            String migrationsFile
    ) {
        this.config = config;
        this.migrationsFile = migrationsFile;
    }

    @Override
    public void run() {
        try {
            config.<Optional<LiquibaseException>>withConnection(
                    connection -> {
                        try {
                            Database database = DatabaseFactory.getInstance()
                                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

                            Liquibase liquibase = new Liquibase(
                                    migrationsFile,
                                    new ClassLoaderResourceAccessor(),
                                    database
                            );

                            liquibase.update(new Contexts());
                        } catch (LiquibaseException e) {
                            return Optional.of(e);
                        }
                        return Optional.empty();
                    }
            ).ifPresent(Throwable::printStackTrace);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
