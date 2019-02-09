package mod.codewarrior.sips;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import static net.minecraftforge.fluids.FluidRegistry.enableUniversalBucket;

@Mod(modid = SipsMod.MODID, name = SipsMod.NAME, version = SipsMod.VERSION, dependencies = "after:rustic;after:thermalfoundation;after:thermalexpansion")
@Mod.EventBusSubscriber
public class SipsMod
{
    public static final String MODID = "sips";
    public static final String NAME = "Sips";
    public static final String VERSION = "1.0";

    public static SipsItem lil_sip;
    public static SipsItem big_chug;

    public static Fluid fluidMilk;
    public static Fluid fluidMushroomStew;

    public static Logger logger;

    @SidedProxy(clientSide = "mod.codewarrior.sips.ClientProxy", serverSide = "mod.codewarrior.sips.CommonProxy")
    public static CommonProxy proxy;

    public static final ResourceLocation MILK_STILL = new ResourceLocation("sips:blocks/fluid/milk_still");
    public static final ResourceLocation MILK_FLOW = new ResourceLocation("sips:blocks/fluid/milk_flow");
    public static final ResourceLocation MUSHROOM_STEW_STILL = new ResourceLocation("sips:blocks/fluid/mushroom_stew_still");
    public static final ResourceLocation MUSHROOM_STEW_FLOW = new ResourceLocation("sips:blocks/fluid/mushroom_stew_flow");


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
                        MILK_STILL,
                        MILK_FLOW
                )
        );
        fluidMushroomStew = setupFluid(
                new Fluid("mushroom_stew",
                        MUSHROOM_STEW_STILL,
                        MUSHROOM_STEW_FLOW
                )
        );

        proxy.preInit();

        Config.preInit(event);
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

        proxy.registerItems();
    }


}
