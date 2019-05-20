/*
 * Copyright 2015, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package bank.server;

import currency.Currency;
import currency.CurrencyServiceGrpc;
import currency.SubscribeArguments;
import currency.SubscribeResult;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CurrencyClient {
	private static final Logger logger = Logger.getLogger(CurrencyClient.class.getName());

	private final ManagedChannel channel;
	private final CurrencyServiceGrpc.CurrencyServiceStub currencyStub;

	/** Construct currency.client connecting to HelloWorld currency.server at {@code host:port}. */
	public CurrencyClient(String host, int port)
	{
		channel = ManagedChannelBuilder.forAddress(host, port)
				// Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid needing certificates.
				.usePlaintext(true)
				.build();
		currencyStub = CurrencyServiceGrpc.newStub(channel);
	}

	public void shutdown() throws InterruptedException {
	    System.out.println("Shutting down...");
		Context.CancellableContext.current().withCancellation().cancel(new Exception());
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}


	public void run(LoanManager loanManager) {
        SubscribeArguments subscribeArguments = SubscribeArguments.newBuilder()
                .addAllSubscribedTo(loanManager.getSubscribedTo())
                .build();
        StreamObserverImpl streamObserver1 = new StreamObserverImpl(loanManager);
        currencyStub.subscribe(subscribeArguments, streamObserver1);
        try {
            while(!loanManager.isInitialized())
                Thread.sleep(200);
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

	public void stop() throws InterruptedException {
	    shutdown();
    }

    private static class StreamObserverImpl implements StreamObserver<SubscribeResult> {
	    private LoanManager loanManager;

        public StreamObserverImpl(LoanManager loanManager) {
            this.loanManager = loanManager;
        }

        @Override
        public void onNext(SubscribeResult subscribeResult) {
            Currency currency = subscribeResult.getCurrency();
            double rate = subscribeResult.getRate();
            System.out.format("\tCurrency rate changed: %s %.2f\n", currency, rate);
            loanManager.updateCurrency(currency, rate);
        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onCompleted() {

        }
    }
}
