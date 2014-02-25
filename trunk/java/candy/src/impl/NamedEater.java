package impl;

import contracts.Candy;
import contracts.CandyEater;

/**
 * Реализация поедателя конфет.
 */
public class NamedEater implements CandyEater {
    /**
     * Имя поедателя.
     */
    final String name;

    public NamedEater(String name) {
        this.name = name;
    }

    @Override
    public void eat(Candy candy) {
        try {
            System.out.println(name+" eating "+candy.getFlavour().toString()+" candy!");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("thread interrupted");
        }
    }
}
