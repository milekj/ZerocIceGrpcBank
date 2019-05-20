#ifndef BANK_ICE
#define BANK_ICE

module bank {
    struct FullName {
        string firstName;
        string LastName;
    };

    enum AccountType {
        STANDARD,
        PREMIUM
    };

    enum Currency {
      CHF,
      USD,
      GBP,
      EUR
    };

    exception PeselAlreadyUsedException {
    };

    exception InvalidCredentialsException {
    };

    exception AccountNotFoundException {
    };

    exception CurrencyNotSupportedException {
    };


    interface Account {
        int getBalance(string pesel) throws InvalidCredentialsException;
    };

    struct LoanRequest {
        int amount;
        Currency currency;
        int years;
    };

    struct LoanInfo {
        int givenCurrencyCost;
        int naitveCurrencyCost;
    };


    sequence<Currency> currencies;

    interface PremiumAccount extends Account {
        currencies getSupportedCurrencies(string pesel) throws InvalidCredentialsException;
        LoanInfo getLoanInfo(string pesel, LoanRequest loanRequest) throws InvalidCredentialsException, CurrencyNotSupportedException;
    };

    struct AccountCreationInfo {
        Account* account;
        string password;
        AccountType accountType;

    };

    interface AccountFactory {
            AccountCreationInfo create(string pesel, FullName fullName, int monthlyIncome) throws PeselAlreadyUsedException;
            Account* get(string pesel) throws AccountNotFoundException, InvalidCredentialsException;
    };



};

#endif
