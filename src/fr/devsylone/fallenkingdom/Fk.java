package fr.devsylone.fallenkingdom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipOutputStream;

import fr.devsylone.fallenkingdom.commands.FkAsyncCommandExecutor;
import fr.devsylone.fallenkingdom.commands.FkAsyncRegisteredCommandExecutor;
import fr.devsylone.fallenkingdom.commands.brigadier.BrigadierSpigotManager;
import fr.devsylone.fallenkingdom.manager.*;
import fr.devsylone.fallenkingdom.scoreboard.PlaceHolderExpansion;
import fr.devsylone.fkpi.rules.Rule;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.devsylone.fallenkingdom.commands.FkCommandExecutor;
import fr.devsylone.fallenkingdom.game.Game;
import fr.devsylone.fallenkingdom.manager.packets.PacketManager;
import fr.devsylone.fallenkingdom.manager.packets.PacketManager1_13;
import fr.devsylone.fallenkingdom.manager.packets.PacketManager1_14;
import fr.devsylone.fallenkingdom.manager.packets.PacketManager1_16;
import fr.devsylone.fallenkingdom.manager.packets.PacketManager1_8;
import fr.devsylone.fallenkingdom.manager.packets.PacketManager1_9;
import fr.devsylone.fallenkingdom.manager.saveable.DeepPauseManager;
import fr.devsylone.fallenkingdom.manager.saveable.PlayerManager;
import fr.devsylone.fallenkingdom.manager.saveable.PortalsManager;
import fr.devsylone.fallenkingdom.manager.saveable.ScoreboardManager;
import fr.devsylone.fallenkingdom.manager.saveable.StarterInventoryManager;
import fr.devsylone.fallenkingdom.pause.PauseRestorer;
import fr.devsylone.fallenkingdom.players.FkPlayer;
import fr.devsylone.fallenkingdom.updater.PluginUpdater;
import fr.devsylone.fallenkingdom.utils.ChatUtils;
import fr.devsylone.fallenkingdom.utils.DebuggerUtils;
import fr.devsylone.fallenkingdom.utils.FkSound;
import fr.devsylone.fallenkingdom.utils.Version;
import fr.devsylone.fallenkingdom.utils.ZipUtils;
import fr.devsylone.fkpi.FkPI;
import fr.devsylone.fkpi.teams.Team;

public class Fk extends JavaPlugin
{
	private static boolean DEBUG_MODE;

	private Game game;
	private CommandManager cmdManager;
	private PlayerManager pManager;
	private WorldManager wManager;
	private PauseRestorer pRestorer;
	private StarterInventoryManager siManager;
	private ScoreboardManager sbManager;
	private PacketManager pcktManager;
	private DeepPauseManager dpManager;
	private TipsManager tipsManager;
	private SaveablesManager saveableManager;
	private PortalsManager portalManager;

	private static Fk instance;

	private FkPI fkpi;

	private final List<String> onConnectWarnings = new ArrayList<>();
	private String pluginError = "";

	private String lastVersion = getDescription().getVersion();

	public static Fk getInstance()
	{
		return instance;
	}

	public Fk()
	{
		instance = this;
	}

	@Override
	public void onEnable()
	{
		try
		{
			/*
			 * Mode debug
			 */
			DEBUG_MODE = DebuggerUtils.getServerFolderName().endsWith("-debug") || new File(getDataFolder(), "debug").exists();
			if(DEBUG_MODE)
			{
				debug("##########################");
				debug("STARTED IN DEBUG MODE");
				debug("##########################");
			}
		}catch(Exception e)
		{
			DEBUG_MODE = false;
		}

		/*
		 * Random
		 */

		if(!getDataFolder().exists())
			getDataFolder().mkdir();

		ListenersManager.registerListeners(this);
		if (!check())
			return;

		LanguageManager.init(this);

		/*
		 * FkPI
		 */

		fkpi = new FkPI();

		/*
		 * command /fk
		 */

		PluginCommand command = Objects.requireNonNull(getCommand("fk"), "Unable to register /fk command");
		if (Version.isAsyncTabCompleteSupported())
			if (Version.isAsyncPlayerSendCommandsEventSupported())
				this.cmdManager = new FkAsyncRegisteredCommandExecutor(this, command);
			else
				this.cmdManager = new FkAsyncCommandExecutor(this, command);
		else
			this.cmdManager = new FkCommandExecutor(this, command);

		if (Version.isBrigadierSupported() && !Version.isAsyncPlayerSendCommandsEventSupported())
			new BrigadierSpigotManager<>(this).register(this.cmdManager, command);

		/*
		 * MANAGER
		 */
		pManager = new PlayerManager();
		pRestorer = new PauseRestorer();
		siManager = new StarterInventoryManager();
		sbManager = new ScoreboardManager();
		wManager = new WorldManager(this);
		pcktManager = initPacketManager();
		dpManager = new DeepPauseManager();
		tipsManager = new TipsManager();
		tipsManager.startBroadcasts();
		portalManager = new PortalsManager();

		game = new Game();

		saveableManager = new SaveablesManager(this);

		/*
		 * Update & load
		 */

		if(!saveableManager.getFileConfiguration("save.yml").contains("last_version"))
			saveableManager.getFileConfiguration("save.yml").set("last_version", "2.5.0");

		lastVersion = saveableManager.getFileConfiguration("save.yml").getString("last_version");

		try
		{
			saveableManager.loadAll();
		}catch(Exception ex)
		{
			onConnectWarnings.add("§cVotre configuration était corrompue ou invalide, elle a donc été sauvegardée puis supprimée. Désolé :S");
			File zip = new File(getDataFolder(), "invalid-" + new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(Calendar.getInstance().getTimeInMillis()) + ".zip");
			ZipOutputStream outputStream;
			try
			{
				zip.createNewFile();
				outputStream = new ZipOutputStream(new FileOutputStream(zip));
				ZipUtils.zipFile(getDataFolder(), "FallenKingdom", outputStream, false);
				outputStream.flush();
				outputStream.close();
			} catch(IOException e1)
			{
				e1.printStackTrace();
			}
			for(File f : getDataFolder().listFiles())
				if(f.getName().endsWith(".yml"))
					f.delete();
			saveableManager = new SaveablesManager(this);
			saveableManager.loadAll();
			ex.printStackTrace();
		}

		/*
		 * ServerSocket & load du config.yml
		 */

		File conf = new File(getDataFolder(), "config.yml");
		if(conf.length() == 0L)
			conf.delete();
		saveDefaultConfig();

		if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
			new PlaceHolderExpansion().register();

		/*
		 * Set le sb a tout le monde si jamais rl
		 */
		for(Player p : Bukkit.getOnlinePlayers())
			pManager.registerNewPlayer(pManager.getPlayer(p));

		/*
		 * IF EternalDay
		 */
		if(fkpi.getRulesManager().getRulesList().containsKey(Rule.ETERNAL_DAY) && fkpi.getRulesManager().getRule(Rule.ETERNAL_DAY))
			for(World w : Bukkit.getWorlds())
			{
				if(!Fk.getInstance().getWorldManager().isAffected(w))
					continue;
				w.setGameRuleValue("doDaylightCycle", "false");
				w.setTime(6000L);
			}

		/*
		 * Metrics
		 */
		try {
			metrics();
		} catch (NoClassDefFoundError e) {
			e.printStackTrace();
		}

		/*
		 * Updater
		 */

        PluginUpdater updater = new PluginUpdater(Fk.getInstance());
        updater.runTaskAsynchronously(this);


		new BukkitRunnable() {
			@Override
			public void run() {
				saveableManager.saveAll();
			}
		}.runTaskTimerAsynchronously(this, 5L * 60L * 20L, 5L * 60L * 20L);
	}

	@Override
	public void onDisable()
	{
		saveableManager.saveAll();

		if(game.getState().equals(Game.GameState.PAUSE))
		{
			getDeepPauseManager().unprotectItems();
			getDeepPauseManager().resetAIs();
		}

		sbManager.removeAllScoreboards();

		for(FkPlayer p : getPlayerManager().getConnectedPlayers())
			p.getScoreboard().remove();
	}

	public static boolean isDebug()
	{
		return DEBUG_MODE;
	}

	public CommandManager getCommandManager()
	{
		return cmdManager;
	}

	public PlayerManager getPlayerManager()
	{
		return pManager;
	}

	public WorldManager getWorldManager()
	{
		return wManager;
	}

	public ScoreboardManager getScoreboardManager()
	{
		return sbManager;
	}

	public PacketManager getPacketManager()
	{
		return pcktManager;
	}

	public PauseRestorer getPauseRestorer()
	{
		return pRestorer;
	}

	public DeepPauseManager getDeepPauseManager()
	{
		return dpManager;
	}

	public StarterInventoryManager getStarterInventoryManager()
	{
		return siManager;
	}

	public TipsManager getTipsManager()
	{
		return tipsManager;
	}

	public SaveablesManager getSaveableManager()
	{
		return saveableManager;
	}

	public PortalsManager getPortalsManager()
	{
		return portalManager;
	}

	public String getPreviousVersion()
	{
		return lastVersion;
	}

	public static void broadcast(String message, String prefix, FkSound sound)
	{
		if (message == null || message.isEmpty()) {
			return;
		}
		message = "§r" + message;
		for(FkPlayer p : getInstance().getPlayerManager().getConnectedPlayers())
			p.sendMessage(message, prefix, sound);
	}

	public static void broadcast(String message, String prefix)
	{
		broadcast(message, prefix, null);
	}

	public static void broadcast(String message, FkSound sound)
	{
		broadcast(message, "", sound);
	}

	public static void broadcast(String message)
	{
		broadcast(message, "", null);
	}

	public static void debug(Object message)
	{
		if(DEBUG_MODE)
		{
			if(!Fk.getInstance().isEnabled())
				Bukkit.getScheduler().scheduleSyncDelayedTask(Fk.getInstance(), () -> Bukkit.broadcastMessage(ChatUtils.DEBUG + (message == null ? "null" : message.toString())));
			else
				Bukkit.broadcastMessage(ChatUtils.DEBUG + (message == null ? "null" : message.toString()));
		}

	}

	public Game getGame()
	{
		return game;
	}

	public List<String> getOnConnectWarnings()
	{
		return onConnectWarnings;
	}

	public void addError(String s)
	{
		pluginError = s;
	}

	public String getError()
	{
		return pluginError;
	}

	public void reset()
	{
		Bukkit.getScheduler().cancelTasks(instance);
		fkpi.reset();
		game = new Game();

		for(FkPlayer p : getPlayerManager().getConnectedPlayers())
			p.getScoreboard().remove();

		pManager = new PlayerManager();
		portalManager = new PortalsManager();
		dpManager.unprotectItems();
		dpManager.resetAIs();

		// Reset saveFile & Restorer

		saveableManager.reset();

		pRestorer = new PauseRestorer();

		// Scoreboards
		sbManager = new ScoreboardManager(); //Le recréer pour le réinitialiser

		getScoreboardManager().recreateAllScoreboards();

		saveableManager = new SaveablesManager(this); // En dernier
	}

	public void stop()
	{
		Bukkit.getScheduler().cancelTasks(instance);

		tipsManager.cancelBroadcasts();
		tipsManager.startBroadcasts();

		game.stop();
		dpManager.resetAIs();
		dpManager.unprotectItems();

		for(FkPlayer p : getPlayerManager().getConnectedPlayers())
		{
			p.clearDeaths();
			p.clearKills();
		}

		for(Team team : fkpi.getTeamManager().getTeams())
		{
			if(team.getBase() != null)
				team.getBase().resetChestoom();
		}
		getScoreboardManager().recreateAllScoreboards();
	}

	private boolean check()
	{
		List<String> warns = new ArrayList<>();

		if(getConfig().get("Charged_creepers") != null)
			warns.add("L'option Charged_creepers dans le fichier de configuration n'est plus utilisée, il faut utiliser /fk rules ChargedCreepers");

		if(!Version.hasSpigotApi())
			addError("Le serveur n'est pas supporté par le plugin. Seuls les serveurs basés sur Spigot sont supportés.");

		if(Version.isTooOldApi())
			addError("La version du serveur n'est pas compatible avec le plugin,\nmerci d'utiliser au minimum la version §l§n1.8.3 de Spigot.");

		for(String warn : warns)
		{
			getLogger().warning("------------------------------------------");
			getLogger().warning(warn);
			getLogger().warning("------------------------------------------");

			onConnectWarnings.add(warn);
		}
		return warns.isEmpty();
	}

	public PacketManager initPacketManager() {
		switch (Version.VERSION_TYPE) {
			case V1_8:
				return new PacketManager1_8();
			case V1_9_V1_12:
				return new PacketManager1_9();
			case V1_13:
				return new PacketManager1_13();
			case V1_14_V1_15:
				return new PacketManager1_14();
			case V1_16:
				return new PacketManager1_16();
			default:
				throw new RuntimeException("Could not get packet manager by version!");
		}
	}

	public FkPI getFkPI()
	{
		return fkpi;
	}

	private void metrics() throws NoClassDefFoundError // gson en 1.8.0
	{
		Metrics metrics = new Metrics(this, 6738);
		metrics.addCustomChart(new Metrics.SingleLineChart("server_running_1-8_version", () -> Bukkit.getVersion().contains("1.8") ? 1 : 0));
		metrics.addCustomChart(new Metrics.SimplePie("lang_used", LanguageManager::getLocalePrefix));
	}

	public void addOnConnectWarning(String warning)
	{
		onConnectWarnings.add(warning);
	}
}
