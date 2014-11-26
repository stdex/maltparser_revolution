package org.maltparser.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import org.maltparser.MaltParserService;

import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.concurrent.ConcurrentMaltParserService;
import org.maltparser.concurrent.ConcurrentUtils;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public class CliHandler implements Runnable
{
    private BufferedReader in;
    public static ConcurrentMaltParserModel model;
    public static MaltParserService service;
    
    public CliHandler() throws Exception
    {
        System.out.printf("\nInitializing Cli Command Handler.\n");
        in = new BufferedReader(new InputStreamReader(System.in));
        loadmodel();
    }
    
    public void run()
    {
        System.out.printf("Cli Command Handler started.\n");
        
        String command;
        
        try
        {
            while(true)
            {
                command = in.readLine().trim();
                
                if (command.isEmpty())
                    continue;
                
                System.out.printf("Command Receive: %s\n", command);
                
                if (command.equals("uptime")) {
                    System.out.printf("Server Uptime: %dsec.\n", (System.currentTimeMillis() - Main.startupTime) / 1000);
                }
                else if (command.equals("load")) {
                    loadmodel();
                }
                else if (command.equals("parse")) {
                    //parseF();
                    parseM();
                }
                else if (command.equals("quit") || command.equals("exit"))
                {
                    //destroy();
                    System.exit(0);
                }
                else
                    System.out.printf("Unknown Command: %s\n", command);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
        
    public static void destroy()
    {
        try { }
        catch (Exception e) {}
    }
    
    public static void loadmodel()
    {
        // Loading the Swedish model swemalt-mini
        //ConcurrentMaltParserModel model = null;
        try {
//                URL swemaltMiniModelURL = new File("/home/rostunov/workspace/neuro/malt_prj/malt/test123.mco").toURI().toURL();
//                model = ConcurrentMaltParserService.initializeParserModel(swemaltMiniModelURL);
                service =  new MaltParserService();
                service.initializeParserModel("-a covnonproj -c test123 -F /home/rostunov/workspace/neuro/malt_prj/malt/resource/data/convnonprd_small_with_outputcolumn.xml -if /home/rostunov/workspace/neuro/malt_prj/malt/resource/data/Roma_model.xml -m parse -li true -cs true");
        } catch (Exception e) {
                e.printStackTrace();
        }
    }
    
    /*
    parseF() Start
    org.maltparser.ml.lib.MaltLibsvmModel.predict(MaltLibsvmModel.java:115)
    org.maltparser.core.lw.parser.LWClassifier.predict(LWClassifier.java:58)
    org.maltparser.core.lw.parser.LWDecisionModel.predict(LWDecisionModel.java:83)
    org.maltparser.core.lw.parser.LWDecisionModel.predictFromKBestList(LWDecisionModel.java:95)
    org.maltparser.core.lw.parser.LWDeterministicParser.predict(LWDeterministicParser.java:88)
    org.maltparser.core.lw.parser.LWDeterministicParser.parse(LWDeterministicParser.java:74)
    org.maltparser.core.lw.parser.LWSingleMalt.parse(LWSingleMalt.java:90)
    org.maltparser.concurrent.ConcurrentMaltParserModel.internalParse(ConcurrentMaltParserModel.java:109)
    org.maltparser.concurrent.ConcurrentMaltParserModel.parseTokens(ConcurrentMaltParserModel.java:94)
    org.maltparser.server.CliHandler.parseF(CliHandler.java:102)
    org.maltparser.server.CliHandler.run(CliHandler.java:50)
    java.lang.Thread.run(Thread.java:745)
    */
    public static void parseF()
    {
        BufferedReader reader = null;
    	BufferedWriter writer = null;
    	try {
    		reader = new BufferedReader(new InputStreamReader(new File("/home/rostunov/workspace/neuro/malt_prj/malt/resource/data/inp_Lc_Rc.csv").toURI().toURL().openStream(), "UTF-8"));
    		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("out.conll"), "UTF-8"));
    		int sentenceCount = 0;
    		while (true) {
    			// Reads a sentence from the input file
	    		String[] inputTokens = ConcurrentUtils.readSentence(reader);
	    		
	    		// If there are no tokens then we have reach the end of file
	    		if (inputTokens.length == 0) {
	    			break;
	    		}

	    		// Parse the sentence
	    		String[] outputTokens = model.parseTokens(inputTokens);
	    		
	    		// Writes the sentence to the output file
	    		ConcurrentUtils.writeSentence(outputTokens, writer);
	    		
	    		sentenceCount++;
    		}
    		System.out.println("Parsed " + sentenceCount +" sentences");
    	} catch (Exception e) {
			e.printStackTrace();
    	} finally {
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
        
    }
    
    /*
    Configuration Start
    org.maltparser.ml.lib.Lib.predict(Lib.java:264)
    org.maltparser.parser.guide.instance.AtomicModel.predict(AtomicModel.java:117)
    org.maltparser.parser.guide.decision.OneDecisionModel.predict(OneDecisionModel.java:108)
    org.maltparser.parser.guide.SingleGuide.predict(SingleGuide.java:99)
    org.maltparser.parser.DeterministicParser.predict(DeterministicParser.java:53)
    org.maltparser.parser.DeterministicParser.parse(DeterministicParser.java:40)
    org.maltparser.parser.SingleMalt.parse(SingleMalt.java:215)
    org.maltparser.parser.SingleMaltChartItem.process(SingleMaltChartItem.java:130)
    org.maltparser.core.flow.FlowChartInstance.process(FlowChartInstance.java:200)
    org.maltparser.Engine.process(Engine.java:63)
    org.maltparser.MaltConsoleEngine.maltParser(MaltConsoleEngine.java:119)
    org.maltparser.MaltConsoleEngine.startEngine(MaltConsoleEngine.java:81)
    org.maltparser.Malt.main(Malt.java:18)
    */
    
    /*
    parseM Start
    org.maltparser.ml.lib.Lib.predict(Lib.java:264)
    org.maltparser.ml.lib.MaltLibsvmModel.predict(MaltLibsvmModel.java:116)
    org.maltparser.ml.lib.Lib.predict(Lib.java:260)
    org.maltparser.parser.guide.instance.AtomicModel.predict(AtomicModel.java:117)
    org.maltparser.parser.guide.decision.OneDecisionModel.predict(OneDecisionModel.java:108)
    org.maltparser.parser.guide.SingleGuide.predict(SingleGuide.java:99)
    org.maltparser.parser.DeterministicParser.predict(DeterministicParser.java:53)
    org.maltparser.parser.DeterministicParser.parse(DeterministicParser.java:40)
    org.maltparser.parser.SingleMalt.parse(SingleMalt.java:215)
    org.maltparser.MaltParserService.parse(MaltParserService.java:158)
    org.maltparser.server.CliHandler.parseM(CliHandler.java:221)
    org.maltparser.server.CliHandler.run(CliHandler.java:64)
    java.lang.Thread.run(Thread.java:745)
    */
    
    public static void parseM()
    {
        
        BufferedReader reader = null;
    	BufferedWriter writer = null;
        try {
            String testDataFile = "/home/rostunov/workspace/neuro/malt_prj/malt/resource/data/inp_Lc_Rc.csv";

            reader = new BufferedReader(new InputStreamReader(new File("/home/rostunov/workspace/neuro/malt_prj/malt/resource/data/inp_Lc_Rc.csv").toURI().toURL().openStream(), "UTF-8"));
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("/home/rostunov/workspace/neuro/malt_prj/malt/out.txt")), "UTF-8"));
            String line = null;
            ArrayList<String> lines = new ArrayList<String>();
            
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
                                    }
                                    if (node.hasHead()) {
                                            Edge  e = node.getHeadEdge();
                                            writer.write(e.getSource().getIndex() + "\t");
                                            if (e.isLabeled()) {
                                                    for (SymbolTable table : e.getLabelTypes()) {
                                                            writer.write(e.getLabelSymbol(table) + "\t");
                                                    }
                                            } else {
                                                    for (SymbolTable table : graph.getDefaultRootEdgeLabels().keySet()) {
                                                            writer.write(graph.getDefaultRootEdgeLabelSymbol(table) + "\t");
                                                    }
                                            }
                                    }
                                    writer.write('\n');
                                    writer.flush();
                            }
                    }
                    writer.write('\n');
                    writer.flush();
                    System.out.print(".");
                    lines.clear();

                    sentenceCount++;
            }
            System.out.println("Parsed " + sentenceCount +" sentences");
            
//            while ((line = reader.readLine()) != null) {
//                    if (line.trim().length()==0) {
//                            String[] tokens = lines.toArray(new String[lines.size()]);
//                            DependencyStructure graph = service.parse(tokens);
//                            for (int i = 1; i <= graph.getHighestDependencyNodeIndex(); i++) {
//                                    DependencyNode node = graph.getDependencyNode(i);
//                                    if (node != null) {
//                                            for (SymbolTable table : node.getLabelTypes()) {
//                                                    writer.write(node.getLabelSymbol(table) + "\t");
//                                            }
//                                            if (node.hasHead()) {
//                                                    Edge  e = node.getHeadEdge();
//                                                    writer.write(e.getSource().getIndex() + "\t");
//                                                    if (e.isLabeled()) {
//                                                            for (SymbolTable table : e.getLabelTypes()) {
//                                                                    writer.write(e.getLabelSymbol(table) + "\t");
//                                                            }
//                                                    } else {
//                                                            for (SymbolTable table : graph.getDefaultRootEdgeLabels().keySet()) {
//                                                                    writer.write(graph.getDefaultRootEdgeLabelSymbol(table) + "\t");
//                                                            }
//                                                    }
//                                            }
//                                            writer.write('\n');
//                                            writer.flush();
//                                    }
//                            }
//                            writer.write('\n');
//                            writer.flush();
//                            System.out.print(".");
//                            lines.clear();
//                    } else {
//                            lines.add(line);
//                    }
//            }
            
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
    }
    
    
}
