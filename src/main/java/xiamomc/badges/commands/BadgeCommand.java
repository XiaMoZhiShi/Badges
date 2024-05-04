package xiamomc.badges.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xiamomc.badges.BadgeManager;
import xiamomc.badges.XiamoBadges;
import xiamomc.badges.commands.builder.CommandBuilder;
import xiamomc.badges.misc.TabCompletions;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Command.SubCommandHandler;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.List;

public class BadgeCommand extends SubCommandHandler<XiamoBadges>
{
    public BadgeCommand()
    {
        buildCommands();
    }

    @Resolved
    private BadgeManager badgeManager;

    private void buildCommands()
    {
        subCommands = CommandBuilder.builder()
                .startNew()
                .name("use")
                //.permission("")
                .onFilter((sender, args) ->
                {
                    if (!(sender instanceof Player player))
                        return List.of();

                    var input = !args.isEmpty() ? args.get(0) : "";

                    var available = badgeManager.getPlayerdata(player);
                    return available.unlockedBadges.stream()
                            .filter(bid -> bid.toLowerCase().startsWith(input.toLowerCase()))
                            .toList();
                })
                .executes((sender, args) ->
                {
                    if (!(sender instanceof Player player))
                    {
                        sender.sendMessage("Illegal sender");
                        return true;
                    }

                    var input = !args.isEmpty() ? args.get(0) : "";

                    var available = badgeManager.getPlayerdata(player);
                    var match = available.unlockedBadges
                            .stream()
                            .filter(bid ->
                            {
                                logger.info("%s <-> %s --> %s".formatted(bid, input, bid.equalsIgnoreCase(input)));
                                return bid.equalsIgnoreCase(input);
                            })
                            .findFirst()
                            .orElse(null);

                    if (match == null)
                    {
                        sender.sendMessage("Badge not found");
                        return true;
                    }

                    badgeManager.applyBadge(player.getUniqueId(), match);
                    sender.sendMessage("Applied '%s'".formatted(match));
                    return true;
                })

                .startNew()
                .name("unload")
                //.permission("")
                .onFilter((sender, args) -> List.of())
                .executes((sender, args) ->
                {
                    if (!(sender instanceof Player player))
                    {
                        sender.sendMessage("Not player");
                        return true;
                    }

                    badgeManager.applyBadge(player.getUniqueId(), null);
                    sender.sendMessage("Unloaded badge");
                    return true;
                })

                .startNew()
                .name("revoke")
                //.permission("")
                .onFilter((sender, args) ->
                {
                    if (args.size() == 1)
                    {
                        return TabCompletions.filterOnlinePlayers.apply(sender, args);
                    }
                    else if (args.size() == 2)
                    {
                        var input = args.get(1);

                        return badgeManager.getAllAvailableBadges().stream()
                                .filter(badge -> badge.identifier.toLowerCase().startsWith(input.toLowerCase()))
                                .map(b -> b.identifier)
                                .toList();
                    }

                    return List.of();
                })
                .executes((sender, args) ->
                {
                    if (args.size() < 2)
                    {
                        sender.sendMessage("Not enough arguments!");
                        return true;
                    }

                    var playerName = args.get(0);
                    var badgeId = args.get(1);

                    var offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                    var availableBadges = badgeManager.getAllAvailableBadges();

                    var targetBadge = availableBadges.stream().filter(b -> b.identifier.equalsIgnoreCase(badgeId))
                            .findFirst().orElse(null);
                    if (targetBadge == null)
                    {
                        sender.sendMessage("Badge not found");
                        return true;
                    }

                    var data = badgeManager.getPlayerdata(offlinePlayer);
                    data.unlockedBadges.remove(badgeId);
                    sender.sendMessage("Success.");

                    return true;
                })

                .startNew()
                .name("grant")
                //.permission("")
                .onFilter((sender, args) ->
                {
                    if (args.size() == 1)
                    {
                        return TabCompletions.filterOnlinePlayers.apply(sender, args);
                    }
                    else if (args.size() == 2)
                    {
                        var input = args.get(1);

                        return badgeManager.getAllAvailableBadges().stream()
                                .filter(badge -> badge.identifier.toLowerCase().startsWith(input.toLowerCase()))
                                .map(b -> b.identifier)
                                .toList();
                    }

                    return List.of();
                })
                .executes((sender, args) ->
                {
                    if (args.size() < 2)
                    {
                        sender.sendMessage("Not enough arguments!");
                        return true;
                    }

                    var playerName = args.get(0);
                    var badgeId = args.get(1);

                    var offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                    var availableBadges = badgeManager.getAllAvailableBadges();

                    var targetBadge = availableBadges.stream().filter(b -> b.identifier.equalsIgnoreCase(badgeId))
                            .findFirst().orElse(null);
                    if (targetBadge == null)
                    {
                        sender.sendMessage("Badge not found");
                        return true;
                    }

                    var data = badgeManager.getPlayerdata(offlinePlayer);

                    if (data.unlockedBadges.contains(badgeId))
                    {
                        sender.sendMessage("They already have!");
                    }
                    else
                    {
                        data.unlockedBadges.add(badgeId);
                        sender.sendMessage("Success.");
                    }

                    return true;
                })

                .startNew()
                .name("set")
                //.permission("")
                .onFilter((sender, args) ->
                {
                    var input = args.size() > 1 ? args.get(0) : "";

                    return badgeManager.getAllAvailableBadges().stream()
                            .filter(badge -> badge.identifier.toLowerCase().startsWith(input.toLowerCase()))
                            .map(b -> b.identifier)
                            .toList();
                })
                .executes((sender, args) ->
                {
                    var targetIdentifier = !args.isEmpty() ? args.get(0) : null;
                    if (targetIdentifier == null)
                    {
                        sender.sendMessage("Null identifier!");
                        return true;
                    }

                    var display = args.size() >= 2 ? getElementfrom(1, args) : " ~ %s ~ ".formatted(targetIdentifier.toUpperCase());
                    var badge = badgeManager.getModifiableBadge(targetIdentifier);
                    if (badge == null)
                    {
                        badgeManager.addBadge(targetIdentifier, display);
                        sender.sendMessage("Added badge '%s' for display '%s'".formatted(targetIdentifier, display));
                        return true;
                    }

                    badge.name = display;
                    sender.sendMessage("Changed display of '%s' to '%s'".formatted(targetIdentifier, display));

                    return true;
                })

                .buildAll();
    }

    private String getElementfrom(int index, List<String> strList)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = index; i < strList.size(); i++)
        {
            if (i != index) builder.append(" ");

            var string = strList.get(i);
            if (string.equalsIgnoreCase("!SPACE"))
                string = " ";

            if (string.equalsIgnoreCase("!NULL"))
                string = "";

            builder.append(string);
        }

        return builder.toString();
    }

    private List<ISubCommand> subCommands;

    @Override
    public List<ISubCommand> getSubCommands()
    {
        return subCommands;
    }

    @Override
    public List<FormattableMessage> getNotes()
    {
        return List.of();
    }

    @Override
    public String getCommandName()
    {
        return "badge";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return new FormattableMessage(XiamoBadges.instance(), "badge");
    }

    @Override
    protected String getPluginNamespace()
    {
        return XiamoBadges.namespace();
    }
}
