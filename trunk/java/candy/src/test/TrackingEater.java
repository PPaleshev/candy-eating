package test;

import contracts.Candy;
import contracts.CandyEater;
import contracts.Flavour;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class TrackingEater implements CandyEater {
    public static HashMap<Flavour, AtomicInteger> COUNTERS = new HashMap<Flavour, AtomicInteger>();
    public static HashMap<Flavour, Long> SEQUENCES = new HashMap<Flavour, Long>();

    public static int MAX_PARALLEL_FLAVOURS = 10;

    volatile double value;

    @Override
    public void eat(Candy candy) {
        invariant(candy);
//        consumeCPU(Math.abs(candy.hashCode())%1000000);
    }

    private void consumeCPU(int count) {
        double s = 0;
        for(int i = 0 ; i< count; i++)
            s += Math.sin(i);
        value = s;
    }

    private void invariant(Candy candy) {
        //System.out.println("eating "+ candy.getFlavour().getId());
        AtomicInteger counter = COUNTERS.get(candy.getFlavour());
        if (counter.incrementAndGet() > MAX_PARALLEL_FLAVOURS)
            System.out.println("Flavour "+ candy.getFlavour().toString()+" violates maximum");
        if(MAX_PARALLEL_FLAVOURS == 1) {
            if(candy.getSequenceNo() <= SEQUENCES.get(candy.getFlavour()))
                System.out.println("Order violation from "+ SEQUENCES.get(candy.getFlavour()) + " to " + candy.getSequenceNo() + " - "+ candy.getFlavour());
            SEQUENCES.put(candy.getFlavour(), candy.getSequenceNo());
            candy.eatMe();
        }
        if(counter.decrementAndGet() < 0)
            System.out.println("Flavour"+ candy.getFlavour().toString()+" violates minimum");
    }
}
