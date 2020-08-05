/*
 *      Copyright (C) Jordan Erickson                     - 2014-2020,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2020
 *       on behalf of Jordan Erickson.
 *
 * This file is part of Cool Mic.
 *
 * Cool Mic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cool Mic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cool Mic.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cc.echonet.coolmicapp.Icecast.Request;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import cc.echonet.coolmicapp.Icecast.State;

public class Stats extends Request {

    public Stats(@NotNull URL url) throws IOException {
        super(url);
        Log.d("CM-StreamStatsService", "url=" + url);
    }

    private @NotNull String exceptionToString(@NotNull Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    @Override
    public void finish() {
        try {
            connect();

            if (connection.getResponseCode() != 200) {
                Log.e("CM-StreamStatsService", "HTTP error, invalid server status code: " + connection.getResponseMessage());
                setError();
                return;
            }

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(connection.getInputStream());

            Log.d("CM-StreamStatsService", "Parsed Document " + doc.toString());

            final XPathFactory xpathFactory = XPathFactory.newInstance();
            final XPath xpath = xpathFactory.newXPath();

            Log.d("CM-StreamStatsService", "post xpath");

            final XPathExpression expr_listeners = xpath.compile("/icestats/source/listeners/text()");
            final XPathExpression expr_listeners_peak = xpath.compile("/icestats/source/listener_peak/text()");

            Log.d("CM-StreamStatsService", "post xpath compile");

            final String listeners = (String) expr_listeners.evaluate(doc, XPathConstants.STRING);
            final String listeners_peak = (String) expr_listeners_peak.evaluate(doc, XPathConstants.STRING);

            Log.d("CM-StreamStatsService", "post xpath eval " + listeners + " " + listeners_peak);

            int listenersCurrent = -1;
            int listenersPeak = -1;

            if (!listeners.isEmpty()) {
                listenersCurrent = Integer.valueOf(listeners);
            } else {
                Log.d("CM-StreamStatsService", "found no listeners");
            }

            if (!listeners_peak.isEmpty()) {
                listenersPeak = Integer.valueOf(listeners_peak);
            } else {
                Log.d("CM-StreamStatsService", "found no listeners peak");
            }

            response = new cc.echonet.coolmicapp.Icecast.Response.Stats(listenersCurrent, listenersPeak);
            state = State.FINISHED;
        } catch (XPathExpressionException e) {
            Log.e("CM-StreamStatsService", "XPException while fetching Stats: " + exceptionToString(e));
            setError();
        } catch (SAXException e) {
            Log.e("CM-StreamStatsService", "SAXException while fetching Stats: " + exceptionToString(e));
            setError();
        } catch (ParserConfigurationException e) {
            Log.e("CM-StreamStatsService", "PCException while fetching Stats: " + exceptionToString(e));
            setError();
        } catch (IOException e) {
            Log.e("CM-StreamStatsService", "IOException while fetching Stats: " + exceptionToString(e));
            setError();
        } catch (Exception e) {
            Log.e("CM-StreamStatsService", "Exception while fetching Stats: " + exceptionToString(e));
            setError();
        }
    }

    @Override
    public cc.echonet.coolmicapp.Icecast.Response.Stats getResponse() {
        return (cc.echonet.coolmicapp.Icecast.Response.Stats) super.getResponse();
    }
}
