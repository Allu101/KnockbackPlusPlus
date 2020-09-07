package tk.hugo4715.anticheat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class KbChecker extends PacketAdapter implements Listener, CommandExecutor {

	private Map<Integer, ACPlayer> players = new HashMap<>(); //entity id, ACPlayer

	public KbChecker() {
		super(KbPlus.get(), ListenerPriority.MONITOR, PacketType.Play.Server.ENTITY_VELOCITY);
		ProtocolLibrary.getProtocolManager().addPacketListener(this);

		if (KbPlus.get().getConfig().getBoolean("enable-random-checks",false)) {
			KbPlus.get().getLogger().info("Enabled random checks.");
			new BukkitRunnable() {
				
				@Override
				public void run() {
					for (ACPlayer gp : players.values()) {
						double chance = 0.1;
						
						if (gp.violations > 0){
							chance = 1;
						}
						if (gp.getPlayer().isOnGround() && !gp.isInWater() && !gp.isOnLadder() && !gp.isInWeb() && Math.random() < chance){
							gp.getPlayer().setVelocity(new Vector(0,0.2,0));
						}
					}
				}
			}.runTaskTimer(KbPlus.get(), 20,20*5);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("knockbackplug") && args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			KbPlus.get().reloadConfig();
			KbPlus.get().getLogger().info(ChatColor.GREEN + "Config successfully reloaded!");
		}
		return false;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		ACPlayer acp = new ACPlayer(e.getPlayer());
		players.put(acp.getPlayer().getEntityId(), acp);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		if (e.getPlayer() != null) {
			players.remove(e.getPlayer().getEntityId());
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		players.remove(e.getEntity().getEntityId());
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		players.put(e.getPlayer().getEntityId(), new ACPlayer(e.getPlayer()));
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		int entId = event.getPacket().getIntegers().getValues().get(0);
		int velY = event.getPacket().getIntegers().getValues().get(2);
		onVelocityPacket(entId,velY);
	}

	private void onVelocityPacket(int entId, int velY) {
		//found player
		ACPlayer acp = players.get(entId);
		if (acp == null) {
			return;
		}
		if (acp.getPlayer().getGameMode() != GameMode.ADVENTURE) {
			return;
		}
		Player p = acp.getPlayer();
		//sync process in order to fix
		new BukkitRunnable() {
			@Override
			public void run() {
				//don't check if there is a ceiling or anything that could block from taking kb
				if (acp.hasCeiling() || !p.isOnGround() || acp.isOnLadder() || p.isInsideVehicle() ||
						p.getFireTicks() > 0 || p.isFlying() || acp.isInWeb() || acp.isInWater() || p.isDead()) {
					return;
				}
				final int ticksToReact = (int) (KbPlus.get().getConfig().getDouble("check-time",1.5)*20);//ticks for the client to get up

				if (velY < 5000){
					//give client some time to react
					new BukkitRunnable() {
						private int iterations = 0;
						double reachedY = 0;//dif reached
						double baseY = p.getLocation().getY();

						@Override
						public void run() {
							iterations += 2;
							if (p.getLocation().getY()-baseY > reachedY) {
								reachedY = p.getLocation().getY()-baseY;
							}
							if (iterations >= ticksToReact){
								checkKnockback(acp, velY, reachedY);
								cancel();
							}
						}
					}.runTaskTimer(KbPlus.get(), 2, 2);
				}
			}
		}.runTask(KbPlus.get());
	}

	private void checkKnockback(ACPlayer acp, int packetY, double realY) {
		//old equation is y = 0,0006x - 0,8253 (thx excel)
		//new equation is y = 8E-08x2 + 1E-04x - 0,0219
		//double predictedY = 0.0006 * packetY - 0.8253;
		double predictedY = (0.00000008 * packetY * packetY) + (0.0001 * packetY)- 0.0219;
		if (predictedY < realY || acp.getPlayer().hasPermission("knockbackplusplus.bypass")) {
			//legit
			acp.onLegit();
		} else {
			//hax
			double percentage = Math.abs(((realY-predictedY)/predictedY));
			if (percentage > KbPlus.get().getConfig().getDouble("min-notify-percentage", 0.3)) {
				acp.onViolation(percentage);
			}
		}
	}

}
