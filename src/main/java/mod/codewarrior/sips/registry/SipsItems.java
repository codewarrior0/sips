package mod.codewarrior.sips.registry;

import mod.codewarrior.sips.SipsMod;
import mod.codewarrior.sips.config.SipsConfig;
import mod.codewarrior.sips.items.SipsItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class SipsItems {
    public static Item LIL_SIP = new SipsItem(new Item.Settings(), SipsConfig.getLilSipCapacity(), 32);
    public static Item BIG_CHUG = new SipsItem(new Item.Settings(), SipsConfig.getBigChugCapacity(), 16);

    public static void init() {
        register("lil_sip", LIL_SIP);
        register("big_chug", BIG_CHUG);
    }

    public static void register(String name, Item item) {
        Registry.register(Registry.ITEM, SipsMod.getId(name), item);
    }
}
