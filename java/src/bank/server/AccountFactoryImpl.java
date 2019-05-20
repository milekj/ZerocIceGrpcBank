package bank.server;

import bank.*;
import com.zeroc.Ice.*;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class AccountFactoryImpl implements AccountFactory {
    private final static int PASSWORD_LENGTH = 16;
    private static SecureRandom random = new SecureRandom();
    private LoanManager loanManager;
    private int monthlyIncomeThreshold;

    public AccountFactoryImpl(LoanManager loanManager, int monthlyIncomeThreshold) {
        this.loanManager = loanManager;
        this.monthlyIncomeThreshold = monthlyIncomeThreshold;
    }

    @Override
    public AccountCreationInfo create(String pesel, FullName fullName, int monthlyIncome, Current current) throws PeselAlreadyUsedException {
        if (peselAlreadyUsed(pesel, current.adapter)) {
            System.out.format("Cannot create account: pesel %s already used\n", pesel);
            throw new PeselAlreadyUsedException();
        }

        String password = generatePassword();
        AccountType accountType;
        Account account;
        if (monthlyIncome < monthlyIncomeThreshold) {
            accountType = AccountType.STANDARD;
            account = new AccountImpl(password);
        }
        else {
            accountType = AccountType.PREMIUM;
            account = new PremiumAccountImpl(password, loanManager);
        }
        try {
            Identity identity = new Identity(pesel, accountType.toString());
            ObjectPrx objectPrx = current.adapter.add(account, identity);
            AccountPrx accountPrx = AccountPrx.checkedCast(objectPrx);
            System.out.format("Created new %s account for: pesel %s\n", accountType, pesel);
            return new AccountCreationInfo(accountPrx, password, accountType);
        } catch (AlreadyRegisteredException e) {
            throw new PeselAlreadyUsedException();
        }
    }

    @Override
    public AccountPrx get(String pesel, Current current) throws AccountNotFoundException, InvalidCredentialsException {
        if (!peselAlreadyUsed(pesel, current.adapter)) {
            System.out.format("Cannot find account for pesel %s\n", pesel);
            throw new AccountNotFoundException();
        }
        String password = current.ctx.get("password");
        Identity identity = new Identity(pesel, AccountType.STANDARD.toString());
        AccountImpl account = (AccountImpl) current.adapter.find(identity);
        if (account == null) {
            identity = new Identity(pesel, AccountType.PREMIUM.toString());
            account = (AccountImpl) current.adapter.find(identity);
        }
        account.throwExceptionIfCredentialsAreInvalid(pesel, password, pesel);
        ObjectPrx objectPrx = current.adapter.createProxy(identity);
        System.out.format("Authorized account for: pesel %s\n", pesel);
        return AccountPrx.checkedCast(objectPrx);
    }

    private boolean peselAlreadyUsed(String pesel, ObjectAdapter adapter) {
        Identity standardIdentity = new Identity(pesel, AccountType.STANDARD.toString());
        Identity premiumIdentity = new Identity(pesel, AccountType.PREMIUM.toString());
        return adapter.find(standardIdentity) != null || adapter.find(premiumIdentity) != null;
    }

    private static String generatePassword() {
        byte[] letters = new byte[PASSWORD_LENGTH];
        random.nextBytes(letters);
        for (int i = 0; i < letters.length; i++) {
            int absByte = Math.abs(letters[i]) + 1;
            byte potentialLetter = (byte) (absByte % 92 + 32);
            letters[i] = (potentialLetter < 0) ? 0 : potentialLetter;
        }
        return new String(letters, StandardCharsets.US_ASCII);
    }
}
