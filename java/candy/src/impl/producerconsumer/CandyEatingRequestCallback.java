package impl.producerconsumer;

/**
 * Интерфейс для выполнения обратных вызовов по факту завершения поедания конфеты.
 */
public interface CandyEatingRequestCallback {
    /**
     * Вызывается для уведомления о завершении поедания конфеты.
     */
    void complete() throws InterruptedException;
}
