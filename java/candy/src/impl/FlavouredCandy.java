package impl;

import contracts.Candy;
import contracts.Flavour;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Общая реализация конфеты с любым вкусом.
 */
public class FlavouredCandy implements Candy {
    /**
     * Вкус конфеты.
     */
    private final Flavour flavour;
    private final long sequence;
    AtomicInteger eaten = new AtomicInteger();

    public FlavouredCandy(Flavour flavour, long sequence) {
        this.flavour = flavour;
        this.sequence = sequence;
    }

    @Override
    public Flavour getFlavour() {
        return flavour;
    }

    @Override
    public long getSequenceNo() {
        return sequence;
    }

    @Override
    public void eatMe() {
        if (!eaten.compareAndSet(0, 1))
            System.out.println("already eaten");
    }
}
