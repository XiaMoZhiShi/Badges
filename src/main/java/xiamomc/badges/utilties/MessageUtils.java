package xiamomc.badges.utilties;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.badges.BadgePluginObject;
import xiamomc.badges.XiamoBadges;
import xiamomc.badges.config.BadgeConfigManager;
import xiamomc.badges.config.BadgeConfigOptions;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Managers.DependencyManager;
import xiamomc.pluginbase.Messages.FormattableMessage;

public class MessageUtils extends BadgePluginObject
{
    private static void setupConfigManager()
    {
        if (pluginDepMgr == null)
            pluginDepMgr = DependencyManager.getInstance(XiamoBadges.namespace());

        config = pluginDepMgr.get(BadgeConfigManager.class);
        plugin = XiamoBadges.instance();
    }

    private static DependencyManager pluginDepMgr;
    private static BadgeConfigManager config;
    private static XiamoBadges plugin;

    public static Component prefixes(CommandSender sender, Component[] c)
    {
        if (config == null)
            setupConfigManager();

        if (!(sender instanceof Player))
            return Component.translatable("%s", c);

        var finalComponent = Component.empty();

        for (var cc : c)
            finalComponent = finalComponent.append(cc);

        var prefix = new FormattableMessage(plugin, config.getOrDefault(String.class, BadgeConfigOptions.MESSAGE_PREFIX));

        return prefix
                .withLocale(getLocale(sender))
                .resolve("message", finalComponent)
                .toComponent(null);
    }

    public static Component prefixes(CommandSender sender, String str)
    {
        return prefixes(sender, Component.text(str));
    }

    public static Component prefixes(CommandSender sender, Component c)
    {
        return prefixes(sender, new Component[]{c});
    }

    public static Component prefixes(CommandSender sender, FormattableMessage formattable)
    {
        if (formattable.getLocale() == null)
            formattable.withLocale(getLocale(sender));

        return prefixes(sender, formattable.toComponent(null));
    }

    @NotNull
    public static String getLocale(Player player)
    {
        if (isSingleLanguage())
            return getServerLocale();

        var nmsLocale = player.locale().getISO3Language();

        return nmsLocale == null ? getServerLocale() : nmsLocale.toLowerCase().replace('-', '_');
    }

    @NotNull
    public static String getLocaleOr(CommandSender sender, @NotNull String defaultValue)
    {
        var locale = getLocale(sender);
        return locale == null ? defaultValue : locale;
    }

    private static BadgeConfigManager configManager;

    private static void initializeConfigManager()
    {
        if (configManager != null) return;

        var depMgr = DependencyManager.getInstance(XiamoBadges.namespace());
        var config = depMgr.get(BadgeConfigManager.class);

        if (config != null)
        {
            //config.bind(serverLocale, FConfigOptions.LANGUAGE_CODE);
            //config.bind(singleLanguage, FConfigOptions.SINGLE_LANGUAGE);
        }

        configManager = config;
    }

    private final static Bindable<String> serverLocale = new Bindable<>("zh_cn");
    private final static Bindable<Boolean> singleLanguage = new Bindable<>(true);

    public static String getServerLocale()
    {
        initializeConfigManager();

        return serverLocale.get();
    }

    public static boolean isSingleLanguage()
    {
        initializeConfigManager();

        return singleLanguage.get();
    }

    @Nullable
    public static String getLocale(CommandSender sender)
    {
        if (sender instanceof Player player && !isSingleLanguage())
            return getLocale(player);
        else
            return getServerLocale();
    }
}
