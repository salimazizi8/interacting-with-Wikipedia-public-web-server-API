package cpen221.mp3.server;

public class Request {
    String id, type, query, limit, timeout, hops;

    public Request(String id, String type, String query, String limit, String timeout) {
        this.id = id;
        this.type = type;
        this.query = query;
        this.timeout = timeout;
        if (type.equals("getConnectedPages")) {
            this.hops = limit;
            this.limit = "";
        } else {
            this.limit = limit;
            this.hops = "";
        }
    }

    public Request(String id, String type, String query, String limit) {
        this.id = id;
        this.type = type;
        this.query = query;
        if (type.equals("getConnectedPages")) {
            this.hops = limit;
            this.limit = "";
        } else {
            this.limit = limit;
            this.hops = "";
        }
    }

    public Request(String id, String type, String limit) {
        this.id = id;
        this.type = type;
        this.limit = limit;
        this.query = "";
    }

    public String getID() {
        return this.id;
    }

    public int getLimit() {
        if (this.limit != null) {
            return Integer.parseInt(this.limit);
        }
        return Integer.parseInt(this.hops);
    }

    public String getQuery() {
        return this.query;
    }

    public String getTimeOut() {
        return this.timeout;
    }

    public String getType() {
        return this.type;
    }
}
