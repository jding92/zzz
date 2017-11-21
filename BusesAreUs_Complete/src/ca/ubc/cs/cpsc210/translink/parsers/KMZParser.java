package ca.ubc.cs.cpsc210.translink.parsers;

import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class KMZParser {
    /**
     * Parse route information from XML data in a KMZ file.
     *
     * @param bytes string data to be parsed
     * @return route parsed from KMZ data
     */
    public static Map<String, List<Pair<LatLon>>> parseKMZ(byte[] bytes) {
        ZipFileMemoryIterator zfi = new ZipFileMemoryIterator(bytes);
        Map<String, List<Pair<LatLon>>> ans =  new HashMap<String, List<Pair<LatLon>>>();
        while (zfi.hasNext()) {
            MemoryFile x = zfi.next();
            if (x.getName().endsWith(".kml")) {
                // Now parse the XML data in that file
                String routeNumber = x.getName().replaceAll("\\.kml", "");
                List<Pair<LatLon>> routeMap = parseKML(x.getContents());
                ans.put(routeNumber, routeMap);
            }
        }

        return ans;
    }

    private static List<Pair<LatLon>> parseKML(String xmlData) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(false);
            SAXParser parser = spf.newSAXParser();
            MyHandler handler = new MyHandler();

            Reader sr = new StringReader(xmlData);
            InputSource is = new InputSource(sr);
            parser.parse(is, handler);
            return handler.getResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
class MyHandler extends DefaultHandler {
    StringBuilder sb = new StringBuilder();
    private static double EPS = 0.000003;
    private static double REPS = 0.000012;
    private static boolean close(LatLon last, LatLon first) {
        return Math.abs(last.getLatitude() - first.getLatitude()) < EPS
                && Math.abs(last.getLatitude() - first.getLatitude()) < EPS;
    }
    private static double dist(LatLon last, LatLon first) {
        return Math.abs(last.getLatitude() - first.getLatitude())
                + Math.abs(last.getLatitude() - first.getLatitude());
    }

    List<Pair<LatLon>> coordinates = new ArrayList<Pair<LatLon>>() {
        private Pair<LatLon> last = null;
        @Override
        public boolean add(Pair<LatLon> x) {
            if (last != null) {
                if (size() == 1) {
                    // Maybe we have to swap the first one
                    Pair<LatLon> first = get(0);
                    if (!close(first.second, x.first) && !close(first.second, x.second)) {
//                        System.out.println("Swapping first " + first + " because of " + x);
                        if (!(close(first.first, x.first) || close(first.first, x.second))) {
                            System.exit(1);
                            throw new RuntimeException("!close(" + first.first + ", " + x.first + ") || close(" + first.first + ", " + x.second + ")");
                        }
                        first.swap();
                    }
                }
                if (!close(last.second, x.first)) {
//                    System.out.println("!close(" + last.second + ", " + x.first + ")");
                    if (dist(last.second, x.second) < dist(last.second, x.first)) {
//                        System.out.println("Swapping " + x + " because of " + last);
                        x.swap();
                    }
                    if (!close(last.second, x.first) && dist(last.second, x.first) > REPS) {
                        System.out.println("Jump of distance " + dist(last.second, x.first) + " from " + last.second + " to " + x.first);
                    }
                }
            }
            last = x;
            return super.add(x);
        }
    };

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    @Override
    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException {

        String key = qName;
        sb.setLength(0);
    }

    String numberP = "(-?[0-9]+\\.[0-9]+)";
    String commaP = " *, *";
    String zP = " *, *0 *";
    Pattern coordinatePattern = Pattern.compile(numberP + commaP + numberP + zP + numberP + commaP + numberP + zP);

    public Pair<LatLon> parseCoordinate(String coordinate) {
        Matcher m = coordinatePattern.matcher(coordinate);
        if (m.find()) {
            String sx1 = m.group(1);
            String sy1 = m.group(2);
            String sx2 = m.group(3);
            String sy2 = m.group(4);
            double x1 = Double.parseDouble(sx1);
            double y1 = Double.parseDouble(sy1);
            double x2 = Double.parseDouble(sx2);
            double y2 = Double.parseDouble(sy2);
            LatLon start = new LatLon(y1, x1);
            LatLon end = new LatLon(y2, x2);
            return new Pair<LatLon>(start, end);
        }
        return null;
    }

    @Override
    public void endElement(String namespaceURI,
                           String localName,
                           String qName)
            throws SAXException {

        String key = qName;
        String text = sb.toString();
        if (key.equals("coordinates")) {
            coordinates.add(parseCoordinate(text));
        }
        sb.setLength(0);
    }

    public void characters(char ch[], int start, int length)
            throws SAXException {
        sb.append(ch, start, length);
    }

    public List<Pair<LatLon>> getResult() {
        return coordinates;
    }
}

