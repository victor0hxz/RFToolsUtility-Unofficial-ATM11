package mcjty.rftoolsutility.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandCleanupReceivers implements Command<CommandSourceStack> {
   private static final CommandCleanupReceivers CMD = new CommandCleanupReceivers();

   public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
      return ((LiteralArgumentBuilder)Commands.literal("cleanupreceivers").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).executes(CMD);
   }

   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      TeleportDestinations.get(((CommandSourceStack)context.getSource()).getLevel()).cleanupInvalid();
      return 0;
   }
}
