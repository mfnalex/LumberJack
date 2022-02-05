package de.jeff_media.lumberjack.hooks;

import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.function.Predicate;

public class FarmLimiterListener implements Listener {

    public static class FarmLimiterListenerEventExecutor implements EventExecutor {

        Class eventClass;

        {
            try {
                eventClass = Class.forName("me.filoghost.farmlimiter.api.FarmLimitEvent");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void execute(@NotNull Listener listener, @NotNull Event event) throws EventException {
            System.out.println("Event");
            if(eventClass== null) return;
            if(!eventClass.isInstance(event)) return;
            System.out.println("Farmlimiter event");
            try {
                Collection<Entity> toRemove = (Collection<Entity>) event.getClass().getDeclaredMethod("getEntitiesToRemove").invoke(event);
                toRemove.removeIf(new Predicate<Entity>() {
                    @Override
                    public boolean test(Entity entity) {
                        if(entity instanceof FallingBlock) {
                            System.out.println("Removed entity from FarmLimiter event: " + entity);
                            return true;
                        }
                        return false;
                    }
                });
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {

            }
        }
    }
}
