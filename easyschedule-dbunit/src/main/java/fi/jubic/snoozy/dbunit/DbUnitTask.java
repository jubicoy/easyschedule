package fi.jubic.snoozy.dbunit;

import fi.jubic.easyconfig.db.SqlDatabaseConfig;
import fi.jubic.snoozy.Task;
import fi.jubic.snoozy.TaskSchedulerException;
import fi.jubic.snoozy.dbunit.template.base64.Base64Encoder;
import fi.jubic.snoozy.dbunit.template.date.DateObject;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;

import java.io.*;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DbUnitTask implements Task {
    private final SqlDatabaseConfig config;
    private final ClassLoader classLoader;
    private final String datasetFile;
    private final String dtdFile;

    public DbUnitTask(
            SqlDatabaseConfig config,
            ClassLoader classLoader,
            String datasetFile,
            String dtdFile
    ) {
        this.config = config;
        this.classLoader = classLoader;
        this.datasetFile = datasetFile;
        this.dtdFile = dtdFile;
    }


    @Override
    public void run() {
        try {
            config.withConnection(
                    connection -> {
                        try {
                            DatabaseConnection databaseConnection = new DatabaseConnection(connection);

                            Driver driver = DriverManager.getDriver(connection.getMetaData().getURL());
                            String driverName = driver.getClass().getName();

                            DefaultDataTypeFactory dataTypeFactory;
                            switch (driverName) {
                                case "org.postgresql.Driver":
                                    dataTypeFactory = new PostgresqlDataTypeFactory();
                                    break;

                                case "com.mysql.jdbc.Driver":
                                case "com.mysql.cj.jdbc.Driver":
                                    dataTypeFactory = new MySqlDataTypeFactory();
                                    break;

                                case "org.hsqldb.jdbcDriver":
                                    dataTypeFactory = new HsqldbDataTypeFactory();
                                    break;

                                default:
                                    dataTypeFactory = new DefaultDataTypeFactory();
                                    break;
                            }

                            DatabaseConfig databaseConfig = databaseConnection.getConfig();
                            databaseConfig.setProperty(
                                    DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                                    dataTypeFactory
                            );

                            InputStream dtdStream = classLoader.getResourceAsStream(dtdFile);
                            InputStream dataSetStream = classLoader.getResourceAsStream(datasetFile);
                            IDataSet dataSet = new FlatXmlDataSetBuilder()
                                    .setMetaDataSetFromDtd(dtdStream)
                                    .build(processStream(dataSetStream));

                            /*if (databaseConfigurator != null)
                                databaseConfigurator.configure(connection.getConfig());*/

                            DatabaseOperation.CLEAN_INSERT.execute(databaseConnection, dataSet);

                            dtdStream.close();
                            dataSetStream.close();
                        } catch (DatabaseUnitException | IOException | TemplateException exception) {
                            throw new TaskSchedulerException(exception);
                        }
                    }
            );
        } catch (SQLException exception) {
            throw new TaskSchedulerException(exception);
        }
    }

    private InputStream processStream (
            InputStream is
    ) throws IOException, TemplateException {
        Template t = new Template(
                "dataset",
                new InputStreamReader(is),
                new freemarker.template.Configuration(
                        freemarker.template.Configuration.VERSION_2_3_23
                )
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Map<String, Object> model = new HashMap<>();
        model.put("t", new DateObject(new Date()));
        model.put("base64", new Base64Encoder());
        t.process(model, new OutputStreamWriter(out));
        return new ByteArrayInputStream(out.toByteArray());
    }
}
