package impl.scheduling;

import contracts.Candy;
import contracts.FlavourScheduler;
import impl.producerconsumer.CandyEatingRequestCallback;
import impl.EatingRequest;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * Реализация стратегии планирования поедания конфет, использующей блокировки.
 */
public class BlockingFlavourScheduler implements FlavourScheduler, CandyEatingRequestCallback {
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

    public BlockingFlavourScheduler(int degreeOfParallelism, Queue<EatingRequest> outputRequests) {
        semaphore = new Semaphore(degreeOfParallelism);
        this.outputRequests = outputRequests;
    }

    @Override
    public void enqueue(Candy candy) {
        candies.add(candy);
        if (!semaphore.tryAcquire())
            return;
        Candy next = candies.poll();
        if(next != null)
            outputRequests.add(new EatingRequest(next, this));
        else
            semaphore.release();
    }

    @Override
    public void complete() throws InterruptedException {
        Candy next = candies.poll();
        if(next == null)
            semaphore.release();
        else
            outputRequests.add(new EatingRequest(next, this));
    }
}
