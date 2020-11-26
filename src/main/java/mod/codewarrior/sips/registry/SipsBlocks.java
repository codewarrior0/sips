package mod.codewarrior.sips.registry;

import mod.codewarrior.sips.SipsMod;
import mod.codewarrior.sips.fluids.SipsFluidBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.Registry;

public class SipsBlocks {
    public static Block MILK = new SipsFluidBlock(SipsFluids.MILK, AbstractBlock.Settings.copy(Blocks.WATER));
    public static Block MUSHROOM_STEW = new SipsFluidBlock(SipsFluids.MUSHROOM_STEW, AbstractBlock.Settings.copy(Blocks.WATER));

    public static void init() {
        subRegister("milk", MILK);
        subRegister("mushroom_stew", MUSHROOM_STEW);
    }

    public static void subRegister(String id, Block block) {
        Registry.register(Registry.BLOCK, SipsMod.getId(id), block);
    }
}
