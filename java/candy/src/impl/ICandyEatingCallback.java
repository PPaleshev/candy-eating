package impl;

import contracts.CandyEater;

/**
 * Интерфейс для выполнения обратного вызова по факту завершения поедания конфеты.
 */
public interface ICandyEatingCallback {
    /**
     * Завершает выполнение задачи поедания конфеты.
     * @param task завершённая задача.
     */
    void complete(CandyEatingTask task) throws InterruptedException;
}
