package xiamomc.badges.messages.strings;

import xiamomc.badges.XiamoBadges;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xiamomc.pluginbase.Messages.IStrings;

public abstract class AbstractStrings implements IStrings
{
    private static final String nameSpace = XiamoBadges.namespace();

    protected static FormattableMessage getFormattable(String key, String defaultValue)
    {
        return new FormattableMessage(nameSpace, key, defaultValue);
    }
}
