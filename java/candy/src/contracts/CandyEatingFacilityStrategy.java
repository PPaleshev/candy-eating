package contracts;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Стратегия работы механизма поедания конфет.
 */
public interface CandyEatingFacilityStrategy {
    /**
     * Возвращает количество конфет, ожидающих поедания.
     */
    int getPendingCandies();

    /**
     * Начинает обработку.
     */
    void start();

    /**
     * Завершает выемку новых конфет из входящей очереди и синхронно дожидается
     * доедания всех взятых конфет.
     */
    void shutdownAndWait();
}
