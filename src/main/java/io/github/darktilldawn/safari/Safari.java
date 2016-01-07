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
package io.github.darktilldawn.safari;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.TeleportHelper;

import io.github.darktilldawn.safari.commands.SafariExecutor;
import io.github.darktilldawn.safari.commands.SafariReloadExecutor;
import io.github.darktilldawn.safari.commands.SafariSetExecutor;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = "Safari", name = "Safari", version = "0.1")
public class Safari {

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
    private ConfigurationNode config;
    private EconomyService service;
    private boolean freeMode;

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
		Safari.instance = this;
        Safari.helper = this.game.getTeleportHelper();
        try {
            this.config = configManager.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Optional<EconomyService> economyServiceOptional = this.game.getServiceManager().provide(EconomyService.class);
        if(!economyServiceOptional.isPresent()) {
            this.logger.warn("No economy plugin is installed! Starting up in free mode.");
            this.logger.warn("In free mode, all warps are free! (Assuming you don't want that, install an economy plugin!)");
            this.freeMode = true;
        } else {
            this.service = economyServiceOptional.get();
            this.freeMode = false;
        }
    }

	@Listener
	public void init(GameInitializationEvent event) {
		this.registerCommands();
	}

	@Listener
	public void onGameStarted(GameStartedServerEvent event) {
		logger.info("Safari has been loaded!");
	}

	private void registerCommands() {
		HashMap<List<String>, CommandSpec> subcommands = new HashMap<List<String>, CommandSpec>();

		// /safari reload
		subcommands.put(Arrays.asList("reload"), CommandSpec.builder().description(Text.of("Reload Config"))
				.permission("safari.command.safari.reload").executor(new SafariReloadExecutor()).build());

		// /safari set
		subcommands.put(Arrays.asList("set", "edit"), CommandSpec.builder().description(Text.of("Edit or Create a SafariWarp"))
				.permission("safari.command.safari.set").executor(new SafariSetExecutor()).build());

		// /safari
		CommandSpec safariCommandSpec = CommandSpec.builder().description(Text.of("Usage: /safari [set|edit|reload]"))
				.permission("safari.command.safari")
				.arguments(GenericArguments
						.optional(GenericArguments.onlyOne(GenericArguments.player(Text.of("player")))))
				.executor(new SafariExecutor()).children(subcommands).build();

		this.game.getCommandManager().register(this, safariCommandSpec, "safari");
	}

    public ConfigurationLoader<CommentedConfigurationNode> getConfigManager() {
        return this.configManager;
    }

    public ConfigurationNode getConfig() {
        return this.config;
    }

    public EconomyService getService() {
        return this.service;
    }

    public TeleportHelper getHelper() {
        return Safari.helper;
    }

    public boolean isFreeMode() {
        return this.freeMode;
    }
}
