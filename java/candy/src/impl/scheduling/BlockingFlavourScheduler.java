package impl.scheduling;

import contracts.Candy;
import contracts.FlavourScheduler;
import impl.EatingRequest;
import impl.ICandyEatingRequestCallback;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * Реализация стратегии планирования поедания конфет, использующей блокировки.
 */
public class BlockingFlavourScheduler implements FlavourScheduler, ICandyEatingRequestCallback {
    /**
     * Семафор для управления количеством
     */
    private final Semaphore semaphore;

    /**
     * Очередь запросов на поедание.
     */
    private final Queue<EatingRequest> outputRequests;

    /**
     * Очередь конфет, ожидающих поедания.
     */
    private final Queue<Candy> candies = new ConcurrentLinkedQueue<Candy>();

    /**
     * Объект для блокировки при работе с очередью конфет.
     */
    private final Object lock = new Object();

    public BlockingFlavourScheduler(int degreeOfParallelism, Queue<EatingRequest> outputRequests) {
        semaphore = new Semaphore(degreeOfParallelism);
        this.outputRequests = outputRequests;
    }

    @Override
    public void enqueue(Candy candy) {
        candies.add(candy);
        if (!semaphore.tryAcquire())
            return;
        synchronized (lock) {
            Candy next = candies.poll();
            if(next != null)
                outputRequests.add(new EatingRequest(next, this));
            else
                semaphore.release();
        }
    }

    @Override
    public void complete() throws InterruptedException {
        synchronized (lock) {
            Candy next = candies.poll();
            if(next == null)
                semaphore.release();
            else
                outputRequests.add(new EatingRequest(next, this));
        }
    }
}
