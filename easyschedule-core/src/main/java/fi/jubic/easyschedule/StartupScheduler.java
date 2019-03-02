package fi.jubic.easyschedule;

import java.util.ArrayList;
import java.util.List;

public class StartupScheduler implements TaskScheduler {
    private final List<Task> startupTasks;

    public StartupScheduler() {
        this.startupTasks = new ArrayList<>();
    }

    @Override
    public TaskScheduler registerStartupTask(Task task) {
        startupTasks.add(task);
        return this;
    }

    @Override
    public TaskScheduler registerTask(String cron, Task task) {
        throw new UnsupportedOperationException(
                this.getClass().getCanonicalName() + " does not support cron tasks"
        );
    }

    @Override
    public void start() {
        startupTasks.forEach(Task::run);
    }
}
