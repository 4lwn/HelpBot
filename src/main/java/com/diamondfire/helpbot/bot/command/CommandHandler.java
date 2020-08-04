package com.diamondfire.helpbot.bot.command;

import com.diamondfire.helpbot.bot.command.impl.Command;
import com.diamondfire.helpbot.bot.command.reply.PresetBuilder;
import com.diamondfire.helpbot.bot.command.reply.feature.informative.*;
import com.diamondfire.helpbot.bot.events.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.HashMap;
import java.util.concurrent.*;

public class CommandHandler {

    private final HashMap<String, Command> CMDS = new HashMap<>();
    private final HashMap<String, Command> ALIASES = new HashMap<>();
    private final ExecutorService POOL = Executors.newCachedThreadPool();

    public void register(Command... commands) {
        for (Command command : commands) {
            this.CMDS.put(command.getName().toLowerCase(), command);
            for (String alias : command.getAliases()) {
                this.ALIASES.put(alias.toLowerCase(), command);
            }
        }

    }

    public void run(CommandEvent e) {
        Command command = e.getCommand();

        if (command != null) {
            if (!command.getPermission().hasPermission(e.getMember())) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("No Permission!");
                builder.setDescription("Sorry, you do not have permission to use this command. Commands that you are able to use are listed in ?help.");
                builder.setFooter("Permission Required: " + command.getPermission().name());
                e.getChannel().sendMessage(builder.build()).queue();
                return;
            }

            CompletableFuture.runAsync(() -> {
                        try {
                            command.run(e);
                        } catch (Exception error) {
                            error.printStackTrace();

                            PresetBuilder builder = new PresetBuilder().withPreset(new InformativeReply(InformativeReplyType.ERROR, "This command failed to execute, sorry for the inconvenience."));
                            e.reply(builder);
                        }
                    }, POOL
            );
        }

    }

    public HashMap<String, Command> getCommands() {
        return CMDS;
    }

    public HashMap<String, Command> getAliases() {
        return ALIASES;
    }
}
