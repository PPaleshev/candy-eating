package impl;

import contracts.*;

import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * Механизм поедания конфет.
 */
public class EaterFacility implements CandyEatingFacility {
    /**
     * Стратегия работы механизма.
     */
    final CandyEatingFacilityStrategy strategy;

    /**
     * Флаг, равный true, если процесс был инициализирован, иначе false.
     */
    volatile boolean launched = false;

    public EaterFacility(CandyEatingFacilityStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public synchronized void launch(BlockingQueue<Candy> candies, Set<CandyEater> candyEaters) {
        if (launched)
            throw new IllegalStateException("eaters facility is already launched");
        strategy.start();
        launched = true;
        System.out.println("facility is launched");
    }

    @Override
    public synchronized void shutdown() {
        if (!launched)
            throw new IllegalStateException("eaters facility is not launched");
        launched = false;
        strategy.shutdownAndWait();
    }
}
