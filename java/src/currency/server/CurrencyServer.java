package currency.server;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;


public class CurrencyServer
{
	private static final Logger logger = Logger.getLogger(CurrencyServer.class.getName());

	private int port;
	private Server server;

	public CurrencyServer(int port) {
		this.port = port;
	}

	private void start() throws IOException, InterruptedException {
		CurrencyChangesSimulator currencyChangesSimulator = new CurrencyChangesSimulator();
		currencyChangesSimulator.startSimulation();
		CurrencyServiceImpl currencyService = new CurrencyServiceImpl(currencyChangesSimulator);
		server = ServerBuilder.forPort(port)
				.addService(currencyService)
				.build()
				.start();
		logger.info("Server started, listening on " + port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Use stderr here since the logger may have been reset by its JVM shutdown hook.
				System.err.println("*** shutting down gRPC currency.server since JVM is shutting down");
				CurrencyServer.this.stop();
				System.err.println("*** currency.server shut down");
			}
		});
	}

	private void stop() {
		if (server != null) {
			server.shutdown();
		}
	}

	/**
	 * Await termination on the main thread since the grpc library uses daemon threads.
	 */
	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	/**
	 * Main launches the currency.server from the command line.
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter port number: ");
		int portNumber = scanner.nextInt();
		final CurrencyServer server = new CurrencyServer(portNumber);
		server.start();
		server.blockUntilShutdown();
	}

}
