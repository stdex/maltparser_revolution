package org.maltparser;

import org.maltparser.grizzly.Config;


/**
 * Application class in the MaltParser library.
 *
 * @author Johan Hall
 * @since 1.0
**/
public class Malt {
	/**
	 * The starting point of MaltParser
	 * 
	 * @param args command-line arguments
	 */
    
	public static void main(String[] args) throws Exception {

                Config.loadConfig();
                
		MaltConsoleEngine engine = new MaltConsoleEngine();
		engine.startEngine(args);
	}
}
