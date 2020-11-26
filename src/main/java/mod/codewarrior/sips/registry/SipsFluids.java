package mod.codewarrior.sips.registry;

import alexiil.mc.lib.attributes.fluid.FluidContainerRegistry;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import mod.codewarrior.sips.SipsMod;
import mod.codewarrior.sips.fluids.MilkFluid;
import mod.codewarrior.sips.fluids.MushroomStewFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;

public class SipsFluids {
    public static final FlowableFluid FLOWING_MILK = new MilkFluid.Flowing();
    public static final FlowableFluid MILK = new MilkFluid.Still();
    public static final FlowableFluid FLOWING_MUSHROOM_STEW = new MushroomStewFluid.Flowing();
    public static final FlowableFluid MUSHROOM_STEW = new MushroomStewFluid.Still();

    public static void init() {
        register("flowing_milk", FLOWING_MILK);
        register("milk", MILK, Items.BUCKET, Items.MILK_BUCKET);
        register("flowing_mushroom_stew", FLOWING_MUSHROOM_STEW);
        register("mushroom_stew", MUSHROOM_STEW, Items.BOWL, Items.MUSHROOM_STEW);
    }

    private static void register(String id, Fluid value) {
        Registry.register(Registry.FLUID, SipsMod.getId(id), value);
    }

    private static void register(String id, Fluid value, Item empty, Item full) {
        register(id, value);
        FluidContainerRegistry.mapContainer(empty, full, FluidKeys.get(value).withAmount(FluidAmount.BUCKET));
    }
}
