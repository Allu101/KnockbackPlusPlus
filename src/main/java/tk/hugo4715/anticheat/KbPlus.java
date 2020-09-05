package tk.hugo4715.anticheat;

import com.comphenix.protocol.ProtocolLibrary;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author hugo4715
 */
public class KbPlus extends JavaPlugin {
	public static final String PREFIX = ChatColor.GOLD + "[" + ChatColor.GREEN + "AntiCheat" + ChatColor.GOLD + "]" + ChatColor.GREEN;

	private KbChecker kbChecker;

	@Override
	public void onEnable() {
		saveDefaultConfig();

		if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")){
			this.kbChecker = new KbChecker();
		} else {
			getLogger().severe("");
			getLogger().severe("ProtocolLib is required for this plugin!");
			getLogger().severe("");
			Bukkit.getPluginManager().disablePlugin(this);
			throw new UnknownDependencyException("ProtocolLib");
		}
		getServer().getPluginManager().registerEvents(kbChecker, this);
		getCommand("knockbackplug").setExecutor(kbChecker);
	}

	@Override
	public void onDisable() {
		if (kbChecker != null) {
			ProtocolLibrary.getProtocolManager().removePacketListener(kbChecker);
		}
	}

	public static KbPlus get(){
		return getPlugin(KbPlus.class);
	}
}
