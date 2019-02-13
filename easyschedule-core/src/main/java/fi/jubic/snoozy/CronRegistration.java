package fi.jubic.snoozy;

class CronRegistration {
    private final String cron;
    private final Task task;

    private CronRegistration(String cron, Task task) {
        this.cron = cron;
        this.task = task;
    }

    static CronRegistration of(String cron, Task task) {
        return new CronRegistration(cron, task);
    }

    String getCron() {
        return cron;
    }

    Task getTask() {
        return task;
    }
}
