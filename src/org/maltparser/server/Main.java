package org.maltparser.server;

public class Main
{
    public static long startupTime;
    
    public static void main(String[] args)
    {
        try
        {
            
            System.out.printf("Server Run\n\n");

            // Start CLI
            new Thread(new CliHandler()).start();
      
            startupTime = System.currentTimeMillis();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.printf("Unknown error occur while starting the server.\n");
            System.out.printf("Message: %s\n", e.getMessage());
            System.exit(0);
        }
     
    }
}