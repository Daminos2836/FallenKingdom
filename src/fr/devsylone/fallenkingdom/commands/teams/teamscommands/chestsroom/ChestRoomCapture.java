package fr.devsylone.fallenkingdom.commands.teams.teamscommands.chestsroom;

import fr.devsylone.fallenkingdom.Fk;
import fr.devsylone.fallenkingdom.commands.ArgumentParser;
import fr.devsylone.fallenkingdom.commands.abstraction.CommandPermission;
import fr.devsylone.fallenkingdom.commands.abstraction.CommandResult;
import fr.devsylone.fallenkingdom.commands.abstraction.FkCommand;
import fr.devsylone.fallenkingdom.exception.FkLightException;
import fr.devsylone.fallenkingdom.game.Game;
import fr.devsylone.fallenkingdom.utils.Messages;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ChestRoomCapture extends FkCommand {
    public ChestRoomCapture() {
        super("captureTime", "<i:sec>", Messages.CMD_MAP_CHEST_ROOM_CAPTURE, CommandPermission.ADMIN);
    }

    @Override
    public CommandResult execute(Fk plugin, CommandSender sender, List<String> args, String label)
    {
        if(plugin.getGame().getState() != Game.GameState.BEFORE_STARTING)
            throw new FkLightException(Messages.CMD_ERROR_CHEST_ROOM_STARTED);

        int time = ArgumentParser.parsePositiveInt(args.get(0), false, Messages.CMD_ERROR_CHEST_ROOM_CAPTURE_TIME_FORMAT);

        plugin.getFkPI().getChestsRoomsManager().setCaptureTime(time);
        broadcast(Messages.CMD_TEAM_CHEST_ROOM_CAPTURE_TIME.getMessage()
                .replace("%time%", args.get(0))
                .replace("%unit%", Messages.Unit.SECONDS.tl(time))
        );
        return CommandResult.SUCCESS;
    }
}
