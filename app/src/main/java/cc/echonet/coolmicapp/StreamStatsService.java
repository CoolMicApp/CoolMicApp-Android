package cc.echonet.coolmicapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class StreamStatsService extends IntentService {

    public StreamStatsService() {
        super("StreamStatsService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionStatsFetch(Context context, String url) {
        Intent intent = new Intent(context, StreamStatsService.class);
        intent.setAction(Constants.ACTION_STATS_FETCH);
        intent.putExtra(Constants.EXTRA_URL, url);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_STATS_FETCH.equals(action)) {
                final String url = intent.getStringExtra(Constants.EXTRA_URL);

                StreamStats obj = new StreamStats(-1, -1);

                try {
                    Uri u = Uri.parse(url);

                    Log.e("CM-StreamStatsServe", "URL:" + url);

                    String authority[] = u.getUserInfo().split(":");

                    DefaultHttpClient httpClient = new DefaultHttpClient();

                    httpClient.getCredentialsProvider().setCredentials(
                            new AuthScope(u.getHost(),u.getPort()),
                            new UsernamePasswordCredentials(authority[0], authority[1]));

                    HttpGet httpGet = new HttpGet(url);

                    // Set up the header types needed to properly transfer JSON
                    httpGet.setHeader("Accept-Encoding", "text/xml");
                    httpGet.setHeader("Accept-Language", "en-US");

                    // Execute POST
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    StatusLine status = httpResponse.getStatusLine();
                    if (status.getStatusCode() != 200) {
                        Log.e("CM-StreamStatsService", "HTTP error, invalid server status code: " + httpResponse.getStatusLine());
                    }
                    else {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document doc = builder.parse(httpResponse.getEntity().getContent());

                        XPathFactory xpathFactory = XPathFactory.newInstance();
                        XPath xpath = xpathFactory.newXPath();

                        XPathExpression expr_listeners = xpath.compile("/icestats/source/listeners/text()");
                        XPathExpression expr_listeners_peak = xpath.compile("/icestats/source/listener_peak/text()");

                        String listeners = (String) expr_listeners.evaluate(doc, XPathConstants.STRING);
                        String listeners_peak = (String) expr_listeners_peak.evaluate(doc, XPathConstants.STRING);

                        if(listeners != "") {
                            obj.setListenersCurrent(Integer.valueOf(listeners));
                        }

                        if(listeners_peak != "") {
                            obj.setListenersPeak(Integer.valueOf(listeners_peak));
                        }
                    }
                } catch (XPathExpressionException e) {
                    Log.e("CM-StreamStatsService", "XPException while fetching Stats: " + e);
                } catch (SAXException e) {
                    Log.e("CM-StreamStatsService", "SAXException while fetching Stats: " + e);
                } catch (ParserConfigurationException e) {
                    Log.e("CM-StreamStatsService", "PCException while fetching Stats: " + e);
                } catch (ClientProtocolException e) {
                    Log.e("CM-StreamStatsService", "CPException while fetching Stats: " + e);
                } catch (IOException e) {
                    Log.e("CM-StreamStatsService", "UOException while fetching Stats: " + e);
                }

                sendResponseIntent(obj);
            }
        }
    }

    void sendResponseIntent(StreamStats obj)
    {
        Intent localIntent = new Intent(Constants.BROADCAST_STREAM_STATS_SERVICE).putExtra(Constants.EXTRA_DATA_STATS_OBJ, obj);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
