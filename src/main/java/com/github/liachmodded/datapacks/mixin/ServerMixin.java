package com.github.liachmodded.datapacks.mixin;

import com.github.liachmodded.datapacks.DatapacksPackCreator;
import java.io.File;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourcePackContainer;
import net.minecraft.resource.ResourcePackContainerManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.GameTaskQueue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class ServerMixin extends GameTaskQueue<ServerTask> {

  @Shadow
  @Final
  private ResourcePackContainerManager<ResourcePackContainer> resourcePackContainerManager;

  @Shadow
  @Final
  private ReloadableResourceManager dataManager;

  @Shadow
  public abstract CommandFunctionManager getCommandFunctionManager();

  protected ServerMixin(String string_1) {
    super(string_1);
  }

  @Inject(method = "method_3800(Ljava/io/File;Lnet/minecraft/world/level/LevelProperties;)V",
      at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;field_4553:Lnet/minecraft/resource/FileResourcePackCreator;", ordinal = 0))
  public void addToCreators(CallbackInfo ci) {
    resourcePackContainerManager.addCreator(new DatapacksPackCreator("shared", new File(FabricLoader.getInstance().getGameDirectory(), "datapacks")));
  }

}
