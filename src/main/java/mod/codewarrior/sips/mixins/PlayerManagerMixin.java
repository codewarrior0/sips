package mod.codewarrior.sips.mixins;

import mod.codewarrior.sips.registry.SippableRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "onDataPacksReloaded", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerRecipeBook;sendInitRecipesPacket(Lnet/minecraft/server/network/ServerPlayerEntity;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void sips_dispatchSippables(CallbackInfo ctx, SynchronizeRecipesS2CPacket w, Iterator<ServerPlayerEntity> var5, ServerPlayerEntity player) {
        if (!this.server.isSinglePlayer()) {
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, SippableRegistry.SIPPABLES_S2C, SippableRegistry.toPacket());
        }
    }

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;)V", shift = At.Shift.BEFORE))
    public void sips_dispatchSippables(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ctx) {
        if (!this.server.isSinglePlayer()) {
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, SippableRegistry.SIPPABLES_S2C, SippableRegistry.toPacket());
        }
    }
}
