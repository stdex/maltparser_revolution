/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maltparser.grizzly;

import java.io.BufferedReader;
import java.io.File;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HeaderValue;
import org.glassfish.grizzly.impl.FutureImpl;
import org.glassfish.grizzly.memory.MemoryManager;

import java.io.IOException;
import java.io.InputStreamReader;
import org.glassfish.grizzly.memory.Buffers;

public class ClientFilter extends BaseFilter {
            private static final HeaderValue HOST_HEADER_VALUE =
                    HeaderValue.newHeaderValue(Client.HOST + ':' + Client.PORT).prepare();

            private static String[] CONTENT = {""};

            private FutureImpl<String> future;

            private StringBuilder sb = new StringBuilder();

            // ---------------------------------------------------- Constructors


            ClientFilter(FutureImpl<String> future) {
                this.future = future;                
            }


            // ----------------------------------------- Methods from BaseFilter


            @SuppressWarnings({"unchecked"})
            @Override
            public NextAction handleConnect(FilterChainContext ctx) throws IOException {
                System.out.println("\nClient connected!\n");

                HttpRequestPacket request = createRequest();
                System.out.println("Writing request:\n");
                System.out.println(request.toString());
                ctx.write(request); // write the request

                BufferedReader reader = new BufferedReader(new InputStreamReader(new File("/home/rostunov/workspace/neuro/malt_prj/malt/resource/data/inp_Lc_Rc_test.csv").toURI().toURL().openStream(), "UTF-8"));

                //String formatedString = IOUtils.toString(reader);
                String formatedString = "";
                String line = "";
                while ( (line = reader.readLine()) != null ) {
                    formatedString = formatedString + "\n" + line;
                }
                System.out.println(formatedString.trim());
                CONTENT[0] = formatedString.trim();
                
                // for each of the content parts in CONTENT, wrap in a Buffer,
                // create the HttpContent to wrap the buffer and write the
                // content.
                MemoryManager mm = ctx.getConnection().getTransport().getMemoryManager();
                for (int i = 0, len = CONTENT.length; i < len; i++) {
                    HttpContent.Builder contentBuilder = request.httpContentBuilder();
                    Buffer b = Buffers.wrap(mm, CONTENT[i]);
                    contentBuilder.content(b);
                    HttpContent content = contentBuilder.build();
                    System.out.printf("(Client writing: %s)\n", b.toStringContent());
                    ctx.write(content);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace(); 
                    }
                }

                // since the request created by createRequest() is chunked,
                // we need to write the trailer to signify the end of the
                // POST data
                ctx.write(request.httpTrailerBuilder().build());

                System.out.println("\n");

                return ctx.getStopAction(); // discontinue filter chain execution

            }


            @Override
            public NextAction handleRead(FilterChainContext ctx) throws IOException {

                HttpContent c = (HttpContent) ctx.getMessage();
                Buffer b = c.getContent();
                if (b.hasRemaining()) {
                    sb.append(b.toStringContent());
                }

                // Last content from the server, set the future result so
                // the client can display the result and gracefully exit.
                if (c.isLast()) {
                    future.result(sb.toString());
                }
                return ctx.getStopAction(); // discontinue filter chain execution

            }


            // ------------------------------------------------- Private Methods


            private HttpRequestPacket createRequest() {

                HttpRequestPacket.Builder builder = HttpRequestPacket.builder();
                builder.method("POST");
                builder.protocol("HTTP/1.1");
                builder.uri("/echo");
                builder.chunked(true);
                HttpRequestPacket packet = builder.build();
                packet.addHeader(Header.Host, HOST_HEADER_VALUE);
                return packet;

            }

        }
