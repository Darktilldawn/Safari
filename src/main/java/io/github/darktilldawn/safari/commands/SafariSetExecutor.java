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

import io.github.darktilldawn.safari.Safari;
import io.github.darktilldawn.safari.SafariWarp;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.LocatedSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class SafariSetExecutor implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Location location;

        if(args.getOne("location").isPresent()) {
            location = (Location) args.getOne("location").get();
        } else if (src instanceof LocatedSource) {
            location = ((LocatedSource) src).getLocation();
        } else {
            src.sendMessage(Text.of(TextColors.RED, "No location was specified, and since you are not located anywhere, no default location could be used."));
            return CommandResult.success();
        }

        String name = (String) args.getOne("name").get();
        String currency = (String) args.getOne("currency").get();
        double cost = (Double) args.getOne("cost").get();
        long duration = ((Integer) (args.getOne("duration").get())).longValue();

        if(duration < 0) {
            src.sendMessage(Text.of(TextColors.RED, "Warning! The duration you have entered is less than 0, this will cause the warp teleport to be permanent!"));
        }

        if(cost < 0) {
            src.sendMessage(Text.of(TextColors.RED, "Cost cannot be less than 0..."));
        } else {
            if(Safari.getInstance().isFreeMode() || Safari.getInstance().getService().getCurrencies().stream().filter(curr -> curr.getDisplayName().toPlain().equalsIgnoreCase(currency)).findFirst().isPresent()) {
                if(!(location.getExtent() instanceof World)) {
                    src.sendMessage(Text.of(TextColors.RED, "That location is not linked to a world. It may be a chunk location."));
                } else {
                    SafariWarp warp = new SafariWarp(name, currency, location, cost, duration);
                    Safari safari = Safari.getInstance();
                    if(safari.getWarps().stream().filter(warp1 -> warp.getName().equals(warp1.getName())).findFirst().isPresent()) {
                        safari.removeWarp(safari.getWarps().stream().filter(warp1 -> warp.getName().equals(warp1.getName())).findFirst().get());
                        safari.addWarp(warp);
                        src.sendMessage(Text.of(TextColors.AQUA, "Edited warp '", warp.getName(), "'"));
                    } else {
                        safari.addWarp(warp);
                        src.sendMessage(Text.of(TextColors.AQUA, "Created new warp '", warp.getName(), "'"));
                    }
                }
            } else {
                src.sendMessage(Text.of(TextColors.RED, "Unrecognized currency '", currency, "'"));
            }
        }
		return CommandResult.success();
	}

}