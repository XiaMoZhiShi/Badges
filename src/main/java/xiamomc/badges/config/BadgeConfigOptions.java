package xiamomc.badges.config;

import xiamomc.pluginbase.Configuration.ConfigNode;
import xiamomc.pluginbase.Configuration.ConfigOption;

public class BadgeConfigOptions
{
    public static ConfigOption<String> MESSAGE_PREFIX = new ConfigOption<>(rootNode().append("prefix"), "<message>");

    public static ConfigNode rootNode()
    {
        return ConfigNode.create("root");
    };
}
