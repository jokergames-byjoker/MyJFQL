package org.jokergames.myjfql.command;

import org.jokergames.myjfql.core.MyJFQL;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ShutdownCommand extends Command {

    public ShutdownCommand() {
        super("shutdown", Collections.singletonList("COMMAND"));
    }

    @Override
    public void handle(final CommandSender sender, final Map<String, List<String>> args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendForbidden();
            return;
        }

        MyJFQL.getInstance().shutdown();
    }

}
