package me.airijko.endlessskills.listeners;

import me.airijko.endlessskills.skills.SkillAttributes;
import me.airijko.endlessskills.combat.DamageHologram;

import org.bukkit.entity.Entity;
import java.text.DecimalFormat;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class DamageListener implements Listener {
    private final SkillAttributes skillAttributes;
    private final HashMap<UUID, Boolean> eventProcessed = new HashMap<>();
    private final JavaPlugin plugin;

    public DamageListener(SkillAttributes skillAttributes, JavaPlugin plugin) {
        this.skillAttributes = skillAttributes;
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        // Check if the damager is a player
        if (damager instanceof Player) {
            System.out.println("EntityDamageByEntityEvent triggered");
            Player player = (Player) damager;
            UUID playerUUID = player.getUniqueId();
            Entity entity = event.getEntity();

            if (eventProcessed.containsKey(playerUUID) && eventProcessed.get(playerUUID)) {
                return; // Ignore the event if it has already been processed
            }

            // Calculate damage value and check for critical hit
            int precisionLevel = skillAttributes.getAttributeLevel(player.getUniqueId(), "Precision");
            int ferocityLevel = skillAttributes.getAttributeLevel(player.getUniqueId(), "Ferocity");

            double criticalHitChance = skillAttributes.modifyPrecision(player, precisionLevel);
            double ferocityMultiplier = skillAttributes.modifyFerocity(player, ferocityLevel);

            boolean isCritical = Math.random() < criticalHitChance;
            double damageValue = event.getDamage();

            if (isCritical) {
                damageValue += damageValue * ferocityMultiplier;
            }

            // Format the damage value to two decimal places
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            damageValue = Double.parseDouble(decimalFormat.format(damageValue));

            // Display the damage hologram
            Location location = entity.getLocation(); // Use the entity's location
            DamageHologram hologram = new DamageHologram(plugin, location, String.valueOf(damageValue), isCritical);
            Bukkit.getScheduler().runTaskLater(plugin, hologram::remove, 40L);

            // Mark the event as processed for this player
            eventProcessed.put(playerUUID, true);

            // Reset the flag after a short delay to allow for subsequent attacks
            Bukkit.getScheduler().runTaskLater(plugin, () -> eventProcessed.put(playerUUID, false), 1L);
        }
    }
}