using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Globalization;
using System.Threading;
using System.Threading.Tasks;

namespace CandyEaterFacilityTest
{
    class Program
    {
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
            var candies = new ICandy[50000000];
            for(long i = 0; i < candies.Length; i++) 
            {
                int index = rnd.Next(0, flavours.Length);
                candies[i] = new CandyTest(flavours[index], flavoursSequences[index]++);
            }
            var candiesQueue = new ConcurrentQueue<ICandy>(candies);
            var inputQueue = new BlockingCollection<ICandy>(candiesQueue);
            inputQueue.CompleteAdding();
            IEatingProcessModel model = null;
            ICandyEatingFacility facility = new EatingFacilityFacade((c, e) => model = (IEatingProcessModel) null);
            facility.Launch(inputQueue, eaters);

            Task.Factory.StartNew(() =>
            {
                while (model == null)
                {
                    Thread.Sleep(10);
                }
                while (model.PendingCandies.Get() > 0)
                {
                    Thread.Sleep(1000);
                    Console.Out.WriteLine("Pending candies: " + model.PendingCandies.Get());
                }
                Console.Out.WriteLine("Done with input candies!");
            });

            Console.WriteLine("Press any key to shutdown...");

            facility.Shutdown();

            Console.WriteLine("Shutdown completed!... Candies remained: {0} | Pending candies: {1}", inputQueue.Count, model.PendingCandies.Get());
            Console.ReadLine();
        }
    }
}
