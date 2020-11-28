package mod.codewarrior.sips.registry;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import mod.codewarrior.sips.config.SipsConfig;
import mod.codewarrior.sips.utils.Sippable;
import mod.codewarrior.sips.utils.SippableRegistryCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SipsEvents {
    public static void init() {
        SippableRegistryCallback.EVENT.register(() -> {
            if (SipsConfig.liquidXpHasEffect()) {
                Sippable.fromPredicate("liquid_xp", new Sippable() {
                    @Override
                    public void onSipped(FluidKey drank, World world, PlayerEntity player) {
                        player.addExperience(7 + world.random.nextInt(5));
                    }
                });
            }
        });

        UseBlockCallback.EVENT.register(((player, world, hand, rtr) -> {
            ItemStack stack = player.getStackInHand(hand);
            BlockPos pos = rtr.getBlockPos().offset(rtr.getSide());
            if (player.isSneaking()) {
                if (stack.getItem() instanceof MilkBucketItem && world.getBlockState(pos).canBucketPlace(SipsFluids.MILK)) {
                    return handleFluid(player, world, hand, pos, stack, Items.BUCKET, SipsBlocks.MILK);
                } else if (stack.getItem() instanceof MushroomStewItem && world.getBlockState(pos).canBucketPlace(SipsFluids.MUSHROOM_STEW)) {
                    return handleFluid(player, world, hand, pos, stack, Items.BOWL, SipsBlocks.MUSHROOM_STEW);
                }
            }
            return ActionResult.PASS;
        }));
    }

    public static ActionResult handleFluid(PlayerEntity player, World world, Hand hand, BlockPos pos, ItemStack stack, Item empty, Block toPlace) {
        if (!player.abilities.creativeMode) {
            stack.decrement(1);
            ItemStack e = new ItemStack(empty);
            if (stack.isEmpty()) {
                player.setStackInHand(hand, e);
            } else {
                player.inventory.offerOrDrop(world, e);
            }
        }
        world.setBlockState(pos, toPlace.getDefaultState());
        world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
        return ActionResult.SUCCESS;
    }
}