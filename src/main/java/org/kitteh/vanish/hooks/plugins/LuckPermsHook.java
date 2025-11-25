package org.kitteh.vanish.hooks.plugins;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.vanish.VanishPlugin;
import org.kitteh.vanish.hooks.Hook;

public class LuckPermsHook extends Hook {

  boolean enabled = false;

  private static class VanishContextCalculator implements ContextCalculator<Player> {

    private final VanishPlugin plugin;

    public VanishContextCalculator(VanishPlugin plugin) {
      this.plugin = plugin;
    }

    @Override
    public void calculate(@NonNull Player target, @NonNull ContextConsumer consumer) {
      consumer.accept("vanished", this.plugin.getManager().isVanished(target) ? "true" : "false");
    }

    @Override
    public @NonNull ContextSet estimatePotentialContexts() {
      return ImmutableContextSet.builder().add("vanished", "true").add("vanished", "false").build();
    }
  }

  public LuckPermsHook(@NonNull VanishPlugin plugin) {
    super(plugin);
  }

  @Override
  public void onEnable() {
    RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager()
        .getRegistration(LuckPerms.class);

    if (provider == null) {
      this.plugin.getLogger().info("You wanted Luckperms support. I could not find LuckPerms.");
      this.enabled = false;
      return;
    }

    LuckPerms luckperms = provider.getProvider();
    this.plugin.getLogger().info("Now hooking into Luckperms");
    this.enabled = true;
    luckperms.getContextManager().registerCalculator(new VanishContextCalculator(this.plugin));
  }
}
