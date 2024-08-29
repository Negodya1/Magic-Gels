package com.negodya1.magicgels.mixin;

import com.negodya1.magicgels.Config;
import com.negodya1.magicgels.MagicGels;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = ItemStack.class, priority = 400)
public class ItemMixin {
     /**
     * Add the list of magic gels to tooltip lines.
     */
    @ModifyVariable(method = "getTooltipLines", at = @At(value = "TAIL"), ordinal = 0)
    private List<Component> magicgels_ModifyTooltip(List<Component> list, @Nullable Player p_41652_, TooltipFlag p_41653_) {
        ItemStack ths = (ItemStack) (Object) this;

        if (!ths.hasTag()) return list;

        if (ths.getTag().contains("MagicGels")) {
            int size = 0;
            if (ths.getOrCreateTagElement("MagicGels").contains("Size"))
                size = ths.getOrCreateTagElement("MagicGels").getInt("Size");
            if (size > 0) {
                list.add(Component.literal(""));
                list.add(Component.translatable("tooltip." +  MagicGels.MODID + ".gels_applied")
                        .append(" (" + size + "/" + Config.magicGelsLimit + "):").withStyle(ChatFormatting.DARK_PURPLE));

                for (int i = 0; i < size; i++) {
                    if (ths.getTag().contains("Gel_" + i)) {
                        if (ths.getOrCreateTagElement("Gel_" + i).contains("Potion") &&
                                ths.getOrCreateTagElement("Gel_" + i).contains("Uses")) {
                            ItemStack gel = ItemStack.EMPTY;
                            gel.deserializeNBT(ths.getOrCreateTagElement("Gel_" + i).getCompound("Potion"));
                            for (MobEffectInstance effectInstance : PotionUtils.getMobEffects(gel)) {
                                MutableComponent mutablecomponent = Component.literal(" ").append(Component.translatable(effectInstance.getDescriptionId()));

                                if (effectInstance.getAmplifier() > 0) {
                                    mutablecomponent = Component.translatable("potion.withAmplifier", mutablecomponent,
                                            Component.translatable("potion.potency." + effectInstance.getAmplifier()));
                                }

                                list.add(mutablecomponent.append(" (" +
                                        ths.getOrCreateTagElement("Gel_" + i).getInt("Uses") + "/" +
                                                effectInstance.getDuration() / (20 * Config.effectDurationPerHit) + ")")
                                        .withStyle(effectInstance.getEffect().getCategory().getTooltipFormatting()));
                            }

                            if (i < size - 1)
                                list.add(Component.literal(""));
                        }
                    }
                }


            }
        }

        return list;
    }
}
