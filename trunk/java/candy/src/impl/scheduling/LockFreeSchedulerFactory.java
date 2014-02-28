package impl.scheduling;

import contracts.Flavour;
import contracts.FlavourScheduler;
import contracts.SchedulerFactory;
import impl.EatingRequest;

import java.util.Map;
import java.util.Queue;

/**
 * Фабрика, создающая неблокирующие планировщики поедания конфет.
 */
public class LockFreeSchedulerFactory implements SchedulerFactory {
    /**
     * Уровень параллелизма по умолчанию.
     */
    final int defaultDegreeOfParallelism;

    /**
     * Отображение из вкуса в уровень параллелизма, используемого при обработке этого вкуса
     */
    Map<Flavour, Integer> degreeByFlavourMap;

    /**
     * Создаёт новый экземпляр фабрики, создающий планировщики, которые позволяют поедать только одну конфету
     * определённого вкуса в текущий момент времени.
     */
    public LockFreeSchedulerFactory() {
        this(null, 1);
    }

    /**
     * Создаёт новый экземпляр фабрики, создающий экземпляры планировщиков с настраиваемыми уровнями параллелизма
     * для каждого вкуса.
     * @param defaultDegreeOfParallelism уровень параллелизма по умолчанию.
     * @param degreeByFlavourMap отображение из вкуса в уровень параллелизма, используемого при обработке этого вкуса.
     */
    public LockFreeSchedulerFactory(Map<Flavour, Integer> degreeByFlavourMap, int defaultDegreeOfParallelism) {
        this.defaultDegreeOfParallelism = defaultDegreeOfParallelism;
        this.degreeByFlavourMap = degreeByFlavourMap;
    }

    @Override
    public FlavourScheduler create(Flavour flavour, Queue<EatingRequest> requestQueue) {
        int degree = defaultDegreeOfParallelism;
        if(degreeByFlavourMap != null){
            Integer setting = degreeByFlavourMap.get(flavour);
            if(setting != null)
                degree = setting;
        }
        return new NonBlockingFlavourScheduler(requestQueue, degree);
    }
}
