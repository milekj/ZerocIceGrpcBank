package bank.server;


import currency.Currency;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class CurrencyEnumsConverter {
    public static currency.Currency iceToGrpc(bank.Currency c) {
        return currency.Currency.valueOf(c.name());
    }

    public static bank.Currency grpcToIce(currency.Currency c) {
        return bank.Currency.valueOf(c.name());
    }

    public static Collection<currency.Currency> iceToGrpc(Collection<bank.Currency> c) {
        return c
                .stream()
                .map(CurrencyEnumsConverter::iceToGrpc)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public static Collection<bank.Currency> grpcToIce(Collection<currency.Currency> c) {
        return c
                .stream()
                .map(CurrencyEnumsConverter::grpcToIce)
                .collect(Collectors.toCollection(LinkedList::new));
    }
}
