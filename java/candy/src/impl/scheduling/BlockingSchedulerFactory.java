package impl.scheduling;

import contracts.Flavour;
import contracts.FlavourScheduler;
import contracts.SchedulerFactory;
import impl.EatingRequest;

import java.util.Map;
import java.util.Queue;

/**
 * Фабрика создания планировщиков, использующих блокировки.
 */
public class BlockingSchedulerFactory implements SchedulerFactory {
    /**
     * Уровень параллелизма по умолчанию.
     */
    final int defaultDegreeOfParallelism;

    /**
     * Отображение из вкуса в уровень параллелизма, используемого при обработке этого вкуса
     */
    Map<Flavour, Integer> degreeByFlavourMap;

    public BlockingSchedulerFactory(Map<Flavour, Integer> degreeByFlavourMap, int defaultDegreeOfParallelism) {
        this.degreeByFlavourMap = degreeByFlavourMap;
        this.defaultDegreeOfParallelism = defaultDegreeOfParallelism;
    }

    public BlockingSchedulerFactory() {
        this(null, 1);
    }

    @Override
    public FlavourScheduler create(Flavour flavour, Queue<EatingRequest> requestQueue) {
        int degree = defaultDegreeOfParallelism;
        if (degreeByFlavourMap != null) {
            Integer value = degreeByFlavourMap.get(flavour);
            if (value != null)
                degree = value;
        }
        return new BlockingFlavourScheduler(degree, requestQueue);
    }
}
