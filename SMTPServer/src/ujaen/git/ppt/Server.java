package ujaen.git.ppt;


import java.io.IOException;
import java.net.*;

public class Server {

	public static final int TCP_SERVICE_PORT = 6000;

	static ServerSocket server = null;

	public static void main(String[] args) {
			
		System.out.println("Servidor> Iniciando servidor");
		try {
			server = new ServerSocket(TCP_SERVICE_PORT);
			while (true) {
				final Socket newsocket = server.accept();
				System.out.println("Servidor> Conexión entrante desde "
						+ newsocket.getInetAddress().toString() + ":"
						+ newsocket.getPort());
				new Thread(new Connection(newsocket)).start();
			}
		} catch (IOException e) {
			System.err.println("Server "+e.getMessage());
			e.printStackTrace();
		}

	}

}