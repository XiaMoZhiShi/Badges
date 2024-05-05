package xiamomc.badges.messages;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.badges.XiamoBadges;
import xiamomc.badges.messages.strings.PlayerString;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Messages.IStrings;
import xiamomc.pluginbase.Messages.MessageStore;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class BadgeMessageStore extends MessageStore<XiamoBadges>
{
    private final List<Class<? extends IStrings>> strings = ObjectList.of(
            PlayerString.class
    );

    private final Map<String, BadgeMessageSubStore> subStores = new Object2ObjectOpenHashMap<>();

    private final Bindable<String> serverLanguage = new Bindable<>();

    public File getFile(String relativePath, boolean nullIfNotFound)
    {
        if (relativePath == null || relativePath.isBlank() || relativePath.isEmpty()) return null;

        var dataFolder = plugin.getDataFolder().toURI();
        var file = new File(URI.create("" + dataFolder + "/" + relativePath));

        if (!nullIfNotFound) return file;

        if (file.exists()) return file;
        return null;
    }

    @Nullable
    @Contract("null -> null; !null -> !null")
    public String getAbsolutePath(String relativePath)
    {
        if (relativePath == null || relativePath.isBlank() || relativePath.isEmpty()) return null;

        return plugin.getDataFolder().toURI().getPath() + "/" + relativePath;
    }

    @Initializer
    private void load()
    {
        var legacyDefaultFile = this.getFile("messages/default.json", true);

        if (legacyDefaultFile != null)
        {
            var targetFile = this.getFile(this.getFileName(), false);

            if (targetFile.exists()) targetFile.delete();

            try
            {
                Files.move(legacyDefaultFile.toPath(), targetFile.toPath());
                this.addSchedule(super::reloadConfiguration);
            }
            catch (Throwable t)
            {
                logger.error("Unable to update builtin message store: %s".formatted(t));
            }
        }

        serverLanguage.set("zh_cn");
        //config.bind(serverLanguage, ConfigOption.LANGUAGE_CODE);
    }

    private BadgeMessageSubStore getOrCreateSubStore(String locale)
    {
        BadgeMessageSubStore store;

        synchronized (subStores)
        {
            store = subStores.getOrDefault(locale, null);

            if (store != null) return store;

            store = new BadgeMessageSubStore(locale, strings, this);
            store.initializeStorage();
            store.saveConfiguration();

            subStores.put(locale, store);
        }

        return store;
    }

    public Map<String, String> getAllMessages()
    {
        return storingObject;
    }

    @Override
    protected @NotNull String getFileName()
    {
        return "messages/template.json";
    }

    @Override
    public String get(String key, @Nullable String defaultValue, @Nullable String locale)
    {
        var serverLanguage = this.serverLanguage.get();

        if (locale == null || locale.isBlank() || locale.isEmpty())
        {
            logger.warn("Resolving message key " + key + " for null or empty locale");
            locale = serverLanguage;
        }

        var messageStores = ObjectArrayList.of(
                this.getOrCreateSubStore("override"),
                this.getOrCreateSubStore(locale)
        );

        if (!locale.equals(serverLanguage))
            messageStores.add(this.getOrCreateSubStore(serverLanguage));

        messageStores.add(this.getOrCreateSubStore("en_us"));

        for (var store : messageStores)
        {
            var msg = store.get(key, null, null);
            if (msg != null) return msg;
        }

        return defaultValue == null ? "%s@%s".formatted(key, locale) : defaultValue;
    }

    public boolean reloadOverwriteNonDefault()
    {
        this.oWNonDef = true;

        return reloadConfiguration();
    }

    private boolean oWNonDef;

    @Override
    public boolean reloadConfiguration()
    {
        var allSuccess = new AtomicBoolean(true);

        subStores.forEach((l, s) ->
        {
            if (oWNonDef)
                s.overWriteNonDefaultAfterReload();

            if (!s.reloadConfiguration()) allSuccess.set(false);
        });

        oWNonDef = false;

        return super.reloadConfiguration() && allSuccess.get();
    }

    @Override
    protected List<Class<? extends IStrings>> getStrings()
    {
        return strings;
    }

    @Override
    protected String getPluginNamespace()
    {
        return XiamoBadges.namespace();
    }
}
