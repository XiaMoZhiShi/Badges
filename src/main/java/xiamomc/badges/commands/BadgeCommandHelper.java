package xiamomc.badges.commands;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import xiamomc.badges.XiamoBadges;
import xiamomc.badges.commands.builder.CommandBuilder;
import xiamomc.pluginbase.Command.CommandHelper;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.XiaMoJavaPlugin;

import java.util.List;

public class BadgeCommandHelper extends CommandHelper<XiamoBadges>
{
    public BadgeCommandHelper()
    {
        buildCommands();
    }

    private void buildCommands()
    {
        commands.add(new BadgeCommand());
    }

    private final List<IPluginCommand> commands = new ObjectArrayList<>();

    @Override
    public List<IPluginCommand> getCommands()
    {
        return commands;
    }

    @Override
    protected XiaMoJavaPlugin getPlugin()
    {
        return XiamoBadges.getInstance(XiamoBadges.namespace());
    }

    @Override
    protected String getPluginNamespace()
    {
        return XiamoBadges.namespace();
    }
}
