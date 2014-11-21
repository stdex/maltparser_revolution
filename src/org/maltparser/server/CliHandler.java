package org.maltparser.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.concurrent.ConcurrentMaltParserService;
import org.maltparser.concurrent.ConcurrentUtils;

public class CliHandler implements Runnable
{
    private BufferedReader in;
    public static ConcurrentMaltParserModel model;
    
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
                
                if (command.equals("uptime"))
                    System.out.printf("Server Uptime: %dsec.\n", (System.currentTimeMillis() - Main.startupTime) / 1000);
                else if (command.equals("load"))
                    loadmodel();
                else if (command.equals("parse"))
                    parseF();
                else if (command.equals("quit") || command.equals("exit"))
                {
                    //destroy();
                    System.exit(0);
                }
                else
                    System.out.printf("Unknown Command: %s\n", command);
            }
        }
        catch(Exception e){}
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
                URL swemaltMiniModelURL = new File("/home/rostunov/workspace/neuro/malt_prj/malt/test123.mco").toURI().toURL();
                model = ConcurrentMaltParserService.initializeParserModel(swemaltMiniModelURL);
        } catch (Exception e) {
                e.printStackTrace();
        }
    }
    
    /*
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
    		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("/home/rostunov/workspace/neuro/malt_prj/malt/out.txt")), "UTF-8"));
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
}
