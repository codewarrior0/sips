package mod.codewarrior.sips;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Sippable {

    public static class Effect {
        public String name;
        public int duration;
        public int level;

        public Effect(String name, int duration, int level) {
            this.name = name;
            this.duration = duration;
            this.level = level;
        }

        public PotionEffect getEffect() {
            Potion potion = Potion.getPotionFromResourceLocation(name);
            if(potion != null) {
                return new PotionEffect(potion, duration, level);
            }
            return null;
        }
    }

    public static final Sippable UNDEFINED = new Sippable("", 0, 0, 0);

    public String fluidName;
    public int shanks = 0;
    public float saturation = 0f;
    public float damage = 0.0f;

    public List<Effect> effects = new ArrayList<>();

    public Sippable(String fluidName) {
        this.fluidName = fluidName;
    }

    public Sippable(String fluidName, int shanks, float saturation, float damage) {
        this.fluidName = fluidName;
        this.shanks = shanks;
        this.saturation = saturation;
        this.damage = damage;
    }

    public Sippable(String fluidName, int shanks, float saturation, float damage, List<Effect> effects) {
        this.fluidName = fluidName;
        this.shanks = shanks;
        this.saturation = saturation;
        this.damage = damage;
        this.effects.addAll(effects);
    }

    public static Sippable fromConfig(String configLine) {
        List<String> fields = Arrays.stream(configLine.split(",")).map(String::trim).collect(Collectors.toList());
        if (fields.size() == 0 || (fields.size() == 1 && fields.get(0).length() == 0)) return null;

        if (fields.size() < 3) {
            SipsMod.logger.error("Not enough fields in fluid stats line '{}'", configLine);
            return null;
        }
        String fluidName = fields.get(0);
        int shanks;
        float saturation;
        float damage = 0.0f;


        try {
            shanks = Integer.parseInt(fields.get(1));
        } catch (NumberFormatException e) {
            SipsMod.logger.error("Could not parse shanks integer '{}' in fluid stats line '{}'", fields.get(1), configLine);
            return null;
        }
        try {
            saturation = Float.parseFloat(fields.get(2));
        } catch (NumberFormatException e) {
            SipsMod.logger.error("Could not parse saturation float '{}' in fluid stats line '{}'", fields.get(2), configLine);
            return null;
        }

        if(fields.size() > 3) {
            try {
                damage = Float.parseFloat(fields.get(3));
            } catch (NumberFormatException e) {
                SipsMod.logger.error("Could not parse damage float '{}' in fluid stats line '{}'", fields.get(3), configLine);
                return null;
            }
        }
        List<Effect> effects = new ArrayList<>();

        if(fields.size() > 4) {
            fields = fields.subList(4, fields.size());

            while (fields.size() > 0) {
                Effect effect = parseEffect(fields, configLine);
                if (effect != null) effects.add(effect);
            }
        }
        return new Sippable(fluidName, shanks, saturation, damage, effects);
    }

    public static Effect parseEffect(List<String> fields, String configLine) {
        String name = null;
        int duration = 0;
        int level = 0;

        name = fields.remove(0);

        if (fields.size() > 0) {
            try {
                duration = Integer.parseInt(fields.get(0));
                fields.remove(0);
            } catch (NumberFormatException e) {
                SipsMod.logger.error("Could not parse potion duration integer '{}' in fluid stats line '{}'", fields.get(0), configLine);
                return null;
            }
        }

        if (fields.size() > 0) {
            try {
                level = Integer.parseInt(fields.get(0));
                fields.remove(0);
            } catch (NumberFormatException e) {
                SipsMod.logger.error("Could not parse potion level integer '{}' in fluid stats line '{}'", fields.get(0), configLine);
                return null;
            }
        }
        return new Effect(name, duration, level);
    }

    public void onSipped(FluidStack drank, World world, EntityPlayer player) {

    }
}
