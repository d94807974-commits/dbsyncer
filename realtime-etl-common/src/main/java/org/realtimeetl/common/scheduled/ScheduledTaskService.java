package org.realtimeetl.common.scheduled;

public interface ScheduledTaskService {

    /**
     * Recalling that,seconds，RemoveValue0-59
     * Number two.min，RemoveValue0-59
     * Number three.hours，RemoveValue0-23
     * Number four, date paragraphs.days/Shit, take it.Value1-31
     * NumberYeah.. Date and month.Value1-12
     * Sixth, week, take.Value1-7
     * [seconds min Day and week]
     *
     * @param key  TasksUniquekey
     * @param cron TasksExpression
     * @param job  TasksAchieved
     */
    void start(String key, String cron, ScheduledTaskJob job);

    void start(String key, long period, ScheduledTaskJob job);

    void start(String cron, ScheduledTaskJob job);

    void start(long period, ScheduledTaskJob job);

    void stop(String key);
}
