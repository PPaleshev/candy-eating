package impl;

import contracts.CandyEater;
import impl.producerconsumer.CandyEatingCallback;

/**
 * Задача поедания конфеты некоторым поедателем.
 */
public class CandyEatingTask implements Runnable {
    private final EatingRequest request;
    private final CandyEater eater;
    private final CandyEatingCallback callback;

    public EatingRequest getRequest() {
        return request;
    }

    public CandyEater getEater() {
        return eater;
    }

    public CandyEatingTask(EatingRequest request, CandyEater eater, CandyEatingCallback callback) {
        this.request = request;
        this.eater = eater;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            eater.eat(request.getCandy());
        }catch (Exception e){
            System.out.println(e.toString());
        }

        try {
            callback.complete(this);
        } catch (InterruptedException e){
            System.out.println("STOP INTERRUPT ME!!11");
            Thread.currentThread().interrupt();
        }
    }
}
