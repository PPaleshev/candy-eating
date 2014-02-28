using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Globalization;
using System.Threading;
using System.Threading.Tasks;
using CandyEaterFacilityTest.DisruptorCandyEatingFacility;

namespace CandyEaterFacilityTest
{
    class Program
    {
        private static volatile bool stopFlag;

        static void Main(string[] args)
        {
            var flavours = new IFlavour[32];
            var flavoursSequences = new long[flavours.Length];
            for (int i = 0; i < flavours.Length; i++)
            {
                flavours[i] = new NamedFlavour(i.ToString(CultureInfo.InvariantCulture), 1);
                flavoursSequences[i] = 0;
                TrackingEater.FlavourConcurrencyCounters[flavours[i]] = new AtomicLong(0);
                TrackingEater.FlavourSequences[flavours[i]] = new AtomicLong(-1L);
            }

            var eaters = new HashSet<ICandyEater>();
            for (int i = 0; i < 16; i++)
                eaters.Add(new TrackingEater());

            var rnd = new Random(DateTime.Now.Second);
            const int candyCount = 20000000;
            const int capacity = 1024*1024;
            var candies = new ICandy[candyCount];
            for(long i = 0; i < candies.Length; i++) 
            {
                int index = rnd.Next(0, flavours.Length);
                candies[i] = new CandyTest(flavours[index], flavoursSequences[index]++);
            }
            //var initialCandies = new ICandy[capacity];
            //for (int i = 0; i < capacity; i++)
            //{
            //    initialCandies[i] = candies[i];
            //}
            //var candiesQueue = new ConcurrentQueue<ICandy>(initialCandies);
            var candiesQueue = new ConcurrentQueue<ICandy>();
            var inputQueue = new BlockingCollection<ICandy>(candiesQueue, int.MaxValue);
            ICandyEatingProcessModel model = null;
            ICandyEatingFacility facility = new EatingFacilityFacade(candyEaters => model = new DisruptorCandyCandyEatingProcessModel(candyEaters));
            facility.Launch(inputQueue, eaters);

            //Console.Out.WriteLine("Start producing candies... Initial candies count = {0}", inputQueue.Count);
            ////for (int i = capacity; i < candies.Length; i++)
            //for (int i = 0; i < candies.Length; i++)
            //{
            //    inputQueue.Add(candies[i]);
            //    if (i % 10000 == 0 && stopFlag)
            //        break;
            //    if (i % 200000 == 0)
            //        Console.Out.WriteLine("Candies produced: {0}. InputQueueCount = {1}", i, inputQueue.Count);
            //}
            //inputQueue.CompleteAdding();
            //Console.Out.WriteLine("End producing candies!");

            Task.Factory.StartNew(() =>
            {
                while (model == null)
                {
                    Thread.Sleep(10);
                }
                Console.Out.WriteLine("Start producing candies...");
                //for (int i = capacity; i < candies.Length; i++)
                for (int i = 0; i < candies.Length; i++)
                {
                    inputQueue.Add(candies[i]);
                    if (i % 10000 == 0 && stopFlag)
                        break;
                    if (i % 200000 == 0)
                        Console.Out.WriteLine("Candies produced: {0} | InputQueueCount = {1} | Pending candies: {2}", i + 1, inputQueue.Count, model.PendingCandies.Get());
                }
                inputQueue.CompleteAdding();
                Console.Out.WriteLine("End producing candies!");
            });

            Task.Factory.StartNew(() =>
            {
                while (model == null)
                {
                    Thread.Sleep(10);
                }
                while (!stopFlag)
                {
                    Thread.Sleep(1000);
                    Console.Out.WriteLine("Pending candies: {0}.  InputQueueCount = {1}", model.PendingCandies.Get(), inputQueue.Count);
                }
                //Console.Out.WriteLine("Done with input candies!");
            });

            Console.WriteLine("Press any key to shutdown...");
            Console.ReadLine();
            stopFlag = true;

            Console.WriteLine("Shutdown begin... Pending candies: {0} InputQueueCount = {1}", model.PendingCandies.Get(), inputQueue.Count);

            facility.Shutdown();

            Console.WriteLine("Shutdown completed!... Pending candies: {0} InputQueueCount = {1}", model.PendingCandies.Get(), inputQueue.Count);
            Console.ReadLine();
        }
    }
}
