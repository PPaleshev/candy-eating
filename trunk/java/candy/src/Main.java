import contracts.*;
import impl.EaterFacility;
import impl.FlavouredCandy;
import impl.IdentityFlavour;
import impl.producerconsumer.EatingProcessModel;
import impl.scheduling.LockFreeSchedulerFactory;
import test.TrackingEater;

import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) {
        Flavour[] flavours = new Flavour[32];
        for (int i = 0; i < 32; i++) {
            flavours[i] = new IdentityFlavour(i);
            TrackingEater.COUNTERS.put(flavours[i], new AtomicInteger());
            TrackingEater.SEQUENCES.put(flavours[i], -1L);
        }

        HashSet<CandyEater> eaters = new HashSet<>();
        for (int i = 0; i < 7; i++)
            eaters.add(new TrackingEater());

        Random rnd = new Random(System.currentTimeMillis());

        BlockingQueue<Candy> inputQueue = new ArrayBlockingQueue<>(1000000);
        SchedulerFactory schedulerFactory = new LockFreeSchedulerFactory(null, 4);
        CandyEatingFacilityStrategy strategy = new EatingProcessModel(inputQueue, eaters, schedulerFactory);
//        CandyEatingFacilityStrategy strategy = new DisruptionProcessModel(inputQueue, eaters);
        EaterFacility facility = new EaterFacility(strategy);

        //wait for start.
        System.out.println("Press any key to start");
        new Scanner(System.in).nextLine();

        System.out.println("Allocating candies... ");
        Candy[] candies = new Candy[1000000];
        for (int i = 0; i < candies.length; i++)
            candies[i] = new FlavouredCandy(flavours[rnd.nextInt(32)], i);
        System.out.println("Allocated "+ candies.length+" candies");

        facility.launch(inputQueue, eaters);

        long nanos = System.nanoTime();
        try {
            for(int iteration = 0; iteration < 50; iteration++) {
                //System.out.println("Iteration "+(iteration+1));
                for (Candy candy : candies)
                    inputQueue.put(candy);
            }
        } catch (InterruptedException e){
            System.out.println("Interrupt");
        }

        long current = System.nanoTime();
        System.out.println("all candies are enqueued in " + time(current-nanos));

        while (strategy.getPendingCandies() > 0) {
            try {
                Thread.sleep(1000);
                System.out.println("Pending requests: " + strategy.getPendingCandies());
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
