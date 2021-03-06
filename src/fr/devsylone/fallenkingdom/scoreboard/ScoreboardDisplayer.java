package fr.devsylone.fallenkingdom.scoreboard;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.devsylone.fallenkingdom.Fk;
import fr.devsylone.fallenkingdom.players.FkPlayer;

public class ScoreboardDisplayer
{
	private final String player;
	private final List<Integer> headIds = new ArrayList<>();
	private final List<Integer> footerIds = new ArrayList<>();
	private final List<Integer> placeholdersIds = new ArrayList<>();

	private final List<BukkitRunnable> runnables = new ArrayList<>();

	public ScoreboardDisplayer(FkPlayer p)
	{
		player = p.getName();
	}

	public void display()
	{
		if(Bukkit.getPlayer(player) == null)
			return;

		Player p = Bukkit.getPlayer(player);

		headIds.add(Fk.getInstance().getPacketManager().createFloattingText("§e Pour modifier une ligne : ", p, null));
		headIds.add(Fk.getInstance().getPacketManager().createFloattingText("§e >>>>> /fk scoreboard SetLine <<<<< ", p, null));
		headIds.add(Fk.getInstance().getPacketManager().createFloattingText("§aUtilise la §2molette §a de ta §2souris", p, null));
		headIds.add(Fk.getInstance().getPacketManager().createFloattingText("§aPuis regarde ton scoreboard     ↘↘↘↘↘↘↘", p, null));

		placeholdersIds.add(Fk.getInstance().getPacketManager().createFloattingText("§bVoici la liste des variables utilisables !", p, null));

		for(PlaceHolder ph : PlaceHolder.values())
			placeholdersIds.add(Fk.getInstance().getPacketManager().createFloattingText("§8" + ph.getDescription() + "        §c->§r        " + ph.getShortestKey(), p, null));

		footerIds.add(Fk.getInstance().getPacketManager().createFloattingText("Pour §cquitter §r: §e/fk scoreboard LeaveEdit", p, null));

		startUpdateRunnable();
		Fk.getInstance().getScoreboardManager().recreateAllScoreboards();
	}

	public void updateLoc()
	{
		Location loc = Bukkit.getPlayer(player).getLocation().add(0, 1, 0);
		loc.setPitch(0.0f);
		loc = getSight(loc, 5);
		loc.setY(loc.getY() + 0.25 * ((headIds.size() + placeholdersIds.size() + footerIds.size()) / 2) - 1);

		for (Integer headId : headIds) {
			Fk.getInstance().getPacketManager().updateFloattingText(headId, loc);
			loc.add(0, -0.25, 0);
		}

		loc.add(0, -0.75, 0);
		for (Integer placeholdersId : placeholdersIds) {
			Fk.getInstance().getPacketManager().updateFloattingText(placeholdersId, loc);
			loc.add(0, -0.25, 0);
		}
		
		loc.add(0, -0.75, 0);
		for (Integer footerId : footerIds) {
			Fk.getInstance().getPacketManager().updateFloattingText(footerId, loc);
			loc.add(0, -0.25, 0);
		}

	}

	private Location getSight(final Location loc, int limit)
	{
		Location end = loc.clone();
		while(end.getBlock().getType().equals(Material.AIR) && loc.distance(end) < limit)
			end.add(loc.getDirection().multiply(0.2));

		if(loc.distance(end) > 1)
			end.add(loc.getDirection().multiply(-1));

		return end;
	}

	public void exit()
	{
		cancelRunnables();

		for(int id : headIds)
			Fk.getInstance().getPacketManager().remove(id);

		for(int id : placeholdersIds)
			Fk.getInstance().getPacketManager().remove(id);
		
		for(int id : footerIds)
			Fk.getInstance().getPacketManager().remove(id);


		headIds.clear();
		placeholdersIds.clear();
		footerIds.clear();
		FkScoreboard scoreboard;
		if((scoreboard = Fk.getInstance().getPlayerManager().getPlayer(player).getScoreboard()) != null)
		{
			scoreboard.setFormatted(true);
			scoreboard.refreshAll();
		}
	}

	public void startUpdateRunnable()
	{
		BukkitRunnable br = new BukkitRunnable()
		{
			private double lastX = 0;
			private double lastY = 0;
			private double lastZ = 0;

			private float lastYaw = 0;

			@Override
			public void run()
			{
				Location l = Bukkit.getPlayer(player).getLocation();

				if(lastX != l.getX() || lastY != l.getY() || lastZ != l.getZ() || lastYaw != l.getYaw())
				{
					lastX = l.getX();
					lastY = l.getY();
					lastZ = l.getZ();
					lastYaw = l.getYaw();
					updateLoc();
				}

			}
		};
		br.runTaskTimer(Fk.getInstance(), 5l, 3l);
		runnables.add(br);
	}

	public void cancelRunnables()
	{
		for(BukkitRunnable br : runnables)
			br.cancel();
		runnables.clear();
	}
}
