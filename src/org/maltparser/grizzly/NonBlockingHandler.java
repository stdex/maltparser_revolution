/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maltparser.grizzly;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import static org.maltparser.grizzly.Server.parseM;

public class NonBlockingHandler extends HttpHandler {

        // -------------------------------------------- Methods from HttpHandler


        @Override
        public void service(Request request, Response response) throws Exception {

            final char[] buf = new char[128];
            Reader in = null;
            Writer out = null;
            //BufferedReader reader = null;
            try {
                //in = request.getReader(); // put the stream in blocking mode
                  InputStream is = request.getInputStream();              
                  //out = response.getWriter();
                  OutputStream osm = response.getOutputStream();
                  out = new BufferedWriter(new OutputStreamWriter(osm, "UTF-8"));
  
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                
                BufferedReader br = new BufferedReader(isr);
//                String formatedString = "";
//                String line;
//                while ( (line = tempbr.readLine()) != null ) {
//                    System.out.println(line.trim().replace("\"", ""));
//                    formatedString = formatedString + "\n" + line.trim().replace("\"", "");
//                }
//
//                StringReader sr = new StringReader(formatedString.trim());
//                BufferedReader br = new BufferedReader(sr);

                String outString = "";
                outString = parseM(br);
      
                out.write(outString);
                
                //final byte[] out = outString.getBytes("UTF-8");
                
                out.flush();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ignore) {
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ignore) {
                    }
                }
            }

        }


} // END NonBlockingEchoHandler
