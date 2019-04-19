package com.github.liachmodded.datapacks.mixin;

import com.github.liachmodded.datapacks.ExecutingFunctionManager;
import com.github.liachmodded.datapacks.FunctionExecutionCallback;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.stream.IntStream;
import net.minecraft.command.arguments.FunctionArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.text.TranslatableTextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FunctionCommand.class)
public class FunctionCommandMixin {

  @Shadow
  @Final
  public static SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER;

  /**
   * @author liach
   */
  @Overwrite
  public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher_1) {
    commandDispatcher_1.register(
        CommandManager.literal("function")
            .requires((serverCommandSource_1) -> serverCommandSource_1.hasPermissionLevel(2))
            .then(CommandManager
                .argument("name", FunctionArgumentType.create())
                .suggests(SUGGESTION_PROVIDER)
                .executes((commandContext_1) -> execute(commandContext_1, FunctionArgumentType.getFunctions(commandContext_1, "name")))
            )
    );
  }

  private static int execute(CommandContext<ServerCommandSource> context, Collection<CommandFunction> collection_1) {
    ServerCommandSource serverCommandSource_1 = context.getSource();
    int int_1 = 0;

    ExecutingFunctionManager manager = (ExecutingFunctionManager) serverCommandSource_1.getMinecraftServer().getCommandFunctionManager();
    final boolean executing = manager.isExecuting();
    final int[] arr = new int[1];
    final FunctionExecutionCallback extra =
        executing ? (count) -> context.getSource().onCommandComplete(context, true, count) : FunctionExecutionCallback.EMPTY;

    manager.execute(extra.after((i) -> arr[0] = i), serverCommandSource_1.withSilent().withMaxLevel(2), collection_1.toArray(new CommandFunction[0]));

    if (!executing) {
      if (collection_1.size() == 1) {
        serverCommandSource_1
            .sendFeedback(new TranslatableTextComponent("commands.function.success.single", arr[0], collection_1.iterator().next().getId()), true);
      } else {
        serverCommandSource_1
            .sendFeedback(new TranslatableTextComponent("commands.function.success.multiple", IntStream.of(arr).sum(), collection_1.size()), true);
      }
    }

    return int_1;
  }
}
