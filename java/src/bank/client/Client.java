package bank.client;
// **********************************************************************
//
// Copyright (c) 2003-2016 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

import bank.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;
import com.zeroc.Ice.ObjectPrx;

public class Client 
{

	public static void main(String[] args) 
	{
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter server port number: ");
		int portNumber = scanner.nextInt();

		int status = 0;
		Communicator communicator = null;

		try {
			// 1. Inicjalizacja ICE
			communicator = Util.initialize(args);

			// 2. Uzyskanie referencji obiektu na podstawie linii w pliku konfiguracyjnym
			//Ice.ObjectPrx base = communicator.propertyToProxy("Calc1.Proxy");
			// 2. To samo co powy¿ej, ale mniej ³adnie
			String configString = String.format("factory/factory:tcp -h localhost -p %d:udp -h localhost -p %d", portNumber, portNumber);
			ObjectPrx base = communicator.stringToProxy(configString);

			// 3. Rzutowanie, zawê¿anie
			AccountFactoryPrx obj = AccountFactoryPrx.checkedCast(base);
			if (obj == null) throw new Error("Invalid proxy");

			// 4. Wywolanie zdalnych operacji

			String pesel = "AAAA";
			AccountCreationInfo info = obj.create(pesel, new FullName("Jan", "Kowalski"), 8000);
			AccountPrx accountPrx = info.account;

			Map<String, String> context = new HashMap<>();
			context.put("password", info.password);

			System.out.format("%s %s %d\n", info.password, info.accountType, accountPrx.getBalance(pesel, context));

			accountPrx = obj.get(pesel, context);
			PremiumAccountPrx premiumAccountPrx = PremiumAccountPrx.checkedCast(accountPrx);
			if (premiumAccountPrx == null) {
				System.out.println(accountPrx.getBalance(pesel, context));
			}

			else {
				System.out.println(Arrays.toString(premiumAccountPrx.getSupportedCurrencies(pesel, context)));
				LoanInfo loanInfo = premiumAccountPrx.getLoanInfo(pesel, new LoanRequest(1000, Currency.USD, 2), context);
				System.out.println(loanInfo.givenCurrencyCost);
				System.out.println(loanInfo.naitveCurrencyCost);
			}

		} catch (Exception e) {
			e.printStackTrace();
			status = 1;
		}
		if (communicator != null) {
			// Clean up
			//
			try {
				communicator.destroy();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				status = 1;
			}
		}
		System.exit(status);
	}

}