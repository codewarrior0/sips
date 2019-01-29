package mod.codewarrior.sips;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import rustic.common.blocks.fluids.FluidDrinkable;
import rustic.common.blocks.fluids.ModFluids;

public class RusticCompat {
    public static void addFluids() {
        for (Fluid fluid : ModFluids.getFluids()) {
            if(fluid instanceof FluidDrinkable) {
                FluidDrinkable drink = (FluidDrinkable) fluid;
                Config.put(new Sippable(drink.getName()) {
                    @Override
                    public void onSipped(FluidStack drank, World world, EntityPlayer player) {
                        drink.onDrank(world, player, null, drank);
                    }
                });
            }
        }
    }
}
