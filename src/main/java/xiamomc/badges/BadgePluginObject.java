package xiamomc.badges;

import xiamomc.pluginbase.PluginObject;

public class BadgePluginObject extends PluginObject<XiamoBadges>
{
    @Override
    protected String getPluginNamespace()
    {
        return XiamoBadges.namespace();
    }
}
