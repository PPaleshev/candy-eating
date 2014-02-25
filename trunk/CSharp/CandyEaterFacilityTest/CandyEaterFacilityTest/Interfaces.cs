using System.Collections.Concurrent;
using System.Collections.Generic;

namespace CandyEaterFacilityTest
{
    public interface IFlavour
    {
        /// <summary>
        /// How many candies of the same flavour is possible to eat simultaneously (default: 1) 
        /// </summary>
        int GetConcurrencyLevel();
    }

    public interface ICandy
    {
        IFlavour GetFlavour();

        /// <summary>
        /// Test purpose only
        /// </summary>
        long GetFlavourSequenceNumber();
    }

    public interface ICandyEater 
    {
        void Eat(ICandy candy);
    }
    
    public interface ICandyEatingFacility
    {
        void Launch(BlockingCollection<ICandy> candies, HashSet<ICandyEater> candyEaters);

        void Shutdown();
    }

}
