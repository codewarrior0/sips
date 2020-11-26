package mod.codewarrior.sips.compat.rei;

import alexiil.mc.lib.attributes.fluid.FluidContainerRegistry;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import com.google.common.collect.ImmutableList;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import mod.codewarrior.sips.registry.SipsItems;
import mod.codewarrior.sips.utils.FluidUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class SipsREIDisplay implements RecipeDisplay {
    private static final ItemStack lilSip = new ItemStack(SipsItems.LIL_SIP);
    private static final ItemStack bigChug = new ItemStack(SipsItems.BIG_CHUG);
    public static List<EntryStack> inputs = EntryStack.ofItemStacks(ImmutableList.of(lilSip, bigChug));
    private List<EntryStack> buckets;
    private List<EntryStack> results;
    private FluidKey fluid;

    public SipsREIDisplay(FluidKey fluid) {
        List<ItemStack> stacks = new ArrayList<>();
        Set<Item> items = FluidContainerRegistry.getFullContainersFor(fluid);
        if (items.isEmpty()) {
            if (fluid.getRawFluid() != null)
                stacks.add(new ItemStack(fluid.getRawFluid().getBucketItem()));
        } else {
            items.forEach(item -> stacks.add(new ItemStack(item)));
        }
        this.buckets = EntryStack.ofItemStacks(stacks);
        ItemStack sip = lilSip.copy();
        FluidUtil.setFluid(sip, fluid);
        FluidUtil.setAmount(sip, FluidUtil.getMaxCapacity(sip));
        ItemStack chug = bigChug.copy();
        FluidUtil.setFluid(chug, fluid);
        FluidUtil.setAmount(chug, FluidUtil.getMaxCapacity(chug));
        this.results = EntryStack.ofItemStacks(ImmutableList.of(sip, chug));
        this.fluid = fluid;
    }

    public SipsREIDisplay(Set<Potion> potions) {
        List<ItemStack> pots = new ArrayList<>();
        List<ItemStack> filled = new ArrayList<>();
        potions.forEach(potion -> {
            FluidKey key = FluidKeys.get(potion);
            ItemStack pot = PotionUtil.setPotion(new ItemStack(Items.POTION), potion);
            pots.add(pot);
            ItemStack sip = lilSip.copy();
            FluidUtil.setFluid(sip, key);
            FluidUtil.setAmount(sip, FluidUtil.getMaxCapacity(sip));
            filled.add(sip);
        });
        potions.forEach(potion -> {
            FluidKey key = FluidKeys.get(potion);
            ItemStack chug = bigChug.copy();
            FluidUtil.setFluid(chug, key);
            FluidUtil.setAmount(chug, FluidUtil.getMaxCapacity(chug));
            filled.add(chug);
        });
        this.buckets = EntryStack.ofItemStacks(pots);
        this.results = EntryStack.ofItemStacks(filled);
        this.fluid = FluidKeys.get(Potions.LUCK);
    }

    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        List<List<EntryStack>> list = new ArrayList<>();
        list.add(0, inputs);
        list.add(1, buckets);
        list.add(2, fluid.getRawFluid() != null
                ? Collections.singletonList(EntryStack.create(fluid.getRawFluid()))
                : Collections.emptyList());
        return list;
    }

    @NotNull
    @Override
    public List<List<EntryStack>> getResultingEntries() {
        return Collections.singletonList(results);
    }

    public FluidKey getFluid() {
        return this.fluid;
    }

    @Override
    public @NotNull Identifier getRecipeCategory() {
        return SipsREIPlugin.BEVERAGES;
    }
}
