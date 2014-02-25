package contracts;

import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * Установка для поедания конфет.
 */
public interface CandyEatingFacility {
    /**
     * Запускает параллельное поедание конфет из очереди <code>candies</code>
     * поедателями <code>candyEaters</code>.
     * <p/>
     * Обеспечивает, что<br/>
     * &bull; в любой момент времени поедается не более одной конфеты каждого вкуса и<br/>
     * &bull; конфеты одного вкуса поедаются в той очерёдности, в которой они находились в оч
     *
     * ереди.
     * <p/>
     * Возвращает управление после запуска поедания.
     * <p/>
     * Переданные параметры, включая элементы коллекций и вкусы конфет,
     * должны быть не нул, это не проверяется.
     *
     * @param candies    очередь конфет
     * @param candyEaters набор поедателей конфет
     */
    void launch(BlockingQueue<Candy> candies, Set<CandyEater> candyEaters);

    /**
     * Прекращает выемку новых конфет из очереди и возвращает управление,
     * когда все уже вынутые из очереди конфеты съедены.
     */
    void shutdown();
}
