package mod.codewarrior.sips;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.ItemTextureQuadConverter;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static net.minecraftforge.fluids.FluidRegistry.enableUniversalBucket;

@Mod(modid = SipsMod.MODID, name = SipsMod.NAME, version = SipsMod.VERSION)
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

        ModelLoaderRegistry.registerLoader(SipsModelLoader.INSTANCE);


        ModelLoader.registerItemVariants(lil_sip, SipsModelLoader.LIL_SIP);
        ModelLoader.registerItemVariants(big_chug, SipsModelLoader.BIG_CHUG);

        Config.preInit(event);
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

        ModelLoader.setCustomMeshDefinition(lil_sip, stack -> SipsModelLoader.LIL_SIP);
        ModelLoader.setCustomMeshDefinition(big_chug, stack -> SipsModelLoader.BIG_CHUG);

    }


    public static class SipsModelLoader implements ICustomModelLoader {
        public static ModelResourceLocation LIL_SIP = new ModelResourceLocation(new ResourceLocation("sips", "lil_sip"), "inventory");
        public static ModelResourceLocation BIG_CHUG = new ModelResourceLocation(new ResourceLocation("sips", "big_chug"), "inventory");
        public static final SipsModelLoader INSTANCE = new SipsModelLoader();

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {

        }

        @Override
        public boolean accepts(ResourceLocation modelLocation) {
            return modelLocation.getResourceDomain().equals("sips") &&
                    (
                            modelLocation.getResourcePath().contains("lil_sip") ||
                            modelLocation.getResourcePath().contains("big_chug")
                    );
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception {
            if (modelLocation.getResourcePath().contains("lil_sip"))
                return new SipsModel(LIL_SIP_TEX, LIL_SIP_MASK, null);
            if (modelLocation.getResourcePath().contains("big_chug"))
                return new SipsModel(BIG_CHUG_TEX, BIG_CHUG_MASK, null);
            return null;
        }
    }

    public static ResourceLocation LIL_SIP_TEX = new ResourceLocation("sips:items/lil_sip");
    public static ResourceLocation LIL_SIP_MASK = new ResourceLocation("sips:items/lil_sip_mask");
    public static ResourceLocation BIG_CHUG_TEX = new ResourceLocation("sips:items/big_chug");
    public static ResourceLocation BIG_CHUG_MASK = new ResourceLocation("sips:items/big_chug_mask");

    public static class SipsModel implements IModel {

        public final ResourceLocation TEX;
        public final ResourceLocation MASK;

        Fluid fluid;

        public SipsModel(ResourceLocation tex, ResourceLocation mask, Fluid fluid) {
            this.TEX = tex;
            this.MASK = mask;
            this.fluid = fluid;
        }

        @Override
        public Collection<ResourceLocation> getTextures() {
            return ImmutableList.of(TEX, MASK);
        }

        @Override
        public IModel process(ImmutableMap<String, String> customData) {
            String fluidName = customData.get("fluid");
            Fluid fluid = FluidRegistry.getFluid(fluidName);
            if (fluid == null) {
                fluid = this.fluid;
            }
            return new SipsModel(TEX, MASK, fluid);
        }

        private static final float NORTH_Z_FLUID = 7.498f / 16f;
        private static final float SOUTH_Z_FLUID = 8.502f / 16f;

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
            ImmutableMap<TransformType, TRSRTransformation> transformMap = PerspectiveMapWrapper.getTransforms(state);

            TRSRTransformation transform = state.apply(Optional.empty()).orElse(TRSRTransformation.identity());;
            TextureAtlasSprite template = bakedTextureGetter.apply(MASK);
            TextureAtlasSprite fluidSprite = null;

            if (fluid != null) {
                fluidSprite = bakedTextureGetter.apply(fluid.getStill());
            }

            ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

            //builder.addAll(ItemLayerModel.getQuadsForSprite(0xFFFFFF, sprite, format, transform));
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(TEX))).bake(state, format,
                    bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));

            if (fluidSprite != null) {
                builder.addAll(ItemTextureQuadConverter.convertTexture(format, transform, template, fluidSprite, NORTH_Z_FLUID, EnumFacing.NORTH, fluid.getColor()));
                builder.addAll(ItemTextureQuadConverter.convertTexture(format, transform, template, fluidSprite, SOUTH_Z_FLUID, EnumFacing.SOUTH, fluid.getColor()));
            }
            return new SipsBakedModel(this, builder.build(), fluidSprite, format, transformMap);
        }
    }

    public static class SipsModelOverrideHandler extends ItemOverrideList {
        public static final SipsModelOverrideHandler INSTANCE = new SipsModelOverrideHandler();

        private SipsModelOverrideHandler() {
            super(ImmutableList.of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            FluidStack fluidStack = null;
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey(FluidHandlerItemStack.FLUID_NBT_KEY)) {
                fluidStack = FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag(FluidHandlerItemStack.FLUID_NBT_KEY));
            }

            if (fluidStack == null) {
                return originalModel;
            }

            SipsBakedModel model = (SipsBakedModel) originalModel;

            Fluid fluid = fluidStack.getFluid();
            String name = fluid.getName();

            if (!model.cache.containsKey(name)) {
                IModel parent = model.parent.process(ImmutableMap.of("fluid", name));
                Function<ResourceLocation, TextureAtlasSprite> textureGetter;
                textureGetter = new Function<ResourceLocation, TextureAtlasSprite>() {
                    public TextureAtlasSprite apply(ResourceLocation location) {
                        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
                    }
                };
                IBakedModel bakedModel = parent.bake(new SimpleModelState(model.transforms), model.format,
                        textureGetter);
                model.cache.put(name, bakedModel);
                return bakedModel;
            }

            return model.cache.get(name);
        }
    }

    public static class SipsBakedModel implements IBakedModel {

        SipsModel parent;
        List<BakedQuad> quads;
        TextureAtlasSprite particle;
        VertexFormat format;
        ImmutableMap<TransformType, TRSRTransformation> transforms;
        Map<String, IBakedModel> cache = new HashMap<>();

        public SipsBakedModel(SipsModel parent, List<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format, ImmutableMap<TransformType, TRSRTransformation> transforms) {
            this.parent = parent;
            this.quads = quads;
            this.particle = particle;
            this.format = format;
            this.transforms = transforms;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return quads;
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
            return PerspectiveMapWrapper.handlePerspective(this, transforms, cameraTransformType);
        }

        @Override
        public boolean isAmbientOcclusion() {
            return false;
        }

        @Override
        public boolean isGui3d() {
            return false;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return particle;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return SipsModelOverrideHandler.INSTANCE;
        }
    }
}
