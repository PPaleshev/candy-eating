package impl;

/**
 * Интерфейс для выполнения обратных вызовов по факту завершения поедания конфеты.
 */
public interface ICandyEatingRequestCallback {
    /**
     * Вызывается для уведомления о завершении поедания конфеты.
     */
    void complete() throws InterruptedException;
}
