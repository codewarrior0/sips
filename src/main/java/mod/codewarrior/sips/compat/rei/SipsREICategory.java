package mod.codewarrior.sips.compat.rei;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Label;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.widget.Widget;
import mod.codewarrior.sips.SipsMod;
import mod.codewarrior.sips.registry.SippableRegistry;
import mod.codewarrior.sips.registry.SipsItems;
import mod.codewarrior.sips.utils.FluidUtil;
import mod.codewarrior.sips.utils.RomanConverter;
import mod.codewarrior.sips.utils.Sippable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class SipsREICategory implements RecipeCategory<SipsREIDisplay> {
    protected Identifier bg = SipsMod.getId("textures/gui/background.png");
    private final Identifier RECIPECONTAINER = new Identifier("roughlyenoughitems:textures/gui/recipecontainer.png");
    private MinecraftClient mc = MinecraftClient.getInstance();

    @Override
    public @NotNull Identifier getIdentifier() {
        return SipsREIPlugin.BEVERAGES;
    }

    @Override
    public @NotNull EntryStack getLogo() {
        ItemStack logo = new ItemStack(SipsItems.LIL_SIP);
        FluidUtil.setFluid(logo, FluidKeys.WATER);
        FluidUtil.setAmount(logo, (int) (FluidUtil.getMaxCapacity(logo) * 0.75d));
        return EntryStack.create(logo);
    }

    @Override
    public @NotNull String getCategoryName() {
        return I18n.translate("rei.sips.category");
    }

    @Override
    public @NotNull List<Widget> setupDisplay(SipsREIDisplay display, Rectangle bounds) {
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createTexturedWidget(this.bg, bounds.x - 11, bounds.y - 50, 0, 0, 172, 229, 172, 229));
        widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            matrices.translate(0.0D, 0.0D, 50.0D);
            if (mc.player != null) {
                drawEntity(bounds.getCenterX() - 12, bounds.getCenterY() + 105, 104, 35f, -35f, mc.player);
            }
            drawSippableDetails(helper, matrices, display.getFluid(), bounds);
        }));

        widgets.add(Widgets.createSlot(new Point(bounds.getCenterX() - 33, bounds.getCenterY() - 16)).entries(display.getInputEntries().get(0)));
        widgets.add(Widgets.createSlot(new Point(bounds.getCenterX() - 8, bounds.getCenterY())).entries(display.getInputEntries().get(1)).disableBackground().disableHighlight());
        widgets.add(Widgets.createLabel(new Point(bounds.getCenterX() - 8, bounds.getCenterY() + 4), new LiteralText("+")));
        Rectangle rect = new Rectangle(bounds.getCenterX() - 11, bounds.getCenterY() - 16, 24, 17);
        widgets.add(Widgets.createTexturedWidget(RECIPECONTAINER, rect, 40, 223));
        widgets.add(new ClickableTexWidget(rect).noBackground().disableFavoritesInteractions());

        widgets.add(new SipsWidget(bounds.x + 90, bounds.y + 16).entries(display.getResultingEntries().get(0)).noBackground().noHighlight());

        int qnum = new Random().nextInt(48);
        String quote = WordUtils.wrap(I18n.translate("rei.sips.quote" + qnum), 28, "\n", false);
        String[] str = quote.split("\n");
        int i = 0;
        int v = 5;
        for (String q : str) {
            Label text = Widgets.createLabel(new Point(bounds.getCenterX() - 13, bounds.getMaxY() + 35 - v * str.length + i), new LiteralText(q));
            widgets.add(text);
            i += 10;
        }
        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return 128;
    }

    @Override
    public int getDisplayWidth(SipsREIDisplay display) {
        return 150;
    }

    @Override
    public int getMaximumRecipePerPage() {
        return 1;
    }

    @Override
    public int getFixedRecipesPerPage() {
        return -1;
    }

    protected void drawEntity(int x, int y, int size, float mouseX, float mouseY, PlayerEntity entity) {
        float f = (float) Math.atan(mouseX / 40.0F);
        float g = (float) Math.atan(mouseY / 40.0F);
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float) x, (float) y, 50.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.translate(0.0D, 0.0D, 50.0D);
        matrixStack.scale((float) size, (float) size, (float) size);
        Quaternion quaternion = Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F);
        Quaternion quaternion2 = Vector3f.POSITIVE_X.getDegreesQuaternion(g * 20.0F);
        quaternion.hamiltonProduct(quaternion2);
        matrixStack.multiply(quaternion);
        float h = entity.bodyYaw;
        float i = entity.yaw;
        float j = entity.pitch;
        float k = entity.prevHeadYaw;
        float l = entity.headYaw;
        float hsp = entity.handSwingProgress;
        ItemStack stack = entity.getOffHandStack();
        entity.preferredHand = Hand.OFF_HAND;
        entity.handSwingProgress = 0.1f;
        entity.lastHandSwingProgress = 0.1f;
        entity.inventory.offHand.set(0, ItemStack.EMPTY);
        entity.bodyYaw = 190.0F + f * 20.0F;
        entity.yaw = 180.0F + f * 40.0F;
        entity.pitch = -15f;
        entity.headYaw = 180f;
        entity.prevHeadYaw = 180f;

        EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        quaternion2.conjugate();
        entityRenderDispatcher.setRotation(quaternion2);
        entityRenderDispatcher.setRenderShadows(false);
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        entityRenderDispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixStack, immediate, 15728880);
        immediate.draw();
        entityRenderDispatcher.setRenderShadows(true);
        entity.bodyYaw = h;
        entity.yaw = i;
        entity.pitch = j;
        entity.prevHeadYaw = k;
        entity.headYaw = l;
        entity.handSwingProgress = hsp;
        entity.lastHandSwingProgress = hsp;
        entity.inventory.offHand.set(0, stack);
        RenderSystem.popMatrix();
    }

    protected void drawSippableDetails(DrawableHelper helper, MatrixStack matrices, FluidKey fluid, Rectangle bounds) {
        int x, y = bounds.getMinY(), i = 0, j = 0, up = 0, amt = 0;
        if (SippableRegistry.SIPS.containsKey(fluid)) {
            Sippable sip = SippableRegistry.SIPS.get(fluid);

            if (sip.shanks > 0) {
                int outline = 16;
                int shank = 52;
                int halfshank = shank + 9;
                int shankloop = (int) Math.ceil(Math.abs(sip.shanks) / 2f);
                x = bounds.getMaxX() - 22 + (9 * shankloop) / 2;

                mc.getTextureManager().bindTexture(Screen.GUI_ICONS_TEXTURE);
                for (int h = 0; h < shankloop * 2; h += 2) {
                    x -= 9;
                    helper.drawTexture(matrices, x, y, outline, 27, 9, 9);

                    if (sip.shanks > h) {
                        helper.drawTexture(matrices, x, y, sip.shanks - 1 == h ? halfshank : shank, 27, 9, 9);
                    }
                }
                up = 1;
            }
            if (sip.saturation > 0) {
                int satX = 43;
                int sat = 70;
                int halfsat = sat + 9;
                int satloop = (int) Math.max(1, Math.ceil(Math.abs(sip.saturation) / 2f));
                x = bounds.getMaxX() - 22 + (9 * satloop) / 2;
                y -= 9 * up;
                for (int h = 0; h < satloop * 2; h += 2) {
                    float effectiveSaturationOfBar = (Math.abs(sip.saturation) - h) / 2f;
                    x -= 9;
                    RenderSystem.color4f(1f, 0.8f, 0f, 1f);
                    helper.drawTexture(matrices, x, y, satX, 27, 9, 9);
                    RenderSystem.color4f(1f, 1f, 1f, 1f);
                    helper.drawTexture(matrices, x, y, effectiveSaturationOfBar >= 1 ? sat : halfsat, 27, 9, 9);

                }
                up = 1;
            }

            if (sip.damage != 0) {
                boolean heal = sip.damage < 0;
                int loop = (int) Math.max(1, Math.ceil(Math.abs(sip.damage) / 2f));
                int outline = heal ? 16 : 34;
                int heart = heal ? 52 : 124;
                int halfheart = heart + 9;
                x = bounds.getMaxX() - 22 + (9 * loop) / 2;
                y -= 9 * up;
                mc.getTextureManager().bindTexture(Screen.GUI_ICONS_TEXTURE);
                for (int h = 0; h < loop * 2; h += 2) {
                    float effectivedmg = (Math.abs(sip.damage) - h) / 2f;
                    x -= 9;
                    helper.drawTexture(matrices, x, y, outline, 0, 9, 9);
                    helper.drawTexture(matrices, x, y, effectivedmg >= 1 ? heart : halfheart, 0, 9, 9);
                }
            }

            for (Sippable.Effect effect : sip.effects) {
                StatusEffectInstance fx = effect.getEffect();
                StatusEffect type = fx.getEffectType();

                x = bounds.getMaxX() + 9;
                y = bounds.getMaxY() - 2;
                i++;
                amt++;
                x -= 24 * i;
                if (amt > 7) {
                    x = bounds.getMaxX() + 9;
                    ++j;
                    x -= 24 * j;
                    y -= 24;
                }
                mc.getTextureManager().bindTexture(HandledScreen.BACKGROUND_TEXTURE);
                helper.drawTexture(matrices, x, y, 141, 166, 24, 24);
                String duration = StatusEffectUtil.durationToString(fx, 1f);
                Sprite sprite = mc.getStatusEffectSpriteManager().getSprite(type);
                mc.getTextureManager().bindTexture(sprite.getAtlas().getId());
                DrawableHelper.drawSprite(matrices, x + 3, y + 3, helper.getZOffset(), 18, 18, sprite);
                matrices.push();
                float f = 0.65f;
                matrices.scale(f, f, 1f);
                if (fx.getAmplifier() > 0) {
                    String roman = RomanConverter.toRoman(fx.getAmplifier());
                    mc.textRenderer.drawWithShadow(matrices, roman, (x + 12) / f - mc.textRenderer.getWidth(roman) / 2f, (y + 3) / f, 0xFFFFFFFF);
                }
                if (!duration.equals("0:00"))
                    mc.textRenderer.drawWithShadow(matrices, duration, (x + 12) / f - mc.textRenderer.getWidth(duration) / 2f, (y + 16) / f, 0xFFFFFFFF);
                matrices.pop();
            }
        }
    }
}
