package xiamomc.badges;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import xiamomc.badges.messages.strings.PlayerString;
import xiamomc.badges.storage.badge.BadgeStorage;
import xiamomc.badges.storage.badge.StoredBadge;
import xiamomc.badges.storage.playerdata.PlayerdataStorage;
import xiamomc.badges.storage.playerdata.SinglePlayerdata;
import xiamomc.badges.utilties.MessageUtils;

import java.util.List;
import java.util.Map;
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

    public void applyBadge(UUID uuid, @NotNull String identifier)
    {
        var playerData = playerdataStorage.getData(uuid);
        playerData.currentBadge = identifier;

        applyBadgeToLuckPerms(uuid, identifier);
    }

    /**
     *
     * @param uuid 目标玩家的UUID
     * @param targetId 要移除的前缀，若为null则移除当前前缀
     */
    public void removeBadge(UUID uuid, @Nullable String targetId)
    {
        var data = playerdataStorage.getData(uuid);
        if (targetId == null || targetId.equalsIgnoreCase(data.currentBadge))
            data.currentBadge = null;

        applyBadgeToLuckPerms(uuid, null);
    }

    public void refreshBadge(Player player)
    {
        var dataFuture = getPlayerdataAsync(player);

        dataFuture.thenAccept(data ->
        {
            applyBadgeToLuckPerms(player.getUniqueId(), data.currentBadge);
        });
    }

    private void applyBadgeToLuckPerms(UUID uuid, @Nullable String identifier)
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

    /**
     *
     * @param identifier
     * @param display
     * @return 操作是否成功
     */
    public boolean registerBadge(@NotNull String identifier, @NotNull String display)
    {
        identifier = identifier.toLowerCase();

        if (badgeStorage.getStored(identifier) != null) return false;

        badgeStorage.add(identifier, display);
        return true;
    }

    public boolean unregisterBadge(@NotNull String identifier)
    {
        identifier = identifier.toLowerCase();

        var badge = badgeStorage.getStored(identifier);
        if (badge == null) return true;

        badgeStorage.remove(identifier);

        // 解除所有正装备此前缀的玩家的前缀
        String idFinal = identifier;
        CompletableFuture.runAsync(() ->
        {
            Map<OfflinePlayer, String> players = new Object2ObjectArrayMap<>();

            playerdataStorage.getAllModifiableData()
                    .forEach(data ->
                    {
                        if (data.unlockedBadges.contains(idFinal))
                        {
                            players.put(Bukkit.getOfflinePlayer(data.uuid), idFinal);
                            data.unlockedBadges.remove(idFinal);
                        }
                    });

            this.addSchedule(() -> players.forEach((p, id) -> this.removeBadge(p.getUniqueId(), id)));
        });
        return true;
    }

    public enum GrantResult
    {
        SUCCESS,
        FAIL,
        ID_NOT_EXIST
    }

    /**
     *
     * @param offline
     * @param badgeIdentifier
     * @return Whether this operation was successful
     */
    public GrantResult grantBadgeToPlayer(OfflinePlayer offline, String badgeIdentifier)
    {
        badgeIdentifier = badgeIdentifier.toLowerCase();

        if (!badgeStorage.contains(badgeIdentifier))
            return GrantResult.ID_NOT_EXIST;

        var data = playerdataStorage.getData(offline.getUniqueId());

        synchronized (data)
        {
            if (data.unlockedBadges.contains(badgeIdentifier))
                return GrantResult.SUCCESS;

            data.unlockedBadges.add(badgeIdentifier);
        }

        var player = offline.getPlayer();
        if (player != null)
        {
            var badge = getModifiableBadgeData(badgeIdentifier);
            var message = PlayerString.unlockedBadgeNotify()
                    .resolve("display", badge == null ? "<italic>???</italic>" : badge.name);

            player.sendMessage(MessageUtils.prefixes(player, message));

            var usage = PlayerString.unlockedBadgeUsage()
                    .resolve("command", "/badge use %s".formatted(badgeIdentifier));

            player.sendMessage(MessageUtils.prefixes(player, usage));
        }

        return GrantResult.SUCCESS;
    }

    public boolean revokeBadgeFromPlayer(OfflinePlayer player, String badgeIdentifier)
    {
        badgeIdentifier = badgeIdentifier.toLowerCase();

        var data = playerdataStorage.getData(player.getUniqueId());

        synchronized (data)
        {
            data.unlockedBadges.remove(badgeIdentifier);

            // 如果当前正在佩戴，那么取消佩戴此Badge
            if (badgeIdentifier.equals(data.currentBadge))
            {
                data.currentBadge = null;
                removeBadge(player.getUniqueId(), badgeIdentifier);
            }

        }

        return true;
    }

    public void invokeSaveBadgeConfiguration()
    {
        badgeStorage.saveConfiguration();
    }

    @Nullable
    public StoredBadge getModifiableBadgeData(String identifier)
    {
        return badgeStorage.getStored(identifier);
    }

    @Unmodifiable
    public List<StoredBadge> getAllAvailableBadges()
    {
        return badgeStorage.getAllStoredCopy();
    }

    public void saveConfigurations()
    {
        this.badgeStorage.saveConfiguration();
        this.playerdataStorage.saveConfiguration();
    }
}
