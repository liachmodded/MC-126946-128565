package com.github.liachmodded.datapacks.mixin;

import java.util.ArrayDeque;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.function.CommandFunctionManager.Entry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandFunctionManager.class)
public abstract class CommandFunctionManagerMixin {

  @Shadow
  @Final
  private MinecraftServer server;
  @Shadow
  @Final
  private ArrayDeque<Entry> chain;
  @Shadow
  private boolean field_13411;

  @Shadow
  public abstract int getMaxCommandChainLength();

  private ArrayDeque<Entry> waitlist;

  @Inject(method = "<init>*", at = @At("RETURN"))
  public void onCtor(CallbackInfo ci) {
    this.waitlist = new ArrayDeque<>();
  }

  /**
   * @reason To add another stack for in-the-middle functions
   * @author liach
   * @return The command result
   */
  @Overwrite
  public int execute(CommandFunction commandFunction_1, ServerCommandSource serverCommandSource_1) {
    int int_1 = this.getMaxCommandChainLength();

    // liach: remove unnecessary boolean field
    if (!this.chain.isEmpty()) {
      // liach start - add to waitlist instead
      if (this.chain.size() + this.waitlist.size() < int_1) {
        this.waitlist.add(new CommandFunctionManager.Entry((CommandFunctionManager) (Object) this, serverCommandSource_1,
            new CommandFunction.FunctionElement(commandFunction_1)));
      }
      // liach end

      return 0;
    } else {
      try {
        //this.field_13411 = true; // liach - use chain.isEmpty() instead
        int int_2 = 0;
        CommandFunction.Element[] commandFunction$Elements_1 = commandFunction_1.getElements();

        int int_3;
        for (int_3 = commandFunction$Elements_1.length - 1; int_3 >= 0; --int_3) {
          this.chain.push(
              new CommandFunctionManager.Entry((CommandFunctionManager) (Object) this, serverCommandSource_1, commandFunction$Elements_1[int_3]));
        }

        while (!this.chain.isEmpty()) {
          try {
            CommandFunctionManager.Entry commandFunctionManager$Entry_1 = this.chain.removeFirst();
            this.server.getProfiler().push(commandFunctionManager$Entry_1::toString);
            // liach start
            this.waitlist.clear();
            // liach - changed method contract to pass in waitlist and max waitlist size
            commandFunctionManager$Entry_1.execute(this.waitlist, int_1 - this.chain.size());

            while (!waitlist.isEmpty()) {
              chain.addFirst(waitlist.removeLast());
              // effectively same as that chain push before the while loop
            }
            // liach end
          } finally {
            this.server.getProfiler().pop();
          }

          ++int_2;
          if (int_2 >= int_1) {
            int_3 = int_2;
            return int_3;
          }
        }

        int_3 = int_2;
        return int_3;
      } finally {
        this.chain.clear();
        //this.field_13411 = false;
      }
    }
  }
}
