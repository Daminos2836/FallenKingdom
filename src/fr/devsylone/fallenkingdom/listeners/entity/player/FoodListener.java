package fr.devsylone.fallenkingdom.listeners.entity.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import fr.devsylone.fallenkingdom.Fk;
import fr.devsylone.fallenkingdom.game.Game.GameState;

public class FoodListener implements Listener
{
	@EventHandler
	public void food(FoodLevelChangeEvent e)
	{
		if (!Fk.getInstance().getWorldManager().isAffected(e.getEntity().getWorld()))
			return;
		e.setCancelled(!Fk.getInstance().getGame().getState().equals(GameState.STARTED));
	}
}
