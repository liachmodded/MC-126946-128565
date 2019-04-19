package com.github.liachmodded.datapacks;

import java.util.ArrayDeque;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager.Entry;

public class FunctionHeadEntry extends FunctionMarkerEntry {

  private final FunctionReturnEntry tail;

  public FunctionHeadEntry(CommandFunction[] functions, ServerCommandSource source, FunctionReturnEntry tail) {
    super(functions, source);
    this.tail = tail;
  }

  @Override
  public void execute(ArrayDeque<Entry> arrayDeque_1, int executedCount) {
    tail.setStartIndex(executedCount + 1);
  }

  @Override
  public String toString() {
    return "~~elements of function(s) " + nameString + " by " + sourceString + " begin";
  }
}
