package fi.jubic.snoozy;

public class TaskSchedulerException extends RuntimeException {
    public TaskSchedulerException() {
        super();
    }

    public TaskSchedulerException(String s) {
        super(s);
    }

    public TaskSchedulerException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public TaskSchedulerException(Throwable throwable) {
        super(throwable);
    }

    protected TaskSchedulerException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
