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
package org.kitteh.vanish;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VanishCommand {

  private static final Logger log = LoggerFactory.getLogger(VanishCommand.class);
  private final VanishPlugin plugin;
  private final Component reloadMessage = MiniMessage.miniMessage()
      .deserialize("<dark_aqua>[Vanish] Users reloaded and some settings refreshed</dark_aqua>");
  private final Component invalidToggleMessage = Component.text("You cant toggle that!",
      NamedTextColor.DARK_RED);
  private final String sucessfulToggleMessage = "Successfully toggled: <toggle>";
  private final String alreadyStateMessage = "<red>You are already <status>visible</red>";

  public VanishCommand(@NonNull VanishPlugin plugin) {
    this.plugin = plugin;
    registerCommands();
  }

  private void registerCommands() {
    plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commandManager -> {
      final Commands commands = commandManager.registrar();

      final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("vanish")
          .requires(predicate -> {
            final Entity executor = predicate.getExecutor();
            if (executor == null) {
              return true;
            }
            if (!(executor instanceof Player)) {
              return false;
            }
            return VanishPerms.canVanish((Player) executor);
          }).executes(ctx -> {
            this.plugin.getManager()
                .toggleVanish((Player) Objects.requireNonNull(ctx.getSource().getExecutor()));
            return SINGLE_SUCCESS;
          }).then(Commands.literal("reload").requires(predicate -> {
            final Entity executor = predicate.getExecutor();
            if (executor == null) {
              return true;
            }
            return VanishPerms.canReload(executor);
          }).executes(ctx -> {
            this.plugin.reload();
            final Entity executor = ctx.getSource().getExecutor();
            if (executor == null) {
              log.info(ANSIComponentSerializer.ansi().serialize(this.reloadMessage));
            } else {
              executor.sendMessage(this.reloadMessage);
            }
            return SINGLE_SUCCESS;
          })).then(Commands.literal("list").requires(predicate -> {
            final Entity executor = predicate.getExecutor();
            if (executor == null) {
              return true;
            }
            return VanishPerms.canList(executor);
          }).executes(ctx -> {
            Component list = Component.empty();
            for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
              if (list.hasStyling()) {
                list = list.append(Component.text(", ", NamedTextColor.DARK_AQUA))
                    .append(Component.text(player.getName(), NamedTextColor.AQUA));
              } else {
                list = Component.text(player.getName(), NamedTextColor.AQUA);
              }
            }

            Entity executor = ctx.getSource().getExecutor();
            final Component message = Component.text("Vanished: ", NamedTextColor.DARK_AQUA)
                .append(list);
            if (executor == null) {
              log.info(ANSIComponentSerializer.ansi().serialize(message));
            } else {
              executor.sendMessage(message);
            }
            return SINGLE_SUCCESS;
          })).then(Commands.literal("toggle").requires(predicate -> {
            final Entity executor = predicate.getExecutor();
            return executor != null;
          }).then(Commands.argument("Toggle", new VanishToggleArgument()).executes(ctx -> {
            VanishPerms.TogglePermissions toggle = ctx.getArgument("Toggle",
                VanishPerms.TogglePermissions.class);

            if (toggle == null || !toggle.getCanToggle()
                .apply((Player) Objects.requireNonNull(ctx.getSource().getExecutor()))) {
              Objects.requireNonNull(ctx.getSource().getExecutor())
                  .sendMessage(invalidToggleMessage);
              return 0;
            }
            final Boolean status = toggle.getToggle()
                .apply((Player) Objects.requireNonNull(ctx.getSource().getExecutor()));
            ctx.getSource().getExecutor().sendMessage(MiniMessage.miniMessage()
                .deserialize(sucessfulToggleMessage, Placeholder.component("toggle",
                    Component.text(toggle.toString(),
                        (status) ? NamedTextColor.GREEN : NamedTextColor.DARK_RED).hoverEvent(
                        HoverEvent.showText(Component.text((status) ? "ON" : "OFF",
                            (status) ? NamedTextColor.GREEN : NamedTextColor.DARK_RED))))));
            return SINGLE_SUCCESS;
          }))).then(Commands.literal("on").requires(predicate -> {
            final Entity executor = predicate.getExecutor();
            if (!(executor instanceof Player)) {
              return false;
            }
            return VanishPerms.canVanish((Player) executor);
          }).executes(ctx -> {
            Player player = (Player) ctx.getSource().getExecutor();

            assert player != null;
            if (this.plugin.getManager().isVanished(player)) {
              player.sendMessage(MiniMessage.miniMessage()
                  .deserialize(alreadyStateMessage, Placeholder.unparsed("status", "in")));
              return 1;
            }

            this.plugin.getManager().toggleVanish(player);
            return SINGLE_SUCCESS;
          })).then(Commands.literal("off").requires(predicate -> {
            final Entity executor = predicate.getExecutor();
            if (!(executor instanceof Player)) {
              return false;
            }
            return VanishPerms.canVanish((Player) executor);
          }).executes(ctx -> {

            Player player = (Player) ctx.getSource().getExecutor();

            assert player != null;
            if (!this.plugin.getManager().isVanished(player)) {
              player.sendMessage(MiniMessage.miniMessage()
                  .deserialize(alreadyStateMessage, Placeholder.unparsed("status", "")));
              return 1;
            }

            this.plugin.getManager().toggleVanish(player);
            return SINGLE_SUCCESS;
          })).then(Commands.literal("fakequit").requires(predicate -> {
                final Entity executor = predicate.getExecutor();
                if (!(executor instanceof Player)) {
                  return false;
                }
                return VanishPerms.canFakeAnnounce((Player) executor);
              }).executes(ctx -> fakeQuit(ctx, false))
              .then(Commands.literal("force").executes(ctx -> fakeQuit(ctx, true))))
          .then(Commands.literal("fakejoin").requires(predicate -> {
                final Entity executor = predicate.getExecutor();
                if (!(executor instanceof Player)) {
                  return false;
                }
                return VanishPerms.canFakeAnnounce((Player) executor);
              }).executes(ctx -> fakeJoin(ctx, false))
              .then(Commands.literal("force").executes(ctx -> fakeJoin(ctx, true))));

      commands.register(command.build(), "Vanish", List.of(new String[]{"v", "vnp"}));
    });
  }

  private int fakeQuit(CommandContext<CommandSourceStack> ctx, boolean force) {
    Player player = (Player) ctx.getSource().getExecutor();
    assert player != null;
    if (!this.plugin.getManager().isVanished(player)) {
      this.plugin.getManager().toggleVanish(player);
    } else {
      player.sendMessage(MiniMessage.miniMessage()
          .deserialize(alreadyStateMessage, Placeholder.unparsed("status", "in")));
    }
    this.plugin.getManager().getAnnounceManipulator().fakeQuit(player, force);
    return SINGLE_SUCCESS;
  }

  private int fakeJoin(CommandContext<CommandSourceStack> ctx, boolean force) {
    Player player = (Player) ctx.getSource().getExecutor();
    assert player != null;
    if (this.plugin.getManager().isVanished(player)) {
      this.plugin.getManager().toggleVanish(player);
    } else {
      player.sendMessage(MiniMessage.miniMessage()
          .deserialize(alreadyStateMessage, Placeholder.unparsed("status", "")));
    }
    this.plugin.getManager().getAnnounceManipulator().fakeJoin(player, force);
    return SINGLE_SUCCESS;
  }
}
