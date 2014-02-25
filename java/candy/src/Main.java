import contracts.Candy;
import contracts.CandyEater;
import contracts.Flavour;
import impl.*;
import org.omg.CORBA.Environment;
import test.TrackingEater;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) {
        Flavour[] flavours = new Flavour[32];
        for (int i = 0; i < 32; i++) {
            flavours[i] = new NamedFlavour(String.valueOf(i));
            TrackingEater.COUNTERS.put(flavours[i], new AtomicInteger());
            TrackingEater.SEQUENCES.put(flavours[i], -1L);
        }

        HashSet<CandyEater> eaters = new HashSet<CandyEater>();
        for (int i = 0; i < 16; i++)
            eaters.add(new TrackingEater());

        Random rnd = new Random(System.currentTimeMillis());

        BlockingQueue<Candy> inputQueue = new ArrayBlockingQueue<Candy>(1000000);
        EaterFacility facility = new EaterFacility();
        facility.launch(inputQueue, eaters);

        long nanos = System.nanoTime();

        for(long i = 0; i<50000000; i++) {
            int index = rnd.nextInt(32);
            try {
                inputQueue.put(new FlavouredCandy(flavours[index], i));
            } catch (InterruptedException e){
                System.out.println("Interrupt");
            }
        }

        long current = System.nanoTime();
        System.out.println("all candies are enqueued in "+ TimeUnit.SECONDS.convert(current-nanos, TimeUnit.NANOSECONDS));

        while (EatingProcessModel.pendingRequests.get() > 0) {
            try {
                Thread.sleep(1000);
                System.out.println("Pending requests: "+EatingProcessModel.pendingRequests.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        facility.shutdown();
        System.out.println("done in "+ TimeUnit.SECONDS.convert(System.nanoTime()-current, TimeUnit.NANOSECONDS));
    }
}
