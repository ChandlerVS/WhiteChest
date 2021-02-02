package com.chandlervanscoy.whitechest.commands;

import com.chandlervanscoy.whitechest.WhiteChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class WhiteListTabCompletion implements TabCompleter {
    private Logger logger;
    public WhiteListTabCompletion() {
        logger = WhiteChest.getPlugin(WhiteChest.class).getLogger();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> returnList = new ArrayList<String>();

        for (String arg :
                args) {
            logger.info(arg);
        }

        if(args.length <= 1) {
            returnList.add("list");
            returnList.add("add");
            returnList.add("remove");
            return returnList;
        }

        return null;
    }
}
