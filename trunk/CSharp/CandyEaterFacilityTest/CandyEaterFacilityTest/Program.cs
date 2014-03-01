using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.Threading;
using System.Threading.Tasks;

namespace CandyEaterFacilityTest
{
    class Program
    {
        private static volatile bool stopFlag;

        static void Main(string[] args)
        {
            Console.Out.WriteLine("Disruptor (d) or ProducerConsumer (p)?");
            var disruptorStr = Console.ReadLine();
            if (disruptorStr != "d" && disruptorStr != "p")
                return;
            bool disruptor = disruptorStr == "d";

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
            //const int candyCount = 20000000;
            const int candyCount = 10000000;
            const int capacity = 1024*1024;
            var candies = new ICandy[candyCount];
            Console.Out.WriteLine("Allocate memory for {0} candies...", candyCount);
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
            ICandyEatingFacility facility = disruptor ?
                new EatingFacilityFacade(candyEaters => model = new DisruptorCandyEatingFacility.CandyEatingProcessModel(candyEaters)) :
                new EatingFacilityFacade(candyEaters => model = new LockFreeProducerConsumerCandyEatingFacility.CandyEatingProcessModel(candyEaters));
            facility.Launch(inputQueue, eaters);

            Console.Out.WriteLine("Start measure time");
            var timer = Stopwatch.StartNew();

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
                    if (i % 500000 == 0)
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
                    Console.Out.WriteLine("-- InputQueueCount = {0} Pending candies: {1}.  ", inputQueue.Count, model.PendingCandies.Get());
                }
            });

            Console.WriteLine("Press any key to shutdown...");
            Console.ReadLine();
            stopFlag = true;

            Console.WriteLine("Shutdown begin... InputQueueCount = {0} Pending candies: {1}", inputQueue.Count, model.PendingCandies.Get());

            facility.Shutdown();

            Console.WriteLine("Shutdown completed!... InputQueueCount = {0} Pending candies: {1}", inputQueue.Count, model.PendingCandies.Get());
            Console.Out.WriteLine("Time elapsed: {0}", timer.Elapsed);
            timer.Stop();
            Console.ReadLine();
        }
    }
}
