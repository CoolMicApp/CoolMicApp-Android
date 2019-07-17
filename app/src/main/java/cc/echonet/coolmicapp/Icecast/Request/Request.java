package cc.echonet.coolmicapp.Icecast.Request;

import android.renderscript.RSInvalidStateException;
import android.util.Base64;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import cc.echonet.coolmicapp.Icecast.Response.Response;
import cc.echonet.coolmicapp.Icecast.State;

abstract public class Request implements Closeable {
    protected HttpURLConnection connection;
    protected State state = State.UNCONNECTED;
    protected Response response;

    public Request(URL url) throws IOException {
        connection = (HttpURLConnection) url.openConnection();

        connection.setUseCaches(false);
        connection.setDoOutput(false);

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept-Charset", "utf-8");
        connection.setRequestProperty("Accept-Encoding", "text/xml");
        connection.setRequestProperty("Accept-Language", "en-US");
        connection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString(url.getUserInfo().getBytes(), Base64.NO_WRAP));
    }

    public Response getResponse() {
        if (state != State.FINISHED)
            throw new RSInvalidStateException("Request not yet finished");

        return response;
    }

    protected void setError() {
        state = State.ERROR;
    }

    public State getState() {
        return state;
    }

    abstract public void finish();

    public void connect() throws IOException {
        if (state != State.UNCONNECTED)
            return;

        connection.connect();
        state = State.CONNECTED;
    }

    @Override
    public void close() {
        if (connection == null)
            return;

        connection.disconnect();
        connection = null;
        state = State.CLOSED;
    }
}
