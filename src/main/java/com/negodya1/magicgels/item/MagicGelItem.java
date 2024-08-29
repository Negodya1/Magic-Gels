package com.negodya1.magicgels.item;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.negodya1.magicgels.Config;
import com.negodya1.magicgels.MagicGels;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MagicGelItem extends PotionItem {

    public MagicGelItem(Properties properties) {
        super(properties);
    }

    public int getUseDuration(ItemStack p_43001_) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack p_42997_) {
        return UseAnim.BRUSH;
    }

    @Override
    public InteractionResult useOn(UseOnContext p_220235_) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.getOffhandItem().isDamageableItem() && !(player.getOffhandItem().getItem() instanceof ArmorItem))
            return ItemUtils.startUsingInstantly(level, player, hand);

        if (player.getMainHandItem().isDamageableItem() && !(player.getMainHandItem().getItem() instanceof ArmorItem))
            return ItemUtils.startUsingInstantly(level, player, hand);

        return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        Player player = entity instanceof Player ? (Player)entity : null;
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, itemStack);
        }

        ItemStack applyingItem = ItemStack.EMPTY;
        if (entity.getMainHandItem().getItem() instanceof MagicGelItem) {
            if (entity.getOffhandItem().isDamageableItem() && !(entity.getOffhandItem().getItem() instanceof ArmorItem))
                applyingItem = entity.getOffhandItem();
        }
        else if (entity.getOffhandItem().getItem() instanceof MagicGelItem) {
            if (entity.getMainHandItem().isDamageableItem() && !(entity.getMainHandItem().getItem() instanceof ArmorItem))
                applyingItem = entity.getMainHandItem();
        }

        if (!level.isClientSide) {
            int size = 0;
            if (applyingItem.getOrCreateTagElement("MagicGels").contains("Size"))
                size = applyingItem.getOrCreateTagElement("MagicGels").getInt("Size");

            if (size >= Config.magicGelsLimit) return itemStack;

            applyingItem.getOrCreateTagElement("Gel_" + size)
                    .put("Potion", itemStack.serializeNBT());

            Potion potion = PotionUtils.getPotion(itemStack);
            if (potion.hasInstantEffects())
                applyingItem.getOrCreateTagElement("Gel_" + size)
                        .putInt("Uses", 0);
            else if (potion.getEffects().size() > 0)
                applyingItem.getOrCreateTagElement("Gel_" + size)
                        .putInt("Uses", potion.getEffects().get(0).getDuration() / (20 * Config.effectDurationPerHit));


            applyingItem.getOrCreateTagElement("MagicGels").putInt("Size", size + 1);
        }

        if (player != null) {
            int size = 0;
            if (applyingItem.getOrCreateTagElement("MagicGels").contains("Size"))
                size = applyingItem.getOrCreateTagElement("MagicGels").getInt("Size");

            if (size >= Config.magicGelsLimit) return itemStack;

            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }

        entity.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        return itemStack;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> componentList, TooltipFlag flag) {
        List<MobEffectInstance> effectInstances = PotionUtils.getMobEffects(stack);

        List<Pair<Attribute, AttributeModifier>> list = Lists.newArrayList();
        if (effectInstances.isEmpty()) {
            componentList.add(Component.translatable("effect.none").withStyle(ChatFormatting.GRAY));
        } else {
            for(MobEffectInstance mobeffectinstance : effectInstances) {
                MutableComponent mutablecomponent = Component.translatable(mobeffectinstance.getDescriptionId());
                MobEffect mobeffect = mobeffectinstance.getEffect();
                Map<Attribute, AttributeModifier> map = mobeffect.getAttributeModifiers();
                if (!map.isEmpty()) {
                    for(Map.Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
                        AttributeModifier attributemodifier = entry.getValue();
                        AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), mobeffect.getAttributeModifierValue(mobeffectinstance.getAmplifier(), attributemodifier), attributemodifier.getOperation());
                        list.add(new Pair<>(entry.getKey(), attributemodifier1));
                    }
                }

                if (mobeffectinstance.getAmplifier() > 0) {
                    mutablecomponent = Component.translatable("potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + mobeffectinstance.getAmplifier()));
                }

                if (!mobeffectinstance.endsWithin(20)) {
                    mutablecomponent = Component.translatable("potion.withDuration", mutablecomponent, Component.literal("" + mobeffectinstance.getDuration() / (20 * Config.effectDurationPerHit)));
                }

                componentList.add(mutablecomponent.withStyle(mobeffect.getCategory().getTooltipFormatting()));
            }
        }

        if (!list.isEmpty()) {
            componentList.add(CommonComponents.EMPTY);
            componentList.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            for(Pair<Attribute, AttributeModifier> pair : list) {
                AttributeModifier attributemodifier2 = pair.getSecond();
                double d0 = attributemodifier2.getAmount();
                double d1;
                if (attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    d1 = attributemodifier2.getAmount();
                } else {
                    d1 = attributemodifier2.getAmount() * 100.0D;
                }

                if (d0 > 0.0D) {
                    componentList.add(Component.translatable("attribute.modifier.plus." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().getDescriptionId())).withStyle(ChatFormatting.BLUE));
                } else if (d0 < 0.0D) {
                    d1 *= -1.0D;
                    componentList.add(Component.translatable("attribute.modifier.take." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().getDescriptionId())).withStyle(ChatFormatting.RED));
                }
            }
        }

    }

}
