package mod.codewarrior.sips;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

import static net.minecraftforge.fluids.FluidRegistry.enableUniversalBucket;

@Mod(modid = SipsMod.MODID, name = SipsMod.NAME, version = SipsMod.VERSION, dependencies = "after:rustic;after:thermalfoundation;after:thermalexpansion")
@Mod.EventBusSubscriber
public class SipsMod
{
    public static final String MODID = "sips";
    public static final String NAME = "Sips";
    public static final String VERSION = "1.0";

    private static SipsItem lil_sip;
    private static SipsItem big_chug;

    public static Fluid fluidMilk;
    public static Fluid fluidMushroomStew;

    public static Logger logger;


    public static Fluid setupFluid(Fluid fluid)
    {
        FluidRegistry.addBucketForFluid(fluid);
        if(!FluidRegistry.registerFluid(fluid))
            return FluidRegistry.getFluid(fluid.getName());
        return fluid;
    }

    static {
        enableUniversalBucket();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        lil_sip = new SipsItem("lil_sip", 1000, 32);
        big_chug = new SipsItem("big_chug", 8000, 16);

        fluidMilk = setupFluid(
                new Fluid("milk",
                        new ResourceLocation("sips:blocks/fluid/milk_still"),
                        new ResourceLocation("sips:blocks/fluid/milk_flow")
                )
        );
        fluidMushroomStew = setupFluid(
                new Fluid("mushroom_stew",
                        new ResourceLocation("sips:blocks/fluid/mushroom_stew_still"),
                        new ResourceLocation("sips:blocks/fluid/mushroom_stew_flow")
                )
        );

        if(FMLLaunchHandler.side() == Side.CLIENT) {
            initModel();
        }

        Config.preInit(event);
    }

    public void initModel() {
        ModelLoaderRegistry.registerLoader(SipsModel.SipsModelLoader.INSTANCE);

        ModelLoader.registerItemVariants(lil_sip, SipsModel.SipsModelLoader.LIL_SIP);
        ModelLoader.registerItemVariants(big_chug, SipsModel.SipsModelLoader.BIG_CHUG);
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event)
    {
        TextureMap map = event.getMap();
        for (ResourceLocation location : ImmutableList.of(fluidMilk.getStill(), fluidMilk.getFlowing(), fluidMushroomStew.getStill(), fluidMushroomStew.getFlowing())) {
            map.registerSprite(location);
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        //logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(lil_sip);
        event.getRegistry().register(big_chug);

        if(FMLLaunchHandler.side() == Side.CLIENT) {
            ModelLoader.setCustomMeshDefinition(lil_sip, stack -> SipsModel.SipsModelLoader.LIL_SIP);
            ModelLoader.setCustomMeshDefinition(big_chug, stack -> SipsModel.SipsModelLoader.BIG_CHUG);
        }
    }


}
