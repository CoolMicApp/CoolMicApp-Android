package cc.echonet.coolmicapp.Icecast;

public enum Command {
    STATS("stats.xml");

    private final String endpoint;

    Command(String endpoint) {
        this.endpoint = endpoint;
    }

    String getEndpoint() {
        return endpoint;
    }
}
