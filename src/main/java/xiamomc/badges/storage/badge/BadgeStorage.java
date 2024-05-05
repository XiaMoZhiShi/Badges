package xiamomc.badges.storage.badge;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.badges.XiamoBadges;
import xiamomc.badges.storage.BStorage;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    //region R/W Lock
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    /**
     *
     * @return 操作是否成功
     */
    private boolean lockRead()
    {
        boolean locked = false;

        try
        {
            locked = readLock.tryLock(100, TimeUnit.MILLISECONDS);
        }
        catch (Throwable t)
        {
            logger.error("Failed to lock read: " + t.getMessage());
        }

        if (!locked)
        {
            logger.error("Failed to lock read: Timed out");
            return false;
        }

        return true;
    }

    private void unlockRead()
    {
        readLock.unlock();
    }

    /**
     *
     * @return 操作是否成功
     */
    private boolean lockWrite()
    {
        boolean locked = false;

        try
        {
            locked = writeLock.tryLock(100, TimeUnit.MILLISECONDS);
        }
        catch (Throwable t)
        {
            logger.error("Failed to lock writeLock: " + t.getMessage());
        }

        if (!locked)
        {
            logger.error("Failed to lock writeLock: Timed out");

            return false;
        }

        return true;
    }

    private void unlockWrite()
    {
        writeLock.unlock();
    }

    //endregion R/W Lock

    public List<StoredBadge> getAllStored()
    {
        return new ObjectArrayList<>(storingObject.badges);
    }

    public List<StoredBadge> getAllStoredCopy()
    {
        var list = new ObjectArrayList<StoredBadge>();

        storingObject.badges.forEach(stored -> list.add(stored.clone()));

        return list;
    }

    public boolean contains(String id)
    {
        return storingObject.badges.stream().anyMatch(stored -> stored.identifier.equalsIgnoreCase(id));
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

    public boolean add(@NotNull String identifier, @NotNull String name)
    {
        if (!lockWrite())
            return false;

        try
        {
            var instance = new StoredBadge();
            instance.identifier = identifier;
            instance.name = name;

            storingObject.badges.add(instance);

            saveConfiguration();
        }
        catch (Throwable t)
        {
            logger.error("Failed to add badge: " + t.getMessage());
            t.printStackTrace();

            return false;
        }
        finally
        {
            unlockWrite();
        }

        return true;
    }

    public boolean remove(String identifier)
    {
        if (!lockWrite())
            return false;

        try
        {
            storingObject.badges.removeIf(stored -> stored.identifier.equalsIgnoreCase(identifier));
            saveConfiguration();
        }
        catch (Throwable t)
        {
            logger.error("Failed to remove badge: " + t.getMessage());
            t.printStackTrace();

            return false;
        }
        finally
        {
            unlockWrite();
        }

        return true;
    }
}
