/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maltparser.grizzly;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import org.maltparser.net.HttpURLConnectionSend;

public class Sender {

    public static void main(String[] args) throws Exception {
        
        String url = "http://localhost:8080/echo";
        String response = "";
        
        BufferedReader reader = null;
        
//      String exampleString = ""; 
//      InputStream stream = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8));
        
        reader = new BufferedReader(new InputStreamReader(new File("/home/rostunov/workspace/neuro/malt_prj/malt/resource/data/test_NRCinMALT_OF.txt").toURI().toURL().openStream(), "UTF-8"));
        
        //String formatedString = IOUtils.toString(reader);
        String formatedString = "";
        String line = "";
        while ( (line = reader.readLine()) != null ) {
            formatedString = formatedString + "\n" + line;
        }
        System.out.println(formatedString.trim());
        response = HttpURLConnectionSend.sendPost(url, formatedString.trim());
        System.out.println(response);
        
    }
    
}
