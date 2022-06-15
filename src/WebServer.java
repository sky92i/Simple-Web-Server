import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {
	ServerSocket server_socket;
	public int PORT = 8080; // web server port number
	
	public static void main(String[] args) throws Exception {
		// save console output to "log.txt" file
		PrintStream out = new PrintStream(new FileOutputStream("log.txt", true), true);
		System.setOut(out);

		// web server start
		WebServer web_server = new WebServer();
		web_server.runServer();
	}

	public void runServer() throws Exception {
		System.out.println("\r--------Web Server has started--------");
		System.out.println("Enter \"127.0.0.1:" + PORT + "\" in your web browser.\n");
		server_socket = new ServerSocket(PORT);
		processHTTPRequest();
	}

	public void processHTTPRequest() {
		while(true) {
			Socket socket = null;
			try {
				socket = server_socket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Connection connection = null;
			try {
				connection = new Connection(socket);
			} catch (Exception e) {
				e.printStackTrace();
			}
			connection.start();			
		}
	}
}
