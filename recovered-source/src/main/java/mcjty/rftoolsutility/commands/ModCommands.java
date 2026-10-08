package mcjty.rftoolsutility.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {
   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      LiteralCommandNode<CommandSourceStack> commands = dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("rftoolsutility").then(CommandSetBuffs.register(dispatcher)))
            .then(CommandCleanupReceivers.register(dispatcher))
      );
      dispatcher.register((LiteralArgumentBuilder)Commands.literal("rfutil").redirect(commands));
   }
}
