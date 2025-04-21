
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
import java.io.File;
import java.util.HashMap;
import java.util.List;

public class XMLHandler {
    public static void save(File file, List<Feld> felder, List<Kante> kanten) {
        int walkingIdFields = 0;
        HashMap<Integer, Integer> renewIds = new HashMap<>(); //old id, new id
        int walkingIdKrone = 0;
        int walkingIdSperrstein = 0;
        int walkingIdSpawns = 0;

        try {
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
                    System.out.println(f.data);
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

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(file));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load(File file, List<Feld> felder, List<Kante> kanten) {
        try {
            felder.clear();
            kanten.clear();
            Feld.nextId = 0;
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList feldNodes = doc.getElementsByTagName("feld");
            for (int i = 0; i < feldNodes.getLength(); i++) {
                Element e = (Element) feldNodes.item(i);
                int id = Integer.parseInt(e.getAttribute("id"));
                int x = Integer.parseInt(e.getAttribute("posX"));
                int y = Integer.parseInt(e.getAttribute("posY"));
                String data = e.getAttribute("data");

                Feld f = new Feld(x, y);
                f.id = id;
                f.data = data;

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
