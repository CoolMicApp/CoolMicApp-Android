package cc.echonet.coolmicapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

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

                    Log.e("CM-StreamStatsService", "URL:" + url);

                    HttpURLConnection conn = (HttpURLConnection) new URL(u.toString()).openConnection();
                    conn.setUseCaches(false);

                    conn.setDoOutput(false);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept-Charset", "utf-8");
                    conn.setRequestProperty("Accept-Encoding", "text/xml");
                    conn.setRequestProperty("Accept-Language", "en-US");
                    conn.setRequestProperty("Authorization","Basic " + Base64.encodeToString(u.getUserInfo().getBytes(),Base64.NO_WRAP));
                    conn.connect();

                    if (conn.getResponseCode() != 200) {
                        Log.e("CM-StreamStatsService", "HTTP error, invalid server status code: " + conn.getResponseMessage());
                    }
                    else {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document doc = builder.parse(conn.getInputStream());

                        Log.d("CM-StreamStatsService", "Parsed Document "+doc.toString());

                        XPathFactory xpathFactory = XPathFactory.newInstance();
                        XPath xpath = xpathFactory.newXPath();

                        Log.d("CM-StreamStatsService", "post xpath");

                        XPathExpression expr_listeners = xpath.compile("/icestats/source/listeners/text()");
                        XPathExpression expr_listeners_peak = xpath.compile("/icestats/source/listener_peak/text()");

                        Log.d("CM-StreamStatsService", "post xpath compile");

                        String listeners = (String) expr_listeners.evaluate(doc, XPathConstants.STRING);
                        String listeners_peak = (String) expr_listeners_peak.evaluate(doc, XPathConstants.STRING);

                        Log.d("CM-StreamStatsService", "post xpath eval "+listeners+ " "+listeners_peak);

                        if(!listeners.isEmpty()) {
                            obj.setListenersCurrent(Integer.valueOf(listeners));
                        }
                        else
                        {
                            Log.d("CM-StreamStatsService", "found no listeners");
                        }

                        if(!listeners_peak.isEmpty()) {
                            obj.setListenersPeak(Integer.valueOf(listeners_peak));
                        }
                        else
                        {
                            Log.d("CM-StreamStatsService", "found no listeners peak");
                        }
                    }
                } catch (XPathExpressionException e) {
                    Log.e("CM-StreamStatsService", "XPException while fetching Stats: " + exToString(e));
                } catch (SAXException e) {
                    Log.e("CM-StreamStatsService", "SAXException while fetching Stats: " + exToString(e));
                } catch (ParserConfigurationException e) {
                    Log.e("CM-StreamStatsService", "PCException while fetching Stats: " + exToString(e));
                } catch (IOException e) {
                    Log.e("CM-StreamStatsService", "IOException while fetching Stats: " + exToString(e));
                } catch (Exception e) {
                    Log.e("CM-StreamStatsService", "Exception while fetching Stats: " + exToString(e));
                }

                sendResponseIntent(obj);
            }
        }
    }

    String exToString(Exception ex)
    {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    void sendResponseIntent(StreamStats obj)
    {
        Intent localIntent = new Intent(Constants.BROADCAST_STREAM_STATS_SERVICE).putExtra(Constants.EXTRA_DATA_STATS_OBJ, obj);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
