import java.io.*;
import java.net.*;
/* javac this file to compile it and then you should be able to run
 * in seperate terminals: java Server , java Client
 * then type in the client window and it will print in the server */


class Client {
    static public void main(String[] args) throws IOException {
        
        if (args.length != 1) {
            System.err.println(
                "Usage: java Client <host name>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = 5544;

        try (
            Socket echoSocket = new Socket(hostName, portNumber);
            PrintWriter out =
                new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
            BufferedReader stdIn =
                new BufferedReader(
                    new InputStreamReader(System.in))
        ) {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
            }
        
        
		} 
        catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } 
        catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } 
    }
}

class Server {
    static public void main(String[] args) throws IOException {
        
        if (args.length != 0) {
            System.err.println("Usage: java Server");
            System.exit(1);
        }
        
        int portNumber = 5544;
        
        try (
            ServerSocket serverSocket =
                new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();     
            PrintWriter out =
                new PrintWriter(clientSocket.getOutputStream(), true);                   
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}
