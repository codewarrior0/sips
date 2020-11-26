package mod.codewarrior.sips.client;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.codewarrior.sips.SipsMod;
import mod.codewarrior.sips.registry.SipsFluids;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.Fluid;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class FluidResourceListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return SipsMod.getId("fluid_resourcelistener");
    }

    @Override
    public void apply(ResourceManager manager) {
        final Function<Identifier, Sprite> atlas = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        Map<List<Fluid>, FluidRenderHandler> map = new Object2ObjectOpenHashMap<>();
        map.put(ImmutableList.of(SipsFluids.MILK, SipsFluids.FLOWING_MILK), (blockRenderView, blockPos, fluidState) -> new Sprite[]{
                atlas.apply(SipsMod.getId("block/fluid/milk_still")),
                atlas.apply(SipsMod.getId("block/fluid/milk_flow"))
        });
        map.put(ImmutableList.of(SipsFluids.MUSHROOM_STEW, SipsFluids.FLOWING_MUSHROOM_STEW), ((blockRenderView, blockPos, fluidState) -> new Sprite[]{
                atlas.apply(SipsMod.getId("block/fluid/mushroom_stew_still")),
                atlas.apply(SipsMod.getId("block/fluid/mushroom_stew_flow"))
        }));

        map.forEach((k, v) -> k.forEach(f -> FluidRenderHandlerRegistry.INSTANCE.register(f, v)));
    }
}
