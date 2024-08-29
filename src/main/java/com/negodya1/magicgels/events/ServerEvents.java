package com.negodya1.magicgels.events;

import com.negodya1.magicgels.Config;
import com.negodya1.magicgels.MagicGels;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MagicGels.MODID)
public class ServerEvents {


	@SubscribeEvent
	public static void entityAttack(LivingHurtEvent event) {
		LivingEntity target = event.getEntity();

		if (!target.level().isClientSide() && event.getSource().getDirectEntity() instanceof LivingEntity living) {
			ItemStack weapon = living.getMainHandItem();

			if (!weapon.isEmpty()) {
				if (weapon.getOrCreateTagElement("MagicGels").contains("Size")) {
					int size = weapon.getOrCreateTagElement("MagicGels").getInt("Size");
					for (int i = 0; i < size; i++) {
						if (weapon.getOrCreateTagElement("Gel_" + i).contains("Potion")) {
							ItemStack itemStack = ItemStack.EMPTY;
							itemStack.deserializeNBT(weapon.getOrCreateTagElement("Gel_" + i).getCompound("Potion"));

							boolean effectApplied = false;

							for (MobEffectInstance effect : PotionUtils.getMobEffects(itemStack)) {
								if (effect.getEffect().isInstantenous()) {
									effect.getEffect().applyInstantenousEffect(target, target, target, effect.getAmplifier(), 1.0D);
									if (!effectApplied) {
										if (living instanceof Player player) {
											if (!player.isCreative()) {
												int uses = 0;
												if (weapon.getOrCreateTagElement("Gel_" + i).contains("Uses"))
													uses = weapon.getOrCreateTagElement("Gel_" + i).getInt("Uses");
												weapon.getOrCreateTagElement("Gel_" + i).putInt("Uses", uses - 1);
												effectApplied = true;
											}
										}
										else {
											int uses = 0;
											if (weapon.getOrCreateTagElement("Gel_" + i).contains("Uses"))
												uses = weapon.getOrCreateTagElement("Gel_" + i).getInt("Uses");
											weapon.getOrCreateTagElement("Gel_" + i).putInt("Uses", uses - 1);
											effectApplied = true;
										}
									}
								}
								else {
									int previousEffectDuration = 0;
									if (Config.stackingEffects) if (target.getEffect(effect.getEffect()) != null)
										previousEffectDuration = target.getEffect(effect.getEffect()).getDuration();

									if (target.addEffect(new MobEffectInstance(effect.getEffect(), 20 * Config.effectDurationPerHit + previousEffectDuration, effect.getAmplifier()))) {
										if (!effectApplied) {
											if (living instanceof Player player) {
												if (!player.isCreative()) {
													int uses = 0;
													if (weapon.getOrCreateTagElement("Gel_" + i).contains("Uses"))
														uses = weapon.getOrCreateTagElement("Gel_" + i).getInt("Uses");
													weapon.getOrCreateTagElement("Gel_" + i).putInt("Uses", uses - 1);
													effectApplied = true;
												}
											}
											else {
												int uses = 0;
												if (weapon.getOrCreateTagElement("Gel_" + i).contains("Uses"))
													uses = weapon.getOrCreateTagElement("Gel_" + i).getInt("Uses");
												weapon.getOrCreateTagElement("Gel_" + i).putInt("Uses", uses - 1);
												effectApplied = true;
											}
										}
									}
								}
							}
						}
					}

					for (int i = 0; i < size; i++) {
						int uses = 0;
						if (weapon.getOrCreateTagElement("Gel_" + i).contains("Uses"))
							uses = weapon.getOrCreateTagElement("Gel_" + i).getInt("Uses");

						if (uses <= 0) {
							weapon.getOrCreateTagElement("MagicGels")
									.putInt("Size", weapon.getOrCreateTagElement("MagicGels")
											.getInt("Size") - 1);
							if (weapon.getOrCreateTagElement("MagicGels").getInt("Size") <= 0)
								weapon.removeTagKey("MagicGels");

							for (int j = i; j < size - 1; j++)
								if (weapon.getOrCreateTag().contains("Gel_" + (j + 1)))
									weapon.getOrCreateTagElement("Gel_" + j).merge(weapon.getOrCreateTagElement("Gel_" + (j + 1)));
							weapon.removeTagKey("Gel_" + (size - 1));
							size -= 1;
							i--;
						}
					}
				}
			}
		}
	}

}