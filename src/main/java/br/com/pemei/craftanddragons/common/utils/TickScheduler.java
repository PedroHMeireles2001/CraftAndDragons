package br.com.pemei.craftanddragons.common.utils;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static br.com.pemei.craftanddragons.Craftanddragons.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TickScheduler {

    private static final List<Task> RUNNING_TASKS = new ArrayList<>();
    private static final List<Task> NEW_TASKS = new ArrayList<>();

    public static synchronized void remove(int taskId) {
        // Uma tarefa pode cancelar a si mesma ou outra tarefa durante a iteração.
        for (Task task : RUNNING_TASKS) {
            if (task.id == taskId) {
                task.cancelled = true;
                break;
            }
        }
        NEW_TASKS.removeIf(task -> task.id == taskId);
    }


    private static class Task {
        int id;
        static int next_id = 1;
        int ticksRemaining;
        int duration;
        int executionsLeft;
        boolean loop;
        boolean cancelled;
        final Runnable action;

        Task(int ticks, Runnable action, boolean loop) {
            this.duration = ticks;
            this.ticksRemaining = ticks;
            this.action = action;
            this.loop = loop;
            this.executionsLeft = loop ? -1 : 1; // -1 representa loop infinito
            this.id = next_id++;
        }

        Task(int ticks,int executions, Runnable action) {
            this.duration = ticks;
            this.ticksRemaining = ticks;
            this.action = action;
            this.loop = false;
            this.executionsLeft = executions;
            this.id = next_id++;
        }

        void resetTimer() {
            this.ticksRemaining = this.duration;
            if (!loop && this.executionsLeft > 0) {
                this.executionsLeft--;
            }
        }
        boolean shouldRemove() {
            return cancelled || (!loop && this.executionsLeft <= 0);
        }
    }

    /**
     * Agenda uma ação para ser executada na Thread Principal do servidor após N ticks.
     * @param ticks Qtd de ticks de espera (Ex: 20 ticks = 1s, 10000 ticks = ~8.3 minutos).
     * @param action Código a ser executado.
     */
    public static synchronized int schedule(int ticks, Runnable action) {
        return schedule(ticks,action,false);
    }
    public static synchronized int schedule(int ticks,int n, Runnable action) {
        Task task = new Task(ticks, n, action);
        NEW_TASKS.add(task);
        return task.id;
    }
    public static synchronized int schedule(int ticks, Runnable action,boolean loop) {
        Task task = new Task(ticks, action, loop);
        NEW_TASKS.add(task);
        return task.id;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        // Roda apenas ao final do tick do servidor
        if (event.phase != TickEvent.Phase.END) return;

        synchronized (TickScheduler.class) {
            if (!NEW_TASKS.isEmpty()) {
                RUNNING_TASKS.addAll(NEW_TASKS);
                NEW_TASKS.clear();
            }

            Iterator<Task> iterator = RUNNING_TASKS.iterator();
            while (iterator.hasNext()) {
                Task task = iterator.next();
                if (task.cancelled) {
                    iterator.remove();
                    continue;
                }
                task.ticksRemaining--;

                if (task.ticksRemaining <= 0) {
                    try {
                        task.action.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        task.resetTimer();
                        if (task.shouldRemove()) {
                            iterator.remove();
                        }
                    }

                }
            }
        }
    }
}
