package mod.codewarrior.sips;

import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class Config {

    public static Map<String, FluidStats> stats = new HashMap<>();

    public static class FluidStats {
        public String fluidName;
        public int shanks;
        public float saturation;
        public float damage = 0.0f;
        public String potionName;
        public int potionDuration;
        public int potionLevel;

        public FluidStats(String fluidName, int shanks, float saturation, float damage, String potionName, int potionDuration, int potionLevel) {
            this.fluidName = fluidName;
            this.shanks = shanks;
            this.saturation = saturation;
            this.damage = damage;
            this.potionName = potionName;
            this.potionDuration = potionDuration;
            this.potionLevel = potionLevel;
        }

        public static FluidStats fromConfig(String configLine) {
            List<String> fields = Arrays.stream(configLine.split(",")).map(String::trim).collect(Collectors.toList());
            if (fields.size() < 3) {
                SipsMod.logger.error("Not enough fields in fluid stats line '%s'", configLine);
                return null;
            }
            String fluidName = fields.get(0);
            int shanks;
            float saturation;
            float damage = 0.0f;
            String potionName = null;
            int potionDuration = 0;
            int potionLevel = 0;

            try {
                shanks = Integer.parseInt(fields.get(1));
            } catch (NumberFormatException e) {
                SipsMod.logger.error("Could not parse integer '%s' in fluid stats line '%s'", fields.get(1), configLine);
                return null;
            }
            try {
                saturation = Float.parseFloat(fields.get(2));
            } catch (NumberFormatException e) {
                SipsMod.logger.error("Could not parse float '%s' in fluid stats line '%s'", fields.get(2), configLine);
                return null;
            }

            if(fields.size() > 3) {
                try {
                    damage = Float.parseFloat(fields.get(3));
                } catch (NumberFormatException e) {
                    SipsMod.logger.error("Could not parse float '%s' in fluid stats line '%s'", fields.get(3), configLine);
                    return null;
                }
            }

            if(fields.size() > 4) {
                potionName = fields.get(4);
            }

            if(fields.size() > 5) {
                try {
                    potionDuration = Integer.parseInt(fields.get(5));
                } catch (NumberFormatException e) {
                    SipsMod.logger.error("Could not parse integer '%s' in fluid stats line '%s'", fields.get(5), configLine);
                    return null;
                }
            }

            if(fields.size() > 6) {
                try {
                    potionLevel = Integer.parseInt(fields.get(6));
                } catch (NumberFormatException e) {
                    SipsMod.logger.error("Could not parse integer '%s' in fluid stats line '%s'", fields.get(6), configLine);
                    return null;
                }
            }

            return new FluidStats(fluidName, shanks, saturation, damage, potionName, potionDuration, potionLevel);
        }
    }
    @net.minecraftforge.common.config.Config(modid = SipsMod.MODID)
    public static class SipsConfig {
        @Comment({"Drinkable fluids. Format: fluid_name, half-shanks (0-20), saturation (0.0-1.0)[, damage[, potion_name, potion_duration[, potion_level]]]" +
                "\nUnlisted fluids will have zero food value and will deal damage if they are too hot or cold."})
        public static String[] sips = new String[]{"water, 0, 0.0", "lava, 0, 0.0, 1000", "mushroom_stew, 6, 0.6"};

    }

    public static void preInit(FMLPreInitializationEvent event)
    {
        onConfigUpdate();
    }

    private static void onConfigUpdate()
    {
        Arrays.stream(SipsConfig.sips).map(FluidStats::fromConfig).filter(Objects::nonNull).forEachOrdered(stats -> {
            Config.stats.put(stats.fluidName, stats);
        });
    }
}
