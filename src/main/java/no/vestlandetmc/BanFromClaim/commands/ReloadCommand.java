package no.vestlandetmc.BanFromClaim.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.Permissions;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class ReloadCommand implements BasicCommand {

	@Override
	public void execute(CommandSourceStack commandSourceStack, String[] args) {
		Config.initialize();
		Messages.initialize();

		final CommandSender sender = commandSourceStack.getSender();
		if (sender instanceof org.bukkit.entity.Player player) {
			MessageHandler.sendMessage(player, Messages.RELOAD);
		} else {
			MessageHandler.sendConsole(Messages.RELOAD);
		}
	}

	@Override
	public @Nullable String permission() {
		return Permissions.ADMIN.getName();
	}
}
