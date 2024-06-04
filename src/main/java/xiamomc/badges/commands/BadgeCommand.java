package xiamomc.badges.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import xiamomc.badges.BadgeManager;
import xiamomc.badges.XiamoBadges;
import xiamomc.badges.commands.builder.CommandBuilder;
import xiamomc.badges.messages.strings.CommandString;
import xiamomc.badges.messages.strings.CommonString;
import xiamomc.badges.messages.strings.PlayerString;
import xiamomc.badges.misc.TabCompletions;
import xiamomc.badges.utilties.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Command.SubCommandHandler;
import xiamomc.pluginbase.Messages.FormattableMessage;

import javax.swing.*;
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
                .permission("xiamomc.badge.use")
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
                        sender.sendMessage(MessageUtils.prefixes(sender, CommandString.notAPlayer()));
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
                        sender.sendMessage(MessageUtils.prefixes(sender, CommandString.noSuchBadge()));
                        return true;
                    }

                    badgeManager.applyBadge(player.getUniqueId(), match);
                    sender.sendMessage(MessageUtils.prefixes(sender, CommandString.appliedBadge().resolve("id", match)));
                    return true;
                })

                .startNew()
                .name("unload")
                .permission("xiamomc.badge.use")
                .onFilter((sender, args) -> List.of())
                .executes((sender, args) ->
                {
                    if (!(sender instanceof Player player))
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender, CommandString.notAPlayer()));
                        return true;
                    }

                    badgeManager.removeBadge(player.getUniqueId(), null);
                    sender.sendMessage(MessageUtils.prefixes(sender, CommandString.unloadedBadge()));
                    return true;
                })

                .startNew()
                .name("revoke")
                .permission("xiamomc.badge.manage")
                .onFilter((sender, args) ->
                {
                    if (args.size() == 1)
                    {
                        return TabCompletions.filterOnlinePlayers.apply(sender, args);
                    }
                    else if (args.size() == 2)
                    {
                        var input = args.get(1);
                        var offline = Bukkit.getOfflinePlayer(args.get(0));
                        var data = badgeManager.getPlayerdata(offline);

                        return data.unlockedBadges.stream()
                                .filter(id -> id.toLowerCase().startsWith(input.toLowerCase()))
                                .toList();
                    }

                    return List.of();
                })
                .executes((sender, args) ->
                {
                    if (args.size() < 2)
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender, CommandString.notEnoughParameters()));
                        return true;
                    }

                    var playerName = args.get(0);
                    var badgeId = args.get(1);

                    var offlinePlayer = Bukkit.getOfflinePlayer(playerName);

                    FormattableMessage formattable = badgeManager.revokeBadgeFromPlayer(offlinePlayer, badgeId)
                            ? CommandString.revokeSuccess()
                            : CommandString.revokeFail();

                    formattable.resolve("who", playerName)
                            .resolve("id", badgeId);

                    sender.sendMessage(MessageUtils.prefixes(sender, formattable));

                    return true;
                })

                .startNew()
                .name("grant")
                .permission("xiamomc.badge.manage")
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
                        sender.sendMessage(MessageUtils.prefixes(sender, CommandString.notEnoughParameters()));
                        return true;
                    }

                    var playerName = args.get(0);
                    var badgeId = args.get(1);

                    var offlinePlayer = Bukkit.getOfflinePlayer(playerName);

                    var result = badgeManager.grantBadgeToPlayer(offlinePlayer, badgeId);
                    switch (result)
                    {
                        case SUCCESS ->
                        {
                            var message = CommandString.grantSuccess()
                                    .resolve("id", badgeId)
                                    .resolve("who", playerName);

                            sender.sendMessage(MessageUtils.prefixes(sender, message));
                        }
                        case ID_NOT_EXIST -> sender.sendMessage(MessageUtils.prefixes(sender, CommandString.noSuchBadge()));
                        case FAIL ->
                        {
                            var message = CommandString.grantFailed()
                                    .resolve("who", playerName);

                            sender.sendMessage(MessageUtils.prefixes(sender, message));
                        }
                    }

                    return true;
                })

                .startNew()
                .name("set")
                .permission("xiamomc.badge.manage")
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
                        sender.sendMessage(MessageUtils.prefixes(sender, CommandString.badgeNotSpecified()));
                        return true;
                    }

                    var display = args.size() >= 2 ? getElementfrom(1, args) : " ~ %s ~ ".formatted(targetIdentifier.toUpperCase());
                    var badge = badgeManager.getModifiableBadgeData(targetIdentifier);
                    if (badge == null)
                    {
                        badgeManager.registerBadge(targetIdentifier, display);

                        var formattable = CommandString.addBadgeSuccess()
                                .resolve("id", targetIdentifier)
                                .resolve("display", display);

                        sender.sendMessage(MessageUtils.prefixes(sender, formattable));
                        return true;
                    }

                    badge.name = display;
                    badgeManager.invokeSaveBadgeConfiguration();

                    var formattable = CommandString.changedBadgeDisplay()
                            .resolve("id", targetIdentifier)
                            .resolve("display", display);

                    sender.sendMessage(MessageUtils.prefixes(sender, formattable));

                    return true;
                })

                .startNew()
                .name("unset")
                .permission("xiamomc.badge.manage")
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
                    if (args.isEmpty())
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender, CommandString.badgeNotSpecified()));
                        return true;
                    }

                    var id = args.get(0);
                    badgeManager.unregisterBadge(id);
                    sender.sendMessage(MessageUtils.prefixes(sender, CommandString.unsetSuccess().resolve("id", id)));

                    return true;
                })

                .startNew()
                .name("list")
                .permission("xiamomc.badge.use")
                .executes((sender, args) ->
                {
                    if (args.isEmpty() && !(sender instanceof Player))
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender, CommandString.playerNotSpecified()));
                        return true;
                    }

                    var targetPlayerName = args.isEmpty() ? sender.getName() : args.get(0);

                    var player = Bukkit.getOfflinePlayer(targetPlayerName);
                    var data = badgeManager.getPlayerdata(player);

                    var message = CommandString.unlockedBadgeHeader().resolve("player", targetPlayerName);
                    sender.sendMessage(MessageUtils.prefixes(sender, message));
                    data.unlockedBadges.forEach(id ->
                    {
                        var formattable = CommonString.badgeDisplay();

                        var badge = badgeManager.getModifiableBadgeData(id);
                        formattable.resolve("id", id)
                                .resolve("display", badge == null ? "<italic>???</italic>" : badge.name);

                        sender.sendMessage(MessageUtils.prefixes(sender, formattable));
                    });


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
            string = string.replace("!SPACE", " ").replace("!NULL", "");

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
