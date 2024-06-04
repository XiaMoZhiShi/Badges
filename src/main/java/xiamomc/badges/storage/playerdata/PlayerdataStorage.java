package xiamomc.badges.storage.playerdata;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.badges.storage.BStorage;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerdataStorage extends BStorage<PlayerdataRoot>
{
    @Override
    protected @NotNull String getFileName()
    {
        return "playerdata.json";
    }

    @Override
    protected @NotNull PlayerdataRoot createDefault()
    {
        return new PlayerdataRoot();
    }

    @Override
    protected @NotNull String getDisplayName() {
        return "Player data storage";
    }

    @NotNull
    public synchronized SinglePlayerdata getData(UUID uuid)
    {
        var storing = this.storingObject;

        synchronized (storing)
        {
            var data = storing.data
                    .stream()
                    .filter(d -> uuid.equals(d.uuid))
                    .findFirst()
                    .orElse(null);

            if (data != null) return data;

            var newData = new SinglePlayerdata();
            newData.uuid = uuid;

            storing.data.add(newData);

            return newData;
        }
    }

    public SinglePlayerdata getData(OfflinePlayer offlinePlayer)
    {
        return getData(offlinePlayer.getUniqueId());
    }

    public SinglePlayerdata getData(Player player)
    {
        return getData(player.getUniqueId());
    }

    public CompletableFuture<SinglePlayerdata> getDataAsync(UUID uuid)
    {
        return CompletableFuture.supplyAsync(() -> getData(uuid));
    }

    public List<SinglePlayerdata> getAllModifiableData()
    {
        var storing = this.storingObject;

        synchronized (storing)
        {
            return new ObjectArrayList<>(storing.data);
        }
    }
}
