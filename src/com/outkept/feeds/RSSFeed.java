package com.outkept.feeds;

import com.outkept.Outkept;
import com.outkept.notifiers.Notifier;
import java.net.InetAddress;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import org.w3c.dom.CharacterData;

/**
 *
 * @author pedrodias
 */
public class RSSFeed extends Feed {

    private String feed;
    private boolean verify;
    private String field;

    public RSSFeed(String name, String feed, String field, boolean verify, int pooling) {
        this.name = name;
        this.feed = feed;
        this.field = field;
        this.verify = verify;
        this.pooling = pooling;
    }

    @Override
    public void run() {
        while (running) {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                URL u = new URL(this.feed);
                Document doc = builder.parse(u.openStream());

                NodeList nodes = doc.getElementsByTagName("item");

                for (int i = 0; i < nodes.getLength(); i++) {
                    Element element = (Element) nodes.item(i);
                    if (element != null) {
                        String urlc = getElementValue(element, this.field);

                        if (urlc != null) {
                            String aux[] = urlc.split("/");

                            if (aux.length >= 2) {
                                String host = aux[2];
                                InetAddress addr = InetAddress.getByName(host);
                                String ip = addr.getHostAddress();

                                if ((!verify || Outkept.ipsDomain.contains(ip)) && !this.wasFired(urlc)) {
                                    Outkept.notifier.notify(Notifier.MAIL, this.name + " reported " + urlc);
                                    Outkept.notifier.notify(Notifier.TWITTER, this.name + " reported " + urlc);
                                    this.fire(urlc);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                if (!(ex instanceof java.net.UnknownHostException)) {
                    System.out.println("RSS feed failure " + this.name);
                }
            }
            try {
                Thread.sleep(this.pooling * 60 * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(RSSFeed.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private String getDataFromElement(Element e) {
        try {
            Node child = e.getFirstChild();
            if (child instanceof CharacterData) {
                CharacterData cd = (CharacterData) child;
                return cd.getData();
            } else {
                return child.getNodeValue();
            }
        } catch (Exception ex) {
            Logger.getLogger(RSSFeed.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    private String getElementValue(Element parent, String label) {
        return getDataFromElement((Element) parent.getElementsByTagName(label).item(0));
    }
}
