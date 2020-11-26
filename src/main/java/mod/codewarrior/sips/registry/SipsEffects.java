package mod.codewarrior.sips.registry;

import mod.codewarrior.sips.SipsMod;
import mod.codewarrior.sips.effects.LavaDigestionEffect;
import mod.codewarrior.sips.effects.PurgeEffect;
import mod.codewarrior.sips.effects.ThermodynamicsEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.registry.Registry;

public class SipsEffects {
    public static final StatusEffect PURGE = new PurgeEffect();
    public static final StatusEffect LAVA_DIGESTION = new LavaDigestionEffect();
    public static final StatusEffect THERMODYNAMICS = new ThermodynamicsEffect();

    public static void init() {
        register("purge", PURGE);
        register("lava_digestion", LAVA_DIGESTION);
        register("thermodynamics", THERMODYNAMICS);
    }

    private static void register(String id, StatusEffect effect) {
        Registry.register(Registry.STATUS_EFFECT, SipsMod.getId(id), effect);
    }
}
