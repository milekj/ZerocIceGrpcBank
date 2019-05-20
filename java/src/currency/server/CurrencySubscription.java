package currency.server;

import currency.Currency;
import currency.SubscribeResult;
import io.grpc.stub.StreamObserver;

import java.util.Map;
import java.util.Set;

public class CurrencySubscription {
    private Set<Currency> subscribedTo;
    private StreamObserver<SubscribeResult> responseObserver;

    public CurrencySubscription(Set<Currency> subscribedTo, StreamObserver<SubscribeResult> responseObserver) {
        this.subscribedTo = subscribedTo;
        this.responseObserver = responseObserver;
    }

    public void sendResponseIfSubscribedTo(Currency currency, double rate) {
        if (subscribedTo.contains(currency)) {
            SubscribeResult result = newResult(currency, rate);
            responseObserver.onNext(result);
        }
    }

    public void sendResponseForAllSubscribedTo(Map<Currency, Double> currencyRates) {
        subscribedTo.forEach((c) -> {
            double rate = currencyRates.get(c);
            SubscribeResult result = newResult(c, rate);
            responseObserver.onNext(result);
        });
    }

    private SubscribeResult newResult(Currency currency, double rate) {
        return SubscribeResult.newBuilder()
                .setCurrency(currency)
                .setRate(rate)
                .build();
    }
}
