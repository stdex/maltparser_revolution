/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maltparser.grizzly;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.ServerConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.maltparser.MaltParserService;
import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.concurrent.ConcurrentUtils;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import static org.maltparser.webserver.HttpServerStart.service;

/**
 * Class initializes and starts the echo server, based on Grizzly 2.3
 */
public class Server {
    public static final Logger logger = Logger.getLogger(Server.class.getName());
    public static ConcurrentMaltParserModel model;
    public static MaltParserService service;

    public static void main(String[] args) throws IOException {

        loadmodel();
        
        // create a basic server that listens on port 8080.
        final HttpServer server = HttpServer.createSimpleServer();

        final ServerConfiguration config = server.getServerConfiguration();

        // Map the path, /echo, to the NonBlockingHandler
        config.addHttpHandler(new NonBlockingHandler(), "/echo");

        try {
            server.start();
            logger.info("Press any key to stop the server...");
            System.in.read();
        } finally {
            logger.info("Stopping transport...");
            // stop the transport
            server.shutdownNow();
            logger.info("Stopped transport...");
        }
       
    }
    
    public static void loadmodel() {
        
        try {
            service =  new MaltParserService();
            service.initializeParserModel("-a covnonproj -c test123 -F /home/rostunov/workspace/neuro/malt_prj/malt/resource/data/convnonprd_small_with_outputcolumn.xml -if /home/rostunov/workspace/neuro/malt_prj/malt/resource/data/Roma_model.xml -m parse -li true -cs true");
        } catch (Exception e) {
                e.printStackTrace();
        }
        
    }
    
    
    public static String parseM(BufferedReader reader) {

        //BufferedReader reader = null;
        BufferedWriter writer = null;
        String formatedString = "";

        try {
            String line = null;
            ArrayList<String> lines = new ArrayList<String>();
            
           //reader = new BufferedReader(new InputStreamReader(inpStream, "UTF-8"));  
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

//        System.out.println("---------------------");
//        System.out.println(formatedString);
//        System.out.println("---------------------");
        return formatedString.trim();
    }
      
}
