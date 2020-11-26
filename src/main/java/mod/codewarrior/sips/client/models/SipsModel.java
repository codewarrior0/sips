package mod.codewarrior.sips.client.models;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import mod.codewarrior.sips.SipsMod;
import mod.codewarrior.sips.config.SipsConfig;
import mod.codewarrior.sips.utils.FluidUtil;
import mod.codewarrior.sips.utils.LRUCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class SipsModel implements UnbakedModel {
    final Identifier fallback;
    final SpriteIdentifier tex;
    final boolean isChug;
    final boolean is2d;

    public SipsModel(String modelPath, String texPath, boolean isChug, boolean is2d) {
        this.fallback = SipsMod.getId(modelPath);
        this.tex = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, SipsMod.getId(texPath));
        this.isChug = isChug;
        this.is2d = is2d;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return ImmutableSet.of(fallback);
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return ImmutableSet.of(tex);
    }

    @Override
    public @Nullable BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        BakedModel fallback = loader.getOrLoadModel(this.fallback).bake(loader, textureGetter, rotationContainer, this.fallback);
        return new BakedSip(fallback, textureGetter.apply(tex), isChug, is2d, this.fallback);
    }

    static class BakedSip implements BakedModel, FabricBakedModel {
        BakedModel fallback;
        Mesh mesh;
        boolean swap = true;

        final Sprite tex;
        final boolean isChug;
        final boolean is2d;
        final Identifier id;
        final Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        final RenderMaterial layer = renderer.materialFinder().blendMode(0, BlendMode.TRANSLUCENT).find();
        final static LRUCache<FluidKey, RenderState> cache = new LRUCache<>(SipsConfig.getModelCacheSize());
        final static MinecraftClient mc = MinecraftClient.getInstance();

        public BakedSip(BakedModel fallback, Sprite tex, boolean isChug, boolean is2d, Identifier id) {
            this.fallback = fallback;
            this.tex = tex;
            this.mesh = createMesh(fallback);
            this.isChug = isChug;
            this.is2d = is2d;
            this.id = id;
        }

        @Override
        public boolean isVanillaAdapter() {
            return false;
        }

        @Override
        public void emitBlockQuads(BlockRenderView blockRenderView, BlockState blockState, BlockPos blockPos, Supplier<Random> supplier, RenderContext renderContext) {

        }

        @Override
        public void emitItemQuads(ItemStack stack, Supplier<Random> supplier, RenderContext renderContext) {
            if (swap) {
                if (is2d) {
                    this.fallback = mc.getBakedModelManager().getModel(new ModelIdentifier(id.toString().replace("item/", ""), "inventory"));
                    this.mesh = createMesh(fallback);
                }
                swap = false;
            }

            renderContext.meshConsumer().accept(mesh);
            FluidKey fluid = FluidUtil.getFluid(stack);
            if (fluid != FluidKeys.EMPTY) {
                RenderState state = RenderState.resolve(fluid);
                QuadEmitter qe = renderContext.getEmitter();
                int amount = FluidUtil.getAmount(stack);
                float min, max, bot, y, depth;
                if (is2d) {
                    if (isChug) {
                        min = 0.5F;
                        max = 0.75F;
                        bot = 0.125F;
                        y = (amount / (float) SipsConfig.getBigChugCapacity()) * 0.5F + bot;
                        depth = 0.47f;
                        qe.material(layer)
                                .square(Direction.NORTH, 0.25F, bot, 0.5625F, y, depth)
                                .spriteColor(0, state.color, state.color, state.color, state.color)
                                .spriteBake(0, state.sprite, MutableQuadView.BAKE_LOCK_UV)
                                .emit();
                        qe.material(layer)
                                .square(Direction.SOUTH, min, bot, max, y, depth)
                                .spriteColor(0, state.color, state.color, state.color, state.color)
                                .spriteBake(0, state.sprite, MutableQuadView.BAKE_LOCK_UV)
                                .emit();
                        qe.material(layer)
                                .square(Direction.UP, min, 0.47f, max, 0.53f, 1 - y)
                                .spriteColor(0, state.color, state.color, state.color, state.color)
                                .spriteBake(0, state.sprite, MutableQuadView.BAKE_LOCK_UV)
                                .emit();
                    } else {
                        min = 0.625F;
                        max = 0.75F;
                        bot = 0.1875F;
                        y = (amount / (float) SipsConfig.getLilSipCapacity()) * 0.4375F + bot;
                        depth = 0.47f;
                        qe.material(layer)
                                .square(Direction.NORTH, 0.25F, bot, 0.375F, y, depth)
                                .spriteColor(0, state.color, state.color, state.color, state.color)
                                .spriteBake(0, state.sprite, MutableQuadView.BAKE_LOCK_UV)
                                .emit();
                        qe.material(layer)
                                .square(Direction.SOUTH, min, bot, max, y, depth)
                                .spriteColor(0, state.color, state.color, state.color, state.color)
                                .spriteBake(0, state.sprite, MutableQuadView.BAKE_LOCK_UV)
                                .emit();
                        qe.material(layer)
                                .square(Direction.UP, min, 0.47f, max, 0.53f, 1 - y)
                                .spriteColor(0, state.color, state.color, state.color, state.color)
                                .spriteBake(0, state.sprite, MutableQuadView.BAKE_LOCK_UV)
                                .emit();
                    }
                } else {
                    if (isChug) {
                        bot = 0.125f;
                        min = 0.313F;
                        max = 0.686F;
                        y = (amount / (float) SipsConfig.getBigChugCapacity()) * 0.38125F + bot;
                        depth = 0.252F;
                        float w1 = 0.376f;
                        float w2 = 0.624f;
                        qe.material(layer)
                                .square(Direction.UP, min, min, max, 0.6875f, 1f - y)
                                .spriteColor(0, state.color, state.color, state.color, state.color)
                                .spriteBake(0, state.sprite, MutableQuadView.BAKE_LOCK_UV)
                                .emit();
                        qe.material(layer)
                                .square(Direction.UP, w1, depth, w2, min, 1f - y)
                                .spriteColor(0, state.color, state.color, state.color, state.color)
                                .spriteBake(0, state.sprite, MutableQuadView.BAKE_LOCK_UV)
                                .emit();
                        qe.material(layer)
                                .square(Direction.UP, w1, max, w2, 0.7485f, 1f - y)
                                .spriteColor(0, state.color, state.color, state.color, state.color)
                                .spriteBake(0, state.sprite, MutableQuadView.BAKE_LOCK_UV)
                                .emit();
                        qe.material(layer)
                                .square(Direction.NORTH, w1, bot, w2, y, depth)
                                .spriteColor(0, state.color, state.color, state.color, state.color)
                                .spriteBake(0, state.sprite, MutableQuadView.BAKE_LOCK_UV)
                                .emit();
                        qe.material(layer)
                                .square(Direction.SOUTH, w1, bot, w2, y, depth)
                                .spriteColor(0, state.color, state.color, state.color, state.color)
                                .spriteBake(0, state.sprite, MutableQuadView.BAKE_LOCK_UV)
                                .emit();
                    } else {
                        bot = 0.021875F;
                        min = 0.375F;
                        max = 0.625F;
                        y = (amount / (float) SipsConfig.getLilSipCapacity()) * 0.38125F + bot;
                        depth = 0.3126F;
                        qe.material(layer)
                                .square(Direction.UP, min, min, max, 0.6875f, 1f - y)
                                .spriteColor(0, state.color, state.color, state.color, state.color)
                                .spriteBake(0, state.sprite, MutableQuadView.BAKE_LOCK_UV)
                                .emit();
                        qe.material(layer)
                                .square(Direction.NORTH, min, bot, max, y, depth)
                                .spriteColor(0, state.color, state.color, state.color, state.color)
                                .spriteBake(0, state.sprite, MutableQuadView.BAKE_LOCK_UV)
                                .emit();
                    }
                }
            }
        }

        private Mesh createMesh(BakedModel model) {
            final MeshBuilder mb = renderer.meshBuilder();
            Random random = new Random();
            random.setSeed(42);
            emitModel(random, model, mb.getEmitter());
            return mb.build();
        }

        void emitModel(Random random, BakedModel model, QuadEmitter qe) {
            for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
                Direction cullFace = ModelHelper.faceFromIndex(i);
                List<BakedQuad> quads = model.getQuads(null, cullFace, random);

                if (quads.isEmpty()) {
                    continue;
                }

                for (final BakedQuad q : quads) {
                    qe.fromVanilla(q.getVertexData(), 0, false);
                    qe.cullFace(cullFace);
                    qe.nominalFace(q.getFace());
                    qe.colorIndex(q.getColorIndex());
                    qe.emit();
                }
            }
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
            return fallback.getQuads(state, face, random);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return fallback.useAmbientOcclusion();
        }

        @Override
        public boolean hasDepth() {
            return fallback.hasDepth();
        }

        @Override
        public boolean isSideLit() {
            return false;
        }

        @Override
        public boolean isBuiltin() {
            return fallback.isBuiltin();
        }

        @Override
        public Sprite getSprite() {
            return this.tex;
        }

        @Override
        public ModelTransformation getTransformation() {
            return fallback.getTransformation();
        }

        @Override
        public ModelOverrideList getOverrides() {
            return fallback.getOverrides();
        }

        static class RenderState {
            final Sprite sprite;
            final int color;

            private RenderState(Sprite sprite, int color) {
                this.sprite = sprite;
                this.color = color;
            }

            public static RenderState resolve(FluidKey fluid) {
                if (cache.get(fluid) == null && mc.cameraEntity != null) {
                    Sprite s;
                    World world = mc.cameraEntity.world;
                    BlockPos pos = BlockPos.ORIGIN;
                    FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid.getRawFluid());
                    s = fluid.getRawFluid() == null
                            ? FluidRenderHandlerRegistry.INSTANCE.get(Fluids.WATER).getFluidSprites(world, pos, Fluids.WATER.getDefaultState())[0]
                            : fluidRenderHandler.getFluidSprites(world, pos, fluid.getRawFluid().getDefaultState())[0];
                    int color = fluid.getRawFluid() == null
                            ? fluid.renderColor
                            : fluidRenderHandler.getFluidColor(world, pos, fluid.getRawFluid().getDefaultState());
                    cache.put(fluid, new RenderState(s, color | 0xFF000000));
                }
                return cache.get(fluid);
            }
        }
    }
}
