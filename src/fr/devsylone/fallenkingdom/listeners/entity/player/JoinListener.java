package fr.devsylone.fallenkingdom.listeners.entity.player;

import java.time.LocalDate;
import java.time.Month;
import fr.devsylone.fallenkingdom.Fk;
import fr.devsylone.fallenkingdom.players.FkPlayer;
import fr.devsylone.fallenkingdom.players.FkPlayer.PlayerState;
import fr.devsylone.fallenkingdom.game.Game.GameState;
import fr.devsylone.fallenkingdom.utils.Messages;
import fr.devsylone.fkpi.teams.Team;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.SkullMeta;

public class JoinListener implements Listener
{
	@EventHandler
	public void prelogin(final AsyncPlayerPreLoginEvent e)
	{
		if(!Fk.getInstance().getPluginError().isEmpty())
		{
			e.setLoginResult(Result.KICK_OTHER);
			e.setKickMessage(kickMessage());
		}
	}

	@EventHandler
	public void join(final PlayerJoinEvent e)
	{
		if(!Fk.getInstance().getPluginError().isEmpty()) // Bukkit n'a pas l'air d'invoquer l'AsyncPlayerPreLoginEvent
			e.getPlayer().kickPlayer(kickMessage());

		if (!Fk.getInstance().getWorldManager().isAffected(e.getPlayer().getWorld()))
			return;

		FkPlayer player = Fk.getInstance().getPlayerManager().getPlayer(e.getPlayer());
		
		if(e.getPlayer().isOp())
			for(String s : Fk.getInstance().getOnConnectWarnings())
				e.getPlayer().sendMessage(s);

		player.recreateScoreboard();

		final Team pTeam = Fk.getInstance().getFkPI().getTeamManager().getPlayerTeam(e.getPlayer());
		if(pTeam != null) //REFRESH LES TEAMS SCOREBOARD (MC=CACA)
			Fk.getInstance().getScoreboardManager().refreshNicks();

		e.setJoinMessage(null);
		Fk.broadcast(Messages.CHAT_JOIN.getMessage().replace("%player%", e.getPlayer().getDisplayName()));

		if(player.getState() == PlayerState.EDITING_SCOREBOARD)
			player.getSbDisplayer().display();
			
		LocalDate currentDate = LocalDate.now();

		if(Fk.getInstance().getGame().getState().equals(GameState.BEFORE_STARTING) && (currentDate.getDayOfMonth() == 12) && (currentDate.getMonth() == Month.JUNE))
			player.setHelmet(head());
	}

	@EventHandler
	public void quit(PlayerQuitEvent e)
	{
		if(Fk.getInstance().getPlayerManager().getPlayer(e.getPlayer()).getState() == PlayerState.EDITING_SCOREBOARD)
			Fk.getInstance().getPlayerManager().getPlayer(e.getPlayer()).getSbDisplayer().exit();

		if (!Fk.getInstance().getWorldManager().isAffected(e.getPlayer().getWorld()))
			return;

		e.setQuitMessage(null);
		Fk.broadcast(Messages.CHAT_QUIT.getMessage().replace("%player%", e.getPlayer().getDisplayName()));
	}

	private String kickMessage()
	{
		return "§d§m----------§5 Fallenkingdom §d§m----------\n"
				+ "\n"
				+ "§6Le plugin a rencontré une erreur\n\n"
				+ "§7Erreur : §c" + Fk.getInstance().getPluginError();
	}
	
	private ItemStack head()
	{
		LocalDate currentDate = LocalDate.now();

		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setDisplayName(Messages.EASTER_EGG_ANNIVERSARY_NAME);
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(Messages.EASTER_EGG_ANNIVERSARY_LORE_1);
		lore.add(Messages.EASTER_EGG_ANNIVERSARY_LORE_2.replace("%age%", currentDate.getYear() - 2016)));
		meta.setLore(lore);
		meta.setOwner("MHF_Cake");
		return skull.setItemMeta(meta);
	}
}
