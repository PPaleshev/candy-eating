package impl.disruption;

import com.lmax.disruptor.EventHandler;
import contracts.Candy;
import contracts.CandyEater;

/**
 * Обработчик ячейки кольцевого буффера.
 */
public class CandyEatingHandler implements EventHandler<CandyEntry> {
    /**
     * Индекс текущего обработчика.
     */
    private final int index;

    /**
     * Общее количество обработчиков.
     */
    private final int eaterCount;

    /**
     * Объект для выполнения обратных вызовов.
     */
    private final CandyHandlerCallback callback;

    /**
     * Поедатель, которым управляет текущий обработчик.
     */
    private final CandyEater eater;

    public CandyEatingHandler(int index, CandyEater eater, int eaterCount, CandyHandlerCallback callback) {
        this.index = index;
        this.eater = eater;
        this.eaterCount = eaterCount;
        this.callback = callback;
    }

    @Override
    public void onEvent(CandyEntry event, long sequence, boolean endOfBatch) throws Exception {
        if (event.warmup != null) {
            event.warmup.decrementAndGet();
            return;
        }
        Candy candy = event.candy.get();
        if (candy == null)
            return;
        int id = candy.getFlavour().getId();
        if (id%eaterCount != index)
            return;
        if (!event.candy.compareAndSet(candy, null))
            return;
        eater.eat(candy);
        callback.complete();
    }
}
