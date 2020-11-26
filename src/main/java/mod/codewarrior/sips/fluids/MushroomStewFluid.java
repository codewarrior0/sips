package mod.codewarrior.sips.fluids;

import mod.codewarrior.sips.registry.SipsBlocks;
import mod.codewarrior.sips.registry.SipsFluids;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public abstract class MushroomStewFluid extends MilkFluid {
    @Override
    public Fluid getFlowing() {
        return SipsFluids.FLOWING_MUSHROOM_STEW;
    }

    @Override
    public Fluid getStill() {
        return SipsFluids.MUSHROOM_STEW;
    }

    @Override
    public Item getBucketItem() {
        return Items.MUSHROOM_STEW;
    }

    @Override
    public BlockState toBlockState(FluidState state) {
        return SipsBlocks.MUSHROOM_STEW.getDefaultState().with(FluidBlock.LEVEL, method_15741(state));
    }

    @Override
    public boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !fluid.isIn(TagRegistry.fluid(new Identifier("c:mushroom_stew")));
    }

    public static class Flowing extends MushroomStewFluid {
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }

        public boolean isStill(FluidState state) {
            return false;
        }
    }

    public static class Still extends MushroomStewFluid {
        public int getLevel(FluidState state) {
            return 8;
        }

        public boolean isStill(FluidState state) {
            return true;
        }
    }
}
