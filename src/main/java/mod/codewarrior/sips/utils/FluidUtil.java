package mod.codewarrior.sips.utils;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.PotionFluidKey;
import mod.codewarrior.sips.items.SipsItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.List;

public interface FluidUtil {

    static boolean isEmpty(ItemStack stack) {
        return stack.getSubTag("fluid") == null;
    }

    static FluidKey getFluid(ItemStack stack) {
        return isEmpty(stack) ? FluidKeys.EMPTY : FluidKey.fromTag(stack.getSubTag("fluid"));
    }

    static void setFluid(ItemStack stack, FluidKey fluid) {
        stack.getOrCreateTag().put("fluid", fluid.toTag());
    }

    static int getAmount(ItemStack stack) {
        if (stack.getTag() != null) {
            return stack.getTag().getInt("amount");
        } else {
            return 0;
        }
    }

    static void addAmount(ItemStack stack, int amount, int max) {
        setAmount(stack, Math.min(getAmount(stack) + amount, max));
    }

    static void setAmount(ItemStack stack, int amount) {
        if (amount != 0) {
            stack.getOrCreateTag().putInt("amount", amount);
        } else {
            stack.removeSubTag("amount");
            stack.removeSubTag("fluid");
        }
    }

    static int getMaxCapacity(ItemStack stack) {
        return stack.getItem() instanceof SipsItem ? ((SipsItem) stack.getItem()).maxCapacity : 0;
    }

    static void sip(ItemStack stack) {
        int amount = getAmount(stack);
        if (amount > 249) {
            setAmount(stack, amount - 250);
        }
    }

    @Environment(EnvType.CLIENT)
    static void appendTooltip(ItemStack stack, List<Text> tooltip, int sip, int max) {
        FluidKey fluid = getFluid(stack);
        if (!fluid.isEmpty()) {
            if (fluid instanceof PotionFluidKey) {
                PotionFluidKey pot = (PotionFluidKey) fluid;
                PotionUtil.buildTooltip(PotionUtil.setPotion(new ItemStack(Items.POTION), pot.potion), tooltip, 1f);
            } else {
                tooltip.add(fluid.name);
            }
            if (Screen.hasShiftDown()) {
                tooltip.add(new TranslatableText("desc.sips.amount", getAmount(stack), max).formatted(Formatting.GRAY, Formatting.ITALIC));
            } else {
                int amt = getAmount(stack) / sip;
                tooltip.add(new TranslatableText("desc.sips.sips", amt).formatted(Formatting.GRAY, Formatting.ITALIC));
            }
        } else {
            tooltip.add(new TranslatableText("desc.sips.empty").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        }
    }
}
