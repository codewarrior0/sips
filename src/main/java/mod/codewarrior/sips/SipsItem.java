package mod.codewarrior.sips;

import mod.codewarrior.sips.Config.SipsConfig;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;

public class SipsItem extends ItemFood {

    public final int maxCapacity;
    public final int SIP = 250;
    public final int itemUseDuration;

    public SipsItem(String name, int capacity, int duration) {
        super(0, 0, false);

        this.setCreativeTab(CreativeTabs.MISC);
        this.setUnlocalizedName("sips." + name);
        this.setRegistryName(name);
        this.maxCapacity = capacity;
        this.itemUseDuration = duration;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
    {
        FluidStack fs = FluidUtil.getFluidContained(stack);
        if (fs!=null)
        {
            TextFormatting rarity = fs.getFluid().getRarity()== EnumRarity.COMMON?TextFormatting.GRAY: fs.getFluid().getRarity().rarityColor;
            int sips = fs.amount / SIP;
            list.add(rarity+fs.getLocalizedName()+TextFormatting.GRAY+": " + I18n.format("desc.sips.sips", sips) + " (" +fs.amount+"/"+ maxCapacity +"mB" + ")");

            if (Config.nutritionEnabled()) {
                NutritionCompat.addStackInformation(fs.getFluid().getName(), list);
            }
        }
        else {
            list.add(I18n.format("desc.sips.empty"));
        }

    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
        if (target.hasCapability(FLUID_HANDLER_CAPABILITY, null)) {
            IFluidHandler cap = target.getCapability(FLUID_HANDLER_CAPABILITY, null);
            if (cap != null) {
                FluidActionResult result = FluidUtil.tryFillContainer(stack, cap, maxCapacity, playerIn, true);
                if (result.success) return true;
            }
        }

        if (target instanceof EntityCow) {
            FluidStack milked;
            if (target instanceof EntityMooshroom) {
                milked = new FluidStack(SipsMod.fluidMushroomStew, 1000);
            }
            else {
                milked = new FluidStack(SipsMod.fluidMilk, 1000);
            }

            IFluidHandler cap = FluidUtil.getFluidHandler(stack);
            if (cap != null) {
                int result = cap.fill(milked, true);
                if (result > 0) {
                    playerIn.world.playSound((EntityPlayer)null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.PLAYERS, 0.5F, playerIn.world.rand.nextFloat() * 0.1F + 0.9F);
                    return true;
                }
            }
        }
        return false;
    }

    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack itemstack = player.getHeldItem(hand);
        ActionResult<ItemStack> result = new ActionResult<>(EnumActionResult.PASS, itemstack);

        IFluidHandlerItem itemHandler = itemstack.getCapability(FLUID_HANDLER_ITEM_CAPABILITY, EnumFacing.DOWN);
        if(itemHandler == null) return result;

        RayTraceResult raytraceresult = this.rayTrace(world, player, true);
        if (raytraceresult != null && raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
            IFluidHandler blockHandler = FluidUtil.getFluidHandler(world, raytraceresult.getBlockPos(), raytraceresult.sideHit);
            if(blockHandler != null) {
                FluidStack filled = FluidUtil.tryFluidTransfer(itemHandler, blockHandler, Integer.MAX_VALUE, true);
                if (filled != null && filled.amount > 0) {
                    SoundEvent soundevent = filled.getFluid().getFillSound(filled);
                    player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    result = new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
                } else {
                    FluidStack drained = FluidUtil.tryFluidTransfer(itemHandler, blockHandler, Integer.MAX_VALUE, true);
                    if (drained != null && drained.amount > 0) {
                        SoundEvent soundevent = drained.getFluid().getEmptySound(drained);
                        player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        result = new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
                    }
                }
            }
        }

        if (result.getType() == EnumActionResult.PASS) {
            FluidStack drank = itemHandler.drain(250, false);
            if (drank != null && drank.amount == 250) {
                player.setActiveHand(hand);
                result = new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
            }
        }

        return result;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving)
    {
        IFluidHandlerItem cap = stack.getCapability(FLUID_HANDLER_ITEM_CAPABILITY, EnumFacing.DOWN);
        if (cap == null) {
            return stack;
        }

        FluidStack drank = cap.drain(250, true);
        if (drank != null && drank.amount != 250) {
            return stack;
        }

        if (entityLiving instanceof EntityPlayer)
        {
            EntityPlayer entityplayer = (EntityPlayer)entityLiving;
            this.onSipped(drank, worldIn, entityplayer);
        }

        if (SipsConfig.compat.slipChance > 0.0 && stack.hasTagCompound() && stack.getTagCompound().hasKey("oiled")) {
            if(SipsConfig.compat.slipChance < worldIn.rand.nextFloat()) {
                stack.getTagCompound().removeTag("oiled");
                if (entityLiving instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entityLiving;
                    EnumHand hand = null;
                    if (player.getHeldItem(EnumHand.MAIN_HAND) == stack) {
                        hand = EnumHand.MAIN_HAND;
                    } else if (player.getHeldItem(EnumHand.OFF_HAND) == stack) {
                        hand = EnumHand.OFF_HAND;
                    }
                    if (hand != null) {
                        player.dropItem(stack, false);
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        return stack;
    }

    protected void onSipped(FluidStack drank, World world, EntityPlayer player)
    {
        String fluidName = drank.getFluid().getName();
        Sippable stats = Config.stats.get(fluidName);
        float damage = 0;
        int shanks = 0;
        float saturation = 0;

        if (stats != null) {
            shanks = stats.shanks;
            saturation = stats.saturation;
            player.getFoodStats().addStats(shanks, saturation);
            damage = stats.damage;
            for(Sippable.Effect e: stats.effects) {
                PotionEffect effect = e.getEffect();
                if (effect != null) {
                    player.addPotionEffect(effect);
                }
            }
            stats.onSipped(drank, world, player);
        }
        else {
            int kelvins = drank.getFluid().getTemperature();
            if (SipsConfig.temperatureDamagePerKelvin != 0.0) {
                if (kelvins > 320) {
                    damage = (kelvins - 320) * SipsConfig.temperatureDamagePerKelvin;
                } else if (kelvins < 260) {
                    damage = (260 - kelvins) * SipsConfig.temperatureDamagePerKelvin;
                }
            }
            if (SipsConfig.temperatureEffects) {
                if (kelvins > 320) {
                    player.setFire(30);
                } else if (kelvins < 260) {
                    player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 30));
                    player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 30));
                }
            }
        }
        if (damage > 0) {
            player.attackEntityFrom(new DamageSource("sip") {
                @Override
                public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
                    return new TextComponentTranslation("death.sipped", entityLivingBaseIn.getDisplayName(), drank.getLocalizedName());
                }
            }.setDamageBypassesArmor(), damage);
        }
        else if (damage < 0) {
            player.heal(-damage);
        }

        if (fluidName.equals("milk")) {
            ItemStack fakeMilkBucket = new ItemStack(Items.MILK_BUCKET);
            player.curePotionEffects(fakeMilkBucket);
        }

        /* ThermalExpansion potion fluids */
        if (SipsConfig.compat.thermalExpansion && drank.tag != null && drank.tag.hasKey("Potion")) {
            String potionName = drank.tag.getString("Potion");
            PotionType potionType = potionName.length() == 0 ? null : PotionType.getPotionTypeForName(potionName);
            if (potionType != null) {
                for (PotionEffect effect : potionType.getEffects()) {
                    if (effect.getPotion().isInstant()) {
                        effect.getPotion().affectEntity(player, player, player, effect.getAmplifier(), 1.0D);
                    }
                    else {
                        player.addPotionEffect(new PotionEffect(effect));
                    }
                }
            }
        }

        if (Config.nutritionEnabled()) {
            try {
                NutritionCompat.applyNutrition(player, fluidName, shanks);
            } catch (Exception e) {
                SipsMod.logger.error("Failed to apply Nutrition effects! Update both Sips and Nutrition, then report this error to Sips if it occurs again.", e);
            }
        }
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }

    public int getMaxItemUseDuration(ItemStack stack)
    {
        FluidStack contents = FluidUtil.getFluidContained(stack);
        if (contents != null && contents.amount > 0) {
            return itemUseDuration;
        }
        return 0;
    }

    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.DRINK;
    }

    public static Sippable getFluidStats(ItemStack stack) {
        FluidStack contents = FluidUtil.getFluidContained(stack);
        if(contents != null && contents.amount > 0) {
            return Config.stats.getOrDefault(contents.getFluid().getName(), Sippable.UNDEFINED);
        }
        return Sippable.UNDEFINED;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
    {
        if(!stack.isEmpty())
            return new FluidHandlerItemStack(stack, maxCapacity);
        return null;
    }

    /* ItemFood overrides */

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        throw new RuntimeException("Can't get here.");
    }

    @Override
    public int getHealAmount(ItemStack stack) {
        return getFluidStats(stack).shanks;
    }

    @Override
    public float getSaturationModifier(ItemStack stack) {
        return getFluidStats(stack).saturation;
    }

    @Override
    public boolean isWolfsFavoriteMeat() {
        return false;
    }

    @Override
    public ItemFood setPotionEffect(PotionEffect effect, float probability) {
        throw new RuntimeException("Can't get here.");
    }

    @Override
    public ItemFood setAlwaysEdible() {
        throw new RuntimeException("Can't get here.");
    }
}
