import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import FileHandler.DeleteFiles;
import FileHandler.FileHandler;


public class FL_handling {

	//löscht alle Dateien im Verzeichnis BPEL4Chor
	private static void delete_files()
	{
		DeleteFiles.deleteFiles(new File("BPEL4Chor/"));
	}
	
	//liest die Prozesse aus XML Dateien ein
	private static void get_processes() throws JDOMException, IOException
	{
		//BPEL-Dateien aus dem Verzeichnis BPEL einlesen
		String svnVerzeichnis = "BPEL/";
		FileHandler fh = new FileHandler(svnVerzeichnis);
		List<File> files = fh.getFiles("(.*\\.bpel$)");
		System.out.printf("Found %d file%s.%n", files.size(),
				files.size() == 1 ? "" : "s");
		Document doc = null;
		global_variable.i=files.size();
		
		//array mit den Wurzelelementen der Prozesse
		global_variable.roots = new Element[global_variable.i];
		
		//array mit den Namen der prozesse
		global_variable.processes = new String[global_variable.i];
		
		global_variable.i=0;
		//füllen dieser Arrays
		for (File f:files){
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(f);
			global_variable.roots[global_variable.i] = doc.getRootElement();
			global_variable.processes[global_variable.i] = global_variable.roots[global_variable.i].getAttributeValue("targetNamespace") + ":" + global_variable.roots[global_variable.i].getAttributeValue("name");
			System.out.println(global_variable.processes[global_variable.i]);
			global_variable.i++;
			}
	}
	
	//prozesse gegebenenfalls umbenennen
	private static void rename_processes()
	{
		List<String> processes = new ArrayList<String>();
		for (int j = 0; j < global_variable.i; j++)
		{
			if (processes.contains(global_variable.roots[j].getAttributeValue("name")))
			{
				String neu_string = global_variable.roots[j].getAttributeValue("name");
				while (processes.contains(neu_string))
				{
				Random zufallszahl;
		        zufallszahl = new Random();
		        int index = zufallszahl.nextInt();
		        neu_string = global_variable.roots[j].getAttributeValue("name")+index;
				}
				processes.add(neu_string);
				global_variable.roots[j].setAttribute("name", neu_string);
				global_variable.processes[j]=global_variable.roots[j].getAttributeValue("targetNamespace")+ ":" + neu_string;
			}
			else
			{
				processes.add(global_variable.roots[j].getAttributeValue("name"));
			}
		}
	}
	
	//nummeriert entsprechende Aktivitäten anhand des neuen Attributs wsu:id durch
	@SuppressWarnings("unchecked")
	private static void wsuID_it(Element root) 
	{
		String typ = root.getName();
		Namespace wsu;
		wsu = Namespace.getNamespace("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
		if (typ == "process")
		{
			List<Element> children = new ArrayList<Element>();
			children = root.getChildren();
			if (children.isEmpty() == false)
			{
				Iterator<Element> list = children.iterator();
				while (list.hasNext())
					{
					Element current_element = list.next();
					wsuID_it(current_element);
					}
			}
		}
		else 
			if (typ=="sequence" || typ=="flow" || typ=="if" || typ=="while" || typ=="repeatUntil" || typ=="forEach" || typ=="pick" || typ=="onMessage" || typ=="onAlarm" || typ=="scope" || typ=="elseif" || typ=="else")
			{
				if (typ=="forEach" || typ=="scope" || typ=="onMessage")
				{
					root.setAttribute("id",  ""+global_variable.wsu_id_counter, wsu);
					global_variable.wsu_id_counter++;
				}
				List<Element> children = new ArrayList<Element>();
				children = root.getChildren();
				if (children.isEmpty() == false)
				{
					Iterator<Element> list = children.iterator();
					while (list.hasNext())
					{
						Element current_element = list.next();
						wsuID_it(current_element);
					}
				}
			}
			else 
				if (typ=="receive" || typ=="reply" || typ=="invoke")
				{
					root.setAttribute("id",  ""+global_variable.wsu_id_counter, wsu);
					global_variable.wsu_id_counter++;
				}
	}
	
	
	//WSDL-Dateien aus dem Verzeichnis BPEL einlesen
	private static void get_WSDL_definitions() throws JDOMException, IOException
	{
		String svnVerzeichnis = "BPEL/";
		FileHandler fh = new FileHandler(svnVerzeichnis);
		List<File> files = fh.getFiles("(.*\\.wsdl$)");
		Document doc = null;
		
		for (File f:files){
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(f);
			global_variable.WSDL_files.add(doc);
			}
		
		//Fehler vermerken
		if (global_variable.WSDL_files.isEmpty())
		{
			global_variable.Error = global_variable.Error + " Keine WSDL-Datei gefunden. Wurde die Endung .wsdl beachtet?";
		}
	}
	
	public static void fl_handling() throws Exception, IOException
	{
		//alles im Verzeichnis BPEL4Chor löschen
		delete_files();
		
		//Prozesse einlesen
		get_processes();
		
		//Prozesse gegebenenfalls umbenennen
		rename_processes();
		
		//wsu Namespace hinzufügen
		for (int j = 0; j < global_variable.i; j++)
		{
			global_variable.roots[j].addNamespaceDeclaration(global_variable.wsu);
		}
		
		//Aktivitäten mit wsu_id versehen
		for (int j = 0; j < global_variable.i; j++)
		{
			wsuID_it(global_variable.roots[j]);
		}
		
		//WSDL Dateien einlesen
		get_WSDL_definitions();
	}
}
