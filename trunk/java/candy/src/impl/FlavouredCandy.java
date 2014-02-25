package impl;

import contracts.Candy;
import contracts.Flavour;

/**
 * Общая реализация конфеты с любым вкусом.
 */
public class FlavouredCandy implements Candy {
    /**
     * Вкус конфеты.
     */
    private final Flavour flavour;
    private final long sequence;

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
}
