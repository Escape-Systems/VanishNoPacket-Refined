/*
 * VanishNoPacket
 * Copyright (C) 2011-2022 Matt Baxter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.kitteh.vanish.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.LazyMetadataValue.CacheStrategy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.vanish.VanishCheck;
import org.kitteh.vanish.VanishPerms;
import org.kitteh.vanish.VanishPlugin;

public final class ListenPlayerJoin implements Listener {

  private final VanishPlugin plugin;

  public ListenPlayerJoin(@NonNull VanishPlugin instance) {
    this.plugin = instance;
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerJoinEarly(@NonNull PlayerJoinEvent event) {
    event.getPlayer().setMetadata("vanished",
        new LazyMetadataValue(this.plugin, CacheStrategy.NEVER_CACHE,
            new VanishCheck(this.plugin.getManager(), event.getPlayer().getName())));
    this.plugin.getManager().resetSeeing(event.getPlayer());
    if (VanishPerms.joinVanished(event.getPlayer())) {
      this.plugin.getManager().toggleVanishQuiet(event.getPlayer(), false);
      this.plugin.hooksVanish(event.getPlayer());
    }
    this.plugin.hooksJoin(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerJoinLate(@NonNull PlayerJoinEvent event) {
    final StringBuilder statusUpdate = new StringBuilder();
    if (VanishPerms.joinVanished(event.getPlayer())) {
      Component message = Component.text("You have joined vanished.", NamedTextColor.DARK_AQUA);
      if (VanishPerms.canVanish(event.getPlayer())) {
        message = message.append(Component.text(" To appear: /vanish",  NamedTextColor.DARK_AQUA));
      }
      event.getPlayer().sendMessage(message);
      statusUpdate.append("vanished");
    }
    if (VanishPerms.joinWithoutAnnounce(event.getPlayer())) {
      this.plugin.getManager().getAnnounceManipulator()
          .addToDelayedAnnounce(event.getPlayer().getName());
      event.joinMessage(null);

      if (!statusUpdate.isEmpty()) {
        statusUpdate.append(" and ");
      }
      statusUpdate.append("silently");
    }
    if (!statusUpdate.isEmpty()) {
      this.plugin.messageStatusUpdate(
          ChatColor.DARK_AQUA + event.getPlayer().getName() + " has joined " + statusUpdate);
    }
  }
}
