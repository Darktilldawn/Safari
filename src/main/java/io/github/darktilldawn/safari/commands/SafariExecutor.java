/*
 * This file is part of Safari, licensed under the MIT License (MIT).
 *
 * Copyright (c) Darktilldawn <http://github.com/darktilldawn>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.darktilldawn.safari.commands;

import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class SafariExecutor implements CommandExecutor {

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		Optional<Player> optionalTarget = ctx.getOne("player");

		if (optionalTarget.isPresent()) {
			Player player = optionalTarget.get();
			// player.setLocation(location);
			src.sendMessage(Text.of(TextColors.GREEN, "Success: ", TextColors.YELLOW,
					"You have teleported " + player.getName() + " to the SafariZone"));
		} else {
			if (src instanceof Player) {
				Player player = (Player) src;
				// player.setLocation(location);
				src.sendMessage(Text.of(TextColors.GREEN, "Success: ", TextColors.YELLOW,
						"You have been Teleported to the SafariZone"));
			} else {
				src.sendMessage(Text.of(TextColors.DARK_RED, "Error: ", TextColors.RED,
						"You cannot be teleported, you are not a player!"));
			}
		}

		return CommandResult.success();
	}
}
