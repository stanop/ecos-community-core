package ru.citeck.ecos.locks;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LockUtilsImpl implements LockUtils {

    private static final String QNAME_TEMPLATE = "ECOSJob-%s";
    private static final String JOB_ABORTED_TEMPLATE = "Job %s aborted. LockToken: %s";
    private static final String LOCK_INFO_IS_EMPTY = "One or more params is empty.\n\tLockToken: %s\n\tLockQName: %s";
    private static final String LOCK_RELEASE_ERROR = "Error while release lock for job %s. LockToken: %s";

    private JobLockService jobLockService;

    @Override
    public void doWithLock(String lockId, Runnable job) {
        doWithLock(lockId, 30, TimeUnit.SECONDS, job);
    }

    @Override
    public <T> T doWithLock(String lockId, Callable<T> job) {
        return doWithLock(lockId, 30, TimeUnit.SECONDS, job);
    }

    @Override
    public void doWithLock(String lockId, long timeToLive, TimeUnit unit, Runnable job) {
        doWithLock(lockId, timeToLive, unit, Executors.callable(job));
    }

    @Override
    public <T> T doWithLock(String lockId, long timeToLive, TimeUnit unit, Callable<T> job) {
        long millisecondToLive = unit.toMillis(timeToLive);
        QName lockQName = QName.createQName(String.format(QNAME_TEMPLATE, lockId));

        String lockToken = null;
        try {
            lockToken = getAndRefreshLock(lockQName, millisecondToLive);
            return job.call();
        } catch (Exception e) {
            log.error(String.format(JOB_ABORTED_TEMPLATE, lockQName.getLocalName(), lockToken), e.getMessage(), e);
        } finally {
            releaseLock(lockToken, lockQName);
        }
        return null;
    }

    private void releaseLock(String lockToken, QName lockQName) {
        if (StringUtils.isEmpty(lockToken) || lockQName == null) {
            log.error(String.format(LOCK_INFO_IS_EMPTY, lockToken, lockQName));
            return;
        }

        try {
            jobLockService.releaseLockVerify(lockToken, lockQName);
        } catch (Exception e) {
            log.error(String.format(LOCK_RELEASE_ERROR, lockQName.getLocalName(), lockToken), e.getMessage(), e);
        }
    }

    private String getAndRefreshLock(QName lockQName, long millisecondToLive) {
        String lockToken = jobLockService.getLock(lockQName, millisecondToLive, 500, 60);
        jobLockService.refreshLock(lockToken, lockQName, millisecondToLive);
        return lockToken;
    }

    @Autowired
    public void setJobLockService(JobLockService jobLockService) {
        this.jobLockService = jobLockService;
    }
}
