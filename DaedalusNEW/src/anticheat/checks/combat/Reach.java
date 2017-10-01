package anticheat.checks.combat;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import anticheat.Daedalus;
import anticheat.detections.Checks;
import anticheat.detections.ChecksType;
import anticheat.utils.MathUtils;
import anticheat.utils.PlayerUtils;

public class Reach extends Checks {

	public Reach() {
		super("Reach", "Reach", ChecksType.COMBAT , 12, Daedalus.getAC(), true);
	}
	
	@Override
	protected void onEvent(Event event) {
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
	        if (!(e.getDamager() instanceof Player)) {
	            return;
	        }
	        if (!(e.getEntity() instanceof Player)) {
	            return;
	        }
	        Player damager = (Player)e.getDamager();
	        Player player = (Player)e.getEntity();
	        double Reach = MathUtils.trim(2, PlayerUtils.getEyeLocation(damager).distance(player.getEyeLocation()) - 0.32);
	        double Reach2 = MathUtils.trim(2, PlayerUtils.getEyeLocation(damager).distance(player.getEyeLocation()) - 0.32);
	        
	        double Difference;
	        
	        if(Daedalus.getAC().getServer().spigot() < getDaedalus().getTPSCancel()) {
	        	return;
	        }
	        if (damager.getAllowFlight()) {
	            return;
	        }
	        if (player.getAllowFlight()) {
	            return;
	        }
	        
	        if(!count.containsKey(damager)) {
	        	count.put(damager, 0);
	        }
	        
	        int Count = count.get(damager);
	        long Time = System.currentTimeMillis();
	        double MaxReach = 3.1;
	        double YawDifference = Math.abs(damager.getEyeLocation().getYaw() - player.getEyeLocation().getYaw());
	        double speedToVelocityDif = 0;
	        double velocityDifference2 = Math.abs(damager.getVelocity().length() + player.getVelocity().length());
	        double offsets = 0.0D;
	        double offsetsp = 0.0D;
	        double lastHorizontal = 0.0D;
	        if(this.offsets.containsKey(damager)) {
	        	offsets = ((Double)((Map.Entry)this.offsets.get(damager)).getKey()).doubleValue();
	        	lastHorizontal = ((Double)((Map.Entry)this.offsets.get(damager)).getValue()).doubleValue();
	        }
	        if(this.offsets.containsKey(player)) {
	        	offsetsp = ((Double)((Map.Entry)this.offsets.get(player)).getKey()).doubleValue();
	        }
	        if(Latency.getLag(damager) > 100 || Latency.getLag(player) > 100) {
	        	return;
	        }
	        speedToVelocityDif = Math.abs(offsets - player.getVelocity().length());
	        MaxReach += (YawDifference * 0.001);
	         MaxReach += lastHorizontal * 1.5;
	         MaxReach += speedToVelocityDif * 0.09;
	        if (damager.getLocation().getY() > player.getLocation().getY()) {
	            Difference = damager.getLocation().getY() - player.getLocation().getY();
	            MaxReach += Difference / 2.5;
	        } else if (player.getLocation().getY() > damager.getLocation().getY()) {
	            Difference = player.getLocation().getY() - damager.getLocation().getY();
	            MaxReach += Difference / 2.5;
	        }
	        MaxReach += damager.getWalkSpeed() <= 0.2 ? 0 : damager.getWalkSpeed() - 0.2;
	        for (PotionEffect effect : damager.getActivePotionEffects()) {
	            if (effect.getType().equals(PotionEffectType.SPEED)) {
	                 MaxReach += 0.3D * (effect.getAmplifier() + 1);
	            }
	        }
	        int PingD = this.getDaedalus().getLag().getPing(damager);
	        int PingP = this.getDaedalus().getLag().getPing(player);
	        MaxReach += ((PingD + PingP) / 2) * 0.0024;
	        Reach2 -= MathUtils.trim(2, velocityDifference2);
	        Reach2 -= MathUtils.trim(2, offsetsp);
	        if (UtilTime.elapsed(Time, 10000)) {
	            count.remove(damager);
	            Time = System.currentTimeMillis();
	        }
	        if (Reach > MaxReach) {
	        	 this.dumplog(damager, "Count Increase (+1); Reach: " + Reach2 + ", MaxReach: " + MaxReach + ", Damager Velocity: " + damager.getVelocity().length() + ", " + "Player Velocity: " + player.getVelocity().length() + "; New Count: " + Count);
	            count.put(damager, Count + 1);
	        } else {
	        	if(Count >= -2) {
	        		count.put(damager, Count - 1);
	        	}
	        }
	        if(Reach2 > 6) {
	        	e.setCancelled(true);
	        }
	        if(Count >= 2 && Reach > MaxReach && Reach < 20.0) {
	        	count.remove(damager);
	        	if(Latency.getLag(player) < 115) {
	        		this.getDaedalus().logCheat(this, damager, Reach  + " > " + MaxReach + " MS: " + PingD + " Velocity Difference: " + speedToVelocityDif, Chance.HIGH, new String[0]);
	        		
	        	}
	        	this.dumplog(damager, "Logged for Reach" + Reach2 +  " > " + MaxReach);
	        	return;
	        }
	        long attackTime = System.currentTimeMillis();
	        if (this.reachTicks.containsKey(damager)) {
	            attackTime = reachTicks.get(damager);
	        }
		}
	}
}
