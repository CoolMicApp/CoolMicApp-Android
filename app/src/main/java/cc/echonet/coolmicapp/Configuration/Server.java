package cc.echonet.coolmicapp.Configuration;

import java.net.MalformedURLException;
import java.net.URL;

public class Server extends ProfileBase {
    Server(ProfileBase profile) {
        super(profile);
    }

    public String getProtocol() {
        // TODO: This is static for now but may change in future.
        return "http";
    }

    public String getHostname() {
        String serverName = getString("connection_address");

        if (serverName.indexOf(':') > 0) {
            serverName = serverName.split(":", 2)[0];
        }

        return serverName;
    }

    public int getPort() {
        String serverName = getString("connection_address");

        if (serverName.indexOf(':') > 0) {
            return  Integer.parseInt(serverName.split(":", 2)[1]);
        }

        return 8000;
    }

    public String getUsername() {
        return getString("connection_username");
    }

    public String getPassword() {
        return getString("connection_password");
    }

    public boolean getReconnect() {
        return getBoolean("connection_reconnect", false);
    }

    public String getMountpoint() {
        return getString("connection_mountpoint");
    }

    public boolean isSet() {
        return !getHostname().isEmpty() && !getMountpoint().isEmpty() && !getUsername().isEmpty() && !getPassword().isEmpty();
    }

    public URL getStreamURL() throws MalformedURLException {
        return new URL(getProtocol(), getHostname(), getPort(), getMountpoint());
    }
}
