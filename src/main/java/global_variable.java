import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import new_types.*;

//Definition der globalen Variablen
public class global_variable {
	public static List<String> URIs = new ArrayList<String>(); //siehe Ausarbeitung Abschnitt 3.6 ff.
	public static List<PartnerLink> PartnerLinks = new ArrayList<PartnerLink>(); //Liste aller Partner Links
	public static Element[] roots; //Wurzelelemente aller prozesse
	public static String[] processes; //namen aller prozesse
	public static List<PL_pair> PL_pairs = new ArrayList<PL_pair>(); //zu allen PL die passenden PLs
	public static final Namespace wsu = Namespace.getNamespace("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"); //Namensraum, der in jeden BPEL-Prozess eingefügt wird
	public static int[][] B; //Matrix B, siehe Ausarbeitung Abschnitt 3.3
	public static int[][] D; //Matrix D, siehe Ausarbeitung Abschnitt 3.3
	public static int wsu_id_counter = 1; //Zähler für die Werte der wsu:id-Attribute
	public static int i; //Anzahl der gegebenen BPEL-Prozesse
	public static List<ActivityLink> ActivityLinks = new ArrayList<ActivityLink>(); //Liste aller Aktivitätsverbindungen
	public static List<List<part_ref>> PRefs; //Menge der Teilnehmerreferenzen, die zu keiner Teilnehmermenge gehören
	public static List<partSet> partSets; //temporäre Menge von Teilnehmermengen
	public static int counter; //Zähler, über den bei der Bestimmung von Teilnehmern direkt erkannt werden kann, in welchem Prozess aktuell gesucht wird
	public static List<List<partSet>> ParticipantSets; //Menge der Teilnehmermengen
	public static part current_part = null; //aktuelle Teilnehmerreferenz, wird bei der Bestimmung von Teilnehmern benötigt
	public static List<corr_prop> CORR_PROPs = new ArrayList<corr_prop>(); //Menge der Correlation Properties
	public static List<String> NCn = new ArrayList<String>(); //Menge der verwendeten NCNames der Correlation Properties
	public static List<branch> branches = new ArrayList<branch>(); //Menge der Verzweigungen, die bei der Bestimmung von Teilnehmern bearbeitet werden müssen
	public static List<String> PL_names; //Namen der Partner Links, wird benötigt, um zu bestimmen, ob ein gefundener Partner Link umbenannt werden muss
	public static List<String> name_list; //Namen der Partner Links, wird benötigt, um zu bestimmen, ob ein gefundener Partner Link umbenannt werden muss
	public static List<Element> FE = new ArrayList<Element>(); //Menge FE, siehe Ausarbeitung Abschnitt 3.7
	public static List<Document> WSDL_files = new ArrayList<Document>(); //Liste der gegebenen WSDL-Dateien
	public static List<Element> remove_elems = new ArrayList<Element>(); //Liste der Elemente, die bei der Generierung der PBDs aus den entsprechenden BPEL-Prozessen entfernt werden müssen
	public static String Error = ""; //String, in dem wir mögliche Fehlerursachen vermerken
}
