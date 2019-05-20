package bank.server;

import bank.*;
import com.zeroc.Ice.Current;
import currency.Currency;

import java.util.Collection;
import java.util.Set;


public class PremiumAccountImpl extends AccountImpl implements PremiumAccount {
    private LoanManager loanManager;

    public PremiumAccountImpl(String password, LoanManager loanManager) {
        super(password);
        this.loanManager = loanManager;
    }

    @Override
    public bank.Currency[] getSupportedCurrencies(String pesel, Current current) throws InvalidCredentialsException {
        throwExceptionIfCredentialsAreInvalid(pesel, current.ctx.get("password"), current.id.name);
        Set<Currency> iceCurrencies = loanManager.getSubscribedTo();
        Collection<bank.Currency> grpcCurrencies = CurrencyEnumsConverter.grpcToIce(iceCurrencies);
        System.out.format("Returned supported currencies for account: pesel %s\n", pesel);
        return grpcCurrencies.toArray(new bank.Currency[0]);
    }

    @Override
    public LoanInfo getLoanInfo(String pesel, LoanRequest loanRequest, Current current) throws CurrencyNotSupportedException, InvalidCredentialsException {
        throwExceptionIfCredentialsAreInvalid(pesel, current.ctx.get("password"), current.id.name);
        currency.Currency grpcCurrency = CurrencyEnumsConverter.iceToGrpc(loanRequest.currency);
        Double currencyRate = loanManager.getRate(grpcCurrency);
        if (currencyRate == null) {
            System.out.format("Cannot return loan info: currency %s not supported %s\n", loanRequest.currency);
            throw new CurrencyNotSupportedException();
        }
        int givenCurrencyCost = (int) (loanRequest.amount * (1 + loanManager.getInterestRate()) * loanRequest.years);
        int nativeCurrencyCost = (int) (givenCurrencyCost * currencyRate);
        System.out.format("Returned loan info for account: pesel %s\n", pesel);
        return new LoanInfo(givenCurrencyCost, nativeCurrencyCost);
    }
}
