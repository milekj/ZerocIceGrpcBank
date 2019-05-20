package currency.server;


import currency.Currency;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CurrencyChangesSimulator {
    private final static double CHANGE_FACTOR = 0.05;
    private final static long MILLIS_BETWEEN_UPDATES = 5000;
    private Map<Currency, Double> currencyRates;
    private List<CurrencySubscription> subscriptions;
    private Random random;


    public CurrencyChangesSimulator() {
        subscriptions = Collections.synchronizedList(new LinkedList<>());
        currencyRates = new ConcurrentHashMap<>();
        currencyRates.put(Currency.CHF, 3.75);
        currencyRates.put(Currency.USD, 3.8);
        currencyRates.put(Currency.GBP, 4.97);
        currencyRates.put(Currency.EUR, 4.28);
        random = new Random();
    }

    public void startSimulation() throws InterruptedException {
        Runnable simulationRunnable = newSimulationRunnable();
        Thread thread = new Thread(simulationRunnable);
        thread.start();
    }

    public void addSubscription(CurrencySubscription subscription) {
        subscriptions.add(subscription);
        subscription.sendResponseForAllSubscribedTo(currencyRates);
    }

    private Runnable newSimulationRunnable() {
        return () -> {
            while(true) {
                try {
                    Thread.sleep(MILLIS_BETWEEN_UPDATES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                changeRatesAndUpdate();
            }};
    }

    private void changeRatesAndUpdate() {
        currencyRates.replaceAll((c, r) -> {
            boolean increase = random.nextBoolean();
            if (increase)
                r = r * (1 + CHANGE_FACTOR);
            else
                r = r * (1 - CHANGE_FACTOR);
            r = new BigDecimal(r).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (r <= 0.0)
                r = 0.01;
            System.out.format("Currency rate changed: %s %.2f\n", c, r);
            sendUpdates(c, r);
            return r;
        });
        System.out.println();
    }

    private void sendUpdates(Currency currency, double rate) {
        List<CurrencySubscription> subscriptionsToRemove = new LinkedList<>();
        subscriptions.forEach(s -> {
            try {
                s.sendResponseIfSubscribedTo(currency, rate);
            } catch (Exception e) {
                subscriptionsToRemove.add(s);
            }
        });
        subscriptions.removeAll(subscriptionsToRemove);
    }
}
