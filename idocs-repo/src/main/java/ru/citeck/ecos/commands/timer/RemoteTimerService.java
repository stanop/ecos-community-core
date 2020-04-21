package ru.citeck.ecos.commands.timer;

import java.time.Instant;

public interface RemoteTimerService {

    /**
     * Schedules timer on ecos-process microservice.<br/>
     * Callback after timer trigger by time will be returned to current application.
     *
     * @param triggerTime point in time after which the trigger should work.
     * @param commandType type of command, that will be triggered after the timer is triggered.
     * @param callbackData data that will be returned as additional data after the timer is triggered.
     * @param <T> any type of callback data.
     * @return timerId on ecos-process microservice.
     */
    <T> String scheduleTimer(Instant triggerTime, String commandType, T callbackData);

    /**
     * Schedules timer on ecos-process microservice.<br/>
     * Callback after timer trigger by time will be returned to selected in parameters application.
     *
     * @param triggerTime point in time after which the trigger should work.
     * @param commandType type of command, that will be triggered after the timer is triggered.
     * @param targetApp application name, that will receive command after the timer is triggered.
     * @param callbackData data that will be returned as additional data after the timer is triggered.
     * @param <T> any type of callback data.
     * @return timerId on ecos-process microservice.
     */
    <T> String scheduleTimer(Instant triggerTime, String commandType, String targetApp, T callbackData);

    /**
     * Cancels timer after transaction on ecos-process microservice.
     *
     * @param timerId id of timer that need to cancel. See result of schedule methods.
     */
    void cancelTimerAfterTransaction(String timerId);

    /**
     * Cancels timer immediately on ecos-process microservice.
     *
     * @param timerId id of timer that need to cancel. See result of schedule methods.
     */
    void cancelTimer(String timerId);

}
