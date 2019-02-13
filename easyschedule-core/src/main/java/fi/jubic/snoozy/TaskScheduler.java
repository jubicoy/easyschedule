package fi.jubic.snoozy;

public interface TaskScheduler {
    TaskScheduler registerStartupTask(Task task);
    TaskScheduler registerTask(String cron, Task task);
    void start();
}
