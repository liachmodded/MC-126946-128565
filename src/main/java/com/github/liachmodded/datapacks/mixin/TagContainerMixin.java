package com.github.liachmodded.datapacks.mixin;

import java.util.Map;
import net.minecraft.tag.Tag.Builder;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TagContainer.class)
public abstract class TagContainerMixin<T> {

  @Shadow
  @Final
  private boolean ordered;

  @Inject(method = "applyReload(Ljava/util/Map;)V", at = @At("HEAD"))
  public void onApplyReload(Map<Identifier, Builder<T>> map_1, CallbackInfo ci) {
    if (this.ordered) {
      for (Builder<T> builder : map_1.values()) {
        builder.ordered(true);
      }
    }
  }

}
