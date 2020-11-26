package mod.codewarrior.sips.config;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import static mod.codewarrior.sips.SipsMod.LOGGER;

public class SipsConfig {
    static boolean isInitialized = false;
    static int lilSipCapacity;
    static int bigChugCapacity;
    static int modelCacheSize;
    static float temperatureDamagePerCelsius;
    static boolean temperatureEffects;
    static boolean use3DModels;
    static boolean loadDefaultSippables;
    static boolean liquidXpEffect;
    static boolean listDullBeverages;

    public static int getLilSipCapacity() {
        return lilSipCapacity;
    }

    public static int getBigChugCapacity() {
        return bigChugCapacity;
    }

    public static int getModelCacheSize() {
        return modelCacheSize;
    }

    public static boolean use3DModels() {
        return use3DModels;
    }

    public static float getTemperatureDamagePerCelsius() {
        return temperatureDamagePerCelsius;
    }

    public static boolean useTemperatureEffects() {
        return temperatureEffects;
    }

    public static boolean loadDefaultSippables() {
        return loadDefaultSippables;
    }

    public static boolean liquidXpHasEffect() {
        return liquidXpEffect;
    }

    public static boolean listDullBeverages() {
        return listDullBeverages;
    }

    private static void handleConfig(Map<String, String> cfg) {
        lilSipCapacity = Integer.parseInt(cfg.get("lilSipCapacity")) * 250;
        bigChugCapacity = Integer.parseInt(cfg.get("bigChugCapacity")) * 250;
        modelCacheSize = Integer.parseInt(cfg.get("modelCacheSize"));
        temperatureDamagePerCelsius = Float.parseFloat(cfg.get("temperatureDamagePerCelsius"));
        temperatureEffects = Boolean.parseBoolean(cfg.get("temperatureEffects"));
        use3DModels = Boolean.parseBoolean(cfg.get("use3DModels"));
        loadDefaultSippables = Boolean.parseBoolean(cfg.get("loadDefaultSippables"));
        liquidXpEffect = Boolean.parseBoolean(cfg.get("liquidXpEffect"));
        listDullBeverages = Boolean.parseBoolean(cfg.get("listDullBeverages"));
    }

    public static void tryInit() {
        if (!isInitialized) init();
    }

    private static void init() {
        Map<String, String> cfg = new Object2ObjectOpenHashMap<>();
        ImmutableSet<Entry<?>> entries = ImmutableSet.of(
                Entry.of("lilSipCapacity", 4,
                        "lilSipCapacity: The amount of sips a Lil Sip can hold. [Side: BOTH | Default: 4]"),
                Entry.of("bigChugCapacity", 32,
                        "bigChugCapacity: The amount of sips a Big Chug can hold. [Side: BOTH | Default: 32]"),
                Entry.of("temperatureDamagePerCelsius", 0.1f,
                        "temperatureDamagePerCelsius: Unlisted fluids will deal this much damage per Celsius above 46.85 or below -13.15. [Side: SERVER | Default: 0.1]" +
                                "\n#(Default fluid temperature: 26.85C; Lava: 1026.85C; Cryotheum: -223.15C)."),
                Entry.of("temperatureEffects", true,
                        "temperatureEffects: Unlisted fluids will set the player on fire or apply slowness and fatigue effects if they are too hot or cold. [Side: SERVER | Default : true]"),
                Entry.of("modelCacheSize", 48,
                        "modelCacheSize: The amount of fluid states that can be cached for Sip rendering. [Side: CLIENT | Default: 48]"),
                Entry.of("use3DModels", true,
                        "use3DModels: When false, reverts to a less resource-intensive renderer. [Side: CLIENT | Default: true]"),
                Entry.of("loadDefaultSippables", true,
                        "loadDefaultSippables: Toggles the parsing of any sippables json named \"default_sippables.json\". [Side: SERVER | Default: true]"),
                Entry.of("liquidXpEffect", true,
                        "[COMPAT] liquidXpEffect: Fluids with an id of \"liquid_xp\" give experience with each Sip. [Side: SERVER | Default: true]"),
                Entry.of("listDullBeverages", true,
                        "[COMPAT] listEmptyBeverages: Toggles populating the Beverages Category in REI with effectless fluids. [Side: CLIENT | Default: true]")
        );
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("sips.properties");
        try {
            boolean changed = false;
            File configurationFile = configPath.toFile();
            if (Files.notExists(configPath) && !configPath.toFile().createNewFile()) {
                LOGGER.error("[Sips] Error creating config file \"" + configurationFile + "\".");
            }
            Properties config = new Properties();
            StringBuilder content = new StringBuilder().append("#Sips Configuration.\n");
            content.append("#Last generated at: ").append(new Date().toString()).append("\n\n");
            FileInputStream input = new FileInputStream(configurationFile);
            config.load(input);
            for (Entry<?> entry : entries) {
                String key = entry.key;
                Object value = entry.value;
                Class<?> cls = entry.cls;
                if (!config.containsKey(key)) {
                    changed = true;
                    config.setProperty(key, value.toString());
                }
                if (config.containsKey(key)) {
                    Object obj = config.getProperty(key);
                    String s = String.valueOf(obj);
                    if (s.equals("")) {
                        LOGGER.error("[Sips] Error processing configuration file \"" + configurationFile + "\".");
                        LOGGER.error("[Sips] Expected configuration value for " + key + " to be present, found nothing. Using default value \"" + value + "\" instead.");
                        cfg.put(key, value.toString());
                    } else if (cls.equals(Integer.class)) {
                        try {
                            Integer.parseInt(s);
                            cfg.put(key, s);
                        } catch (NumberFormatException e) {
                            LOGGER.error("[Sips] Error processing configuration file \"" + configurationFile + "\".");
                            LOGGER.error("[Sips] Expected configuration value for " + key + " to be an integer, found \"" + s + "\". Using default value \"" + value + "\" instead.");
                            cfg.put(key, value.toString());
                        }
                    } else if (cls.equals(Float.class)) {
                        try {
                            Float.parseFloat(s);
                            cfg.put(key, s);
                        } catch (NumberFormatException e) {
                            LOGGER.error("[Sips] Error processing configuration file \"" + configurationFile + "\".");
                            LOGGER.error("[Sips] Expected configuration value for " + key + " to be a float, found \"" + s + "\". Using default value \"" + value + "\" instead.");
                            cfg.put(key, value.toString());
                        }
                    } else if (cls.equals(Boolean.class)) {
                        if (!"true".equals(s.toLowerCase()) && !"false".equals(s.toLowerCase())) {
                            LOGGER.error("[Sips] Error processing configuration file \"" + configurationFile + "\".");
                            LOGGER.error("[Sips] Expected configuration value for " + key + " to be a boolean, found \"" + s + "\". Using default value \"" + value + "\" instead.");
                            cfg.put(key, value.toString());
                        } else cfg.put(key, s);
                    }
                }
                content.append("#").append(entry.comment.get()).append("\n");
                content.append(key).append("=").append(cfg.get(key)).append("\n");
            }
            if (changed) {
                FileWriter fw = new FileWriter(configurationFile, false);
                fw.write(content.toString());
                fw.close();
            }
            handleConfig(cfg);
            isInitialized = true;
        } catch (IOException e) {
            LOGGER.error("[Sips] Could not read/write config! Stacktrace: " + e);
        }
    }

    private static class Entry<T> {
        private final String key;
        private final T value;
        private final WeakReference<String> comment;
        private final Class<T> cls;

        private Entry(String key, T value, String comment, Class<T> cls) {
            this.key = key;
            this.value = value;
            this.comment = new WeakReference<>(comment);
            this.cls = cls;
        }

        public static Entry<Integer> of(String key, int value, String comment) {
            return new Entry<>(key, value, comment, Integer.class);
        }

        public static Entry<Float> of(String key, float value, String comment) {
            return new Entry<>(key, value, comment, Float.class);
        }

        public static Entry<Boolean> of(String key, boolean value, String comment) {
            return new Entry<>(key, value, comment, Boolean.class);
        }
    }
}
