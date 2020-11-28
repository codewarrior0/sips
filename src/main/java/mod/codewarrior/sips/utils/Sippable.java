package mod.codewarrior.sips.utils;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mod.codewarrior.sips.SipsMod;
import mod.codewarrior.sips.registry.SippableRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Sippable {
    public static final Sippable UNDEFINED = new Sippable(0, 0, 0);

    public int shanks = 0;
    public float saturation = 0f;
    public float damage = 0.0f;

    public List<Effect> effects = new ArrayList<>();

    /**
     * You can use either of these 3 constructors, depending on which values you want to change;
     * The empty one is mainly for overriding {@link Sippable#onSipped(FluidKey, World, PlayerEntity)}
     */
    public Sippable() {}

    public Sippable(int shanks, float saturation, float damage) {
        this.shanks = shanks;
        this.saturation = saturation;
        this.damage = damage;
    }

    public Sippable(int shanks, float saturation, float damage, List<Effect> effects) {
        this.shanks = shanks;
        this.saturation = saturation;
        this.damage = damage;
        this.effects.addAll(effects);
    }

    public static void fromConfig(Map.Entry<String, JsonElement> entry) {
        if (!entry.getValue().isJsonObject()) {
            SipsMod.LOGGER.error(entry + " is not a JsonObject!");
            return;
        }
        JsonObject o = entry.getValue().getAsJsonObject();
        String sid = entry.getKey();
        boolean isTag = false;
        boolean heuristics = false;
        if (sid.charAt(0) == '#') {
            isTag = true;
            sid = sid.substring(1);
        } else if (!sid.contains(":")) {
            heuristics = true;
        }
        int shanks = 0;
        float saturation = 0;
        float damage = 0;

        if (o.has("hunger")) {
            try {
                shanks = Integer.parseInt(o.get("hunger").getAsString());
            } catch (NumberFormatException e) {
                SipsMod.LOGGER.error("Could not parse hunger integer '{}' in fluid stats '{}'", o.get("hunger"), o);
                return;
            }
        }
        if (o.has("saturation")) {
            try {
                saturation = Float.parseFloat(o.get("saturation").getAsString());
            } catch (NumberFormatException e) {
                SipsMod.LOGGER.error("Could not parse saturation float '{}' in fluid stats '{}'", o.get("saturation"), o);
                return;
            }
        }
        if (o.has("damage")) {
            try {
                damage = Float.parseFloat(o.get("damage").getAsString());
            } catch (NumberFormatException e) {
                SipsMod.LOGGER.error("Could not parse damage float '{}' in fluid stats '{}'", o.get("damage"), o);
                return;
            }
        }

        List<Effect> effects = new ArrayList<>();

        if (o.get("effects") != null) {
            JsonObject fx = o.getAsJsonObject("effects");
            fx.entrySet().forEach(effect -> {
                Effect e = parseEffect(effect);
                if (e != null) effects.add(e);
            });
        }

        Identifier id = new Identifier(sid);
        Sippable sippable = new Sippable(shanks, saturation, damage, effects);
        if (isTag) {
            fromTag(id, sippable);
        } else if (heuristics) {
            fromPredicate(sid, sippable);
        } else {
            Fluid f = Registry.FLUID.get(id);
            if (f != Fluids.EMPTY) {
                FluidKey fluidKey = FluidKeys.get(f);
                SippableRegistry.SIPS.put(fluidKey, sippable);
            }
        }
    }

    /**
     * Queries the TagRegistry for a specified tag, then registers the given Sippable for all of its values.
     * @param id the target Tag Identifier
     * @param sippable Sippable object shared between the Tag's values
     */
    public static void fromTag(Identifier id, Sippable sippable) {
        Tag<Fluid> tag = ServerTagManagerHolder.getTagManager().getFluids().getTag(id);
        if (tag != null && !tag.values().isEmpty()) {
            tag.values().forEach(fluid -> {
                if (fluid != Fluids.EMPTY) {
                    FluidKey key = FluidKeys.get(fluid);
                    SippableRegistry.SIPS.put(key, sippable);
                }
            });
        }
    }

    /**
     * Loops the registry using the given predicate, and registers the given Sippable for any match.
     * @param predicate "id" of the target fluid(s)
     * @param sippable Sippable object shared between them
     */
    public static void fromPredicate(String predicate, Sippable sippable) {
        Registry.FLUID.forEach(fluid -> {
            if (Registry.FLUID.getId(fluid).getPath().equals(predicate)) {
                FluidKey key = FluidKeys.get(fluid);
                SippableRegistry.SIPS.put(key, sippable);
            }
        });
    }

    public static Sippable fromPacket(PacketByteBuf buf) {
        int shanks = buf.readInt();
        float saturation = buf.readFloat();
        float damage = buf.readFloat();
        int loop = buf.readInt();
        List<Effect> effects = new ArrayList<>();
        for (int i = 0; i < loop; i++) {
            effects.add(Effect.fromPacket(buf));
        }
        return new Sippable(shanks, saturation, damage, effects);
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeInt(this.shanks);
        buf.writeFloat(this.saturation);
        buf.writeFloat(this.damage);
        buf.writeInt(this.effects.size());
        this.effects.forEach(e -> e.toPacket(buf));
    }

    public static Effect parseEffect(Map.Entry<String, JsonElement> effect) {
        Identifier name;
        int duration = 1;
        int level = 0;
        boolean showParticles = true;
        boolean showIcon = true;

        name = new Identifier(effect.getKey());
        JsonObject o = effect.getValue().getAsJsonObject();
        if (o.has("duration")) {
            try {
                int mult = 1;
                String durationTxt = o.get("duration").getAsString();
                if (durationTxt.endsWith("s")) {
                    mult = 20;
                    durationTxt = durationTxt.substring(0, durationTxt.length() - 1);
                } else if (durationTxt.endsWith("m")) {
                    mult = 20 * 60;
                    durationTxt = durationTxt.substring(0, durationTxt.length() - 1);
                } else if (durationTxt.endsWith("h")) {
                    mult = 20 * 60 * 60;
                    durationTxt = durationTxt.substring(0, durationTxt.length() - 1);
                } else if (durationTxt.endsWith("t")) { /* somebody might try this */
                    mult = 1;
                    durationTxt = durationTxt.substring(0, durationTxt.length() - 1);
                }

                duration = Integer.parseInt(durationTxt) * mult;
            } catch (NumberFormatException e) {
                SipsMod.LOGGER.error("Could not parse potion duration integer '{}' in fluid stats '{}'", o.get("duration"), o);
                return null;
            }
        }

        if (o.has("level")) {
            try {
                level = Integer.parseInt(o.get("level").getAsString());
            } catch (NumberFormatException e) {
                SipsMod.LOGGER.error("Could not parse potion level integer '{}' in fluid stats '{}'", o.get("level"), o);
                return null;
            }
        }

        if (o.has("showParticles")) {
            try {
                showParticles = o.get("showParticles").getAsBoolean();
            } catch (Exception e) {
                SipsMod.LOGGER.error("Could not parse potion showParticles boolean '{}' in fluid stats '{}'", o.get("showParticles"), o);
            }
        }

        if (o.has("showIcon")) {
            try {
                showIcon = o.get("showIcon").getAsBoolean();
            } catch (Exception e) {
                SipsMod.LOGGER.error("Could not parse potion showIcon boolean '{}' in fluid stats '{}'", o.get("showIcon"), o);
            }
        }
        return new Effect(name, duration, level, showParticles, showIcon);
    }

    /**
     * Override this method to add custom effects to your Sippable without using StatusEffects
     * Keep in mind that, for the time being, this only gets called serverside in SMP.
     */
    public void onSipped(FluidKey drank, World world, PlayerEntity player) {}

    public static class Effect {
        Identifier name;
        int duration;
        int level;
        boolean showParticles;
        boolean showIcon;

        public Effect(Identifier name, int duration, int level, boolean showParticles, boolean showIcon) {
            this.name = name;
            this.duration = duration;
            this.level = level;
            this.showParticles = showParticles;
            this.showIcon = showIcon;
        }

        public StatusEffectInstance getEffect() {
            StatusEffect potion = Registry.STATUS_EFFECT.get(name);
            if (potion != null) {
                return new StatusEffectInstance(potion, duration, level, false, showParticles, showIcon);
            }
            return null;
        }

        public static Effect fromPacket(@NotNull PacketByteBuf buf) {
            Identifier name = buf.readIdentifier();
            int duration = buf.readInt();
            int level = buf.readInt();
            return new Effect(name, duration, level, false, false);
        }

        public void toPacket(@NotNull PacketByteBuf buf) {
            buf.writeIdentifier(name);
            buf.writeInt(duration);
            buf.writeInt(level);
        }
    }
}
