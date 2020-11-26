package mod.codewarrior.sips.items;

import alexiil.mc.lib.attributes.AttributeProviderItem;
import alexiil.mc.lib.attributes.ItemAttributeList;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.item.ItemBasedSingleFluidInv;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.fluid.volume.PotionFluidKey;
import alexiil.mc.lib.attributes.misc.LimitedConsumer;
import alexiil.mc.lib.attributes.misc.Reference;
import mod.codewarrior.sips.SipsMod;
import mod.codewarrior.sips.config.SipsConfig;
import mod.codewarrior.sips.registry.SipsFluids;
import mod.codewarrior.sips.utils.FluidUtil;
import mod.codewarrior.sips.utils.Sippable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.math.RoundingMode;
import java.util.List;

import static mod.codewarrior.sips.registry.SippableRegistry.SIPS;

public class SipsItem extends Item implements AttributeProviderItem {
    public final int maxCapacity;
    public final int itemUseDuration;

    public SipsItem(Settings settings, int cap, int duration) {
        super(settings.group(SipsMod.SipsGroup).maxCount(1).food(new FoodComponent.Builder().hunger(0).saturationModifier(0).alwaysEdible().build()));
        this.maxCapacity = cap;
        this.itemUseDuration = duration;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        FluidKey fluid = FluidUtil.getFluid(stack);
        if (fluid instanceof PotionFluidKey) {
            ((PotionFluidKey) fluid).potion.getEffects().forEach(user::addStatusEffect);
        } else if (user instanceof PlayerEntity) {
            onSipped(fluid, (PlayerEntity) user);
        }
        FluidUtil.sip(stack);
        return stack;
    }

    protected void onSipped(FluidKey drank, PlayerEntity player) {
        float damage = 0;
        if (SIPS.containsKey(drank)) {
            Sippable sippable = SIPS.get(drank);
            player.getHungerManager().add(sippable.shanks, sippable.saturation);

            damage = sippable.damage;
            sippable.effects.forEach(e -> player.addStatusEffect(e.getEffect()));
            sippable.onSipped(drank, player.world, player);
        } else {
            //todo persuade someone (anyone) to use temperatures
            double temp = drank.getTemperature() == null ? 26.85 : drank.getTemperature().getTemperature(drank.withAmount(FluidAmount.BUCKET));
            if (SipsConfig.getTemperatureDamagePerCelsius() != 0.0) {
                if (temp > 46.85) {
                    damage = (float) (temp - 46.85) * SipsConfig.getTemperatureDamagePerCelsius();
                } else if (temp < -13.15) {
                    damage = (float) (-13.15 - temp) * SipsConfig.getTemperatureDamagePerCelsius();
                }
            }
            if (SipsConfig.useTemperatureEffects()) {
                if (temp > 46.85) {
                    player.setOnFireFor(30);
                } else {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 30));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 30));
                }
            }
        }
        if (damage < 0) {
            player.heal(-damage);
        } else if (damage > 0) {
            player.damage(new DamageSource("sip") {
                @Override
                public Text getDeathMessage(LivingEntity entity) {
                    return new TranslatableText("death.sipped", entity.getDisplayName(), drank.name);
                }
            }, damage);
        }
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (FluidUtil.getAmount(stack) < maxCapacity) {
            if (entity instanceof CowEntity) {
                FluidKey milked;
                if (entity instanceof MooshroomEntity) milked = FluidKeys.get(SipsFluids.MUSHROOM_STEW);
                else milked = FluidKeys.get(SipsFluids.MILK);

                if (FluidUtil.isEmpty(stack)) {
                    FluidUtil.setFluid(stack, milked);
                } else if (FluidUtil.getFluid(stack) != milked) {
                    return ActionResult.PASS;
                }
                FluidUtil.addAmount(stack, 1000, maxCapacity);
                user.world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_COW_MILK, SoundCategory.PLAYERS, 1f, user.world.random.nextFloat() * 0.1F + 0.9F);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return itemUseDuration;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (attemptFill(world, user, stack)) {
            return TypedActionResult.success(stack, !world.isClient());
        } else if (FluidUtil.isEmpty(stack)) {
            return TypedActionResult.pass(stack);
        } else {
            return ItemUsage.consumeHeldItem(world, user, hand);
        }
    }

    private boolean attemptFill(World world, PlayerEntity user, ItemStack stack) {
        BlockHitResult rtr = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (rtr.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = rtr.getBlockPos();
            Direction direction = rtr.getSide();
            BlockPos blockPos2 = blockPos.offset(direction);
            if (world.canPlayerModifyAt(user, blockPos) && user.canPlaceOn(blockPos2, direction, stack)) {
                BlockState blockState;
                blockState = world.getBlockState(blockPos);
                if (blockState.getBlock() instanceof FluidDrainable) {
                    Fluid fluid = ((FluidDrainable) blockState.getBlock()).tryDrainFluid(world, blockPos, blockState);
                    if (fluid != Fluids.EMPTY) {
                        FluidKey key = FluidKeys.get(fluid);
                        if (FluidUtil.isEmpty(stack)) {
                            FluidUtil.setFluid(stack, FluidKeys.get(fluid));
                        }
                        if (FluidUtil.getFluid(stack) == key) {
                            FluidUtil.addAmount(stack, 1000, maxCapacity);
                        }
                        user.playSound(fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void addAllAttributes(Reference<ItemStack> stack, LimitedConsumer<ItemStack> excess, ItemAttributeList<?> list) {
        list.offer(new ItemBasedSingleFluidInv(stack, excess) {
            @Override
            protected boolean isInvalid(ItemStack stack) {
                return false;
            }

            @Override
            protected HeldFluidInfo getInfo(ItemStack stack) {
                return new HeldFluidInfo(FluidUtil.getFluid(stack).withAmount(FluidAmount.of(FluidUtil.getAmount(stack), 1000)), FluidAmount.of(maxCapacity, 1000));
            }

            @javax.annotation.Nullable
            @Override
            protected ItemStack writeToStack(ItemStack stack, FluidVolume volume) {
                ItemStack c = stack.copy();
                FluidUtil.setFluid(c, volume.getFluidKey());
                FluidUtil.setAmount(c, volume.amount().asInt(1000, RoundingMode.FLOOR));
                return c;
            }
        });
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        FluidUtil.appendTooltip(stack, tooltip, 250, maxCapacity);
    }

    @Override
    public Text getName(ItemStack stack) {
        if (SipsMod.isApril1st()) {
            if (maxCapacity == SipsConfig.getBigChugCapacity()) return new LiteralText("Big Chungus");
            else return new LiteralText("Sipsi");
        }
        return new TranslatableText(this.getTranslationKey(stack));
    }
}
