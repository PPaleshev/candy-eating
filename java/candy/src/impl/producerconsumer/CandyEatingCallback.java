package impl.producerconsumer;

import contracts.CandyEater;
import impl.CandyEatingTask;

/**
 * Интерфейс для выполнения обратного вызова по факту завершения поедания конфеты.
 */
public interface CandyEatingCallback {
    /**
     * Завершает выполнение задачи поедания конфеты.
     * @param task завершённая задача.
     */
    void complete(CandyEatingTask task) throws InterruptedException;
}
