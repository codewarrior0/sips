package mod.codewarrior.sips;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SipsModel implements IModel {

    public static ResourceLocation LIL_SIP_MODEL = new ResourceLocation("sips:item/lil_sip_3d");
    public static ResourceLocation BIG_CHUG_MODEL = new ResourceLocation("sips:item/big_chug_3d");
    public static ResourceLocation FLUID_PLACEHOLDER = new ResourceLocation("sips:fluid_placeholder");
    public static ResourceLocation FLUID_EMPTY = new ResourceLocation("sips:items/empty_fluid");

    public final IModel baseModel;

    Fluid fluid;

    public SipsModel(ResourceLocation modelLoc, Fluid fluid) throws Exception {
        this.baseModel = ModelLoaderRegistry.getModel(modelLoc);
        this.fluid = fluid;
    }

    public SipsModel(IModel baseModel, Fluid fluid) {
        this.baseModel = baseModel;
        this.fluid = fluid;
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return new ImmutableList.Builder<ResourceLocation>().add(FLUID_EMPTY).addAll(baseModel.getTextures()).build();
    }

    @Override
    public IModel process(ImmutableMap<String, String> customData) {
        String fluidName = customData.get("fluid");
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        if (fluid == null) {
            fluid = this.fluid;
        }
        return new SipsModel(baseModel, fluid);
    }

    private static final float NORTH_Z_FLUID = 7.498f / 16f;
    private static final float SOUTH_Z_FLUID = 8.502f / 16f;

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        TextureAtlasSprite fluidSprite = null;

        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        IBakedModel base = this.baseModel.bake(state, format,
                (loc) -> (loc.equals(FLUID_PLACEHOLDER)) ? bakedTextureGetter.apply(fluid != null ? fluid.getStill() : FLUID_EMPTY) : bakedTextureGetter.apply(loc));

        builder.addAll(base.getQuads(null, null, 0));

        return new SipsBakedModel(this, base, state, builder.build(), fluidSprite, format);
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
                textureGetter = location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());

                IBakedModel bakedModel = parent.bake(model.state, model.format, textureGetter);

                model.cache.put(name, bakedModel);
                return bakedModel;
            }

            return model.cache.get(name);
        }
    }

    public static class SipsBakedModel implements IBakedModel {

        SipsModel parent;
        IBakedModel base;
        IModelState state;
        List<BakedQuad> quads;
        TextureAtlasSprite particle;
        VertexFormat format;
        Map<String, IBakedModel> cache = new HashMap<>();

        public SipsBakedModel(SipsModel parent, IBakedModel base, IModelState state, List<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format) {
            this.parent = parent;
            this.base = base;
            this.state = state;
            this.quads = quads;
            this.particle = particle;
            this.format = format;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return quads;
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
            Matrix4f matrix = this.base.handlePerspective(cameraTransformType).getRight();
            return Pair.of(this, matrix);
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
                            modelLocation.getResourcePath().equals("lil_sip") ||
                            modelLocation.getResourcePath().equals("big_chug")
                    );
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception {
            if (modelLocation.getResourcePath().equals("lil_sip"))
                return new SipsModel(LIL_SIP_MODEL, null);
            if (modelLocation.getResourcePath().equals("big_chug"))
                return new SipsModel(BIG_CHUG_MODEL, null);
            return null;
        }
    }
}
