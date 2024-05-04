package xiamomc.badges.storage.badge;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.badges.XiamoBadges;
import xiamomc.badges.storage.BStorage;

import java.util.List;

public class BadgeStorage extends BStorage<BadgeStoreRoot>
{
    @Override
    protected @NotNull String getFileName()
    {
        return "badges.json";
    }

    @Override
    protected @NotNull BadgeStoreRoot createDefault()
    {
        return new BadgeStoreRoot();
    }

    @Override
    protected @NotNull String getDisplayName()
    {
        return "Badge store";
    }

    @Override
    protected String getPluginNamespace()
    {
        return XiamoBadges.namespace();
    }

    public List<StoredBadge> getAllStored()
    {
        return new ObjectArrayList<>(storingObject.badges);
    }

    @Nullable
    public StoredBadge getStored(String identifier)
    {
        if (identifier == null)
            return null;

        return storingObject.badges
                .stream()
                .filter(b -> identifier.equalsIgnoreCase(b.identifier))
                .findFirst().orElse(null);
    }

    public void add(@NotNull String identifier, @NotNull String name)
    {
        var instance = new StoredBadge();
        instance.identifier = identifier;
        instance.name = name;

        storingObject.badges.add(instance);

        saveConfiguration();
    }

    public void remove()
    {
        saveConfiguration();
    }
}
