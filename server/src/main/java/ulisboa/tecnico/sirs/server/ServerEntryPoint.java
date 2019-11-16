package ulisboa.tecnico.sirs.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

import ulisboa.tecnico.sirs.domain.User;
import ulisboa.tecnico.sirs.server.database.DBGateway;

public class ServerEntryPoint 
{
		
	public static void main( String[] args )
	{
		
		DBGateway gateway = new DBGateway();

		//receive clients
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(Integer.parseInt(args[0]));
			while(true) {
				try {
					Socket inSoc = ss.accept();
					ServerThread newServerThread = new ServerThread(inSoc, gateway);
					newServerThread.start();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				gateway.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}


}

class ServerThread extends Thread {
	private Socket socket = null;
	ObjectOutputStream outStream;
	ObjectInputStream inStream;
	private String currUser;
	private DBGateway gateway;

	public ServerThread(Socket clientSocket, DBGateway gateway) {
		socket = clientSocket;
		this.gateway = gateway;
		System.out.println("thread created for a new client");
	}

	public void run() {

		try {
			outStream = new ObjectOutputStream(socket.getOutputStream());
			inStream = new ObjectInputStream(socket.getInputStream());

			String cmd = null;

			//authentication
			try {
				//look for the client
				currUser = (String) inStream.readObject();
				List<User> listUsers = gateway.getUsers(); 
				boolean found = false;
				for(User u : listUsers) {
					System.out.println(u.getName());
					if(u.getName().equals(currUser)) {
						found = true;
					}
				}
				outStream.writeObject(found);
				//decipher password
				//compare
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			try{
				while(true){
					cmd = (String) inStream.readObject();
					switch(cmd) {
					case "-l":
						listMDServer();
						break;
					case "-r":
						readMDServer();
						break;
					case "quit":
						quitUserServer();
						break;
					}
				}
			}catch (Exception e) {
				
			}

		}catch (Exception e) {
			
		}
	}

	private void readMDServer() {
		// TODO Auto-generated method stub

	}

	private void listMDServer() {
		// TODO Auto-generated method stub

	}

	private void quitUserServer() {
		try {
			String user = (String) inStream.readObject();
			System.out.println("User " + user + " disconnected.");
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
}
