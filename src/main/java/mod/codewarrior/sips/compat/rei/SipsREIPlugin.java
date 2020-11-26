package mod.codewarrior.sips.compat.rei;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import com.google.common.collect.ImmutableSet;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import mod.codewarrior.sips.SipsMod;
import mod.codewarrior.sips.config.SipsConfig;
import mod.codewarrior.sips.registry.SippableRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.fluid.Fluids;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;


@Environment(EnvType.CLIENT)
public class SipsREIPlugin implements REIPluginV0 {
    public static final Identifier PLUGIN = SipsMod.getId("rei_plugin");
    public static final Identifier BEVERAGES = SipsMod.getId("beverages");

    @Override
    public Identifier getPluginIdentifier() {
        return PLUGIN;
    }

    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        recipeHelper.registerCategory(new SipsREICategory());
    }

    @Override
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        List<FluidKey> sipless = new ArrayList<>();
        Registry.FLUID.forEach(fluid -> {
            if (fluid != Fluids.EMPTY && fluid.isStill(fluid.getDefaultState())) {
                FluidKey key = FluidKeys.get(fluid);
                if (SippableRegistry.SIPS.containsKey(key)) {
                    recipeHelper.registerDisplay(new SipsREIDisplay(key));
                } else if (SipsConfig.listDullBeverages()) {
                    sipless.add(key);
                }
            }
        });
        if (!sipless.isEmpty()) {
            for (FluidKey key : sipless) recipeHelper.registerDisplay(new SipsREIDisplay(key));
        }

        ImmutableSet.Builder<Potion> potions = ImmutableSet.builder();
        Registry.POTION.forEach(potion -> {
            if (potion != Potions.EMPTY && potion != Potions.WATER) {
                potions.add(potion);
            }
        });
        recipeHelper.registerDisplay(new SipsREIDisplay(potions.build()));
    }

    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        recipeHelper.removeAutoCraftButton(BEVERAGES);
    }
}
