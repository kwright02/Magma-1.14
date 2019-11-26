package org.magmafoundation.magma.api.mixin.server;

import java.util.ArrayList;
import java.util.List;
import joptsimple.OptionSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.server.ServerWorld;
import org.magmafoundation.magma.api.bridge.server.IBridgeMinecraftServer;
import org.magmafoundation.magma.api.core.MagmaOptions;
import org.magmafoundation.magma.api.core.MagmaServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MixinMinecraftServer
 *
 * @author Hexeption admin@hexeption.co.uk
 * @since 24/11/2019 - 05:57 am
 */
@Mixin(MinecraftServer.class)
public class MixinMinecraftServer implements IBridgeMinecraftServer {

    private static MagmaServer magmaServer;
    private static OptionSet options;

    public List<ServerWorld> serverWorldList = new ArrayList<>();

    @Inject(method = "main", at = @At("HEAD"))
    private static void main(String[] p_main_0_, CallbackInfo callbackInfo) {
        options = MagmaOptions.main(p_main_0_);
    }

    @Override
    public MagmaServer getMagmaServer() {
        return magmaServer;
    }

    @Override
    public void setMagmaServer(MagmaServer magmaServer) {
        this.magmaServer = magmaServer;
    }

    @Override
    public OptionSet getOptions() {
        return options;
    }

    @Override
    public List<ServerWorld> getServerWorldList() {
        return serverWorldList;
    }

    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedServer;setGuiEnabled()V"))
    private static void guiEnabled(DedicatedServer dedicatedServer) {
        // Turns of gui all the time.
        // TODO: 24/11/2019 Make this a setting in configs 
    }

}