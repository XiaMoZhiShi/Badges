package xiamomc.badges.messages.strings;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class CommonString extends AbstractStrings
{
    private static String getKey(String key)
    {
        return "common." + key;
    }


    public static FormattableMessage badgeDisplay()
    {
        return getFormattable(getKey("badge_display"), "<id> —— <display>");
    }
}
