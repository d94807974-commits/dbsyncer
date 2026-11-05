package org.realtimeetl.common.util;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;

public abstract class ThreadPoolUtil {

    /**
     * NewThreadsJi.
     *
     * @param corePoolSize CoreThreads
     * @param maxPoolSize MaxThreads
     * @param queueCapacity Queue Capacity
     * @param keepAliveSeconds FreeThreadsDestructionTime（seconds）
     * @param threadNamePrefix ThreadsPrefixName
     * @return
     */
    public static ThreadPoolTaskExecutor newThreadPoolTaskExecutor(int corePoolSize, int maxPoolSize, int queueCapacity, int keepAliveSeconds, String threadNamePrefix) {
        // Watch the line.Log：2. do submit,taskCount [101], completedTaskCount [87], activeCount [5], queueSize [9]
        // /54/7DescriptionSubmitTasksPresent.ThreadsWhen I was in the pool, I called.Yessubmit(Callable task)This way, it's already...SubmitYeah.101individualTasks，FinishYeah.87Yeah, I do.5individualThreadsI'm working on it.Tasks，There's nothing left.9individualTasksIn the queue, wait.ThreadsThe basics of the pool are on the way;
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // CoreThreads10：ThreadsInitialised when the pool was createdThreads
        executor.setCorePoolSize(corePoolSize);
        // MaxThreads128：ThreadsBiggest poolThreads，Only when the buffer line is full will the application go beyond the core.ThreadsYes.Threads
        // maxPoolSize When system loads are largestValueTime,CoreThreadsAlreadyNoneIt's done on time.Tasks,/54/7YesWe need more.Threads.Everyseconds200individualTasksYes.20individualThreads,♪ So when every ♪seconds1000individualTasksTime,Yes.(1000-queueCapacity)*(20/200),That's...60individualThreads,Yes.maxPoolSizeSettingsYes60;
        executor.setMaxPoolSize(maxPoolSize);
        // Social securityTasksqueue
        executor.setQueueCapacity(queueCapacity);
        // verifieThreadsYes.FreeTime30seconds：When the core is exceededThreadsOutsideThreadsYes.FreeTimeThey'll be destroyed when they arrive.
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // ThreadsPool name prefix:SettingsIt'll be easy for us to locate.TasksPlaceThreadsJi.
        executor.setThreadNamePrefix(threadNamePrefix);
        // RiceThreadsIke said no.TasksThe local strategy:CallerRunsPolicyStrategy, whenThreadsWhen the pool doesn't have the capacity to handle it, the strategy will go straight ahead.execute Method CallThreadsRunning rejectedTasks；If execution proceduresDisabled，I'll throw it away.Tasks
        /*
         * CallerRunsPolicy：ThreadsCall to run theTasksYes.execute By itself. This strategy provides simple feedback control mechanisms that can slow down new onesTasksYes.SubmitSpeed.
         * This strategy obviously doesn't want to give up.Tasks。But...YesBecause there's no more in the pool.ResourceThen just use the call.executeYes.ThreadsBy itself. At first I didn't want to leave.Tasksimplementation, butYesFor some applications, it's very likely to create the current situation.ThreadsIt's blocked. If allThreadsBothYesFailure Ninety-five per cent of the total number of cases were reported. Depending Ninety-five per centAbortPolicy：Processor rejected will be thrown out when running
         * RejectedExecutionException This strategy goes straight out.Error，DropTasks。（jdkDefault policy, line upThreadsIt's a direct rejection.AddNewTasks，And throw it out.Error，So sometimes I give up.YesA kind of courage to ensure follow-up.TasksYes.NormalLet's go. Throw some away.YesYou can take it. Make a record.DiscardPolicy：UnenforceableTasksWill beDelete This strategy andAbortPolicyAlmost the same, too.YesDropTasks，Except he didn't throw it.Error。
         * DiscardOldestPolicy：If enforcement procedures are not yet in placeClose，At the head of the task force.TasksWill beDelete，And...Try againExecution procedure (if again)Failed，It's a slightly more complicated strategy.poolAuditorless.CloseIf you want to get rid of the first of the slow queuesTasks，And then try to run that again.Tasks。This strategy requires proper care.
         */
        executor.setRejectedExecutionHandler(rejectedExecutionHandler());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    private static RejectedExecutionHandler rejectedExecutionHandler() {
        return (r, executor)-> {
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }
}