package xiamomc.badges;

import org.bukkit.Bukkit;
import xiamomc.badges.commands.BadgeCommandHelper;
import xiamomc.badges.events.BadgeEventListener;
import xiamomc.pluginbase.Command.CommandHelper;
import xiamomc.pluginbase.XiaMoJavaPlugin;

public final class XiamoBadges extends XiaMoJavaPlugin
{
    public XiamoBadges()
    {
        instance = this;
    }

    public static String namespace()
    {
        return "xmbadges";
    }

    public static XiamoBadges instance()
    {
        return instance;
    }

    private static XiamoBadges instance;

    @Override
    public String getNameSpace()
    {
        return namespace();
    }

    private BadgeManager badgeManager;

    @Override
    public void onEnable()
    {
        super.onEnable();

        // Plugin startup logic
        dependencyManager.cache(badgeManager = new BadgeManager());

        dependencyManager.cacheAs(CommandHelper.class, new BadgeCommandHelper());

        Bukkit.getPluginManager().registerEvents(new BadgeEventListener(), this);
    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        // Plugin shutdown logic

        if (badgeManager != null)
            badgeManager.saveConfigurations();
    }

    //region Folia compat

    @Override
    public void startMainLoop(Runnable r)
    {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, o -> r.run(), 1, 1);
    }

    @Override
    public void runAsync(Runnable r)
    {
        Bukkit.getAsyncScheduler().runNow(this, o -> r.run());
    }

    //endregion Folia compat
}
