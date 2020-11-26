package mod.codewarrior.sips.effects;

import mod.codewarrior.sips.registry.SipsEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class ThermodynamicsEffect extends StatusEffect {
    public ThermodynamicsEffect() {
        super(StatusEffectType.NEUTRAL, 4718847);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            if (entity.getStatusEffect(SipsEffects.LAVA_DIGESTION) != null) {
                entity.removeStatusEffect(SipsEffects.LAVA_DIGESTION);
                entity.extinguish();
                entity.getEntityWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 2f, player.world.random.nextFloat() * 0.1F + 0.9F);
                player.dropItem(new ItemStack(Items.OBSIDIAN, 1 + amplifier), true, true);
            }
        }
    }
}
