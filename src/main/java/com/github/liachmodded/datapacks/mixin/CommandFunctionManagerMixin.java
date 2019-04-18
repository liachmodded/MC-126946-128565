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
  /**
   * Used like a stack, but calls addFirst/removeFirst instead for clarity
   */
  @Shadow
  @Final
  private ArrayDeque<Entry> chain;
  @Shadow
  private boolean field_13411;

  @Shadow
  public abstract int getMaxCommandChainLength();

  /**
   * Used to prevent counting embedded functions to executed count/chain length.
   *
   * <p>Added for MC-148612
   */
  private int extraFunctions; // Don't count embedded functions twice

  /**
   * A deque containing entries discovered from the current entry. (Non-recursive depth-first search)
   *
   * <p>The iteration order of entries of this wait list is the execution order of them.
   *
   * <p>This is effectively a discovery stack, but is LILO (calls addLast/removeLast) instead of FIFO.
   *
   * <p>Added for MC-126946
   */
  private ArrayDeque<Entry> waitlist;

  @Inject(method = "<init>*", at = @At("RETURN"))
  public void onCtor(CallbackInfo ci) {
    this.waitlist = new ArrayDeque<>();
    this.extraFunctions = 0;
    this.field_13411 = false;
  }

  /**
   * @reason to perform non-recursive depth first search
   * @author liach
   * @return the execution count, or 0 if this call is recursive
   */
  @Overwrite
  public int execute(CommandFunction commandFunction_1, ServerCommandSource serverCommandSource_1) {
    int maxCommandChainLength = this.getMaxCommandChainLength();

    if (this.field_13411) {
      // liach start - DFS - MC-126946
      // liach: don't check on new chain length - MC-143266
      this.waitlist.addLast(new CommandFunctionManager.Entry((CommandFunctionManager) (Object) this, serverCommandSource_1,
          new CommandFunction.FunctionElement(commandFunction_1)));
      this.extraFunctions++; // MC-148612: don't count function entries
      // liach end
      return 0;
    } else {
      try {
        this.field_13411 = true;
        this.extraFunctions = 0; // prepare for recursion - MC-148612
        int int_2 = 0;
        CommandFunction.Element[] commandFunction$Elements_1 = commandFunction_1.getElements();

        int int_3;
        for (int_3 = commandFunction$Elements_1.length - 1; int_3 >= 0; --int_3) {
          this.chain.addFirst(new CommandFunctionManager.Entry((CommandFunctionManager) (Object) this, serverCommandSource_1, commandFunction$Elements_1[int_3]));
        }

        while (!this.chain.isEmpty()) {
          try {
            CommandFunctionManager.Entry commandFunctionManager$Entry_1 = this.chain.removeFirst();
            this.server.getProfiler().push(commandFunctionManager$Entry_1::toString);
            // liach start
            this.waitlist.clear();
            // liach - the max command chain length is no longer needed
            commandFunctionManager$Entry_1.execute(this.waitlist, maxCommandChainLength);

            // Remove chain and waitlist entries that is never going to be executed
            // MC-143269: chain entries should be dumped before waitlist ones
            final int chainAllowance = Math.max(maxCommandChainLength - this.waitlist.size() + this.extraFunctions, 0);

            while (this.chain.size() > chainAllowance) {
              this.chain.removeLast();
            }

            if (this.chain.isEmpty()) {
              final int waitlistAllowance = maxCommandChainLength + this.extraFunctions;

              while (this.waitlist.size() > waitlistAllowance) {
                this.waitlist.removeLast();
              }
            }

            while (!waitlist.isEmpty()) {
              chain.addFirst(waitlist.removeLast());
              // effectively same as that chain push before the while loop
            }
            // liach end
          } finally {
            this.server.getProfiler().pop();
          }

          ++int_2;
          if (int_2 >= maxCommandChainLength + this.extraFunctions) {
            int_3 = int_2 - this.extraFunctions; // MC-148612
            return int_3;
          }
        }

        int_3 = int_2 - this.extraFunctions; // MC-148612
        return int_3;
      } finally {
        this.chain.clear();
        this.waitlist.clear();
        this.extraFunctions = 0;
        this.field_13411 = false;
      }
    }
  }
}
