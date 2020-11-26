package mod.codewarrior.sips.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;

public class PurgeEffect extends StatusEffect {
    public PurgeEffect() {
        super(StatusEffectType.BENEFICIAL, 16777215);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        entity.clearStatusEffects();
    }
}
