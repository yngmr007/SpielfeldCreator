import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

public class XMLHandler {
    private static Document createXmlDocument(List<Feld> felder, List<Kante> kanten) throws Exception {
        int walkingIdFields = 0;
        HashMap<Integer, Integer> renewIds = new HashMap<>();
        int walkingIdKrone = 0;
        int walkingIdSperrstein = 0;
        int walkingIdSpawns = 0;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("graph");
        doc.appendChild(root);

        Element felderElem = doc.createElement("felder");
        Element spawnsElem = doc.createElement("spawns");
        for (Feld f : felder) {
            Element feldElem = doc.createElement("feld");
            renewIds.put(f.id, walkingIdFields);
            feldElem.setAttribute("id", String.valueOf(walkingIdFields++));
            feldElem.setAttribute("posX", String.valueOf(f.posX));
            feldElem.setAttribute("posY", String.valueOf(f.posY));

            String data="";
            if (f.data != null){
                String[] split = f.data.split(":");
                f.data = split[0];
                switch (f.data) {
                    case "Krone" -> data = "Krone:" + walkingIdKrone++;
                    case "Sperrstein" -> data = "Sperrstein:" + walkingIdSperrstein++;
                    case "Spawn" ->  {
                        Element spawnElem = doc.createElement("spawn");
                        spawnElem.setAttribute("id", String.valueOf(walkingIdSpawns++));
                        spawnElem.setAttribute("feldId", String.valueOf(renewIds.get(f.id)));
                        data = "";
                        spawnsElem.appendChild(spawnElem);
                    }
                }
            }

            feldElem.setAttribute("data", data);
            felderElem.appendChild(feldElem);
        }
        root.appendChild(spawnsElem);
        root.appendChild(felderElem);

        Element kantenElem = doc.createElement("kanten");
        for (Kante k : kanten) {
            Element kanteElem = doc.createElement("kante");
            if(k.getFrom().id == k.getTo().id){
                break;
            }
            kanteElem.setAttribute("from", String.valueOf(renewIds.get(k.getFrom().id)));
            kanteElem.setAttribute("to", String.valueOf(renewIds.get(k.getTo().id)));
            kantenElem.appendChild(kanteElem);
        }
        root.appendChild(kantenElem);

        return doc;
    }

    public static void save(File file, List<Feld> felder, List<Kante> kanten) {
        try {
            Document doc = createXmlDocument(felder, kanten);
            
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String exportToString(List<Feld> felder, List<Kante> kanten) {
        try {
            Document doc = createXmlDocument(felder, kanten);
            
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // New helper method to parse XML document
    private static void parseXmlDocument(Document doc, List<Feld> felder, List<Kante> kanten) {
        felder.clear();
        kanten.clear();
        Feld.nextId = 0;

        // Parse fields
        NodeList feldNodes = doc.getElementsByTagName("feld");
        for (int i = 0; i < feldNodes.getLength(); i++) {
            Element e = (Element) feldNodes.item(i);
            Feld f = new Feld(
                    Integer.parseInt(e.getAttribute("posX")),
                    Integer.parseInt(e.getAttribute("posY"))
            );
            f.id = Integer.parseInt(e.getAttribute("id"));
            f.data = e.getAttribute("data");
            felder.add(f);
        }

        NodeList spawnNodes = doc.getElementsByTagName("spawn");
        for (int i = 0; i < spawnNodes.getLength(); i++) {
            Element e = (Element) spawnNodes.item(i);
            int feldId = Integer.parseInt(e.getAttribute("feldId"));
            Feld f = felder.stream().filter(feld -> feld.id == feldId).findFirst().orElse(null);
            if (f != null) {
                f.data = "Spawn";
            }
        }

        NodeList kanteNodes = doc.getElementsByTagName("kante");
        for (int i = 0; i < kanteNodes.getLength(); i++) {
            Element e = (Element) kanteNodes.item(i);
            int fromId = Integer.parseInt(e.getAttribute("from"));
            int toId = Integer.parseInt(e.getAttribute("to"));

            Feld from = felder.stream().filter(f -> f.id == fromId).findFirst().orElse(null);
            Feld to = felder.stream().filter(f -> f.id == toId).findFirst().orElse(null);

            if (from != null && to != null) {
                kanten.add(new Kante(from, to));
            }
        }

    }

    // Modified load method
    public static void load(File file, List<Feld> felder, List<Kante> kanten) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(file);
            parseXmlDocument(doc, felder, kanten);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // New method to load from server response
    public static void loadFromString(String xmlString, List<Feld> felder, List<Kante> kanten) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xmlString.getBytes()));
            parseXmlDocument(doc, felder, kanten);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
