package tk.hugo4715.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.List;

public class ACPlayer {

	public int violations = 0;

	private Player player;
	private String name;
	private int notifyTimes = 0;
	
	public ACPlayer(Player p) {
		this.player = p;
		name = p.getName();
	}
	
	public boolean hasCeiling() {
		Location loc = player.getLocation().clone().add(0, 2, 0);
		if (loc.getBlock().getType().isSolid()) {
			return true;
		} else if (loc.getX() > 0.66 && loc.getBlock().getRelative(BlockFace.EAST).getType().isSolid()) {
			return true;
		} else if (loc.getX() < -0.66 && loc.getBlock().getRelative(BlockFace.WEST).getType().isSolid()) {
			return true;
		} else if (loc.getZ() > 0.66 && loc.getBlock().getRelative(BlockFace.SOUTH).getType().isSolid()) {
			return true;
		} else return loc.getZ() < -0.66 && loc.getBlock().getRelative(BlockFace.NORTH).getType().isSolid();
	}
	
	public Player getPlayer() {
		return player;
	}

	public boolean isInWater() {
		return player.getLocation().getBlock().isLiquid() || player.getLocation().clone().add(0, -1, 0).getBlock().isLiquid() || player.getLocation().clone().add(0, 1, 0).getBlock().isLiquid();
	}
	
	public boolean isInWeb() {
		Location loc = player.getLocation().clone();
		double x = loc.getX()-loc.getBlockX();
		double z = loc.getZ()-loc.getBlockZ();

		if (insideWeb(loc, x, z)) {
			return true;
		}

		loc = player.getLocation().clone().add(0, 1, 0);
		x = loc.getX()-loc.getBlockX();
		z = loc.getZ()-loc.getBlockZ();

		if (insideWeb(loc, x, z)) {
			return true;
		}

		return false;
	}

	private boolean isWeb(Block b){
		return b.getType().equals(Material.WEB);
	}

	private boolean insideWeb(Location loc, double x, double z) {
		Block block = loc.getBlock();
		return isWeb(block) ||
		x < 0.31 && isWeb(block.getRelative(BlockFace.WEST)) ||
		x > 0.69 && isWeb(block.getRelative(BlockFace.EAST)) ||
		z < 0.31 && isWeb(block.getRelative(BlockFace.NORTH)) ||
		z > 0.69 && isWeb(block.getRelative(BlockFace.SOUTH)) ||
		x > 0.71 && z < 0.3 && isWeb(block.getRelative(BlockFace.EAST).getRelative(BlockFace.NORTH)) ||
		x > 0.71 && z > 0.71 && isWeb(block.getRelative(BlockFace.EAST).getRelative(BlockFace.SOUTH)) ||
		x < 0.31 && z > 0.71 && isWeb(block.getRelative(BlockFace.WEST).getRelative(BlockFace.SOUTH)) ||
		x < 0.31 && z < 0.31 && isWeb(block.getRelative(BlockFace.WEST).getRelative(BlockFace.NORTH));
	}

	public boolean isOnLadder(){
		Block loc = player.getLocation().getBlock();
		return isClimbable(loc) || isClimbable(loc.getRelative(BlockFace.UP)) || isClimbable(loc.getRelative(BlockFace.DOWN));
	}

	private boolean isClimbable(Block b){
		return b.getType().equals(Material.LADDER) || b.getType().equals(Material.VINE);
	}

	public int getNotifyTimes() {
		return notifyTimes;
	}

	public void onViolation(double percent) {
		if(percent < 0) {
			return;
		}
		violations+= KbPlus.get().getConfig().getInt("violation-lvl.increase");
		
		if (violations >= KbPlus.get().getConfig().getInt("violation-lvl.max")){
			sanction(percent);
			notifyTimes++;
		}
	}
	
	public void sanction(double percent){
		KbPlus.get().getConfig().getStringList("cmd-on-ban").forEach( cmd -> {
			cmd = cmd.replace("%player%", name).replace("%kb%", Math.round(percent*100.0) + "");
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', cmd));
		});
	}
	
	public void onLegit(){
		violations -= KbPlus.get().getConfig().getInt("violation-lvl.decrease");
		violations = Math.max(0, violations);
	}
}
