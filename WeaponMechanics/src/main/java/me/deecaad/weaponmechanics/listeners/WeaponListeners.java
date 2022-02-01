package me.deecaad.weaponmechanics.listeners;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.events.EntityEquipmentEvent;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.IWeaponCompatibility;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.info.WeaponInfoDisplay;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponEquipEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class WeaponListeners implements Listener {

    private WeaponHandler weaponHandler;
    private static final IWeaponCompatibility weaponCompatibility = WeaponCompatibilityAPI.getWeaponCompatibility();

    public WeaponListeners(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    @EventHandler
    public void equip(EntityEquipmentEvent e) {
        if (e.isArmor())
            return;

        LivingEntity entity = (LivingEntity) e.getEntity();
        EntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(entity);
        ItemStack weaponStack = e.getEquipped();

        // Also try auto converting to weapon
        String weaponTitle = weaponHandler.getInfoHandler().getWeaponTitle(weaponStack, true);

        if (weaponTitle != null) {
            if (e.getEntityType() == EntityType.PLAYER) {
                WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                if (weaponInfoDisplay != null) weaponInfoDisplay.send((PlayerWrapper) entityWrapper, weaponTitle, weaponStack);
            }
            Bukkit.getPluginManager().callEvent(new WeaponEquipEvent(weaponTitle, weaponStack, entity, e.getSlot() == EquipmentSlot.HAND));

            weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, e.getSlot());
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void itemHeld(PlayerItemHeldEvent e) {
        WeaponMechanics.getEntityWrapper(e.getPlayer()).getMainHandData().cancelTasks();
        // No need to cancel off hand tasks since this is only called when changing held slot
    }

    @EventHandler
    public void interactEntity(PlayerInteractEntityEvent e) {

        // RIGHT MELEE

        Entity entityVictim = e.getRightClicked();
        HitBox victimHitBox = weaponCompatibility.getHitBox(entityVictim);

        if (victimHitBox == null) return; // Invalid entity

        EquipmentSlot hand = e.getHand();
        Player shooter = e.getPlayer();

        // TODO: melee functions (++ use CollisionData)
    }

    @EventHandler
    public void damage(EntityDamageByEntityEvent e) {
        EntityDamageEvent.DamageCause cause = e.getCause();
        if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && (CompatibilityAPI.getVersion() < 1.09 || cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            return;
        }

        // LEFT MELEE

        Entity entityVictim = e.getEntity();
        Entity shooter = e.getDamager();

        // TODO: melee functions (++ use CollisionData)
    }

    @EventHandler
    public void death(EntityDeathEvent e) {
        WeaponMechanics.removeEntityWrapper(e.getEntity());
    }

    @EventHandler (ignoreCancelled = true)
    public void click(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        // Off hand is also considered as quickbar slot
        if (e.getSlotType() != InventoryType.SlotType.QUICKBAR) return;

        EntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(e.getWhoClicked());
        entityWrapper.getMainHandData().cancelTasks();
        entityWrapper.getOffHandData().cancelTasks();
    }

    @EventHandler (ignoreCancelled = true)
    public void swapHandItems(PlayerSwapHandItemsEvent e) {
        EntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(e.getPlayer());
        entityWrapper.getMainHandData().cancelTasks();
        entityWrapper.getOffHandData().cancelTasks();
    }
}