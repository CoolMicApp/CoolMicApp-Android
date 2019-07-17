package cc.echonet.coolmicapp.Icecast;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import cc.echonet.coolmicapp.Icecast.Request.Stats;

public class Icecast implements Closeable {
    private String protocol;
    private String host;
    private int port;
    private String username;
    private String password;

    public Icecast(String protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    URL getCommandURL(Command command, String mount) throws MalformedURLException {
        String url;

        // TODO: Improve this mess...
        if (mount == null) {
            url = String.format(Locale.ENGLISH, "%s://%s:%s@%s:%d/admin/%s", protocol, username, password, host, port, command.getEndpoint());
        } else {
            url = String.format(Locale.ENGLISH, "%s://%s:%s@%s:%d/admin/%s?mount=/%s", protocol, username, password, host, port, command.getEndpoint(), mount);
        }

        return new URL(url);
    }

    public Stats getStats(String mount) throws IOException {
        return new Stats(getCommandURL(Command.STATS, mount));
    }

    @Override
    public void close() {
        // no-op.
    }
}
