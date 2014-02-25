package impl;

import contracts.Candy;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Модель поедания конфет <b>одного</b> вкуса.
 */
public class FlavourProducerConsumerModel implements ICandyEatingRequestCallback {
    /**
     * Уровень параллеллизма при поедании конфет.
     */
    private final int degreeOfParallellism;

    /**
     * Очередь конфет на поедание.
     */
    private ConcurrentLinkedQueue<Candy> candies;

    /**
     * Очередь для добавления задач, готовых к выполнению.
     */
    private final BlockingQueue<EatingRequest> outputRequests;

    /**
     * Счётчик количества активных задач.
     */
    final AtomicInteger activeTaskCounter = new AtomicInteger(0);

    public FlavourProducerConsumerModel(BlockingQueue<EatingRequest> outputTasks,  int consumerDegreeOfParallelism) {
        this.outputRequests = outputTasks;
        degreeOfParallellism = consumerDegreeOfParallelism;
        candies = new ConcurrentLinkedQueue<Candy>();
    }

    /**
     * Добавляет конфету в очередь поедания.
     * @param candy поедаемая конфета.
     */
    public void enqueue(Candy candy) throws InterruptedException {
        candies.add(candy);
        int reservedConcurrencyLevel = activeTaskCounter.incrementAndGet();
        do {
            if (reservedConcurrencyLevel <= degreeOfParallellism) {
                Candy next = candies.poll();
                if (next != null) {
                    outputRequests.put(new EatingRequest(next, this));
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
    public void complete() throws InterruptedException {
        int currentConcurrencyLevel = activeTaskCounter.get();
        do {
            Candy next = candies.poll();
            if (next != null) {
                outputRequests.put(new EatingRequest(next, this));
                break;
            }
            int releasedConcurrencyLevel = currentConcurrencyLevel - 1;
            if(activeTaskCounter.compareAndSet(currentConcurrencyLevel, releasedConcurrencyLevel))
                break;
            currentConcurrencyLevel = activeTaskCounter.get();
        } while (true);
    }
}
