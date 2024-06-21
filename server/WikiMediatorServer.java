package cpen221.mp3.server;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import cpen221.mp3.wikimediator.WikiMediator;
import fastily.jwiki.core.Wiki;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WikiMediatorServer {

    private ServerSocket wikiServer = null;
    private int port;
    private int maxNumRequests;
    private ExecutorService executorService = null;


    /**
     * Constructor for WikiMediatorServer.
     *
     * @param port the port number to bind the server to
     * @param n the number of concurrent requests the server can handle
     */
    public WikiMediatorServer(int port, int n) {
        this.port = port;
        this.maxNumRequests = n;
    }

    /**
     * Start a server at a given port number, with the ability to process
     * up to n requests concurrently.
     *
     * NOTE: Running the server, listening for connections and handling them.
     **/
    public void serve() {
        // Creating a thread-pool with 'maxNumRequests' number of threads; in
        // other words, having one thread to handle each client.
        executorService = Executors.newFixedThreadPool(this.maxNumRequests);

        try {
            System.out.println("Starting Server");
            // Making a Multi-Server that listens for connections on "port".
            // The constructor for ServerSocket throws an exception if it can't
            // listen on the specified port (Hence Try-Catch Block).
            wikiServer = new ServerSocket(this.port);
            WikiMediator wiki = new WikiMediator();

            // while you are listening and requests are coming:
            while (true) {
                try {
                    // Accepting a connection from a client. (The accept method
                    // waits until a client starts up and requests a connection
                    // on the host and port of this server;
                    // A new Socket is created so that the original "ServerSocket"
                    // keeps listening to new incoming clients.
                    // Listening to the ServerSocket to take clients:
                  Socket clientSocket =  wikiServer.accept();
                  System.out.println("Processing");

                  // Submits a value-returning task for execution and returns
                  // a Future representing the pending results of that client.
                  // *** NOTE: You can submit anything of type Runnable or Callable.
                    // Since WikiHandler implements Runnable, you can pass an object
                    // of type WikiHandler to the ExecutorService's submit method
                    // as argument:
                  executorService.submit(new WikiHandler(clientSocket, wiki));
                } catch (IOException e) {
                    System.out.println("Client cannot connect (Error accepting client");
                }
            }

        } catch (IOException e) {  // this catch block is to catch the ServerSocket exception.
            System.out.println("Error listening to the specified port");
        }
    }

    public static void main(String args[]) {

        // WikiClientipedia.org");
    }

}


