package mod.codewarrior.sips;

import ca.wescook.nutrition.capabilities.INutrientManager;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.nutrients.NutrientList;
import ca.wescook.nutrition.nutrients.NutrientUtils;
import ca.wescook.nutrition.proxy.ClientProxy;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBucketMilk;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class NutritionCompat {
    @CapabilityInject(INutrientManager.class)
    private static final Capability<INutrientManager> NUTRITION_CAPABILITY = null;

    public static final Map<String, List<String>> nutrients = new HashMap<>();

    public static void addNutrients() {
        for (String configLine : Config.SipsConfig.compat.nutrients) {
            List<String> fields = Arrays.stream(configLine.split(",")).map(String::trim).collect(Collectors.toList());
            if(fields.size() == 0) continue;
            if(fields.size() == 1) {
                SipsMod.logger.error("Not enough fields in fluid nutrients line '{}'", configLine);
                continue;
            }
            String fluidName = fields.remove(0);

            nutrients.put(fluidName, fields);
        }
    }

    public static void applyNutrition(EntityPlayer player, String fluidName, int shanks) {
        List<Nutrient> foundNutrients = getNutrients(fluidName);
        if(fluidName.equals("milk") && shanks < 1) shanks = 1; // Nutrition considers a milk bucket as 4 shanks for nutrient purposes
        float nutritionValue = calculateNutrition(shanks, foundNutrients);


        if (!player.getEntityWorld().isRemote) {
            player.getCapability(NUTRITION_CAPABILITY, null).add(foundNutrients, nutritionValue);
        } else {
            ClientProxy.localNutrition.add(foundNutrients, nutritionValue);
        }

    }

    private static List<Nutrient> getNutrients(String fluidName) {
        return nutrients.getOrDefault(fluidName, new ArrayList<String>())
                    .stream().map(NutrientList::getByName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
    }

    private static float calculateNutrition(double shanks, List<Nutrient> foundNutrients) {
        float adjustedFoodValue = (float)(shanks * 0.5D);
        adjustedFoodValue *= ca.wescook.nutrition.utility.Config.nutritionMultiplier;
        float lossPercentage = (float) ca.wescook.nutrition.utility.Config.lossPerNutrient / 100.0F;
        float foodLoss = adjustedFoodValue * lossPercentage * (float)(foundNutrients.size() - 1);
        return Math.max(0.0F, adjustedFoodValue - foodLoss);
    }

    public static void addStackInformation(String fluidName, List<String> info) {
        StringJoiner stringJoiner = new StringJoiner(", ");
        List<Nutrient> foundNutrients = getNutrients(fluidName);

        for (Nutrient nutrient : foundNutrients) {
            if (nutrient.visible) {
                stringJoiner.add(I18n.format("nutrient.nutrition:" + nutrient.name));
            }
        }

        int shanks = 0;
        Sippable stats = Config.stats.get(fluidName);
        if (stats != null) shanks = stats.shanks;
        if(fluidName.equals("milk") && shanks < 1) shanks = 1; // Nutrition considers a milk bucket as 4 shanks for nutrient purposes


        String nutrientString = stringJoiner.toString();
        float nutritionValue = calculateNutrition(shanks, foundNutrients);
        if (!nutrientString.equals("")) {
            String tooltip = I18n.format("tooltip.nutrition:nutrients") + " " + TextFormatting.DARK_GREEN + nutrientString + TextFormatting.DARK_AQUA + " (" + String.format("%.1f", nutritionValue) + "%)";
            info.add(tooltip);
        }
    }
}
