package bank.server;
// **********************************************************************
//
// Copyright (c) 2003-2016 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

import bank.AccountFactory;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Identity;
import currency.Currency;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Server
{
	private int portNumber;
	private double interestRate;
	private int monthlyIncomeThreshold;
	private List<Currency> supportedCurrencies;
	private CurrencyClient currencyClient;
	private LoanManager loanManager;

	public Server(int portNumber, double interestRate, int monthlyIncomeThreshold, List<Currency> supportedCurrencies) {
		this.portNumber = portNumber;
		this.interestRate = interestRate;
		this.monthlyIncomeThreshold = monthlyIncomeThreshold;
		this.supportedCurrencies = supportedCurrencies;
	}

	public void run(String[] args)
	{
		int status = 0;
		Communicator communicator = null;

		try
		{
			initializeCurrencyServiceConnection();
			communicator = Util.initialize(args);
			String configString = String.format("tcp -h localhost -p %d:udp -h localhost -p %d", portNumber, portNumber);
			ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Adapter1", configString);
			AccountFactory accountFactory = new AccountFactoryImpl(loanManager, monthlyIncomeThreshold);
			adapter.add(accountFactory, new Identity("factory", "factory"));
			adapter.activate();
			System.out.println("Entering event processing loop...");
			communicator.waitForShutdown(); 		
			
		}
		catch (Exception e)
		{
			System.err.println(e);
			status = 1;
		}
		if (communicator != null)
		{
			// Clean up
			//
			try
			{
				communicator.destroy();
			}
			catch (Exception e)
			{
				System.err.println(e);
				status = 1;
			}
		}
		System.exit(status);
	}

	private void initializeCurrencyServiceConnection() {
		currencyClient = new CurrencyClient("localhost", 50051);
		loanManager = new LoanManager(supportedCurrencies, interestRate);
		new Thread(() -> currencyClient.run(loanManager)).start();
	}

	public static void main(String[] args)
	{
		Scanner scanner = new Scanner(System.in);
		scanner.useLocale(Locale.US);
		System.out.println("Enter port number: ");
		int portNumber = scanner.nextInt();
		System.out.println("Enter loan interest rate: ");
		double interestRate = scanner.nextDouble();
		System.out.println("Enter loan monthly income threshold: ");
		int monthlyIncomeThreshold = scanner.nextInt();
		System.out.println("Enter supported currencies (separated by spaces): ");
		scanner.nextLine();
		String currenciesInput = scanner.nextLine();
		List<String> currenciesNames = Arrays.asList(currenciesInput.split(" "));
		List<Currency> currencies = currenciesNames
				.stream()
				.map(Currency::valueOf)
				.collect(Collectors.toList());
		Server app = new Server(portNumber, interestRate, monthlyIncomeThreshold, currencies);
		app.run(args);
	}
}
