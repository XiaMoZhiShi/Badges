package xiamomc.badges.storage.playerdata;

import com.google.gson.annotations.Expose;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class PlayerdataRoot
{
    @Expose
    public List<SinglePlayerdata> data = new ObjectArrayList<>();
}
