import contracts.Candy;
import contracts.CandyEater;
import contracts.Flavour;
import contracts.SchedulerFactory;
import impl.EaterFacility;
import impl.EatingProcessModel;
import impl.FlavouredCandy;
import impl.NamedFlavour;
import impl.scheduling.BlockingSchedulerFactory;
import impl.scheduling.LockFreeSchedulerFactory;
import test.TrackingEater;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
        SchedulerFactory factory = new BlockingSchedulerFactory(null, 4);
        EaterFacility facility = new EaterFacility(factory);
        facility.launch(inputQueue, eaters);

        System.out.println("Allocating candies... ");
        Candy[] candies = new Candy[1000000];
        for (int i = 0; i < candies.length; i++)
            candies[i] = new FlavouredCandy(flavours[rnd.nextInt(32)], i);
        System.out.println("Allocated "+ candies.length+" candies");

        long nanos = System.nanoTime();
        try {
            for(int iteration = 0; iteration < 100; iteration++)
            for (Candy candy : candies) {
                inputQueue.put(candy);
            }
        } catch (InterruptedException e){
            System.out.println("Interrupt");
        }

        long current = System.nanoTime();
        System.out.println("all candies are enqueued in " + time(current-nanos));

        while (EatingProcessModel.pendingRequests.get() > 0) {
            try {
                Thread.sleep(1000);
                System.out.println("Pending requests: " + EatingProcessModel.pendingRequests.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        facility.shutdown();
        System.out.println("done in "+ time(System.nanoTime() - current));
    }

    static String time(long nanoseconds) {
        return String.valueOf((double) nanoseconds / 1000000000.0);
    }
}
