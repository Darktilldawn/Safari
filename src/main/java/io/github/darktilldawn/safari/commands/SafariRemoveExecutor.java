/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 socraticphoenix@gmail.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Socratic_Phoenix (socraticphoenix@gmail.com)
 */
package io.github.darktilldawn.safari.commands;

import io.github.darktilldawn.safari.Safari;
import io.github.darktilldawn.safari.SafariWarp;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public class SafariRemoveExecutor implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String warp = (String) args.getOne("warp").get();
        List<SafariWarp> warps = Safari.getInstance().getWarps();
        if(warps.stream().filter(safariWarp -> safariWarp.getName().equals(warp)).findFirst().isPresent()) {
            Safari.getInstance().removeWarp(warps.stream().filter(safariWarp -> safariWarp.getName().equals(warp)).findFirst().get());
            src.sendMessage(Text.of(TextColors.RED, "Successfully removed warp '", warp, "'"));
        } else {
            src.sendMessage(Text.of(TextColors.RED, "Warp '", warp, "' does not exist."));
        }
        return CommandResult.success();
    }

}
