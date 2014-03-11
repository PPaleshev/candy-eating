package impl.disruption;

import com.lmax.disruptor.EventFactory;
import contracts.Candy;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Ячейка кольцевого буфера.
 */
public class CandyEntry {
    /**
     * Экземпляр фабрики ячеек буфера.
     */
    public static final EventFactory<CandyEntry> FACTORY = new Factory();

    /**
     * Конфета.
     */
    public AtomicReference<Candy> candy = new AtomicReference<>();

    public AtomicInteger warmup;

    /**
     * Фабрика ячеек.
     */
    private static class Factory implements EventFactory<CandyEntry> {
        @Override
        public CandyEntry newInstance() {
            return new CandyEntry();
        }
    }
}
