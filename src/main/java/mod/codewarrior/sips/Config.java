package mod.codewarrior.sips;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber
public class Config {

    public static Map<String, Sippable> stats = new HashMap<>();

    @net.minecraftforge.common.config.Config(modid = SipsMod.MODID)
    public static class SipsConfig {
        @Comment({"Drinkable fluids. Format: fluid_name, half-shanks (0-20), saturation (0.0-1.0) [, damage [, potion_name, potion_duration, potion_level] ... ]"})
        public static String[] sips = new String[]{
                "water, 0, 0",
                "mushroom_stew, 6, 0.3",
                "aerotheum, 0, 0, 1, minecraft:levitation, 30s, 0",
                "glowstone, 0, 0, 1, minecraft:speed, 30s, 0, minecraft:jump_boost, 30s, 0, minecraft:glowing, 2m, 0",
                "petrotheum, 0, 0, 1, minecraft:haste, 30s, 0",
                "redstone, 0, 0, 1, minecraft:haste, 30s, 0",
                "astralsorcery.liquidstarlight, 0, 0, 1, minecraft:night_vision, 2m, 0, minecraft:slowness, 30s, 0, minecraft:mining_fatigue, 30s, 0",
                "nutrient_distillation, 2, 0.1, 0",
                //"ender_distillation, 0, 0, 0, "
                "vapor_of_levity, 0, 0, 5, minecraft:levitation, 30s, 0",
                "hootch, 1, 0.05, 1, minecraft:nausea, 30s, 0",
                "rocket_fuel, 0, 0, 5, minecraft:speed, 30s, 1, minecraft:haste, 30s, 1",
                "liquid_sunshine, 0, 0, 0, minecraft:glowing, 2m, 0",
                "cloud_seed, 0, 0, 0, minecraft:slowness, 30s, 0",
                "cloud_seed_concentrated, 0, 0, 0, minecraft:slowness, 2m, 1",
                "for.honey, 3, 0.2, 0"
        };
        @Comment({"Unlisted fluids will deal this much damage per Kelvin above 320 or below 260. (Default fluid temperature: 300K; Lava: 1300K; Cryotheum: 50K"})
        public static float temperatureDamagePerKelvin = 0.1f;
        @Comment({"Unlisted fluids will set the player on fire or apply slowness and fatigue effects if they are too hot or cold."})
        public static boolean temperatureEffects = true;

        public static Compat compat = new Compat();

        public static class Compat extends Object {
            @Comment({"Sipping Thermal Foundation's Resonant Ender or Primal Mana will randomly teleport the drinker."})
            public boolean thermalFoundation = true;
            @Comment({"Sipping Thermal Expansion potion fluids will apply their potion effects."})
            public boolean thermalExpansion = true;
            @Comment({"If a Sip item is Olive Oiled via the Rustic mod, it will slip from the player's hand when drunk with this probability (0.0-1.0)"})
            public float slipChance = 0.25f;

        }

    }

    public static void put(Sippable stats) {
        Config.stats.put(stats.fluidName, stats);
    }

    public static void preInit(FMLPreInitializationEvent event)
    {
        onConfigUpdate();
        if(SipsConfig.compat.thermalFoundation) {
            addThermalFluids();
        }

        if(Loader.isModLoaded("rustic")) {
            try {
                RusticCompat.addFluids();
            } catch (Exception e) {
                SipsMod.logger.error("Failed to initialize rustic compat", e);
            }
        }
    }

    public static void addThermalFluids() {
        put(new Sippable("ender") {
            @Override
            public void onSipped(FluidStack drank, World world, EntityPlayer player) {
                BlockPos randPos = player.getPosition().add(-8 + world.rand.nextInt(17), world.rand.nextInt(8), -8 + world.rand.nextInt(17));

                if (!world.getBlockState(randPos).getMaterial().isSolid()) {
                    player.setPosition(randPos.getX(), randPos.getY(), randPos.getZ());
                    player.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);

                }
            }
        });

        put(new Sippable("mana") {
            @Override
            public void onSipped(FluidStack drank, World world, EntityPlayer player) {
                BlockPos randPos = player.getPosition().add(-8 + world.rand.nextInt(17), world.rand.nextInt(8), -8 + world.rand.nextInt(17));

                if (!world.getBlockState(randPos).getMaterial().isSolid()) {
                    player.setPosition(randPos.getX(), randPos.getY(), randPos.getZ());
                    player.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);

                }
            }
        });
    }

    private static void onConfigUpdate()
    {
        Arrays.stream(SipsConfig.sips).map(Sippable::fromConfig).filter(Objects::nonNull).forEachOrdered(stats -> {
            put(stats);
        });
    }
}
