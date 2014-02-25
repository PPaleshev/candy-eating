package impl;

import contracts.Candy;
import contracts.CandyEater;

/**
 * Запрос на поедание конфеты с возможностью уведомления о завершении поедания.
 */
public class EatingRequest {
    private final Candy candy;
    private final ICandyEatingRequestCallback callback;

    public Candy getCandy() {
        return candy;
    }

    public EatingRequest(Candy candy, ICandyEatingRequestCallback callback) {
        this.candy = candy;
        this.callback = callback;
    }

    public void complete() throws InterruptedException {
        callback.complete();
    }
}
