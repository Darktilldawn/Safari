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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;
import io.github.darktilldawn.safari.commands.SafariListExecutor;
import io.github.darktilldawn.safari.commands.SafariRemoveExecutor;
import io.github.darktilldawn.safari.commands.SafariSaveCommand;
import io.github.darktilldawn.safari.commands.SafariWarpExecutor;
import io.github.darktilldawn.safari.commands.SafariReloadExecutor;
import io.github.darktilldawn.safari.commands.SafariSetExecutor;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.TeleportHelper;


@Plugin(id = "Safari", name = "Safari", version = "0.1")
public class Safari {

    public static TeleportHelper helper;
    private static Safari instance;
    @Inject
    private Logger logger;

    @Inject
    private Game game;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;
    private ConfigurationNode config;
    private EconomyService service;
    private boolean freeMode;
    private List<SafariWarp> warps;

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
        this.warps = new ArrayList<>();
        this.reloadConfig();
    }

    @Listener
    public void init(GameInitializationEvent event) {
        this.registerCommands();
    }

    @Listener
    public void onGameStarted(GameStartedServerEvent event) {
        this.getLogger().info("Searching for economy plugin... ");
        Optional<EconomyService> economyServiceOptional = this.game.getServiceManager().provide(EconomyService.class);
        if (!economyServiceOptional.isPresent()) {
            this.logger.warn("No economy plugin is installed! Starting up in free mode.");
            this.logger.warn("In free mode, all warps are free! (Assuming you don't want that, install an economy plugin!)");
            this.freeMode = true;
        } else {
            this.service = economyServiceOptional.get();
            this.freeMode = false;
            this.getLogger().info("Found economy plugin. Using provider: ".concat(economyServiceOptional.get().getClass().getName()));
        }

        this.getLogger().info("Loading warps...");
        this.reloadWarps();
        this.getLogger().info("Warps loaded!");

        this.getLogger().info("Safari has finished loading!");
    }

    @Listener
    public void onServerStopping(GameStoppingServerEvent event) {
        SafariWarp.endAllSafaris();
    }

    @Listener
    public void onServerStopped(GameStoppedServerEvent event) {
        this.saveConfig();
    }

    public void saveConfig() {
        this.warps.forEach(safariWarp -> {
            safariWarp.writeToConfig(this.getConfig());
        });
        try {
            ConfigurationLoader loader = HoconConfigurationLoader.builder().setFile(this.defaultConfig.toFile()).build();
            loader.save(this.getConfig());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerCommands() {
        HashMap<List<String>, CommandSpec> subcommands = new HashMap<List<String>, CommandSpec>();

        // /safari reload
        subcommands.put(Arrays.asList("reload"), CommandSpec.builder().description(Text.of("Reload Config"))
                .permission("safari.command.safari.reload")
                .executor(new SafariReloadExecutor()).build());

        // /safari set
        subcommands.put(Arrays.asList("set", "edit"), CommandSpec.builder().description(Text.of("Edit or Create a SafariWarp"))
                .permission("safari.command.safari.set")
                .arguments(
                        GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.location(Text.of("location")))),
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("name"))),
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("currency"))),
                        GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("cost"))),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("duration"))))
                .executor(new SafariSetExecutor()).build());

        // /safari warp
        subcommands.put(Arrays.asList("tp", "teleport", "warp", "goto", "take"), CommandSpec.builder().description(Text.of("Takes you on a Safari."))
                .permission("safari.command.safari")
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("warp"))))
                .executor(new SafariWarpExecutor())
                .build());

        // /safari list
        subcommands.put(Arrays.asList("list"), CommandSpec.builder().description(Text.of("Lists all safaris"))
                .executor(new SafariListExecutor()).build());

        // /safari remove
        subcommands.put(Arrays.asList("remove"), CommandSpec.builder().description(Text.of("Removes a safari"))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("warp"))))
                .executor(new SafariRemoveExecutor()).build());

        // /safari save
        subcommands.put(Arrays.asList("save"), CommandSpec.builder().description(Text.of("Saves a safari"))
                .executor(new SafariSaveCommand()).build());

        // /safari
        CommandSpec safariCommandSpec = CommandSpec.builder().description(Text.of("Usage: /safari [set | edit | reload]"))
                .permission("safari.command.safari")
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("warp"))))
                .executor(new SafariWarpExecutor())
                .children(subcommands).build();

        this.game.getCommandManager().register(this, safariCommandSpec, "safari");
    }

    public void reloadWarps() {
        this.warps.clear();
        this.config.getChildrenMap().values().stream().forEach(node -> {
            String name = String.valueOf(node.getKey());
            Optional<SafariWarp> warp = SafariWarp.fromConfig(this.config, name);
            if (warp.isPresent()) {
                this.warps.add(warp.get());
                this.getLogger().info("Loaded warp '".concat(warp.get().getName()).concat("'"));
            } else {
                this.getLogger().error("Failed to load warp '".concat(name).concat("'"));
            }
        });
    }

    public List<SafariWarp> getWarps() {
        return ImmutableList.copyOf(this.warps);
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

    public void replaceWarp(int index, SafariWarp warp) {
        this.warps.set(index, warp);
    }

    public void removeWarp(SafariWarp warp) {
        this.warps.remove(warp);
    }

    public void addWarp(SafariWarp warp) {
        this.warps.add(warp);
    }

    public void reloadConfig() {
        try {
            this.config = this.configManager.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
