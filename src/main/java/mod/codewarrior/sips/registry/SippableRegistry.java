package mod.codewarrior.sips.registry;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import com.google.gson.JsonObject;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.codewarrior.sips.SipsMod;
import mod.codewarrior.sips.config.SipsConfig;
import mod.codewarrior.sips.utils.Sippable;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static mod.codewarrior.sips.SipsMod.GSON;

public class SippableRegistry {
    public static Object2ObjectOpenHashMap<FluidKey, Sippable> SIPS = new Object2ObjectOpenHashMap<>();
    public static final Identifier SIPPABLES_S2C = SipsMod.getId("send_sippables_s2c");

    public static void init() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SippablesResourceListener());
    }

    public static PacketByteBuf toPacket() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(SIPS.size());
        SIPS.forEach((k, v) -> {
            k.toMcBuffer(buf);
            v.toPacket(buf);
        });
        return buf;
    }

    public static void fromPacket(PacketByteBuf buf) {
        SIPS.clear();
        int loop = buf.readInt();
        for (int i = 0; i < loop; i++) {
            SIPS.put(FluidKey.fromMcBuffer(buf), Sippable.fromPacket(buf));
        }
    }

    public static class SippablesResourceListener implements SimpleResourceReloadListener<Collection<Identifier>> {

        @Override
        public Identifier getFabricId() {
            return SipsMod.getId("sippables_resourcelistener");
        }

        @Override
        public CompletableFuture<Collection<Identifier>> load(ResourceManager manager, Profiler profiler, Executor executor) {
            return CompletableFuture.supplyAsync(() -> {
                SIPS.clear();
                return manager.findResources("sippables", s -> SipsConfig.loadDefaultSippables()
                        ? s.endsWith(".json")
                        : s.endsWith(".json") && !s.contains("default_sippables"));
            }, executor);
        }

        @Override
        public CompletableFuture<Void> apply(Collection<Identifier> collection, ResourceManager manager, Profiler profiler, Executor executor) {
            return CompletableFuture.runAsync(() -> {
                collection.forEach(id -> {
                    try {
                        InputStream is = manager.getResource(id).getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        JsonObject json = GSON.fromJson(br, JsonObject.class);
                        json.entrySet().forEach(Sippable::fromConfig);
                    } catch (IOException e) {
                        SipsMod.LOGGER.error("Caught exception during Sippables parsing: " + e);
                    }
                });

                //manual
                if (SipsConfig.liquidXpHasEffect()) {
                    Sippable.fromPredicate("liquid_xp", new Sippable() {
                        @Override
                        public void onSipped(FluidKey drank, World world, PlayerEntity player) {
                            player.addExperience(7 + world.random.nextInt(5));
                        }
                    });
                }
            }, executor);
        }
    }
}
