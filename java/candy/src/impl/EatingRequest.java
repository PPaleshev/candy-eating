package impl;

import contracts.Candy;
import impl.producerconsumer.CandyEatingRequestCallback;

/**
 * Запрос на поедание конфеты с возможностью уведомления о завершении поедания.
 */
public class EatingRequest {
    private final Candy candy;
    private final CandyEatingRequestCallback callback;

    public Candy getCandy() {
        return candy;
    }

    public EatingRequest(Candy candy, CandyEatingRequestCallback callback) {
        this.candy = candy;
        this.callback = callback;
    }

    public void complete() throws InterruptedException {
        callback.complete();
    }
}
