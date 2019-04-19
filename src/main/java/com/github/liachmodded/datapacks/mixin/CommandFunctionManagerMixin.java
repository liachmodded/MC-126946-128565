package com.github.liachmodded.datapacks.mixin;

import com.github.liachmodded.datapacks.DataPacksMain;
import com.github.liachmodded.datapacks.ExecutingFunctionManager;
import com.github.liachmodded.datapacks.FunctionExecutionCallback;
import com.github.liachmodded.datapacks.FunctionHeadEntry;
import com.github.liachmodded.datapacks.FunctionReturnEntry;
import com.github.liachmodded.datapacks.NonCommandEntry;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunction.Element;
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
public abstract class CommandFunctionManagerMixin implements ExecutingFunctionManager {

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

  private int executedCount;

  /**
   * Used to prevent counting embedded functions to executed count/chain length.
   *
   * <p>Added for MC-148612
   */
  private int exemptCount; // Don't count embedded functions twice

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

  private ArrayDeque<Entry> mustRun;

  @Inject(method = "<init>*", at = @At("RETURN"))
  public void onCtor(CallbackInfo ci) {
    this.waitlist = new ArrayDeque<>();
    this.mustRun = new ArrayDeque<>();
    this.exemptCount = 0;
    this.executedCount = 0;
  }

  /**
   * @reason to perform non-recursive depth first search
   * @author liach
   * @return 0; use execution callback instead
   */
  @Overwrite
  public int execute(CommandFunction commandFunction_1, ServerCommandSource serverCommandSource_1) {
    execute(FunctionExecutionCallback.EMPTY, serverCommandSource_1, commandFunction_1);
    return 0;
  }

  private void addToQueue(FunctionExecutionCallback callback, Deque<Entry> queue, ServerCommandSource serverCommandSource_1,
      CommandFunction... functions) {
    FunctionReturnEntry returnEntry = new FunctionReturnEntry(functions, serverCommandSource_1, callback);
    FunctionHeadEntry headEntry = new FunctionHeadEntry(functions, serverCommandSource_1, returnEntry);
    queue.addLast(headEntry);
    for (CommandFunction each : functions) {
      for (Element element : each.getElements()) {
        queue.addLast(new CommandFunctionManager.Entry((CommandFunctionManager) (Object) this, serverCommandSource_1, element));
      }
    }
    queue.addLast(returnEntry);
    this.exemptCount += 2;
  }

  @Override
  public void execute(FunctionExecutionCallback callback, ServerCommandSource serverCommandSource_1, CommandFunction... functions) {
    int maxCommandChainLength = this.getMaxCommandChainLength();

    if (this.field_13411) {
      this.addToQueue(callback, this.waitlist, serverCommandSource_1, functions);
      // liach end
      return;
    }
    try {
      this.field_13411 = true;
      this.exemptCount = 0; // prepare for recursion - MC-148612
      this.executedCount = 0;

      this.addToQueue(callback, this.chain, serverCommandSource_1, functions);

      while (!this.chain.isEmpty()) {
        boolean countCurrent = true;
        try {
          CommandFunctionManager.Entry commandFunctionManager$Entry_1 = this.chain.removeFirst();
          this.server.getProfiler().push(commandFunctionManager$Entry_1::toString);
          // liach start
          this.waitlist.clear();
          // liach - the max command chain length is no longer needed
          if (commandFunctionManager$Entry_1 instanceof NonCommandEntry) {
            countCurrent = false;
            this.exemptCount--;
          }
          // Uncomment to test
          // DataPacksMain.LOGGER.info("Executing function manager entry {}, #{}", commandFunctionManager$Entry_1, this.executedCount + 1);
          commandFunctionManager$Entry_1.execute(this.waitlist, this.executedCount);

          // Remove chain and waitlist entries that is never going to be executed
          // MC-143269: chain entries should be dumped before waitlist ones
          while (this.chain.size() > Math.max(maxCommandChainLength - this.waitlist.size() + this.exemptCount, 0)) {
            Entry removed = this.chain.removeLast();
            if (removed instanceof NonCommandEntry) {
              this.exemptCount--;
              this.mustRun.add(removed);
            }
          }

          if (this.chain.isEmpty()) {
            while (this.waitlist.size() > maxCommandChainLength + this.exemptCount) {
              Entry removed = this.waitlist.removeLast();
              if (removed instanceof NonCommandEntry) {
                this.mustRun.add(removed);
                this.exemptCount--;
              }
            }
          }

          while (!waitlist.isEmpty()) {
            chain.addFirst(waitlist.removeLast());
          }
        } finally {
          this.server.getProfiler().pop();
        }

        if (countCurrent) {
          ++this.executedCount;
        }
        if (this.executedCount >= maxCommandChainLength) {
          for (Entry each : this.chain) {
            if (each instanceof NonCommandEntry) {
              each.execute(null, this.executedCount);
            }
          }
          for (Entry each : this.mustRun) {
            each.execute(null, this.executedCount);
          }

          return;
        }
      }
    } finally {
      this.chain.clear();
      this.waitlist.clear();
      this.mustRun.clear();
      this.exemptCount = 0;
      this.field_13411 = false;
    }

  }

  @Override
  public boolean isExecuting() {
    return this.field_13411;
  }
}
