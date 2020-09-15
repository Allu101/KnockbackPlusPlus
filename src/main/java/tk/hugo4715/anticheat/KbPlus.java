package tk.hugo4715.anticheat;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author hugo4715
 */
public class KbPlus extends JavaPlugin {

	private static Method getHandleMethod;
	private static Field pingField;
	private KbChecker kbChecker;

	@Override
	public void onEnable() {
		saveDefaultConfig();

		if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")){
			kbChecker = new KbChecker();
		} else {
			getLogger().warning("ProtocolLib is required for this plugin!");
			Bukkit.getPluginManager().disablePlugin(this);
			throw new UnknownDependencyException("ProtocolLib");
		}
		getServer().getPluginManager().registerEvents(kbChecker, this);
		getCommand("knockbackplus").setExecutor(kbChecker);
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

	public boolean isThereWallsAround(Location pLoc, Integer velX, Integer velZ) {
		if (velX < 0) {
			if (pLoc.clone().add(-1, 0, 0).getBlock().getType() != Material.AIR ||
					pLoc.clone().add(-1, 1, 0).getBlock().getType() != Material.AIR) {
				return true;
			}
		} else if (velX > 0) {
			if (pLoc.clone().add(1, 0, 0).getBlock().getType() != Material.AIR ||
					pLoc.clone().add(1, 1, 0).getBlock().getType() != Material.AIR) {
				return true;
			}
		} else if (velZ < 0) {
			if (pLoc.clone().add(0, 0, -1).getBlock().getType() != Material.AIR ||
					pLoc.clone().add(0, 1, -1).getBlock().getType() != Material.AIR) {
				return true;
			}
		} else if (velZ > 0) {
			if (pLoc.clone().add(0, 0, 1).getBlock().getType() != Material.AIR ||
					pLoc.clone().add(0, 1, 1).getBlock().getType() != Material.AIR) {
				return true;
			}
		}
		return false;
	}

	public int getPing(Player player) {
		try {
			if (getHandleMethod == null) {
				getHandleMethod = player.getClass().getDeclaredMethod("getHandle");
				getHandleMethod.setAccessible(true);
			}
			Object entityPlayer = getHandleMethod.invoke(player);
			if (pingField == null) {
				pingField = entityPlayer.getClass().getDeclaredField("ping");
				pingField.setAccessible(true);
			}
			return pingField.getInt(entityPlayer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
}
