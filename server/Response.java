package cpen221.mp3.server;

public class Response {
    // Since the Response should be a JSON-formatted string, its attributes are
    // also of type String:
    String id, status, result;

    public Response(String id, String status, String result) {
        this.id = id;
        this.status = status;
        this.result = result;
    }
}
