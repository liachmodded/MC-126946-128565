package com.github.liachmodded.datapacks;

import java.util.ArrayDeque;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager.Entry;

public class FunctionReturnEntry extends FunctionMarkerEntry {

  private final FunctionExecutionCallback callback;
  private int startIndex; // the starting count of the embedded function section

  public FunctionReturnEntry(CommandFunction[] functions, ServerCommandSource source, FunctionExecutionCallback callback) {
    super(functions, source);
    this.callback = callback;
  }

  void setStartIndex(int startIndex) {
    this.startIndex = startIndex;
  }

  @Override
  public void execute(ArrayDeque<Entry> arrayDeque_1, int executedCount) {
    callback.finishExecution(executedCount - startIndex + 1);
  }

  @Override
  public String toString() {
    return "~~elements of function(s) " + nameString + " by " + sourceString + " end";
  }
}
