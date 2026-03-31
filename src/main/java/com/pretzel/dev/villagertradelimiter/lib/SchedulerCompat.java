package com.pretzel.dev.villagertradelimiter.lib;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class SchedulerCompat {
    private SchedulerCompat() {
    }

    public static void runAsync(final Plugin plugin, final Runnable task) {
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
    }

    public static void runGlobal(final Plugin plugin, final Runnable task) {
        Bukkit.getGlobalRegionScheduler().execute(plugin, task);
    }

    public static boolean runNextTick(final Plugin plugin, final Entity entity, final Runnable task) {
        return entity.getScheduler().run(plugin, scheduledTask -> task.run(), null) != null;
    }

    public static boolean runDelayed(final Plugin plugin, final Entity entity, final long delayTicks, final Runnable task) {
        return entity.getScheduler().execute(plugin, task, null, Math.max(1L, delayTicks));
    }
}
