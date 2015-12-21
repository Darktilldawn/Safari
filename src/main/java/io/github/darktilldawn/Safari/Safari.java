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
package io.github.darktilldawn.Safari;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.world.TeleportHelper;

import io.github.darktilldawn.Safari.Commands.SafariExecutor;
import io.github.darktilldawn.Safari.Commands.SafariReloadExecutor;
import io.github.darktilldawn.Safari.Commands.SafariSetExecutor;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = "Safari", name = "Safari", version = ".01")
public class Safari {
	// CTRL + SHIFT + O == Auto Import
	// CTRL + SHIFT + F == Auto Format
	// CTRL + SHIFT + I == Auto Indent

	private static Safari instance;
	public static TeleportHelper helper;

	@Inject
	private Logger logger;

	@Inject
	private Game game;

	@Inject
	@DefaultConfig(sharedRoot = true)
	private Path defaultConfig;

	@Inject
	@DefaultConfig(sharedRoot = true)
	private ConfigurationLoader<CommentedConfigurationNode> configManager;

	public static Safari getInstance() {
		return instance;
	}

	public Logger getLogger() {
		return logger;
	}

	public Game getGame() {
		return game;
	}

	@Listener
	public void preInit(GamePreInitializationEvent event) {
		instance = this;
	}

	@Listener
	public void init(GameInitializationEvent event) {
		registerCommands();
	}

	@Listener
	public void onGameStarted(GameStartedServerEvent event) {
		logger.info("Safari has been loaded!");
	}

	private void registerCommands() {
		HashMap<List<String>, CommandSpec> subcommands = new HashMap<List<String>, CommandSpec>();

		// /safari reload
		subcommands.put(Arrays.asList("reload"), CommandSpec.builder().description(Texts.of("Reload Config"))
				.permission("safari.command.safari.reload").executor(new SafariReloadExecutor()).build());

		// /safari set
		subcommands.put(Arrays.asList("set"), CommandSpec.builder().description(Texts.of("Set SafariZone"))
				.permission("safari.command.safari.set").executor(new SafariSetExecutor()).build());

		// /safari
		CommandSpec safariCommandSpec = CommandSpec.builder().description(Texts.of("Usage: /safari [set|reload]"))
				.permission("safari.command.safari")
				.arguments(GenericArguments
						.optional(GenericArguments.onlyOne(GenericArguments.player(Texts.of("player")))))
				.executor(new SafariExecutor()).children(subcommands).build();

		game.getCommandManager().register(this, safariCommandSpec, "safari");
	}
}
