package impl.scheduling;

import contracts.Candy;
import contracts.FlavourScheduler;
import impl.producerconsumer.CandyEatingRequestCallback;
import impl.EatingRequest;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Модель поедания конфет <b>одного</b> вкуса.
 */
public class NonBlockingFlavourScheduler implements CandyEatingRequestCallback, FlavourScheduler {
    /**
     * Уровень параллеллизма при поедании конфет.
     */
    private final int degreeOfParallelism;

    /**
     * Очередь конфет на поедание.
     */
    private ConcurrentLinkedQueue<Candy> candies;

    /**
     * Очередь для добавления задач, готовых к выполнению.
     */
    private final Queue<EatingRequest> outputRequests;

    /**
     * Счётчик количества активных задач.
     */
    final AtomicInteger activeTaskCounter = new AtomicInteger(0);

    public NonBlockingFlavourScheduler(Queue<EatingRequest> outputTasks, int consumerDegreeOfParallelism) {
        this.outputRequests = outputTasks;
        degreeOfParallelism = consumerDegreeOfParallelism;
        candies = new ConcurrentLinkedQueue<>();
    }

    /**
     * Добавляет конфету в очередь поедания.
     * @param candy поедаемая конфета.
     */
    public void enqueue(Candy candy) {
        candies.add(candy);
        int reservedConcurrencyLevel = activeTaskCounter.incrementAndGet();
        do {
            if (reservedConcurrencyLevel <= degreeOfParallelism) {
                Candy next = candies.poll();
                if (next != null) {
                    outputRequests.add(new EatingRequest(next, this));
                    break;
                }
            }
            int releasedConcurrencyLevel = reservedConcurrencyLevel - 1;
            if(activeTaskCounter.compareAndSet(reservedConcurrencyLevel, releasedConcurrencyLevel))
                break;
            reservedConcurrencyLevel = activeTaskCounter.get();
        } while (true);
    }

    /**
     * Уведомляет модель о завершении обработки запроса.
     */
    public void complete() {
        int currentConcurrencyLevel = activeTaskCounter.get();
        do {
            Candy next = candies.poll();
            if (next != null) {
                outputRequests.add(new EatingRequest(next, this));
                break;
            }
            int releasedConcurrencyLevel = currentConcurrencyLevel - 1;
            if(activeTaskCounter.compareAndSet(currentConcurrencyLevel, releasedConcurrencyLevel))
                break;
            currentConcurrencyLevel = activeTaskCounter.get();
        } while (true);
    }
}
