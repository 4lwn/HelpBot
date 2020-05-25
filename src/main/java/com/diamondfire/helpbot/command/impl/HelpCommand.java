package com.diamondfire.helpbot.command.impl;

import com.diamondfire.helpbot.command.arguments.Argument;
import com.diamondfire.helpbot.command.arguments.LazyStringArg;
import com.diamondfire.helpbot.command.arguments.ValueArgument;
import com.diamondfire.helpbot.command.impl.query.AbstractSingleQueryCommand;
import com.diamondfire.helpbot.command.permissions.Permission;
import com.diamondfire.helpbot.command.permissions.PermissionHandler;
import com.diamondfire.helpbot.components.codedatabase.db.datatypes.SimpleData;
import com.diamondfire.helpbot.events.CommandEvent;
import com.diamondfire.helpbot.instance.BotInstance;
import com.diamondfire.helpbot.util.BotConstants;
import com.diamondfire.helpbot.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.File;
import java.util.function.BiConsumer;


public class HelpCommand extends AbstractSingleQueryCommand {

    public static void sendHelpMessage(SimpleData data, TextChannel channel) {
        if (data == null) return;

        EmbedBuilder builder = data.getEnum().getEmbedBuilder().generateEmbed(data);
        String material;
        File actionIcon;
        File customHead = data.getItem().getHead();

        if (customHead == null) {
            material = data.getItem().getMaterial().toLowerCase();
            actionIcon = Util.fetchMinecraftTextureFile(material);
        } else {
            actionIcon = customHead;
            material = customHead.getName();
        }
        builder.setThumbnail("attachment://" + material + ".png");

        channel.sendMessage(builder.build()).addFile(actionIcon, material + ".png").queue();

    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Gets information for a game value, code block or action. If you cannot find what you want, try using the ?search command. \nSpecifying no arguments causes this help menu to appear.";
    }

    @Override
    public ValueArgument<String> getArgument() {
        return new LazyStringArg();
    }

    @Override
    public Permission getPermission() {
        return Permission.USER;
    }

    @Override
    public void run(CommandEvent event) {
        if (event.getArguments().length == 0) {
            EmbedBuilder builder = new EmbedBuilder();

            builder.setTitle("Help");
            builder.setDescription("HelpBot is a bot dedicated to looking at information regarding game values, codeblocks, actions, and more! Listed below are the commands that you are currently allowed to use. Any additional questions may be forwarded to Owen1212055");
            builder.setThumbnail(BotInstance.getJda().getSelfUser().getAvatarUrl());
            builder.setFooter("Your permissions: " + PermissionHandler.getPermission(event.getMember()));

            for (Command command : BotInstance.getHandler().getCommands().values()) {
                if (command.inHelp() && command.getPermission().hasPermission(event.getMember()) ) {
                    builder.addField(BotConstants.PREFIX + command.getName() + " " + command.getArgument(), command.getDescription(), false);
                }

            }

            event.getChannel().sendMessage(builder.build()).queue();
            return;
        }

        super.run(event);


    }

    @Override
    public BiConsumer<SimpleData, TextChannel> onDataReceived() {
        return HelpCommand::sendHelpMessage;
    }
}