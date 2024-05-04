package xiamomc.badges.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.TabCompleteEvent;
import xiamomc.badges.BadgeManager;
import xiamomc.badges.BadgePluginObject;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.CommandHelper;

public class BadgeEventListener extends BadgePluginObject implements Listener
{
    @Resolved(shouldSolveImmediately = true, allowNull = false)
    private BadgeManager badgeManager;

    @Initializer
    private void load()
    {
        logger.warn("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        var dataFuture = badgeManager.getPlayerdataAsync(e.getPlayer());

        dataFuture.thenAccept(data ->
        {
            badgeManager.applyToLuckPerms(e.getPlayer().getUniqueId(), data.badgeIdentifier);
        });
    }

    @Resolved
    private CommandHelper<?> helper;

    @EventHandler
    public void onTabComplete(TabCompleteEvent e)
    {
        if (e.isCancelled()) return;

        var result = helper.onTabComplete(e.getBuffer(), e.getSender());
        if (result != null) e.setCompletions(result);
    }
}
