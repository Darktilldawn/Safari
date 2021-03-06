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
package io.github.darktilldawn.safari;

import com.flowpowered.math.vector.Vector3d;
import io.github.darktilldawn.safari.scheduler.SafariTeleportTask;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SafariWarp {
    private static Map<UUID, SafariTeleportTask> occupied;

    static {
        occupied = new HashMap<>();
    }

    private Safari safari;
    private String name;
    private String currency;
    private Location<World> location;
    private double amount;
    private long duration;

    public SafariWarp(String name, String currency, Location<World> location, double amount, long duration) {
        this.currency = currency;
        this.location = new Location<>(location.getExtent(), location.getBlockPosition());
        this.amount = amount;
        this.safari = Safari.getInstance();
        this.name = name;
        this.duration = duration;
    }

    public static Optional<SafariWarp> fromConfig(ConfigurationNode node, String name) {
        ConfigurationNode sub = node.getNode(name);
        String currency = sub.getNode("currency").getString();
        double amount = sub.getNode("price").getDouble();
        long duration = sub.getNode("duration").getLong();
        String worldName = sub.getNode("world").getString();
        String locationString = sub.getNode("location").getString();
        if (currency == null || worldName == null || locationString == null) {
            Safari.getInstance().getLogger().error("Safari Warp '".concat(String.valueOf(name)).concat("' is missing a value! (currency=").concat(String.valueOf(currency)).concat(", worldName=").concat(String.valueOf(worldName)).concat(", locationString=").concat(String.valueOf(locationString)).concat(")"));
            return Optional.empty();
        }

        Optional<World> worldOptional = Sponge.getServer().getWorld(worldName);
        if (!worldOptional.isPresent()) {
            Safari.getInstance().getLogger().error("World '".concat(worldName).concat("' not present for warp '").concat(name).concat("'"));
            return Optional.empty();
        }

        World world = worldOptional.get();
        Optional<Vector3d> vector3dOptional = SafariWarp.vectorFromString(locationString);
        if (!vector3dOptional.isPresent()) {
            Safari.getInstance().getLogger().error("Unrecognized location string '".concat(locationString).concat("'"));
            return Optional.empty();
        }

        Location<World> location = new Location<>(world, vector3dOptional.get());

        return Optional.of(new SafariWarp(name, currency, location, amount, duration));
    }

    private static Optional<Vector3d> vectorFromString(String src) {
        try {
            String[] pieces = src.replaceAll(" ", "").split(",");
            double x = Double.parseDouble(pieces[0]);
            double y = Double.parseDouble(pieces[1]);
            double z = Double.parseDouble(pieces[2]);
            return Optional.of(new Vector3d(x, y, z));
        } catch (IndexOutOfBoundsException | NumberFormatException error) {
            return Optional.empty();
        }
    }

    private static String vectorToString(Vector3d vector) {
        return new StringBuilder().append(vector.getX()).append(",").append(vector.getY()).append(",").append(vector.getZ()).toString();
    }

    public static void endAllSafaris() {
        SafariWarp.occupied.values().forEach(SafariTeleportTask::run);
    }

    public void applyTo(Player player) {
        if (SafariWarp.occupied.keySet().contains(player.getUniqueId())) {
            player.sendMessage(Text.of(TextColors.RED, "You are already in a Safari!"));
            return;
        }

        if (this.safari.isFreeMode()) {
            this.executeTeleport(player);
            return;
        }

        Optional<UniqueAccount> accountOptional = this.safari.getService().getAccount(player.getUniqueId());
        Optional<Currency> currencyOptional = this.safari.getService().getCurrencies().stream().filter(currency -> currency.getDisplayName().toPlain().equalsIgnoreCase(this.currency)).findFirst();
        if (!currencyOptional.isPresent()) {
            this.safari.getLogger().error("No currency called '".concat(String.valueOf(this.currency).concat("'")));
            return;
        }

        if (!accountOptional.isPresent()) {
            player.sendMessage(Text.of(TextColors.RED, "You do not have an economy account! ", TextColors.AQUA, "We'll create one for you, but you won't have any money..."));
            this.safari.getService().createAccount(player.getUniqueId());
        } else {
            UniqueAccount account = accountOptional.get();
            TransactionResult result = account.withdraw(currencyOptional.get(), BigDecimal.valueOf(this.amount), Cause.of(this));
            ResultType resultType = result.getResult();
            if (resultType == ResultType.ACCOUNT_NO_FUNDS) {
                player.sendMessage(Text.of(TextColors.RED, "You do not have enough money, you need at least ", this.amount, " ", currencyOptional.get().getPluralDisplayName(), " to use that warp."));
            } else if (resultType != ResultType.SUCCESS) {
                player.sendMessage(Text.of(TextColors.RED, "Failed warp for unknown reasons."));
            } else {
                player.sendMessage(Text.of(TextColors.YELLOW, this.amount, this.amount <= 1 ? currencyOptional.get().getDisplayName() : currencyOptional.get().getPluralDisplayName(), this.amount <= 1 ? "has" : "have", " been subtracted from your accounted!"));
                this.executeTeleport(player);
            }
        }
    }

    private void executeTeleport(Player player) {
        if (this.duration >= 0) {
            SafariTeleportTask task = new SafariTeleportTask(player, player.getLocation(), player.getRotation(), SafariWarp.occupied);
            SafariWarp.occupied.put(player.getUniqueId(), task);
            player.setLocation(this.location);
            task.runTaskLater(Safari.getInstance(), this.duration, TimeUnit.SECONDS);
            String time = new StringBuilder().append(TimeUnit.SECONDS.toMinutes(this.duration)).append(" minutes and ").append(this.duration - (((int) ((double) this.duration / 60))) * 60).append(" seconds").toString();
            player.sendMessage(Text.of(TextColors.AQUA, "You have been teleported to '", this.name, ".' You can remain here for ", time, ", then you will be teleported back."));
        } else {
            player.setLocation(this.location);
            player.sendMessage(Text.of(TextColors.AQUA, "You have been teleported to '", this.name));
        }
    }

    public void writeToConfig(ConfigurationNode node) {
        ConfigurationNode sub = node.getNode(this.name);
        sub.getNode("currency").setValue(this.currency);
        sub.getNode("location").setValue(SafariWarp.vectorToString(this.location.getPosition()));
        sub.getNode("world").setValue(this.location.getExtent().getName());
        sub.getNode("price").setValue(this.amount);
        sub.getNode("duration").setValue(this.duration);
    }

    public double getAmount() {
        return this.amount;
    }

    public Location getLocation() {
        return this.location;
    }

    public String getCurrency() {
        return this.currency;
    }

    public String getName() {
        return this.name;
    }

    public Safari getSafari() {
        return this.safari;
    }

    public long getduration() {
        return this.duration;
    }
}
