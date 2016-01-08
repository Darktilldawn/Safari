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
package io.github.darktilldawn.safari.scheduler;

import com.flowpowered.math.vector.Vector3d;
import io.github.darktilldawn.safari.scheduler.util.SpongeRunnable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.UUID;

public class SafariTeleportTask extends SpongeRunnable {
    private Entity target;
    private Location<World> location;
    private Vector3d rotation;
    private Map<UUID, SafariTeleportTask> safariMap;

    public SafariTeleportTask(Entity target, Location<World> location, Vector3d rotation, Map<UUID, SafariTeleportTask> safariMap) {
        this.target = target;
        this.location = location;
        this.rotation = rotation;
        this.safariMap = safariMap;
    }


    @Override
    public void run() {
        if(this.safariMap.containsKey(this.target.getUniqueId())) {
            this.target.setLocationAndRotation(this.location, this.rotation);
            this.safariMap.remove(this.target.getUniqueId());
            if (this.target instanceof Player) {
                ((Player) this.target).sendMessage(Text.of(TextColors.AQUA, "You have been teleported back to ", this.location.getBlockX(), ", ", this.location.getBlockY(), ", ", this.location.getBlockZ()));
            }
        }
    }

}
