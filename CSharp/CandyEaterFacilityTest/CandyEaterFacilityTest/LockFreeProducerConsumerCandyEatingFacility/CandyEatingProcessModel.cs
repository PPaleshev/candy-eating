using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Threading;

namespace CandyEaterFacilityTest.LockFreeProducerConsumerCandyEatingFacility
{
    public class CandyEatingProcessModel : ICandyEatingProcessModel
    {
        private readonly ConcurrentQueue<ICandyEater> candyEaters;
        private readonly AtomicLong pendingCandies;
        private readonly ConcurrentDictionary<IFlavour, FlavourProducerConsumerModel> modelMap;
        private readonly ConcurrentQueue<EatingRequest> requestQueue;
        private volatile Thread requestQueueThread;
        private volatile bool shutdownFlag;
        
        public CandyEatingProcessModel(IEnumerable<ICandyEater> candyEaters)
        {
            this.candyEaters = new ConcurrentQueue<ICandyEater>(candyEaters);
            pendingCandies = new AtomicLong(0);
            modelMap = new ConcurrentDictionary<IFlavour, FlavourProducerConsumerModel>();
            requestQueue = new ConcurrentQueue<EatingRequest>();
            requestQueueThread = new Thread(RequestQueueDispatcherWork) { Name = "RequestQueueDispatcherWork", IsBackground = true };
            requestQueueThread.Start();
        }

        private void RequestQueueDispatcherWork()
        {
            while (!shutdownFlag)
                TryProcessOne();

            while (pendingCandies.Get() > 0)
                TryProcessOne();
        }

        private void TryProcessOne()
        {
            EatingRequest request = null;
            for (int i = 0; i < 10; i++)
            {
                if (!requestQueue.TryDequeue(out request))
                    Thread.Yield();
            }
            if (request == null)
                return;
            ICandyEater candyEater = null;
            while (candyEater == null)
            {
                if (!candyEaters.TryDequeue(out candyEater))
                    Thread.Yield();
            }
            //ThreadPool.UnsafeQueueUserWorkItem(ThreadPoolWork, new CandyEatingTask(request, candyEater));
            ThreadPool.QueueUserWorkItem(ThreadPoolWork, new CandyEatingTask(request, candyEater));
        }

        private void ThreadPoolWork(object state)
        {
            var task = (CandyEatingTask) state;
            task.CandyEater.Eat(task.Request.Candy);
            candyEaters.Enqueue(task.CandyEater);
            task.Request.Callback.Callback();
            pendingCandies.DecrementAndGet();
        }

        public void PutNextCandy(ICandy candy)
        {
            pendingCandies.IncrementAndGet();
            var flavour = candy.GetFlavour();
            var concurrency = flavour.GetConcurrencyLevel();
            var producerConsumer = modelMap.GetOrAdd(flavour, key => new FlavourProducerConsumerModel(requestQueue, concurrency));
            producerConsumer.Enqueue(candy);
        }

        public void ShutdownSync()
        {
            shutdownFlag = true;
            requestQueueThread.Join();
        }

        public AtomicLong PendingCandies
        {
            get { return pendingCandies; }
        }
    }
}
