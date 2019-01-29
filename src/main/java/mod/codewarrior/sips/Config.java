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
        public static String[] sips = new String[]{"water, 0, 0.0", "lava, 0, 0.0, 1000", "mushroom_stew, 6, 0.6"};
        @Comment({"Unlisted fluids will deal damage if they are too hot or cold."})
        public static boolean temperatureDamage = true;

        public static Compat compat = new Compat();

        public static class Compat extends Object {
            @Comment({"Sipping Thermal Foundation fluids will apply their potion effects. Resonant Ender will randomly teleport the drinker."})
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
        put(new Sippable("aerotheum", 0, 0, 0, ImmutableList.of(
                new Sippable.Effect("levitation", 30 * 20, 0))));

        put(new Sippable("cryotheum", 0, 0, 15, ImmutableList.of(
                new Sippable.Effect("slowness", 30 * 20, 1),
                new Sippable.Effect("mining_fatigue", 30 * 20, 1))));

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

        put(new Sippable("glowstone", 0, 0, 15, ImmutableList.of(
                new Sippable.Effect("speed", 30 * 20, 0),
                new Sippable.Effect("jump_boost", 30 * 20, 0),
                new Sippable.Effect("glowing", 2 * 60 * 20, 0)
        )));

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

        put(new Sippable("petrotheum", 0, 0, 5, ImmutableList.of(
                new Sippable.Effect("haste", 30 * 20, 0)
        )));

        put(new Sippable("pyrotheum", 0, 0, 15));

        put(new Sippable("redstone", 0, 0, 5, ImmutableList.of(
                new Sippable.Effect("haste", 30 * 20, 0)
        )));

    }

    private static void onConfigUpdate()
    {
        Arrays.stream(SipsConfig.sips).map(Sippable::fromConfig).filter(Objects::nonNull).forEachOrdered(stats -> {
            put(stats);
        });
    }
}
