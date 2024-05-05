package xiamomc.badges.messages;

import org.jetbrains.annotations.NotNull;
import xiamomc.badges.XiamoBadges;
import xiamomc.pluginbase.Messages.IStrings;
import xiamomc.pluginbase.Messages.MessageStore;

import java.util.List;
import java.util.Map;

public class BadgeMessageSubStore extends MessageStore<XiamoBadges>
{
    public BadgeMessageSubStore(String locale, List<Class<? extends IStrings>> strings, BadgeMessageStore parentStore)
    {
        this.locale = locale;
        this.strings = strings;

        this.parentStore = parentStore;

        var path = PluginAssetUtils.langPath(locale);
        var asset = PluginAssetUtils.getFileStrings(path);

        assets = createGson().fromJson(asset, this.storingObject.getClass());
    }

    private final List<Class<? extends IStrings>> strings;

    private final String locale;

    private final BadgeMessageStore parentStore;

    public void overWriteNonDefaultAfterReload()
    {
        this.overwriteNonDef = true;
    }

    private boolean overwriteNonDef;

    private final Map<String, String> assets;

    @Override
    public void addMissingStrings()
    {
        //从插件资源获取
        var path = PluginAssetUtils.langPath(locale);
        var asset = PluginAssetUtils.getFileStrings(path);

        if (!asset.isEmpty() && !asset.isBlank())
        {
            try
            {
                var defaults = createGson().fromJson(asset, this.storingObject.getClass());
                defaults.forEach((o1, o2) ->
                {
                    if (o1 instanceof String key && o2 instanceof String msg)
                    {
                        if (!storingObject.containsKey(key)
                                || (overwriteNonDef && !storingObject.getOrDefault(key, "").equals(msg)))
                            storingObject.put(key, msg);
                    }
                });
            }
            catch (Throwable t)
            {
                logger.error("Error occurred while updating localization for locale '" + locale + "': " + t.getMessage());
                t.printStackTrace();
            }
        }

        saveConfiguration();
    }

    @Override
    protected List<Class<? extends IStrings>> getStrings()
    {
        return strings;
    }

    @Override
    protected @NotNull String getFileName()
    {
        return "messages/" + locale + ".json";
    }

    @Override
    protected String getPluginNamespace()
    {
        return XiamoBadges.namespace();
    }
}
