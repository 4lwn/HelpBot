package com.diamondfire.helpbot.command.impl.stats.support;

import com.diamondfire.helpbot.command.argument.ArgumentSet;
import com.diamondfire.helpbot.command.argument.impl.types.ClampedIntegerArgument;
import com.diamondfire.helpbot.command.help.*;
import com.diamondfire.helpbot.command.impl.Command;
import com.diamondfire.helpbot.command.permissions.Permission;
import com.diamondfire.helpbot.components.database.SingleQueryBuilder;
import com.diamondfire.helpbot.events.CommandEvent;
import com.diamondfire.helpbot.util.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.*;

public class TimeTopCommand extends Command {

    @Override
    public String getName() {
        return "toptime";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"timetop"};
    }

    @Override
    public HelpContext getHelpContext() {
        return new HelpContext()
                .description("Gets support members with the top session time in a certain number of days.")
                .category(CommandCategory.SUPPORT)
                .addArgument(
                        new HelpContextArgument()
                                .name("days")
                                .optional()
                );
    }

    @Override
    public ArgumentSet getArguments() {
        return new ArgumentSet()
                .addArgument("days",
                        new ClampedIntegerArgument(1, 4000000).optional(30));
    }

    @Override
    public Permission getPermission() {
        return Permission.SUPPORT;
    }

    @Override
    public void run(CommandEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        int days = event.getArgument("days");
        builder.setTitle(String.format("Top Support Member's time in %s days", days));

        new SingleQueryBuilder()
                .query("SELECT DISTINCT staff, SUM(duration) AS sessions FROM support_sessions WHERE time > CURRENT_TIMESTAMP - INTERVAL ? DAY GROUP BY staff ORDER BY sessions DESC LIMIT 10", (statement) -> {
                    statement.setInt(1, days);
                })
                .onQuery((resultTable) -> {
                    LinkedHashMap<String, Long> stats = new LinkedHashMap<>();
                    do {
                        stats.put(StringUtil.display(resultTable.getString("staff")), resultTable.getLong("sessions"));
                    } while (resultTable.next());

                    for (Map.Entry<String, Long> stat : stats.entrySet()) {
                        builder.addField(stat.getKey(), "\nTotal Duration: " + StringUtil.formatMilliTime(stat.getValue()), false);
                    }

                }).execute();

        event.getChannel().sendMessage(builder.build()).queue();


    }


}

