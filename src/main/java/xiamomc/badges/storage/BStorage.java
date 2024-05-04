package xiamomc.badges.storage;

import xiamomc.badges.XiamoBadges;
import xiamomc.pluginbase.JsonBasedStorage;

public abstract class BStorage<T> extends JsonBasedStorage<T, XiamoBadges>
{
    @Override
    protected String getPluginNamespace()
    {
        return XiamoBadges.namespace();
    }
}
