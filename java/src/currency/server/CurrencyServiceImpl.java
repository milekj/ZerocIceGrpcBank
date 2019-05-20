package currency.server;

import currency.Currency;
import currency.CurrencyServiceGrpc;
import currency.SubscribeArguments;
import currency.SubscribeResult;
import io.grpc.stub.StreamObserver;

import java.util.HashSet;
import java.util.Set;

public class CurrencyServiceImpl extends CurrencyServiceGrpc.CurrencyServiceImplBase {
    private CurrencyChangesSimulator currencyChangesSimulator;

    public CurrencyServiceImpl(CurrencyChangesSimulator currencyChangesSimulator) {
        this.currencyChangesSimulator = currencyChangesSimulator;
    }

    @Override
    public void subscribe(SubscribeArguments request, StreamObserver<SubscribeResult> responseObserver) {
        Set<Currency> subscribedTo = new HashSet<>(request.getSubscribedToList());
        CurrencySubscription subscription = new CurrencySubscription(subscribedTo, responseObserver);
        currencyChangesSimulator.addSubscription(subscription);
    }
}
