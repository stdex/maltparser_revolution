/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maltparser.webserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
 
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.maltparser.MaltParserService;
import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.concurrent.ConcurrentUtils;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import static org.maltparser.server.CliHandler.service;

public class HttpServerStart {
   
    public static ConcurrentMaltParserModel model;
    public static MaltParserService service;
    
    public static void loadmodel() {
        
        try {
            service =  new MaltParserService();
            service.initializeParserModel("-a covnonproj -c test123 -F /home/rostunov/workspace/neuro/malt_prj/malt/resource/data/convnonprd_small_with_outputcolumn.xml -if /home/rostunov/workspace/neuro/malt_prj/malt/resource/data/Roma_model.xml -m parse -li true -cs true");
        } catch (Exception e) {
                e.printStackTrace();
        }
        
    }
    
    public static String parseM(BufferedReader reader) {
        
        BufferedWriter writer = null;
        String formatedString = "";
        
        try {
            String line = null;
            ArrayList<String> lines = new ArrayList<String>();
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("out.txt")), "UTF-8"));
            
            int sentenceCount = 0;
            while (true) {
                    // Reads a sentence from the input file
                    String[] inputTokens = ConcurrentUtils.readSentence(reader);

                    // If there are no tokens then we have reach the end of file
                    if (inputTokens.length == 0) {
                            break;
                    }

                    DependencyStructure graph = service.parse(inputTokens);
                    for (int i = 1; i <= graph.getHighestDependencyNodeIndex(); i++) {
                            DependencyNode node = graph.getDependencyNode(i);
                            if (node != null) {
                                    for (SymbolTable table : node.getLabelTypes()) {
                                            writer.write(node.getLabelSymbol(table) + "\t");
                                            formatedString = formatedString + node.getLabelSymbol(table) + "\t";
                                    }
                                    if (node.hasHead()) {
                                            Edge  e = node.getHeadEdge();
                                            writer.write(e.getSource().getIndex() + "\t");
                                            formatedString = formatedString + e.getSource().getIndex() + "\t";
                                            if (e.isLabeled()) {
                                                    for (SymbolTable table : e.getLabelTypes()) {
                                                            writer.write(e.getLabelSymbol(table) + "\t");
                                                            formatedString = formatedString + e.getLabelSymbol(table) + "\t";
                                                    }
                                            } else {
                                                    for (SymbolTable table : graph.getDefaultRootEdgeLabels().keySet()) {
                                                            writer.write(graph.getDefaultRootEdgeLabelSymbol(table) + "\t");
                                                            formatedString = formatedString + graph.getDefaultRootEdgeLabelSymbol(table) + "\t";
                                                    }
                                            }
                                    }
                                    writer.write('\n');
                                    formatedString = formatedString + '\n';
                                    writer.flush();
                            }
                    }
                    writer.write('\n');
                    formatedString = formatedString + '\n';
                    writer.flush();
                    lines.clear();

                    sentenceCount++;
            }
            System.out.println("Parsed " + sentenceCount +" sentences");
            
            reader.close();
            writer.flush();
            writer.close();
            System.out.println();
            service.terminateParserModel();

        } catch (MaltChainedException e) {
                System.err.println("MaltParser exception: " + e.getMessage());
        } catch (FileNotFoundException e) {
                System.err.println("MaltAPITest exception: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
                System.err.println("MaltAPITest exception: " + e.getMessage());
        } catch (IOException e) {
                System.err.println("MaltAPITest exception: " + e.getMessage());
        }
        finally {
    		if (reader != null) {
                try {
    				reader.close();
    	    	} catch (IOException e) {
    				e.printStackTrace();
    	    	}
    		}
    		if (writer != null) {
    			try {
    				writer.close();
    	    	} catch (IOException e) {
    				e.printStackTrace();
    	    	}
    		}
    	}
        
        return formatedString;
    }
    
    public static void main(String[] args) throws Exception {
        
        loadmodel();
        
        final int port = 8888;
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        ExecutorService httpThreadPool = Executors.newCachedThreadPool();
        httpServer.setExecutor(httpThreadPool);      
        
        httpServer.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                final byte[] out = "Hello, world!".getBytes("UTF-8");
 
                httpExchange.sendResponseHeaders(200, out.length);
 
                OutputStream os = httpExchange.getResponseBody();
                os.write(out);
                os.close();
            }
        });
        httpServer.createContext("/data", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                
                String requestMethod = httpExchange.getRequestMethod();
                System.out.println(requestMethod);
                InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "UTF-8");
                
//                InputStream is = httpExchange.getRequestBody();
//                System.out.println ( is.available() );
//                StringBuffer buffer = new StringBuffer();
//
//                int b;
//                while ( ( b = is.read() ) != -1 ) {
//                    buffer.append ( ( char ) b );
//                }
//                is.close();
//
//                System.out.println ( buffer.toString() );
                
                BufferedReader br = new BufferedReader(isr);
                String formatedString = "";
                String line = "";
                while ( (line = br.readLine()) != null ) {
                    formatedString = formatedString + "\n" + line;
                }
                System.out.println(formatedString);
                
//                import org.apache.commons.io.IOUtils;
//                formatedString = IOUtils.toString(httpExchange.getRequestBody());

                
                if ("post".equalsIgnoreCase(requestMethod)) {

                    String outString = "";

                    BufferedReader reader = null;

//                  ByteArrayInputStream stream = new ByteArrayInputStream(postString.getBytes(StandardCharsets.UTF_8));
//                  InputStream ins = stream;
//                  reader = new BufferedReader(new InputStreamReader(ins, "UTF-8"));

                    reader = new BufferedReader(isr);

                    outString = parseM(reader);
                    final byte[] out = outString.getBytes("UTF-8");
                    
                    httpExchange.sendResponseHeaders(200, out.length);

                    OutputStream os = httpExchange.getResponseBody();
                    os.write(out);
                    os.close();
                
                }
                
                else if("get".equalsIgnoreCase(requestMethod)) {
                
                    final byte[] out = "Hello, world!".getBytes("UTF-8");

                    httpExchange.sendResponseHeaders(200, out.length);

                    OutputStream os = httpExchange.getResponseBody();
                    os.write(out);
                    os.close();
                
                }
                
            }
        });
 
        httpServer.start();
        System.out.println("Server Start!");
        
        String command;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        
        try
        {
            while(true)
            {
                command = in.readLine().trim();
                
                if (command.isEmpty())
                    continue;
                
                if (command.equals("quit") || command.equals("exit") || command.equals("stop"))
                {
                    httpServer.stop(1);
                    httpThreadPool.shutdownNow();
                    System.exit(0);
                    System.out.printf("Server Stop!");
                }
                else
                    System.out.printf("Unknown Command: %s\n", command);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
    }
}