using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Disruptor;
using Disruptor.Dsl;

namespace CandyEaterFacilityTest.DisruptorCandyEatingFacility
{
    public class CandyEatingProcessModel : ICandyEatingProcessModel
    {
        private const int RingSize = 256*1024; // Must be multiple of 2
        private readonly AtomicLong pendingCandies;
        private readonly Disruptor<RingBufferValueEntry> disruptor;
        private readonly RingBuffer<RingBufferValueEntry> ringBuffer;
        private readonly AtomicLong heatingCounter;
        private readonly IEventHandler<RingBufferValueEntry>[] handlers;
        
        public CandyEatingProcessModel(HashSet<ICandyEater> candyEaters)
        {
            heatingCounter = new AtomicLong(0);
            pendingCandies = new AtomicLong(0);
            disruptor = new Disruptor<RingBufferValueEntry>(() => new RingBufferValueEntry(), new SingleThreadedClaimStrategy(RingSize), new SleepingWaitStrategy(), TaskScheduler.Default);
            //disruptor = new Disruptor<RingBufferValueEntry>(() => new RingBufferValueEntry(), new SingleThreadedClaimStrategy(RingSize), new BlockingWaitStrategy(), TaskScheduler.Default);
            //disruptor = new Disruptor<RingBufferValueEntry>(() => new RingBufferValueEntry(), RingSize, TaskScheduler.Default);
            handlers = new IEventHandler<RingBufferValueEntry>[candyEaters.Count];
            int index = 0;
            foreach (var candyEater in candyEaters)
            {
                handlers[index] = new CandyHandler(index, candyEaters.Count, candyEater, CallBack);
                index++;
            }
            disruptor.HandleEventsWith(handlers);
            ringBuffer = disruptor.Start();
            Heat();
        }

        /// <summary>
        /// Hack
        /// </summary>
        private void Heat()
        {
            Console.Out.WriteLine("Disruptor start heating....");
            for (int i = 0; i < RingSize; i++)
            {
                long sequenceNo = ringBuffer.Next();
                var entry = ringBuffer[sequenceNo];
                entry.HeatingCounter = heatingCounter;
                heatingCounter.Add(handlers.Length);
                ringBuffer.Publish(sequenceNo);
            }
            while (heatingCounter.Get() > 0)
            {
                Thread.Sleep(0);
            }
            for (int i = 0; i < RingSize; i++)
            {
                ringBuffer[i].HeatingCounter = null;
            }
            Console.Out.WriteLine("Disruptor is ready to work!");
        }

        public void PutNextCandy(ICandy candy)
        {
            pendingCandies.IncrementAndGet();
            long sequenceNo = ringBuffer.Next();
            var entry = ringBuffer[sequenceNo];
            entry.Candy = candy;
            ringBuffer.Publish(sequenceNo);
        }

        public void ShutdownSync()
        {
            while (pendingCandies.Get() > 0)
            {
                Thread.Sleep(0);
            }
            disruptor.Shutdown();
        }

        public AtomicLong PendingCandies
        {
            get { return pendingCandies; }
        }

        private void CallBack()
        {
            pendingCandies.DecrementAndGet();
        }
    }
}
