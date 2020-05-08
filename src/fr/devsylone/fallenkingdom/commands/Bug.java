package fr.devsylone.fallenkingdom.commands;

import org.bukkit.entity.Player;

import fr.devsylone.fallenkingdom.players.FkPlayer;

public class Bug extends FkCommand
{
	public Bug()
	{
		super("bug", "", 0, "Obtenir le lien pour signaler un bug");
	}

	public void execute(Player sender, FkPlayer fkp, String[] args)
	{
		fkp.sendMessage("Pour signaler un bug : https://discord.gg/2mPXHYX");
	}
}
