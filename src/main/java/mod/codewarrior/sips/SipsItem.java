package mod.codewarrior.sips;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
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

public class SipsItem extends Item {

    public final int maxCapacity;
    public final int SIP = 250;
    public final int itemUseDuration;

    public SipsItem(String name, int capacity, int duration) {
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
            } else {
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

    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, true);
        if (raytraceresult != null && raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
            boolean result = FluidUtil.interactWithFluidHandler(playerIn, handIn, worldIn, raytraceresult.getBlockPos(), raytraceresult.sideHit);
            if (result) return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        }

        IFluidHandlerItem cap = itemstack.getCapability(FLUID_HANDLER_ITEM_CAPABILITY, EnumFacing.DOWN);

        if (cap != null) {
            FluidStack drank = cap.drain(250, false);
            if (drank != null && drank.amount == 250) {
                playerIn.setActiveHand(handIn);
                return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
            }
        }

        return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
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

        return stack;
    }

    protected void onSipped(FluidStack drank, World worldIn, EntityPlayer player)
    {
        String fluidName = drank.getFluid().getName();
        Config.FluidStats stats = Config.stats.get(fluidName);
        if (stats != null) {
            player.getFoodStats().addStats(stats.shanks, stats.saturation);
            if (stats.damage > 0) {
                player.attackEntityFrom(new DamageSource("sip") {
                    @Override
                    public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
                        return new TextComponentTranslation("death.sipped", entityLivingBaseIn.getDisplayName(), drank.getLocalizedName());
                    }
                }, stats.damage);
            }
            if (stats.potionName != null) {
                Potion potion = Potion.getPotionFromResourceLocation(stats.potionName);
                if (potion != null) {
                    player.addPotionEffect(new PotionEffect(potion, stats.potionDuration, stats.potionLevel));
                }
            }
        }

        if (fluidName.equals("milk")) {
            ItemStack fakeMilkBucket = new ItemStack(Items.MILK_BUCKET);
            player.curePotionEffects(fakeMilkBucket);
        }
    }

    public int getMaxItemUseDuration(ItemStack stack)
    {
        return itemUseDuration;
    }

    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.DRINK;
    }


    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
    {
        if(!stack.isEmpty())
            return new FluidHandlerItemStack(stack, maxCapacity);
        return null;
    }
}
