package ru.mail.jira.plugins.groovy.impl.scheduled;

import com.atlassian.scheduler.config.JobId;

public final class JobUtil {
    private static final String JOB_ID_PREFIX = "ru.mail.jira.groovy.script.";

    private JobUtil() {}

    public static JobId getJobId(int jobId) {
        return JobId.of(JOB_ID_PREFIX + jobId);
    }
}
