package ru.mail.jira.plugins.groovy.impl.scheduled;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.workflow.TransitionOptions;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;
import com.atlassian.scheduler.*;
import com.atlassian.scheduler.config.*;
import com.atlassian.scheduler.status.JobDetails;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.scheduled.ScheduledTaskForm;
import ru.mail.jira.plugins.groovy.api.dto.scheduled.TaskResult;
import ru.mail.jira.plugins.groovy.api.entity.ScheduledTaskType;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.repository.ScheduledTaskRepository;
import ru.mail.jira.plugins.groovy.api.dto.scheduled.ScheduledTaskDto;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.service.ScheduledTaskService;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.util.Const;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//todo: run now
@Component
public class ScheduledTaskServiceImpl implements ScheduledTaskService {
    private static final JobRunnerKey JOB_RUNNER_KEY = JobRunnerKey.of("ru.mail.jira.groovy.scriptJobRunner");
    private static final String LOCK_KEY = "ru.mail.jira.groovy.scheduledTasks";
    //todo: make it configurable
    private static final int ISSUE_LIMIT = 1000; //some sane number to avoid OOMs
    private static final int MIN_DELAY = 15000;
    private static final int MAX_JITTER = 10000;

    private final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);
    private final Random random = new Random();

    private final SchedulerService schedulerService;
    private final ClusterLockService lockService;
    private final JiraAuthenticationContext authenticationContext;
    private final UserManager userManager;
    private final SearchService searchService;
    private final IssueManager issueManager;
    private final IssueService issueService;
    private final ScheduledTaskRepository taskRepository;
    private final ScriptService scriptService;
    private final ExecutionRepository executionRepository;


    @Autowired
    public ScheduledTaskServiceImpl(
        @ComponentImport SchedulerService schedulerService,
        @ComponentImport ClusterLockService lockService,
        @ComponentImport JiraAuthenticationContext authenticationContext,
        @ComponentImport UserManager userManager,
        @ComponentImport SearchService searchService,
        @ComponentImport IssueManager issueManager,
        @ComponentImport IssueService issueService,
        ScheduledTaskRepository taskRepository,
        ScriptService scriptService,
        ExecutionRepository executionRepository
    ) {
        this.schedulerService = schedulerService;
        this.lockService = lockService;
        this.authenticationContext = authenticationContext;
        this.userManager = userManager;
        this.searchService = searchService;
        this.issueManager = issueManager;
        this.issueService = issueService;
        this.taskRepository = taskRepository;
        this.scriptService = scriptService;
        this.executionRepository = executionRepository;
    }

    public void onStart() {
        ClusterLock lock = lockService.getLockForName(LOCK_KEY);

        lock.lock();
        try {
            logger.info("synchronizing atlassian scheduler jobs with scheduled tasks");
            schedulerService.registerJobRunner(JOB_RUNNER_KEY, new ScriptJobRunner());

            List<JobDetails> jobs = schedulerService.getJobsByJobRunnerKey(JOB_RUNNER_KEY);
            List<ScheduledTaskDto> currentTasks = taskRepository.getAllTasks(false, false);

            Set<Integer> currentIds = currentTasks.stream().map(ScheduledTaskDto::getId).collect(Collectors.toSet());
            Set<Integer> existingIds = jobs.stream().map(this::getScriptId).collect(Collectors.toSet());

            Set<Integer> removeScripts = Sets.difference(existingIds, currentIds);

            for (ScheduledTaskDto task : currentTasks) {
                int taskId = task.getId();

                if (!existingIds.contains(taskId)) {
                    logger.debug("adding new service {}", taskId);

                    this.createJob(task);
                } else {
                    boolean needsUpdate = false;

                    JobDetails jobDetails = schedulerService.getJobDetails(JobUtil.getJobId(taskId));

                    if (jobDetails == null) {
                        logger.error("Couldn't find job details for {}", taskId);
                        continue;
                    }
                    Schedule schedule = jobDetails.getSchedule();
                    Schedule.Type scheduleType = schedule.getType();

                    String scheduleExpression = task.getScheduleExpression();
                    Integer period = Ints.tryParse(scheduleExpression);

                    if (period != null) {
                        if (scheduleType == Schedule.Type.INTERVAL) {
                            if (schedule.getIntervalScheduleInfo().getIntervalInMillis() != TimeUnit.MINUTES.toMillis(period)) {
                                needsUpdate = true; //different period
                            }
                        } else {
                            needsUpdate = true; //different type
                        }
                    } else {
                        if (scheduleType == Schedule.Type.CRON_EXPRESSION) {
                            if (!schedule.getCronScheduleInfo().getCronExpression().equals(scheduleExpression)) {
                                needsUpdate = true; //different expression
                            }
                        } else {
                            needsUpdate = true; //different type
                        }
                    }

                    if (needsUpdate) {
                        logger.info("overriding job {}", task.getId());
                        createJob(task);
                    }
                }
            }

            for (Integer id : removeScripts) {
                logger.debug("removing job for {}", id);
                schedulerService.unscheduleJob(JobUtil.getJobId(id));
            }

            logger.info("synchronization complete");
        } finally {
            lock.unlock();
        }
    }

    public void onStop() {
        schedulerService.unregisterJobRunner(JOB_RUNNER_KEY);
    }

    private void createJob(ScheduledTaskDto task) {
        String scheduleExpression = task.getScheduleExpression();
        Integer period = Ints.tryParse(scheduleExpression);

        Schedule schedule;
        if (period != null) {
            schedule = Schedule.forInterval(
                TimeUnit.MINUTES.toMillis(period),
                new Date(System.currentTimeMillis() + MIN_DELAY + random.nextInt(MAX_JITTER))
            );
        } else {
            schedule = Schedule.forCronExpression(scheduleExpression);
        }
        JobConfig jobConfig = JobConfig
            .forJobRunnerKey(JOB_RUNNER_KEY)
            .withRunMode(RunMode.RUN_ONCE_PER_CLUSTER)
            .withParameters(createJobParams(task))
            .withSchedule(schedule);

        try {
            schedulerService.scheduleJob(JobUtil.getJobId(task.getId()), jobConfig);
        } catch (SchedulerServiceException e) {
            logger.error("Unable to schedule job for task {}", task.getId(), e);
        }
    }

    private Map<String, Serializable> createJobParams(ScheduledTaskDto taskInfo) {
        return ImmutableMap.of(
            Const.SCHEDULED_TASK_ID, taskInfo.getId()
        );
    }

    private Integer getScriptId(JobDetails jobDetails) {
        return (Integer) jobDetails.getParameters().get(Const.SCHEDULED_TASK_ID);
    }

    private Integer getScriptId(JobConfig jobConfig) {
        return (Integer) jobConfig.getParameters().get(Const.SCHEDULED_TASK_ID);
    }

    @Override
    public ScheduledTaskDto createTask(ApplicationUser user, ScheduledTaskForm form) {
        ClusterLock lock = lockService.getLockForName(LOCK_KEY);

        lock.lock();
        try {
            ScheduledTaskDto task = taskRepository.createTask(user, form);

            createJob(task);

            return task;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ScheduledTaskDto updateTask(ApplicationUser user, int id, ScheduledTaskForm form) {
        ClusterLock lock = lockService.getLockForName(LOCK_KEY);

        lock.lock();
        try {
            ScheduledTaskDto task = taskRepository.updateTask(user, id, form);

            createJob(task); //explicit deletion is not required

            return task;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void deleteTask(ApplicationUser user, int id) {
        ClusterLock lock = lockService.getLockForName(LOCK_KEY);

        lock.lock();
        try {
            taskRepository.deleteTask(user, id);

            schedulerService.unscheduleJob(JobUtil.getJobId(id));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void restoreTask(ApplicationUser user, int id) {
        ClusterLock lock = lockService.getLockForName(LOCK_KEY);

        lock.lock();
        try {
            ScheduledTaskDto task = taskRepository.restoreTask(user, id);

            createJob(task);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setEnabled(ApplicationUser user, int id, boolean enabled) {
        taskRepository.setEnabled(user, id, enabled);
    }

    @Override
    public TaskResult runNow(ApplicationUser user, int id) {
        logger.info("{} started manually job with id {}", user.getName(), id);

        long t = System.currentTimeMillis();
        JobRunnerResponse jobRunnerResponse = runJob(id, true);
        return TaskResult.fromJobRunnerResponse(jobRunnerResponse, System.currentTimeMillis() - t);
    }

    private Exception runScript(ScheduledTaskDto taskInfo, Map<String, Object> bindings, boolean trackAll) {
        String uuid = taskInfo.getUuid();
        long t = System.currentTimeMillis();
        boolean successful = true;
        String error = null;
        Exception exception = null;

        try {
            scriptService.executeScript(
                taskInfo.getUuid(),
                taskInfo.getScriptBody(),
                ScriptType.SCHEDULED_TASK,
                bindings
            );
        } catch (Exception e) {
            exception = e;
            successful = false;
            error = ExceptionHelper.writeExceptionToString(e);
        }

        if (trackAll) {
            executionRepository.trackInline(
                uuid,
                System.currentTimeMillis() - t,
                successful,
                error,
                ImmutableMap.of(
                    "type", ScriptType.LISTENER.name(),
                    "bindings", bindings.toString()
                )
            );
        }

        return exception;
    }

    private boolean transitionIssue(ApplicationUser user, Issue issue, int action, TransitionOptions options) {
        IssueService.TransitionValidationResult validationResult = issueService.validateTransition(
            user,
            issue.getId(),
            action,
            new IssueInputParametersImpl(),
            options
        );

        if (!validationResult.isValid()) {
            logger.warn(
                "Action {} is not valid for issue {}: {}",
                action, issue.getKey(), validationResult.getErrorCollection()
            );
            return false;
        }

        //todo: maybe add transition inputs (???)
        IssueService.IssueResult transitionResult = issueService.transition(user, validationResult);

        if (!transitionResult.isValid()) {
            logger.error(
                "Transition {} for issue {} was unsuccessful: {}",
                action, issue.getKey(), transitionResult.getErrorCollection()
            );
            return false;
        }

        return true;
    }

    private JobRunnerResponse runJob(Integer scriptId, boolean ignoreDisabled) {
        if (scriptId == null) {
            return JobRunnerResponse.failed("Script id is not present.");
        }

        ScheduledTaskDto task = taskRepository.getTaskInfo(scriptId, false, false);
        if (task == null) {
            return JobRunnerResponse.failed("Scheduled task was not found");
        }

        if (!task.isEnabled() && !ignoreDisabled) {
            return JobRunnerResponse.success("Task is disabled");
        }

        ApplicationUser runAs = userManager.getUserByKey(task.getUserKey());

        if (runAs == null) {
            return JobRunnerResponse.failed("Unable to get user with key " + task.getUserKey());
        }

        ApplicationUser currentUser = authenticationContext.getLoggedInUser();

        ScheduledTaskType type = task.getType();
        boolean isTransition = type == ScheduledTaskType.ISSUE_JQL_TRANSITION;

        if (isTransition) {
            if (task.getIssueWorkflowActionId() == null) {
                return JobRunnerResponse.failed("Action id is not present for TRANSITION task type");
            }
            if (task.getTransitionOptions() == null) {
                return JobRunnerResponse.failed("Transition options are not present for TRANSITION task type");
            }
        }

        logger.info("running task {}", task.getId());

        TransitionOptions transitionOptions = isTransition? task.getTransitionOptions().toJiraOptions() : null;

        try {
            authenticationContext.setLoggedInUser(runAs);

            if (
                type == ScheduledTaskType.ISSUE_JQL_SCRIPT ||
                    type == ScheduledTaskType.DOCUMENT_ISSUE_JQL_SCRIPT ||
                    isTransition
                ) {
                String issueJql = task.getIssueJql();
                if (StringUtils.isEmpty(issueJql)) {
                    return JobRunnerResponse.failed("JQL query is not present for task with JQL type");
                }

                SearchService.ParseResult parseResult = searchService.parseQuery(runAs, issueJql);

                if (!parseResult.isValid()) {
                    return JobRunnerResponse.failed(parseResult.getErrors().toString());
                }

                Query query = parseResult.getQuery();

                try {
                    boolean isMutableIssue = type == ScheduledTaskType.ISSUE_JQL_SCRIPT;
                    SearchResults searchResults = searchService.search(runAs, query, new PagerFilter(ISSUE_LIMIT));

                    long total = searchResults.getTotal();
                    long errors = 0;

                    for (Issue issue : searchResults.getIssues()) {
                        Issue issueBinding = issue;

                        if (isTransition) {
                            boolean successful = transitionIssue(runAs, issue, task.getIssueWorkflowActionId(), transitionOptions);

                            if (!successful) {
                                errors++;
                            }
                        } else {
                            if (isMutableIssue) {
                                issueBinding = issueManager.getIssueObject(issue.getId());
                            }

                            Exception error = runScript(task, ImmutableMap.of("issue", issueBinding), false);

                            if (error != null) {
                                errors++;
                                logger.error("Exception occurred in task {} for issue {}", task.getId(), issue.getKey(), error);
                            }
                        }
                    }

                    if (errors > 0) {
                        if (errors == total) {
                            return JobRunnerResponse.failed("Task failed for all of " + total + " issues");
                        } else {
                            return JobRunnerResponse.failed("Task failed for " + errors + " of " + total + " issues");
                        }
                    } else {
                        return JobRunnerResponse.success("Task ran successfully for all of " + total + " issues");
                    }
                } catch (SearchException e) {
                    return JobRunnerResponse.failed(e);
                }
            } else {
                Exception exception = runScript(task, ImmutableMap.of(), true);

                if (exception != null) {
                    return JobRunnerResponse.failed(exception);
                }
            }
        } finally {
            authenticationContext.setLoggedInUser(currentUser);
        }

        return JobRunnerResponse.success();
    }

    private class ScriptJobRunner implements JobRunner {
        @Nullable
        @Override
        @ParametersAreNonnullByDefault
        public JobRunnerResponse runJob(JobRunnerRequest request) {
            return ScheduledTaskServiceImpl.this.runJob(getScriptId(request.getJobConfig()), false);
        }
    }
}
