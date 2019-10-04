package fi.jubic.easyschedule;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class InMemoryScheduler extends StartupScheduler {
    private final List<CronRegistration> cronTasks;
    private final int threadCount;

    public InMemoryScheduler(int threadCount) {
        super();
        this.cronTasks = new ArrayList<>();
        this.threadCount = threadCount;
    }

    @Override
    public TaskScheduler registerTask(String cron, Task task) {
        cronTasks.add(CronRegistration.of(cron, task));
        return this;
    }

    @Override
    public void start() {
        super.start();

        Counter counter = new Counter();

        Map<String, CronRegistration> cronTasksWithNames = cronTasks.stream()
                .collect(
                        Collectors.toMap(
                                registration -> "cron-" + registration.getTask().getClass().getSimpleName() + "-" + counter.getAndIncrement(),
                                registration -> registration
                        )
                );

        StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Properties properties = new Properties();
        properties.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        properties.setProperty("org.quartz.threadPool.threadCount", Integer.toString(this.threadCount));

        Scheduler scheduler;
        try {
            schedulerFactory.initialize(properties);
            scheduler = schedulerFactory.getScheduler();
            scheduler.setJobFactory(
                    new InstanceJobFactory(
                            cronTasksWithNames.entrySet()
                                    .stream()
                                    .collect(
                                            Collectors.toMap(
                                                    Map.Entry::getKey,
                                                    pair -> new RunnableTask(pair.getValue().getTask())
                                            )
                                    )
                    )
            );

            cronTasksWithNames.forEach(
                    (name, registration) -> {
                        try {
                            scheduler.scheduleJob(
                                    JobBuilder.newJob(Job.class)
                                            .withIdentity(name)
                                            .build(),
                                    TriggerBuilder.newTrigger()
                                            .withSchedule(
                                                    CronScheduleBuilder.cronSchedule(registration.getCron())
                                            )
                                            .build()
                            );
                        } catch (SchedulerException expection) {
                            throw new TaskSchedulerException(expection);
                        }
                    }
            );

            scheduler.start();
        } catch (SchedulerException exception) {
            throw new TaskSchedulerException(exception);
        }
    }

    private static class InstanceJobFactory implements JobFactory {
        private final Map<String, RunnableTask> taskMap;

        InstanceJobFactory(Map<String, RunnableTask> taskMap) {
            this.taskMap = taskMap;
        }

        @Override
        public Job newJob(TriggerFiredBundle triggerFiredBundle, Scheduler scheduler) {
            return taskMap.get(triggerFiredBundle.getJobDetail().getKey().getName());
        }
    }

    private static class RunnableTask implements Job {
        private final Task task;

        RunnableTask(Task task) {
            this.task = task;
        }

        @Override
        public void execute(JobExecutionContext jobExecutionContext) {
            this.task.run();
        }
    }

    private static class Counter {
        private int value;

        Counter() {
            value = 1;
        }

        int getAndIncrement() {
            int prevValue = value;
            value++;
            return prevValue;
        }
    }
}
