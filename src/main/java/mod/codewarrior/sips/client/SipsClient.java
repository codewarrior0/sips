package mod.codewarrior.sips.client;

import mod.codewarrior.sips.SipsMod;
import mod.codewarrior.sips.client.models.SipsModel;
import mod.codewarrior.sips.config.SipsConfig;
import mod.codewarrior.sips.registry.SippableRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceType;

import static mod.codewarrior.sips.registry.SipsFluids.*;

@Environment(EnvType.CLIENT)
public class SipsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new FluidResourceListener());
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), MILK, FLOWING_MILK, MUSHROOM_STEW, FLOWING_MUSHROOM_STEW);

        ClientSidePacketRegistry.INSTANCE.register(SippableRegistry.SIPPABLES_S2C, (ctx, buf) -> {
            if (FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
                SippableRegistry.fromPacket(buf);
            }
        });

        String[] str = new String[]{
                "milk_still",
                "milk_flow",
                "mushroom_stew_still",
                "mushroom_stew_flow"
        };
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlas, registry) -> {
            for (String s : str) registry.register(SipsMod.getId("block/fluid/" + s));
        });

        if (!SipsConfig.use3DModels()) {
            ModelLoadingRegistry.INSTANCE.registerAppender((manager, out) -> {
                out.accept(new ModelIdentifier(SipsMod.getId("lil_sip_2d"), "inventory"));
                out.accept(new ModelIdentifier(SipsMod.getId("big_chug_2d"), "inventory"));
            });
        }

        ModelLoadingRegistry.INSTANCE.registerVariantProvider(manager -> (modelId, ctx) -> {
            if (modelId.getNamespace().equals(SipsMod.modid)) {
                String s, s2;
                boolean bl = false, bl2 = false;
                switch (modelId.getPath()) {
                    case "lil_sip":
                        if (SipsConfig.use3DModels()) {
                            s = "item/lil_sip_3d";
                            s2 = s;
                        } else {
                            s = "item/lil_sip_2d";
                            s2 = "item/lil_sip";
                            bl2 = true;
                        }
                        break;
                    case "big_chug":
                        if (SipsConfig.use3DModels()) {
                            s = "item/big_chug_3d";
                            s2 = s;
                        } else {
                            s = "item/big_chug_2d";
                            s2 = "item/big_chug";
                            bl2 = true;
                        }
                        bl = true;
                        break;
                    default:
                        return null;
                }
                return new SipsModel(s, s2, bl, bl2);
            }
            return null;
        });
    }
}
