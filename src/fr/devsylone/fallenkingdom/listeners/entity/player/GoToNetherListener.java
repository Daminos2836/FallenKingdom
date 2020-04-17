package fr.devsylone.fallenkingdom.listeners.entity.player;

import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import fr.devsylone.fallenkingdom.Fk;

public class GoToNetherListener implements Listener
{
	@EventHandler
	public void event(PlayerChangedWorldEvent e)
	{
		if(!e.getPlayer().getWorld().getEnvironment().equals(Environment.THE_END))
			Fk.getInstance().getPlayerManager().getPlayer(e.getPlayer()).setPortal(e.getPlayer().getLocation());
		
	}
}