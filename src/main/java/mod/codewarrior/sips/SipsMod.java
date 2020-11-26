package mod.codewarrior.sips;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mod.codewarrior.sips.config.SipsConfig;
import mod.codewarrior.sips.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.Month;

public class SipsMod implements ModInitializer {
    public static String modid = "sips";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Logger LOGGER = LogManager.getLogger(modid);
    public static LocalDateTime date = LocalDateTime.now();
    static boolean fool = false;

    @Override
    public void onInitialize() {
        if (date.getMonth() == Month.APRIL && date.getDayOfMonth() == 1) {
            fool = true;
        }
        SipsConfig.tryInit();
        SipsItems.init();
        SipsBlocks.init();
        SipsFluids.init();
        SipsEffects.init();
        SipsEvents.init();
        SippableRegistry.init();
    }

    public static boolean isApril1st() {
        return fool;
    }

    public static Identifier getId(String name) {
        return new Identifier(modid, name);
    }

    public static ItemGroup SipsGroup = FabricItemGroupBuilder.build(
            getId(modid),
            () -> new ItemStack(SipsItems.LIL_SIP)
    );
}
