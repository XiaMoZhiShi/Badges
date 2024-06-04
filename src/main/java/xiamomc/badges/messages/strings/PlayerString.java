package xiamomc.badges.messages.strings;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class PlayerString extends AbstractStrings
{
    private static String getKey(String key)
    {
        return "player." + key;
    }

    public static FormattableMessage unlockedBadgeNotify()
    {
        return getFormattable(getKey("unlocked_badge_notify"), "你解锁了一个新的前缀：<display>");
    }

    public static FormattableMessage unlockedBadgeUsage()
    {
        return getFormattable(getKey("unlocked_badge_usage"), "使用 <command> 来激活。");
    }
}
