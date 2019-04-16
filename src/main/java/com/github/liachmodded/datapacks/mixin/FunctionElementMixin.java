package com.github.liachmodded.datapacks.mixin;

import java.util.ArrayDeque;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CommandFunction.FunctionElement.class)
public abstract class FunctionElementMixin {

  @Shadow
  @Final
  private CommandFunction.LazyContainer function;

  /**
   * @reason Update for changed contract
   * @author liach
   *
   * @param commandFunctionManager_1 the function manager
   * @param serverCommandSource_1 the command sender
   * @param arrayDeque_1 the waitlist
   * @param int_1 the max size allowed for the waitlist
   */
  @Overwrite
  public void execute(CommandFunctionManager commandFunctionManager_1, ServerCommandSource serverCommandSource_1,
      ArrayDeque<CommandFunctionManager.Entry> arrayDeque_1, int int_1) {
    this.function.get(commandFunctionManager_1).ifPresent((commandFunction_1) -> {
      CommandFunction.Element[] commandFunction$Elements_1 = commandFunction_1.getElements();
      int int_2 = int_1 - arrayDeque_1.size();
      int int_3 = Math.min(commandFunction$Elements_1.length, int_2);

      // liach start - update for changed method contract
      for (int int_4 = 0; int_4 < int_3; ++int_4) {
        arrayDeque_1.add(new CommandFunctionManager.Entry(commandFunctionManager_1, serverCommandSource_1, commandFunction$Elements_1[int_4]));
      }
      // liach end

    });
  }
}
