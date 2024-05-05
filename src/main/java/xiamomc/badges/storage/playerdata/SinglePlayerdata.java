package xiamomc.badges.storage.playerdata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class SinglePlayerdata
{
    @Expose
    public UUID uuid;

    @Expose
    @SerializedName("badgeIdentifier")
    @Nullable
    public String currentBadge = null;

    @Expose
    public List<String> unlockedBadges = new ObjectArrayList<>();
}
