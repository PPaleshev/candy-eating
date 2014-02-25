using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Runtime.CompilerServices;

namespace CandyEaterFacilityTest
{
    public class EatingFacilityFacade : ICandyEatingFacility 
    {
        private volatile bool launched;

        private readonly Func<BlockingCollection<ICandy>, HashSet<ICandyEater>, IEatingProcessModel> modelFactory;

        private IEatingProcessModel model;

        public EatingFacilityFacade(Func<BlockingCollection<ICandy>, HashSet<ICandyEater>, IEatingProcessModel> modelFactory)
        {
            this.modelFactory = modelFactory;
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public void Launch(BlockingCollection<ICandy> candies, HashSet<ICandyEater> candyEaters) 
        {
            if(launched)
                throw new InvalidOperationException("Eating facility is already launched");
            model = modelFactory(candies, candyEaters);
            model.StartAsync();
            launched = true;
            Console.WriteLine("Facility is launched");
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public void Shutdown() 
        {
            if(!launched)
                throw new InvalidOperationException("Eating facility is not launched");
            launched = false;
            model.ShutdownSync();
        }
    }
}
