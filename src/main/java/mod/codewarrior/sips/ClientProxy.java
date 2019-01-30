package mod.codewarrior.sips;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event)
    {
        TextureMap map = event.getMap();
        for (ResourceLocation location : ImmutableList.of(SipsMod.fluidMilk.getStill(), SipsMod.fluidMilk.getFlowing(), SipsMod.fluidMushroomStew.getStill(), SipsMod.fluidMushroomStew.getFlowing())) {
            map.registerSprite(location);
        }
    }

    @Override
    public void preInit() {
        ModelLoaderRegistry.registerLoader(SipsModel.SipsModelLoader.INSTANCE);

        ModelLoader.registerItemVariants(SipsMod.lil_sip, SipsModel.SipsModelLoader.LIL_SIP);
        ModelLoader.registerItemVariants(SipsMod.big_chug, SipsModel.SipsModelLoader.BIG_CHUG);
    }

    @Override
    public void registerItems() {
        ModelLoader.setCustomMeshDefinition(SipsMod.lil_sip, stack -> SipsModel.SipsModelLoader.LIL_SIP);
        ModelLoader.setCustomMeshDefinition(SipsMod.big_chug, stack -> SipsModel.SipsModelLoader.BIG_CHUG);
    }
}
