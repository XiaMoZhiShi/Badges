package xiamomc.badges.misc;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiFunction;

public class TabCompletions
{
    public static BiFunction<CommandSender, List<String>, List<String>> filterOnlinePlayers = (sender, args) ->
    {
        var name = args.size() > 1 ? args.get(0) : "";
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(pName -> pName.toLowerCase().startsWith(name.toLowerCase()))
                .toList();
    };
}
