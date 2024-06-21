package cpen221.mp3.server;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import cpen221.mp3.wikimediator.WikiMediator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.*;

// A brand-new Class:
class WikiHandler implements Runnable {
    Socket client;
    WikiMediator wiki = new WikiMediator();
    // Gson is a library package made by Google (to help handle JSON-formatted data).
    Gson gson = new Gson();


    /** Takes a client's Socket object and processes that client's request.
     *
     * @param clientSocket The socket where the request is accepted.
     *                     In other words, the socket where client is connected.
     * @param wiki The WikiMediator object used to process requests for this client.
     *
     */
    public WikiHandler(Socket clientSocket, WikiMediator wiki) {
        // Only a client can use a Socket object to establish a connection with a server.
        this.client = clientSocket;
        this.wiki = wiki;
    }

    @Override
    public void run() {
        // Only one thread, because we are processing one client's request:
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        System.out.println("Client connected");
        try {
            // You can wrap an InputStream in an ObjectInputStream and then
            // you can read objects of any type from it.

            // Client Socket receiving data in:
            ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
            // Client Socket sending data out (Because the data going into or
            // coming out of a socket is a stream of bytes).
            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());

            // "fromJson" method de-serializes the Json read from the specified
            // reader into an object of the specified type(Request.class).
            Request req = gson.fromJson((JsonReader) ois.readObject(), Request.class);


            // Checking to see the Request contained a 'timeout' field.
            if (req.getTimeOut() != null) {
                // Returning a Future object representing the pending
                // results of the submitted task.
                // Future<Sting> object is just a wrapper around the "call()" method's return value.
                final Future<String> handler = executorService.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return process(req);
                    }
                });

                // Obtain a Duration representing a number of seconds.
                Duration timeout = Duration.ofSeconds(Integer.parseInt(req.getTimeOut()));

                try {
                    // If the execution period is longer than specified timeout, an exception will be thrown.

                    // Future's "get" method waits if necessary for at most the given time
                    // for the computation to complete, and then retrieves its
                    // result, returned by the "call" method of the Future object:
                    String response = handler.get(timeout.toMinutes(), TimeUnit.MINUTES);
                    // writing the response to the socket (outputStream):
                    oos.writeObject(response);
                    // "response" is already in JSON format.(Hence
                    // write directly into the Socket).

                } catch (TimeoutException e) {
                    // Cancelling the thread executing this task if timed out.
                    handler.cancel(true);
                    System.out.println("Timeout");

                    // write an appropriate response (if timed-out):
                    oos.writeObject(gson.toJson(new Response(req.getID(), "failed", "Operation timed out")));
                }
            } // end of 'IF' statement here!

                // If the Request did not have a 'timeout' field:
            else {
                oos.writeObject(process(req));
            }

            ois.close();
            oos.close();
            client.close(); // closing the client's connection in the network.
        } catch(IOException | ClassNotFoundException | InterruptedException | ExecutionException e){
            System.out.println(e);
        }

        // Stop the current thread for this individual task:
        executorService.shutdownNow();
    }

    // The return type of the "process" is of type JSON String.
    private String process(Request req) {

        String type = req.getType();
        String query = req.getQuery();
        String id = req.getID();
        String timeout = req.getTimeOut();
        int limit = req.getLimit();


        String response;
        String statusS;

        boolean status = true; // meaning 'success'

        if (type.equals("simpleSearch")) {
            // Serializing the specified object(List<String>) to its equivalent JSON String:
            response = gson.toJson(wiki.simpleSearch(query, limit));
        } else if (type.equals("getPage")) {
            response = gson.toJson(wiki.getPage(query));
        } else if (type.equals("getConnectedPages")) {
            response = gson.toJson(wiki.getConnectedPages(query, limit));
        } else if (type.equals("zeitgeist")) {
            response = gson.toJson(wiki.zeitgeist(limit));
        } else if (type.equals("trending")) {
            response = gson.toJson(wiki.trending(limit));
        } else if (type.equals("peakLoad30s")) {
            response = gson.toJson(wiki.peakLoad30s());
        } else {
            status = false;
            response = "request not found";
        }

        if (status) {
            statusS = "success";
        } else {
            statusS = "failed";
        }

        return gson.toJson(new Response(id, statusS, response));
    }
}