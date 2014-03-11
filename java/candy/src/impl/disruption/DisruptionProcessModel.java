package impl.disruption;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import contracts.Candy;
import contracts.CandyEater;
import contracts.CandyEatingFacilityStrategy;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Стратегия поедания конфет, использующая Disruptor.
 */
public class DisruptionProcessModel implements CandyEatingFacilityStrategy, CandyHandlerCallback {
    private final int RING_SIZE = 1024 * 256;

    /**
     * Счётчик конфет, вынутых из входящей очереди, но ещё не съеденных.
     */
    private final AtomicInteger pendingCandies = new AtomicInteger();

    /**
     * Массив обработчиков ячеек.
     */
    private final EventHandler<CandyEntry>[] handlers;


    private final ExecutorService executionService;

    /**
     * Кольцевой буфер.
     */
    private final RingBuffer<CandyEntry> ringBuffer;

    /**
     * Флаг, равный true, если работа должна быть завершена.
     */
    private volatile boolean shutdown;

    /**
     * Поток, публикующий конфеты.
     */
    private final Thread publisherThread;

    private AtomicInteger warmupCounter = new AtomicInteger();

    /**
     * Disruptor.
     */
    private final Disruptor<CandyEntry> disruptor;

    public static DisruptionProcessModel Instance;

    public DisruptionProcessModel(BlockingQueue<Candy> inputQueue, Set<CandyEater> eaters) {
        executionService = Executors.newCachedThreadPool();
        disruptor = new Disruptor<>(CandyEntry.FACTORY, RING_SIZE, executionService, ProducerType.SINGLE, new SleepingWaitStrategy());
        handlers = new EventHandler[eaters.size()];
        int index = 0;
        for (CandyEater eater: eaters) {
            handlers[index] = new CandyEatingHandler(index, eater, eaters.size(), this);
            index++;
        }
        disruptor.handleEventsWith(handlers);
        ringBuffer = disruptor.start();
        publisherThread = new Thread(new Publisher(inputQueue, ringBuffer));
        Instance = this;
    }

    @Override
    public int getPendingCandies() {
        return pendingCandies.get();
    }

    public void warmup(int eaterCount) {
        AtomicInteger heat = new AtomicInteger(0);
        for(int i =0; i < RING_SIZE; i++) {
            long sequenceNo = ringBuffer.next();
            CandyEntry candy = ringBuffer.get(sequenceNo);
            candy.warmup = heat;
            heat.addAndGet(eaterCount);
            ringBuffer.publish(sequenceNo);
        }
        while (heat.get() > 0)
            Thread.yield();
        for(int i =0; i<RING_SIZE; i++) {
            CandyEntry entry = ringBuffer.get(i);
            entry.warmup = null;
        }
    }

    @Override
    public void start() {
        publisherThread.start();
    }

    @Override
    public void shutdownAndWait() {
        shutdown = true;
        try {
            publisherThread.join();
            while (pendingCandies.get() > 0)
                Thread.yield();
            disruptor.shutdown();
            executionService.shutdown();
            assert executionService.awaitTermination(0, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void complete() {
        pendingCandies.decrementAndGet();
    }

    /**
     * Процесс выборки конфет из входящей очереди и публикации их в кольцевом буфере.
     */
    private class Publisher implements Runnable {
        /**
         * Очередь входящих конфет.
         */
        private final BlockingQueue<Candy> inputQueue;

        /**
         * Кольцевой буфер.
         */
        private final RingBuffer<CandyEntry> ringBuffer;

        private Publisher(BlockingQueue<Candy> inputQueue, RingBuffer<CandyEntry> ringBuffer) {
            this.inputQueue = inputQueue;
            this.ringBuffer = ringBuffer;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    Candy candy = inputQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (candy == null)
                        continue;
                    pendingCandies.incrementAndGet();
                    long sequenceNo = ringBuffer.next();
                    CandyEntry cell = ringBuffer.get(sequenceNo);
                    cell.candy.set(candy);
                    ringBuffer.publish(sequenceNo);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
