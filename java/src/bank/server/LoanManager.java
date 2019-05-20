package bank.server;

import currency.Currency;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LoanManager {
    private Map<Currency, Double> rates;
    private double interestRate;

    public LoanManager(List<Currency> currencies, double interestRate) {
        this.interestRate = interestRate;
        rates = new ConcurrentHashMap<>();
        currencies.forEach(c -> rates.put(c, .0));
    }

    public void updateCurrency(Currency currency, double rate) {
        rates.put(currency, rate);
    }

    public Double getRate(Currency currency) {
        return rates.get(currency);
    }

    public Set<Currency> getSubscribedTo() {
        return rates.keySet();
    }

    public double getInterestRate() {
        return interestRate;
    }

    public boolean isInitialized() {
        for (double rate : rates.values())
            if (rate == 0.0)
                return false;
        return true;
    }
}
