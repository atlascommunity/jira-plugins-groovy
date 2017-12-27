package ru.mail.jira.plugins.groovy.impl;

import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.scheduler.*;
import com.atlassian.scheduler.config.*;
import com.atlassian.scheduler.status.JobDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.ExecutionRepository;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@ExportAsService(LifecycleAware.class)
public class OldExecutionDeletionScheduler implements LifecycleAware {
    private static final JobRunnerKey JOB_RUNNER_KEY = JobRunnerKey.of("ru.mail.jira.groovy.jobRunner");
    private static final JobId JOB_ID = JobId.of("ru.mail.jira.groovy.deleteOldExecutions");

    private final Logger logger = LoggerFactory.getLogger(OldExecutionDeletionScheduler.class);

    private final TimeZoneManager timeZoneManager;
    private final SchedulerService schedulerService;
    private final ExecutionRepository executionRepository;

    @Autowired
    public OldExecutionDeletionScheduler(
        @ComponentImport TimeZoneManager timeZoneManager,
        @ComponentImport SchedulerService schedulerService,
        ExecutionRepository executionRepository
    ) {
        this.timeZoneManager = timeZoneManager;
        this.schedulerService = schedulerService;
        this.executionRepository = executionRepository;
    }

    @Override
    public void onStart() {
        schedulerService.registerJobRunner(JOB_RUNNER_KEY, new OldExecutionDeletionJobRunner());

        JobDetails jobDetails = schedulerService.getJobDetails(JOB_ID);

        if (jobDetails == null) {
            Schedule schedule = Schedule.forInterval(
                TimeUnit.DAYS.toMillis(1),
                Date.from(LocalDate.now().plusDays(1).atTime(0, 0).atZone(timeZoneManager.getDefaultTimezone().toZoneId()).toInstant())
            );

            JobConfig jobConfig = JobConfig
                .forJobRunnerKey(JOB_RUNNER_KEY)
                .withRunMode(RunMode.RUN_ONCE_PER_CLUSTER)
                .withSchedule(schedule);

            try {
                schedulerService.scheduleJob(JOB_ID, jobConfig);
            } catch (SchedulerServiceException e) {
                logger.error("Unable to schedule job", e);
            }
        }
    }

    @Override
    public void onStop() {
        schedulerService.unregisterJobRunner(JOB_RUNNER_KEY);
    }

    public class OldExecutionDeletionJobRunner implements JobRunner {
        @Nullable
        @Override
        public JobRunnerResponse runJob(JobRunnerRequest request) {
            try {
                executionRepository.deleteOldExecutions();
                return JobRunnerResponse.success();
            } catch (Exception e) {
                logger.error("unable to run task", e);
                return JobRunnerResponse.failed(e);
            }
        }
    }
}
