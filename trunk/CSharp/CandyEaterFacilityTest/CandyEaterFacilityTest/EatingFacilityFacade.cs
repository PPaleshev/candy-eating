using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using System.Threading;

namespace CandyEaterFacilityTest
{
    public class EatingFacilityFacade : ICandyEatingFacility 
    {
        private volatile bool launched;

        private readonly Func<HashSet<ICandyEater>, ICandyEatingProcessModel> modelFactory;

        private ICandyEatingProcessModel model;

        private Thread dispatcherThread;

        private BlockingCollection<ICandy> inputBlockingCandies;

        private ConcurrentQueue<ICandy> inputQueueCandies;

        public EatingFacilityFacade(Func<HashSet<ICandyEater>, ICandyEatingProcessModel> modelFactory)
        {
            this.modelFactory = modelFactory;
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public void Launch(BlockingCollection<ICandy> candies, HashSet<ICandyEater> candyEaters) 
        {
            if(launched)
                throw new InvalidOperationException("Eating facility is already launched");
            inputBlockingCandies = candies;
            LaunchInner(candyEaters, DispatchBlockingWork);
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public void Launch(ConcurrentQueue<ICandy> candies, HashSet<ICandyEater> candyEaters)
        {
            if (launched)
                throw new InvalidOperationException("Eating facility is already launched");
            inputQueueCandies = candies;
            LaunchInner(candyEaters, DispatchQueueWork);
        }

        private void LaunchInner(HashSet<ICandyEater> candyEaters, ThreadStart dispatchAction)
        {
            model = modelFactory(candyEaters);
            launched = true;
            dispatcherThread = new Thread(dispatchAction) { Name = "CandyDispatchThread", IsBackground = true };
            dispatcherThread.Start();
            Console.WriteLine("Facility is launched");
        }

        private void DispatchBlockingWork()
        {
            Console.Out.WriteLine("Eating facility started dispatching thread (BlockingQueue)");
            foreach (var candy in inputBlockingCandies.GetConsumingEnumerable())
            {
                if (!launched)
                    return;
                model.PutNextCandy(candy);
            }
            //while (launched)
            //{
            //    ICandy candy;
            //    inputBlockingCandies.TryTake(out candy, 100);
            //    if (candy == null)
            //        continue;
            //    model.PutNextCandy(candy);
            //}
        }

        private void DispatchQueueWork()
        {
            Console.Out.WriteLine("Eating facility started dispatching thread (ConcurrentQueue)");
            while (launched)
            {
                ICandy candy;
                if (!inputQueueCandies.TryDequeue(out candy))
                {
                    Thread.Sleep(100);
                    continue;
                }
                if (candy == null)
                    continue;
                model.PutNextCandy(candy);
            }
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public void Shutdown() 
        {
            if(!launched)
                throw new InvalidOperationException("Eating facility is not launched");
            launched = false;
            dispatcherThread.Join();
            model.ShutdownSync();
        }
    }
}
