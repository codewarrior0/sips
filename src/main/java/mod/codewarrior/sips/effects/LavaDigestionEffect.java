package mod.codewarrior.sips.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;

public class LavaDigestionEffect extends StatusEffect {
    public LavaDigestionEffect() {
        super(StatusEffectType.HARMFUL, 12714018);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.isFireImmune() && !entity.isOnFire()) {
            entity.setOnFireFor(15);
        }
    }
}
