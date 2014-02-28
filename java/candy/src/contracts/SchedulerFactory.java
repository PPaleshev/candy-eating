package contracts;

import impl.EatingRequest;

import java.util.Queue;

/**
 * Интерфейс фабрики стратегий планирования поедания конфет.
 */
public interface SchedulerFactory {
    /**
     * Создаёт стратегию планирования поедания конфет указанного вкуса.
     * @param flavour вкус.
     * @param requestQueue очередь запросов на поедание конфеты.
     * @return возвращает стратегию планирования поедания конфет.
     */
    FlavourScheduler create(Flavour flavour, Queue<EatingRequest> requestQueue);
}
