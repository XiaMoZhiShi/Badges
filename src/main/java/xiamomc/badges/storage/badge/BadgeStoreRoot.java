package xiamomc.badges.storage.badge;

import com.google.gson.annotations.Expose;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class BadgeStoreRoot
{
    @Expose
    public List<StoredBadge> badges = new ObjectArrayList<>();
}
