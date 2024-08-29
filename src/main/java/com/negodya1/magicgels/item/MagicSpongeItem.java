package com.negodya1.magicgels.item;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.negodya1.magicgels.Config;
import com.negodya1.magicgels.MagicGels;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class MagicSpongeItem extends Item {
    public MagicSpongeItem(Properties properties) {
        super(properties);
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
        ItemStack applyingItem = ItemStack.EMPTY;
        if (player.getMainHandItem().getItem() instanceof MagicSpongeItem) {
            if (player.getOffhandItem().isDamageableItem() && !(player.getOffhandItem().getItem() instanceof ArmorItem))
                applyingItem = player.getOffhandItem();
        }
        else if (player.getOffhandItem().getItem() instanceof MagicSpongeItem) {
            if (player.getMainHandItem().isDamageableItem() && !(player.getMainHandItem().getItem() instanceof ArmorItem))
                applyingItem = player.getMainHandItem();
        }

        if (applyingItem.isEmpty())
            return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));

        if (!level.isClientSide) {
            int size = 0;
            if (!applyingItem.getOrCreateTag().contains("MagicGels"))
                return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));

            if (applyingItem.getOrCreateTagElement("MagicGels").contains("Size"))
                size = applyingItem.getOrCreateTagElement("MagicGels").getInt("Size");

            for (int i = 0; i < size; i++)
                applyingItem.removeTagKey("Gel_" + i);
            applyingItem.removeTagKey("MagicGels");

            if (size > 0) {
                player.getItemInHand(hand).hurtAndBreak(1, player, consumer -> {});
                player.getCooldowns().addCooldown(this, 40);
            }
        }

        player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltips, TooltipFlag flags) {
        super.appendHoverText(stack, world, tooltips, flags);
        tooltips.add(Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
    }

}
