package xiamomc.badges;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.badges.storage.badge.BadgeStorage;
import xiamomc.badges.storage.badge.StoredBadge;
import xiamomc.badges.storage.playerdata.PlayerdataStorage;
import xiamomc.badges.storage.playerdata.SinglePlayerdata;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BadgeManager extends BadgePluginObject
{
    private final BadgeStorage badgeStorage;
    private final PlayerdataStorage playerdataStorage;

    public BadgeManager()
    {
        this.badgeStorage = new BadgeStorage();
        this.playerdataStorage = new PlayerdataStorage();

        badgeStorage.initializeStorage();
        playerdataStorage.initializeStorage();
    }

    public SinglePlayerdata getPlayerdata(OfflinePlayer offlinePlayer)
    {
        return playerdataStorage.getData(offlinePlayer);
    }

    public SinglePlayerdata getPlayerdata(Player player)
    {
        return playerdataStorage.getData(player);
    }

    public CompletableFuture<SinglePlayerdata> getPlayerdataAsync(Player player)
    {
        return playerdataStorage.getDataAsync(player.getUniqueId());
    }

    public void applyBadge(UUID uuid, @Nullable String identifier)
    {
        var playerData = playerdataStorage.getData(uuid);
        playerData.badgeIdentifier = identifier;

        applyToLuckPerms(uuid, identifier);
    }

    public void applyToLuckPerms(UUID uuid, @Nullable String identifier)
    {
        logger.info("UUId is " + uuid + " :: id is " + identifier);
        var userManager = LuckPermsProvider.get().getUserManager();

        userManager.loadUser(uuid).thenAccept(luckUser ->
        {
            if (luckUser == null)
            {
                logger.warn("Null user for UUID " + uuid + "! Badge will not apply to LP");
                return;
            }

            var data = luckUser.data();
            var badge = badgeStorage.getStored(identifier);
            var badgeDisplay = badge != null ? badge.name : "";

            data.clear(NodeType.PREFIX.predicate());

            logger.info("Badge display is " + badgeDisplay);

            var metaNode = PrefixNode.builder()
                    .prefix(badgeDisplay)
                    .priority(100)
                    .expiry(0)
                    .build();

            var result = data.add(metaNode);
            if (!result.wasSuccessful())
                logger.error("Not success!");

            LuckPermsProvider.get().getUserManager().saveUser(luckUser);
        });
    }

    @Nullable
    public StoredBadge getModifiableBadge(String identifier)
    {
        return badgeStorage.getStored(identifier);
    }

    /**
     *
     * @param identifier
     * @param display
     * @return 操作是否成功
     */
    public boolean addBadge(@NotNull String identifier, @NotNull String display)
    {
        if (badgeStorage.getStored(identifier) != null) return false;

        badgeStorage.add(identifier, display);
        return true;
    }

    public List<StoredBadge> getAllAvailableBadges()
    {
        return badgeStorage.getAllStored();
    }

    public void saveConfigurations()
    {
        this.badgeStorage.saveConfiguration();
        this.playerdataStorage.saveConfiguration();
    }
}
