package de.jokergames.jfql.core;

import de.jokergames.jfql.command.*;
import de.jokergames.jfql.core.lang.ConditionHelper;
import de.jokergames.jfql.core.lang.LangFormatter;
import de.jokergames.jfql.database.DBSession;
import de.jokergames.jfql.database.Database;
import de.jokergames.jfql.database.DatabaseHandler;
import de.jokergames.jfql.exception.CommandException;
import de.jokergames.jfql.exception.ModuleException;
import de.jokergames.jfql.exception.NetworkException;
import de.jokergames.jfql.http.HttpService;
import de.jokergames.jfql.http.util.Builder;
import de.jokergames.jfql.module.ModuleHandler;
import de.jokergames.jfql.user.ConsoleUser;
import de.jokergames.jfql.user.UserHandler;
import de.jokergames.jfql.util.ConfigHandler;
import de.jokergames.jfql.util.Connection;
import de.jokergames.jfql.util.Console;
import de.jokergames.jfql.util.Downloader;
import org.json.JSONObject;

/**
 * @author Janick
 */

public final class JFQL {

    private static JFQL instance;

    private final Console console;
    private final CommandHandler commandHandler;
    private final ConfigHandler configHandler;
    private final LangFormatter langFormatter;
    private final String version;
    private final Downloader downloader;
    private final Connection connection;
    private final JSONObject configuration;
    private final DatabaseHandler dataBaseHandler;
    private final UserHandler userHandler;
    private final Builder builder;
    private final DBSession DBSession;
    private final ModuleHandler moduleHandler;
    private final ConditionHelper conditionHelper;
    private HttpService httpService;

    public JFQL() {
        instance = this;

        this.version = "1.0";
        this.console = new Console();
        this.connection = new Connection();
        this.downloader = new Downloader(connection);
        this.langFormatter = new LangFormatter();
        this.configHandler = new ConfigHandler();
        this.moduleHandler = new ModuleHandler();
        this.builder = new Builder();
        this.conditionHelper = new ConditionHelper();
        this.commandHandler = new CommandHandler();
        this.DBSession = new DBSession();
        this.dataBaseHandler = new DatabaseHandler(configHandler.getFactory());
        this.configuration = configHandler.getConfig();
        this.userHandler = new UserHandler(configHandler.getFactory());
    }

    public static JFQL getInstance() {
        return instance;
    }

    public void start() {
        console.clean("    _                  ______ _ _       ____                        _                                                    \n" +
                "     | |                |  ____(_) |     / __ \\                      | |                                                   \n" +
                "     | | __ ___   ____ _| |__   _| | ___| |  | |_   _  ___ _ __ _   _| |     __ _ _ __   __ _ _   _  __ _ _ __   __ _  ___ \n" +
                " _   | |/ _` \\ \\ / / _` |  __| | | |/ _ \\ |  | | | | |/ _ \\ '__| | | | |    / _` | '_ \\ / _` | | | |/ _` | '_ \\ / _` |/ _ \\\n" +
                "| |__| | (_| |\\ V / (_| | |    | | |  __/ |__| | |_| |  __/ |  | |_| | |___| (_| | | | | (_| | |_| | (_| | | | | (_| |  __/\n" +
                " \\____/ \\__,_| \\_/ \\__,_|_|    |_|_|\\___|\\___\\_\\\\__,_|\\___|_|   \\__, |______\\__,_|_| |_|\\__, |\\__,_|\\__,_|_| |_|\\__, |\\___|\n" +
                "                                                                 __/ |                   __/ |                   __/ |     \n" +
                "                                                                |___/                   |___/                   |___/      ");
        console.logInfo("Developers » jokergames.ddnss.de");
        console.logInfo("Version » " + version);
        console.clean();

        console.logInfo("Connecting to " + configuration.getString("Server") + "...");

        try {
            connection.connect(configuration.getString("Server"));

            if (connection.isMaintenance()) {
                console.logWarning("Database is currently in maintenance!");
                System.exit(0);
                return;
            }

            console.logInfo("Successfully connected.");

            if (!connection.isLatest()) {
                if (configuration.getBoolean("AutoUpdate")) {
                    downloader.download();
                } else {
                    console.logWarning("You aren't up to date. Please download the latest version.");
                }
            }

        } catch (Exception ex) {
            throw new NetworkException("Server connection failed!");
        }

        {
            if (userHandler.getUser("Console") == null)
                userHandler.saveUser(new ConsoleUser());

            if (dataBaseHandler.getDataBase("test") == null)
                dataBaseHandler.saveDataBase(new Database("test"));
        }

        console.clean();

        try {
            commandHandler.registerCommand(new ShutdownCommand());
            commandHandler.registerCommand(new UsrCommand());
            commandHandler.registerCommand(new ListCommand());
            commandHandler.registerCommand(new UseCommand());
            commandHandler.registerCommand(new InsertCommand());
            commandHandler.registerCommand(new CreateCommand());
            commandHandler.registerCommand(new DeleteCommand());
            commandHandler.registerCommand(new SelectCommand());
            commandHandler.registerCommand(new RemoveCommand());
        } catch (Exception ex) {
            throw new CommandException("Can't load commands!");
        }

        try {
            console.logInfo("Starting server on port " + configuration.getInt("Port") + "...");
            httpService = new HttpService();
            console.logInfo("Server was successfully started!");
        } catch (Exception ex) {
            throw new NetworkException("Can't start http server");
        }

        console.clean();

        try {
            moduleHandler.enableModules();
        } catch (Exception ex) {
            throw new ModuleException("Can't load modules!");
        }

        if (moduleHandler.getModules().size() != 0) {
            console.clean();
        }

        while (true) {
            commandHandler.execute(langFormatter.formatCommand(console.read()));
        }
    }

    public void shutdown() {
        moduleHandler.disableModules();
        System.exit(0);
    }

    public ConsoleUser getConsoleUser() {
        return (ConsoleUser) userHandler.getUser("Console");
    }

    public ModuleHandler getModuleHandler() {
        return moduleHandler;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public LangFormatter getFormatter() {
        return langFormatter;
    }

    public String getVersion() {
        return version;
    }

    public Downloader getUpdater() {
        return downloader;
    }

    public JSONObject getConfiguration() {
        return configuration;
    }

    public Connection getConnection() {
        return connection;
    }

    public DatabaseHandler getDataBaseHandler() {
        return dataBaseHandler;
    }

    public UserHandler getUserHandler() {
        return userHandler;
    }

    public DBSession getDBSession() {
        return DBSession;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public ConditionHelper getConditionHelper() {
        return conditionHelper;
    }

    public HttpService getHttpService() {
        return httpService;
    }

    public Builder getBuilder() {
        return builder;
    }

    public Console getConsole() {
        return console;
    }
}
