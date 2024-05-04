package xiamomc.badges.storage.playerdata;

import com.google.gson.annotations.Expose;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class SinglePlayerdata
{
    @Expose
    public UUID uuid;

    @Expose
    @Nullable
    public String badgeIdentifier = null;

    @Expose
    public List<String> unlockedBadges = new ObjectArrayList<>();
}
