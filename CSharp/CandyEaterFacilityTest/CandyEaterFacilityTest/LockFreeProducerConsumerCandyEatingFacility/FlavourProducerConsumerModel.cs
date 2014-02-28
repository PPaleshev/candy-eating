using System.Collections.Concurrent;
using System.Threading;

namespace CandyEaterFacilityTest.LockFreeProducerConsumerCandyEatingFacility
{
    public class FlavourProducerConsumerModel : ICallback
    {
        private readonly ConcurrentQueue<EatingRequest> outputRequests;
        private readonly ConcurrentQueue<ICandy> oneFlavourCandies;
        /// <summary> 
        /// Auxiliary concurrency counter. Important! It can increase actual concurrency but it is OK. 
        /// All non-blocking synchronization based on this counter with help of interlocked anything pattern
        /// </summary>
        private long concurrencyCounter;
        /// <summary> Max dequeue concurrency level </summary>
        private readonly long maxConcurrencyLevel;

        public FlavourProducerConsumerModel(ConcurrentQueue<EatingRequest> outputRequests, int concurrency)
        {
            this.outputRequests = outputRequests;
            maxConcurrencyLevel = concurrency;
            oneFlavourCandies = new ConcurrentQueue<ICandy>();
        }

        public void Enqueue(ICandy candy)
        {
            oneFlavourCandies.Enqueue(candy);
            //note Interlocked Anything Pattern
            var reservedConcurrencyLevel = Interlocked.Increment(ref concurrencyCounter);
            long catchedConcurrency;
            do
            {
                if (reservedConcurrencyLevel <= maxConcurrencyLevel)
                {
                    ICandy nextCandy;
                    if (oneFlavourCandies.TryDequeue(out nextCandy))
                    {
                        outputRequests.Enqueue(new EatingRequest(nextCandy, this));
                        return;
                    }
                }
                long releasedConcurrencyLevel = reservedConcurrencyLevel - 1;
                catchedConcurrency = reservedConcurrencyLevel;
                reservedConcurrencyLevel = Interlocked.CompareExchange(ref concurrencyCounter, releasedConcurrencyLevel, catchedConcurrency);
            } while (catchedConcurrency != reservedConcurrencyLevel);
        }

        public void Callback()
        {
            //note: Interlocked Anything Pattern
            var currentConcurrency = Interlocked.Read(ref concurrencyCounter);
            long catchedConcurrency;
            do
            {
                ICandy nextCandy;
                if (oneFlavourCandies.TryDequeue(out nextCandy))
                {
                    outputRequests.Enqueue(new EatingRequest(nextCandy, this));
                    return;
                }
                long releasedConcurrencyLevel = currentConcurrency - 1;
                catchedConcurrency = currentConcurrency;
                currentConcurrency = Interlocked.CompareExchange(ref concurrencyCounter, releasedConcurrencyLevel, catchedConcurrency);
            } while (catchedConcurrency != currentConcurrency);
        }
    }
}
