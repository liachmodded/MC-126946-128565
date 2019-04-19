package com.github.liachmodded.datapacks;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;

public interface ExecutingFunctionManager {

  boolean isExecuting();

  void execute(FunctionExecutionCallback callback, ServerCommandSource serverCommandSource_1, CommandFunction... function);
}
