package ulisboa.tecnico.sirs.client;

import java.net.Socket;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	try {
    		Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
    	} catch(Exception e) {
    		
    	}
    }
}
