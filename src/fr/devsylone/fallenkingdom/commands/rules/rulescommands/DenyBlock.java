package fr.devsylone.fallenkingdom.commands.rules.rulescommands;

import fr.devsylone.fallenkingdom.Fk;
import fr.devsylone.fallenkingdom.commands.ArgumentParser;
import fr.devsylone.fallenkingdom.commands.abstraction.Argument;
import fr.devsylone.fallenkingdom.commands.abstraction.CommandPermission;
import fr.devsylone.fallenkingdom.commands.abstraction.CommandResult;
import fr.devsylone.fallenkingdom.commands.abstraction.FkCommand;
import fr.devsylone.fallenkingdom.utils.Messages;
import fr.devsylone.fkpi.FkPI;
import fr.devsylone.fkpi.rules.Rule;
import fr.devsylone.fkpi.util.BlockDescription;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.devsylone.fallenkingdom.exception.FkLightException;
import fr.devsylone.fkpi.rules.AllowedBlocks;

import java.util.List;

public class DenyBlock extends FkCommand
{
	public DenyBlock()
	{
		super("denyBlock", Argument.list(Argument.create("block", false, "sinon prendra le bloc tenu en main")),
				Messages.CMD_MAP_RULES_DENY_BLOCK, CommandPermission.ADMIN);
	}

	@Override
	public CommandResult execute(Fk plugin, CommandSender sender, List<String> args, String label) {
		final BlockDescription blockDescription;
		if (!(sender instanceof Player)) {
			if (args.size() <= 0) {
				return CommandResult.NOT_VALID_EXECUTOR;
			}
			blockDescription = ArgumentParser.parseBlock(args.get(0));
		} else {
			blockDescription = ArgumentParser.parseBlock(0, args, (Player) sender,true);
		}
		AllowedBlocks rule = FkPI.getInstance().getRulesManager().getRule(Rule.ALLOWED_BLOCKS);

		if (!rule.isAllowed(blockDescription))
			throw new FkLightException(Messages.CMD_RULES_ERROR_ALREADY_DENIED);

		// Si le nom de bloc donné est moins précis que ceux enregistrés (ex : WOOL avec WOOL:2, WOOL:3)
		// on supprime toutes les occurrences de WOOL (donc des fois plusieurs)
		rule.removeIf(b -> b.equals(blockDescription));

		broadcast(Messages.CMD_RULES_DENY_BLOCK.getMessage().replace("%block%", blockDescription.toString()));

		return CommandResult.SUCCESS;
	}
}
