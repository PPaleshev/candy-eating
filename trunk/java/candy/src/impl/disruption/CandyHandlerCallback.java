package impl.disruption;

/**
 * Интерфейс для выполнения обратных вызовов по факту завершения поедания конфет.
 */
public interface CandyHandlerCallback {
    /**
     * Вызывается по факту завершения поедания конфеты.
     */
    void complete();
}
