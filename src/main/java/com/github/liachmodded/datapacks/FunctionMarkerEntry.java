package com.github.liachmodded.datapacks;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager.Entry;

public abstract class FunctionMarkerEntry extends Entry implements NonCommandEntry {

  protected final String nameString;
  protected final String sourceString;

  public FunctionMarkerEntry(CommandFunction[] functions, ServerCommandSource source) {
    super(null, null, null);
    this.nameString = Arrays.stream(functions).map(func -> func.getId().toString()).collect(Collectors.joining(","));
    this.sourceString = source.toString();
  }

  @Override
  public abstract void execute(ArrayDeque<Entry> arrayDeque_1, int executedCount);

  @Override
  public abstract String toString();
}
