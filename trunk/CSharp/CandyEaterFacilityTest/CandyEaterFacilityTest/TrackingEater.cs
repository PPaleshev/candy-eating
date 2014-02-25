using System;
using System.Collections.Generic;

namespace CandyEaterFacilityTest
{
    public class TrackingEater : ICandyEater 
    {
        public static Dictionary<IFlavour, AtomicLong> FlavourConcurrencyCounters = new Dictionary<IFlavour, AtomicLong>();
        public static Dictionary<IFlavour, AtomicLong> FlavourSequences = new Dictionary<IFlavour, AtomicLong>();
        
        public void Eat(ICandy candy)
        {
            var flavour = candy.GetFlavour();
            AtomicLong counter = FlavourConcurrencyCounters[flavour];
            var maxParallelFlavours = flavour.GetConcurrencyLevel();
            if (counter.IncrementAndGet() > maxParallelFlavours)
                Console.Out.WriteLine("Flavour {0} violates maximum", flavour);
            if (maxParallelFlavours == 1)
            {
                var currentNumber = candy.GetFlavourSequenceNumber();
                var lastNumber = FlavourSequences[flavour].Get();
                if (currentNumber <= lastNumber)
                    Console.Out.WriteLine("Order violation from {0} to {1} flavour: {2}", lastNumber, currentNumber, flavour);
                FlavourSequences[flavour].Exchange(currentNumber);
            }
            if (counter.DecrementAndGet() < 0)
                Console.Out.WriteLine("Flavour {0} violates minimum", flavour);
        }
    }
}
