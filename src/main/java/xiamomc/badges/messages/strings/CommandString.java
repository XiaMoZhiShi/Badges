package xiamomc.badges.messages.strings;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class CommandString extends AbstractStrings
{
    private static String getKey(String key)
    {
        return "command." + key;
    }

    public static FormattableMessage notEnoughParameters()
    {
        return getFormattable(getKey("not_enough_parameters"), "<red>参数不足");
    }

    public static FormattableMessage notAPlayer()
    {
        return getFormattable(getKey("not_a_player"), "<red>必须作为玩家执行指令。");
    }

    public static FormattableMessage playerNotSpecified()
    {
        return getFormattable(getKey("player_not_specified"), "<red>未指定玩家");
    }

    public static FormattableMessage badgeNotSpecified()
    {
        return getFormattable(getKey("badge_not_spcified"), "<red>未指定前缀");
    }

    public static FormattableMessage unsetSuccess()
    {
        return getFormattable(getKey("unset_success"), "成功移除前缀 '<id>'");
    }

    public static FormattableMessage unlockedBadgeHeader()
    {
        return getFormattable(getKey("unlocked_badge_header"), "<player>当前已解锁的所有前缀：");
    }

    public static FormattableMessage addBadgeSuccess()
    {
        return getFormattable(getKey("add_badge_success"), "成功添加ID为'<id>'的前缀'<display>'");
    }

    public static FormattableMessage changedBadgeDisplay()
    {
        return getFormattable(getKey("changed_badge_display"), "已更改<id>的显示为'<display>'");
    }

    public static FormattableMessage grantSuccess()
    {
        return getFormattable(getKey("grant_success"), "成功将<id>给与<who>");
    }

    public static FormattableMessage grantFailed()
    {
        return getFormattable(getKey("grant_fail"), "<red>未能将前缀给与<who>！");
    }

    public static FormattableMessage noSuchBadge()
    {
        return getFormattable(getKey("no_such_badge"), "<red>无此前缀");
    }

    public static FormattableMessage revokeSuccess()
    {
        return getFormattable(getKey("revoke_success"), "成功剥夺<who>的<id>前缀");
    }

    public static FormattableMessage revokeFail()
    {
        return getFormattable(getKey("revoke_fail"), "<red>未能移除<who>的<id>前缀");
    }

    public static FormattableMessage unloadedBadge()
    {
        return getFormattable(getKey("unloaded_badge"), "已卸下前缀");
    }

    public static FormattableMessage appliedBadge()
    {
        return getFormattable(getKey("applied_badge"), "已应用前缀 <id>");
    }
}
