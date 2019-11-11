package ulisboa.tecnico.sirs.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import ulisboa.tecnico.sirs.server.pep.PolicyEnforcementPoint;

public class App 
{
	public static void main( String[] args )
	{
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(Integer.parseInt(args[0]));
			while(true) {
				try {
					Socket inSoc = ss.accept();
					ServerThread newServerThread = new ServerThread(inSoc);
					newServerThread.start();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}

class ServerThread extends Thread {
	private Socket socket = null;
	ObjectOutputStream outStream;
	ObjectInputStream inStream;
	private String currUser;
	private PolicyEnforcementPoint pep;

	public ServerThread(Socket clientSocket) {
		socket = clientSocket;
		System.out.println("thread created for a new client");
	}

	public void run() {
		try {
			pep = new PolicyEnforcementPoint();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
