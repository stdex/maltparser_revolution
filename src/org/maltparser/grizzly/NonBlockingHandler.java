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
                  
//                InputStreamReader isr = new InputStreamReader(request.getInputStream(), "UTF-8");
//                out = response.getWriter();
//
//                reader = new BufferedReader(isr);
                
//                StringBuilder builr = new StringBuilder();
//                String aux = "";
//
//                while ((aux = reader.readLine()) != null) {
//                    builr.append(aux);
//                }
//
//                System.out.println(builr.toString());
                
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                int read;
                StringBuilder builder = new StringBuilder();
                while ((read = isr.read(buf)) != -1) {
                    builder.append(buf, 0, read);
                    //out.write(buf, 0, read); // echo the contents of 'buf' to the client
                }
                //System.out.println(builder.toString());
                String formatedString = builder.toString().trim();
                //System.out.println("--------------------");
                //System.out.println(formatedString);
                //System.out.println("--------------------");

                StringReader sr = new StringReader(formatedString);
                BufferedReader br = new BufferedReader(sr);
//                
//
//                StringBuilder builr = new StringBuilder();
//                String aux = "";
//
//                while ((aux = br.readLine()) != null) {
//                    builr.append(aux);
//                }

//                System.out.println(builr.toString());
                
                
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
