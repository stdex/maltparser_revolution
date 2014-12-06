/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maltparser.grizzly;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.http.HttpClientFilter;
import org.glassfish.grizzly.impl.FutureImpl;
import org.glassfish.grizzly.impl.SafeFutureImpl;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;

public class Client {

        public static final Logger logger = Logger.getLogger(Client.class.getName());
    
        public static final String HOST = "localhost";
        public static final int PORT = 8080;

        public static void main(String[] args) throws IOException {
           Client client = new Client();
           client.run();
        }
        
        public void run() throws IOException {
            final FutureImpl<String> completeFuture = SafeFutureImpl.create();

            // Build HTTP client filter chain
            FilterChainBuilder clientFilterChainBuilder = FilterChainBuilder.stateless();
            // Add transport filter
            clientFilterChainBuilder.add(new TransportFilter());

            // Add HttpClientFilter, which transforms Buffer <-> HttpContent
            clientFilterChainBuilder.add(new HttpClientFilter());
            // Add ClientFilter
            clientFilterChainBuilder.add(new ClientFilter(completeFuture));


            // Initialize Transport
            final TCPNIOTransport transport =
                    TCPNIOTransportBuilder.newInstance().build();
            // Set filterchain as a Transport Processor
            transport.setProcessor(clientFilterChainBuilder.build());

            try {
                // start the transport
                transport.start();

                Connection connection = null;

                // Connecting to a remote Web server
                Future<Connection> connectFuture = transport.connect(HOST, PORT);
                try {
                    // Wait until the client connect operation will be completed
                    // Once connection has been established, the POST will
                    // be sent to the server.
                    connection = connectFuture.get(10, TimeUnit.SECONDS);

                    // Wait no longer than 30 seconds for the response from the
                    // server to be complete.
                    String result = completeFuture.get(30, TimeUnit.SECONDS);

                    // Display the echoed content
                    System.out.println("\nEchoed POST Data: " + result + '\n');
                } catch (Exception e) {
                    if (connection == null) {
                        logger.log(Level.WARNING, "Connection failed.  Server is not listening.");
                    } else {
                        logger.log(Level.WARNING, "Unexpected error communicating with the server.");
                    }
                } finally {
                    // Close the client connection
                    if (connection != null) {
                        connection.closeSilently();
                    }
                }
            } finally {
                // stop the transport
                transport.shutdownNow();
            }
        }
}