package ulisboa.tecnico.sirs.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

import ulisboa.tecnico.sirs.server.database.DBManager;

public class ServerEntryPoint 
{
	public static void main( String[] args )
	{
		//Connection to database
		DBManager manager = new DBManager();
		manager.setProperties("jdbc:mariadb://localhost/", "root", "admin");
		try {
			manager.createConnection();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e1) {
			System.out.println("Error connection to database");
		}

		//receive clients
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
			try {
				manager.closeConnection();
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

	public ServerThread(Socket clientSocket) {
		socket = clientSocket;
		System.out.println("thread created for a new client");
	}

	public void run() {

		try {
			System.out.println(socket);
			outStream = new ObjectOutputStream(socket.getOutputStream());
			inStream = new ObjectInputStream(socket.getInputStream());

			String cmd = null;


			try {
				currUser = (String) inStream.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			try{
				while(true){
					cmd = (String) inStream.readObject();
					//para cada instrucao vai realizar uma funcao especifica
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
