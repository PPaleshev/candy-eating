package test;


import contracts.Candy;
import contracts.Flavour;

public class CountedCandy implements Candy {
    private final long sequenceNo;
    private Flavour flavour;

    public CountedCandy(long sequenceNo, Flavour flavour) {
        this.sequenceNo = sequenceNo;
        this.flavour = flavour;
    }

    public long getSequenceNo() {
        return sequenceNo;
    }

    @Override
    public Flavour getFlavour() {
        return flavour;
    }
}
