package bank.server;

import bank.Account;
import bank.InvalidCredentialsException;
import com.zeroc.Ice.Current;

import java.util.Random;

public class AccountImpl implements Account {
    private static Random random = new Random();
    private String password;
    private int balance;

    public AccountImpl(String password) {
        this.password = password;
        this.balance = random.nextInt(10_000) + 1000;
    }

    @Override
    public int getBalance(String pesel, Current current) throws InvalidCredentialsException {
        String password = current.ctx.get("password");
        throwExceptionIfCredentialsAreInvalid(pesel, password, current.id.name);
        System.out.format("Returned account balance for: pesel %s\n", current.id.name);
        return balance;
    }

    public void throwExceptionIfCredentialsAreInvalid(String actualPesel, String passwordToCheck, String expectedPesel) throws InvalidCredentialsException {
        if (!expectedPesel.equals(actualPesel) || !password.equals(passwordToCheck)) {
            System.out.format("Acces denied for account: pesel %s\n", actualPesel);
            throw new InvalidCredentialsException();
        }

    }
}
