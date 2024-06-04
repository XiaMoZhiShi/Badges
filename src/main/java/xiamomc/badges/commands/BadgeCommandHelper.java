package xiamomc.badges.commands;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import xiamomc.badges.XiamoBadges;
import xiamomc.pluginbase.Command.CommandHelper;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.XiaMoJavaPlugin;

import java.util.List;
import java.util.Objects;

public class BadgeCommandHelper extends CommandHelper<XiamoBadges>
{
    public BadgeCommandHelper()
    {
        buildCommands();
    }

    @Override
    public boolean registerCommand(IPluginCommand command)
    {
        if (Objects.equals(command.getCommandName(), ""))
            throw new IllegalArgumentException("Trying to register a command with empty basename!");

        var cmd = Bukkit.getPluginCommand(command.getCommandName());

        if (cmd == null)
            throw new NullPointerException("'%s' doesn't have a command defined in the server.".formatted(command.getCommandName()));

        if (cmd.getExecutor().equals(this.getPlugin()))
        {
            cmd.setExecutor(command);
            cmd.setTabCompleter(new BadgeTabCompleter(command));
            return true;
        }
        else
        {
            logger.warn("Ignoring command '%s' that doesn't belongs to us.".formatted(command.getCommandName()));
            return false;
        }
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
