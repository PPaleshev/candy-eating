package impl;

import contracts.Candy;
import contracts.CandyEater;
import contracts.CandyEatingFacility;

import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class EaterFacility implements CandyEatingFacility {
    /**
     * Флаг, равный true, если процесс был инициализирован, иначе false.
     */
    volatile boolean launched = false;

    /**
     * Модель процесса поедания конфет.
     */
    EatingProcessModel model;

    @Override
    public synchronized void launch(BlockingQueue<Candy> candies, Set<CandyEater> candyEaters) {
        if(launched)
            throw new IllegalStateException("eaters facility is already launched");
        model = new EatingProcessModel(candies, candyEaters);
        model.startAsync();
        launched = true;
        System.out.println("facility is launched");
    }

    @Override
    public synchronized void shutdown() {
        if(!launched)
            throw new IllegalStateException("eaters facility is not launched");
        launched = false;
        model.shutdownSync();
    }
}
