package fr.devsylone.fallenkingdom.commands.teams.teamscommands;

import fr.devsylone.fallenkingdom.utils.Messages;
import org.bukkit.entity.Player;

import fr.devsylone.fallenkingdom.Fk;
import fr.devsylone.fallenkingdom.commands.teams.FkTeamCommand;
import fr.devsylone.fallenkingdom.players.FkPlayer;

public class RemovePlayer extends FkTeamCommand
{
	public RemovePlayer()
	{
		super("removePlayer", "<player>", 1, Messages.CMD_MAP_TEAM_REMOVE_PLAYER);
	}

	public void execute(Player sender, FkPlayer fkp, String[] args)
	{
		Fk.getInstance().getFkPI().getTeamManager().removePlayerOfHisTeam(args[0]);
		sender.setDisplayName(sender.getName());

		if(args.length < 2 || !args[1].equalsIgnoreCase("nobroadcast"))
			broadcast(args[0] + " a été exclu de son équipe ! ");
	}
}
