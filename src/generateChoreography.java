import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import new_types.ActivityLink;
import new_types.PartnerLink;
import new_types.partSet;
import new_types.part_ref;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class generateChoreography {

	//Participant Topology erzeugen
	@SuppressWarnings("unchecked")
	private static void generate_topology() throws FileNotFoundException, IOException
	{
		Document topology = new Document( new Element("topology") );
		Element root = topology.getRootElement();
		root.setAttribute("name", "topology");
		root.setAttribute("targetNamespace", "urn:chor");
		
		Element participantTypes = new Element("participantTypes");
		root.addContent(participantTypes);
		
		Element participants = new Element("participants");
		root.addContent(participants);
		
		Element messageLinks = new Element("messageLinks");
		root.addContent(messageLinks);
		
		//Participant Types erzeugen
		for (int m = 0; m < global_variable.i; m++)
		{
			Element ptype = new Element("participantType");
			ptype.setAttribute("name", global_variable.roots[m].getAttributeValue("name")+"_type");
			
			Namespace new_NS;
			new_NS = Namespace.getNamespace(global_variable.roots[m].getAttributeValue("name"), global_variable.roots[m].getAttributeValue("targetNamespace"));
			root.addNamespaceDeclaration(new_NS);
			ptype.setAttribute("participantBehaviorDescription", global_variable.roots[m].getAttributeValue("name")+ ":" +global_variable.roots[m].getAttributeValue("name"));
			participantTypes.addContent(ptype);
		}
		
		//Participants erzeugen
		for (int m=0; m < global_variable.ParticipantSets.size(); m++)
		{
			List<partSet> PartSets = global_variable.ParticipantSets.get(m);
			List<partSet> PS_temp = new ArrayList<partSet>();
			List<partSet> old_PS = new ArrayList<partSet>();
			List<Element> FE2 = new ArrayList<Element>();
			
			//äußerste Menge für einen Prozess
			//sie besteht aus höchstens einer Teilnehmermenge
			for(int n = 0; n < PartSets.size(); n++)
			{
				partSet tmp = PartSets.get(n);
				boolean test = true;
				for (int o = 0; o < PartSets.size(); o++)
				{
					if (PartSets.get(o).part_sets.contains(tmp))
					{
						test = false;
					}
				}
				if (test)
				{
					PS_temp.add(tmp);
				}
			}
			
			//für eine äußere Teilnehmermenge wird ein
			//Element participantSet in der Topology erzeugt
			//zur Belegung der Attribute mit Werten siehe
			//Ausarbeitung Abschnitt 3.7
			for (int n = 0; n < PS_temp.size(); n++)
			{
			Element temp_elem = new Element("participantSet");
			temp_elem.setAttribute("name", PS_temp.get(n).name);
			temp_elem.setAttribute("type", global_variable.roots[m].getAttributeValue("name")+"_type");
			if (!PS_temp.get(n).L1.isEmpty())
			{
				List<ActivityLink> ActLinks;
				ActLinks = getActLinks(PS_temp.get(n).initial_acts);
				List<String> PartLinks = new ArrayList<String>();
				for (int z = 0; z < ActLinks.size(); z++)
				{
					for (int y = 0; y < ActLinks.get(z).send_acts.size(); y++)
					{
						String temp;
						temp = ActLinks.get(z).send_acts.get(y).getAttributeValue("partnerLink");
						if (!PartLinks.contains(temp))
						{
							PartLinks.add(temp);
						}
					}
				}
				for (int z = 0; z < PS_temp.get(n).L1.size(); z++)
				{
					if (PS_temp.get(n).L1.get(z).getName() == "forEach")
					{
						boolean test = iteratesSet(PS_temp.get(n).L1.get(z), PS_temp.get(n).L1.get(z).getAttributeValue("counterName"), PartLinks);
						if (test)
						{
							global_variable.FE.add(PS_temp.get(n).L1.get(z));
							FE2.add(PS_temp.get(n).L1.get(z));
						}
					}
				}
			}
			String tmp = "";
			for (int z = 0; z < PS_temp.get(n).L1.size(); z++)
			{
				if (FE2.contains(PS_temp.get(n).L1.get(z)))
				{
					if (tmp == "")
					{
						tmp = PS_temp.get(n).L1.get(z).getDocument().getRootElement().getAttributeValue("name") + ":" + PS_temp.get(n).L1.get(z).getAttributeValue("id", global_variable.wsu);
					}
					else
					{
						tmp = tmp + " " + PS_temp.get(n).L1.get(z).getDocument().getRootElement().getAttributeValue("name") + ":" + PS_temp.get(n).L1.get(z).getAttributeValue("id", global_variable.wsu);
					}
				}
			}
			if (tmp != "")
			{
			temp_elem.setAttribute("forEach", tmp);
			}
			
			//zu einer Teilnehmermenge gehört immer eine Teilnehmerreferenz
			//für eine Teilnehmerreferenz erzeugen wir ein participant Element
			//in der Topology, wobei dieses participant Element
			//Kind des vorherigen participantSet Elements ist
			Element temp_elem2 = new Element("participant");
			temp_elem2.setAttribute("name", PS_temp.get(n).part_ref.name);
			if (tmp != "")
			{
				temp_elem2.setAttribute("forEach", tmp);
			}
			
			tmp = "";
			for (int z = 0; z < PS_temp.get(n).L1.size(); z++)
			{
				if (!FE2.contains(PS_temp.get(n).L1.get(z)))
				{
					Element scope = null;
					List<Element> children = PS_temp.get(n).L1.get(z).getChildren();
					if (!children.isEmpty())
					{
						Iterator<Element> list = children.iterator();
						while(list.hasNext())
						{
							Element current_el = list.next();
							if (current_el.getName() == "scope")
							{
								scope = current_el;
							}
						}
					}
					if (scope != null)
					{
						if (tmp == "")
						{
							tmp = PS_temp.get(n).L1.get(z).getDocument().getRootElement().getAttributeValue("name") + ":" + scope.getAttributeValue("id", global_variable.wsu);
						}
						else
						{
							tmp = tmp + " " + PS_temp.get(n).L1.get(z).getDocument().getRootElement().getAttributeValue("name") + ":" + scope.getAttributeValue("id", global_variable.wsu);
						}
					}
				}
			}
			if (tmp != "")
			{
				temp_elem2.setAttribute("scope", tmp);
			}
			
			tmp = "";
			for (int z = 0; z < PS_temp.get(n).part_ref.select_parts.size(); z++)
			{
				if (tmp == "")
				{
					tmp = PS_temp.get(n).part_ref.select_parts.get(z);
				}
				else
				{
					if (!tmp.contains(PS_temp.get(n).part_ref.select_parts.get(z)))
					{
					tmp = tmp + " " + PS_temp.get(n).part_ref.select_parts.get(z);
					}
				}
			}
			if (tmp != "")
			{
				temp_elem2.setAttribute("selects", tmp);
			}
			
			temp_elem.addContent(temp_elem2);
			participants.addContent(temp_elem);	
			
			}
			old_PS.addAll(PS_temp);
			
			
			//restliche Participant Sets für einen Prozess, 
			//die keine äußere Menge bilden
			//entsprechend ist ein solches participantSet
			//Element ein Kind eines anderen participantSet
			//Elements
			while (!PS_temp.isEmpty())
			{
				PS_temp.clear();
				for (int z = 0; z < old_PS.size(); z++)
				{
					if (!old_PS.containsAll(old_PS.get(z).part_sets))
					{
						PS_temp.addAll(old_PS.get(z).part_sets);
					}
				}
				old_PS.addAll(PS_temp);
				
				for (int n = 0; n < PS_temp.size(); n++)
				{
				Element temp_elem = new Element("participantSet");
				temp_elem.setAttribute("name", PS_temp.get(n).name);
				temp_elem.setAttribute("type", global_variable.roots[m].getAttributeValue("name")+"_type");
				if (!PS_temp.get(n).L1.isEmpty())
				{
					List<ActivityLink> ActLinks;
					ActLinks = getActLinks(PS_temp.get(n).initial_acts);
					List<String> PartLinks = new ArrayList<String>();
					for (int z = 0; z < ActLinks.size(); z++)
					{
						for (int y = 0; y < ActLinks.get(z).send_acts.size(); y++)
						{
							String temp;
							temp = ActLinks.get(z).send_acts.get(y).getAttributeValue("partnerLink");
							if (!PartLinks.contains(temp))
							{
								PartLinks.add(temp);
							}
						}
					}
					for (int z = 0; z < PS_temp.get(n).L1.size(); z++)
					{
						if (PS_temp.get(n).L1.get(z).getName() == "forEach")
						{
							boolean test = iteratesSet(PS_temp.get(n).L1.get(z), PS_temp.get(n).L1.get(z).getAttributeValue("counterName"), PartLinks);
							if (test)
							{
								global_variable.FE.add(PS_temp.get(n).L1.get(z));
								FE2.add(PS_temp.get(n).L1.get(z));
							}
						}
					}
				}
				String tmp = "";
				for (int z = 0; z < PS_temp.get(n).L1.size(); z++)
				{
					if (FE2.contains(PS_temp.get(n).L1.get(z)))
					{
						if (tmp == "")
						{
							tmp = PS_temp.get(n).L1.get(z).getDocument().getRootElement().getAttributeValue("name") + ":" + PS_temp.get(n).L1.get(z).getAttributeValue("id", global_variable.wsu);
						}
						else
						{
							tmp = tmp + " " + PS_temp.get(n).L1.get(z).getDocument().getRootElement().getAttributeValue("name") + ":" + PS_temp.get(n).L1.get(z).getAttributeValue("id", global_variable.wsu);
						}
					}
				}
				if (tmp != "")
				{
				temp_elem.setAttribute("forEach", tmp);
				}
				
				//zu einer Teilnehmermenge gehört immer eine Teilnehmerreferenz
				Element temp_elem2 = new Element("participant");
				temp_elem2.setAttribute("name", PS_temp.get(n).part_ref.name);
				if (tmp != "")
				{
					temp_elem2.setAttribute("forEach", tmp);
				}
				
				tmp = "";
				for (int z = 0; z < PS_temp.get(n).L1.size(); z++)
				{
					if (!FE2.contains(PS_temp.get(n).L1.get(z)))
					{
						Element scope = null;
						List<Element> children = PS_temp.get(n).L1.get(z).getChildren();
						if (!children.isEmpty())
						{
							Iterator<Element> list = children.iterator();
							while(list.hasNext())
							{
								Element act_el = list.next();
								if (act_el.getName() == "scope")
								{
									scope = act_el;
								}
							}
						}
						if (scope != null)
						{
							if (tmp == "")
							{
								tmp = PS_temp.get(n).L1.get(z).getDocument().getRootElement().getAttributeValue("name") + ":" + scope.getAttributeValue("id", global_variable.wsu);
							}
							else
							{
								tmp = tmp + " " + PS_temp.get(n).L1.get(z).getDocument().getRootElement().getAttributeValue("name") + ":" + scope.getAttributeValue("id", global_variable.wsu);
							}
						}
					}
				}
				if (tmp != "")
				{
					temp_elem2.setAttribute("scope", tmp);
				}
				tmp = "";
				for (int z = 0; z < PS_temp.get(n).part_ref.select_parts.size(); z++)
				{
					if (tmp == "")
					{
						tmp = PS_temp.get(n).part_ref.select_parts.get(z);
					}
					else
					{
						if (!tmp.contains(PS_temp.get(n).part_ref.select_parts.get(z)))
						{
						tmp = tmp + " " + PS_temp.get(n).part_ref.select_parts.get(z);
						}
					}
				}
				if (tmp != "")
				{
					temp_elem2.setAttribute("selects", tmp);
				}
								
				temp_elem.addContent(temp_elem2);
				
				//Namen finden von Set, von dem aktuelles Set Teilmenge ist
				String set_name = null;
				for (int z = 0; z < PartSets.size(); z++)
				{
					if (PartSets.get(z).part_sets.contains(PS_temp.get(n)))
					{
						set_name = PartSets.get(z).name;
					}
				}
				//mit dem Namen die Obermenge finden
				Element superset;
				superset = get_superset(participants, set_name);
				superset.addContent(temp_elem);
				}
			}//hier endet while schleife
			
			
		}//hier endet das iterieren über teilnehmermengen
		
		//die einfachen Teilnehmerreferenzen einfügen
		for (int m=0; m < global_variable.PRefs.size(); m++)
		{
			List<part_ref> tmp_refs = global_variable.PRefs.get(m);
			
			//die einfachen Teilnehmerreferenzen für jeden Prozess
			for (int w = 0; w < tmp_refs.size(); w++)
			{
				part_ref current_ref = tmp_refs.get(w);
				Element tmp_elem = new Element("participant");
				tmp_elem.setAttribute("name", current_ref.pref.name);
				String tmp = "";
				for (int y = 0; y < current_ref.pref.select_parts.size(); y++)
				{
					if (tmp == "")
					{
						tmp = current_ref.pref.select_parts.get(y);
					}
					else
					{
						if (!tmp.contains(current_ref.pref.select_parts.get(y)))
						{
						tmp = tmp + " " + current_ref.pref.select_parts.get(y);
						}
					}
				}
				if (tmp != "")
				{
					tmp_elem.setAttribute("selects", tmp);
				}
				if (current_ref.pset == null)
				{
					tmp_elem.setAttribute("type", global_variable.roots[m].getAttributeValue("name")+"_type");
					participants.addContent(tmp_elem);
				}
				else
				{
					Element superset;
					superset = get_superset(participants, current_ref.pset.name);
					superset.addContent(tmp_elem);
				}
			}
			
		}
		
		//Message Links erzeugen
		//Belegung der Attribute mit Werten siehe
		//Ausarbeitung Abschnitt 3.7
		for (int z = 0; z < global_variable.ActivityLinks.size(); z++)
		{
			ActivityLink act_elem = global_variable.ActivityLinks.get(z);
			Element new_elem = new Element("messageLink");
			new_elem.setAttribute("name", ""+(z+1));
			boolean senders = false;
			String tmp = "";
			
			//Sender
			for (int y = 0; y < act_elem.send_part_sets.size(); y++)
			{
				if (tmp == "")
				{
					tmp = act_elem.send_part_sets.get(y).name;
				}
				else
				{
					if (!tmp.contains(act_elem.send_part_sets.get(y).name))
					{
						senders = true;
						tmp = tmp + " " + act_elem.send_part_sets.get(y).name;
					}
					
				}
			}
			for (int y = 0; y < act_elem.send_parts.size(); y++)
			{
				if (tmp == "")
				{
					tmp = act_elem.send_parts.get(y).name;
				}
				else
				{
					if (!tmp.contains(act_elem.send_parts.get(y).name))
					{
						senders = true;
						tmp = tmp + " " + act_elem.send_parts.get(y).name;
					}
				}
			}
			if (senders)
			{
				new_elem.setAttribute("senders", tmp);
			}
			else
			{
				new_elem.setAttribute("sender", tmp);
			}
			
			//sendActivity
			new_elem.setAttribute("sendActivity", act_elem.send_acts.get(0).getAttributeValue("id", global_variable.wsu));
			
			//bindSenderTo
			tmp="";
			if (act_elem.bindSenderTo != null)
			{
				tmp = act_elem.bindSenderTo.name;
			}
			if (!act_elem.bindSendersToRefs.isEmpty())
			{
			for (int y = 0; y < act_elem.bindSendersToRefs.size(); y++)
			{
				if (act_elem.bindSendersToRefs.get(y) != null)
				{
				if (!tmp.contains(act_elem.bindSendersToRefs.get(y).name))
				{
					if (tmp == "")
					{
						tmp = act_elem.bindSendersToRefs.get(y).name;
					}
					else
					{
						tmp = tmp + " " + act_elem.bindSendersToRefs.get(y).name;
					}
				}
				}
			}
			}
			if (tmp != "")
			{
				new_elem.setAttribute("bindSenderTo", tmp);
			}
			
			//receiver
			tmp = "";
			for (int y = 0; y < act_elem.rec_parts.size(); y++)
			{
				if (tmp == "")
				{
					tmp = act_elem.rec_parts.get(y).name;
				}
				else
				{
					if (!tmp.contains(act_elem.rec_parts.get(y).name))
					{
						tmp = tmp + " " + act_elem.rec_parts.get(y).name;
					}
				}
			}
			new_elem.setAttribute("receiver", tmp);
			
			//receiveActivity
			new_elem.setAttribute("receiveActivity", act_elem.rec_acts.get(0).getAttributeValue("id", global_variable.wsu));
			
			new_elem.setAttribute("messageName", ""+(z+1));
			
			messageLinks.addContent(new_elem);
		}
		
		XMLOutputter outp = new XMLOutputter();
        outp.setFormat( Format.getPrettyFormat() );
        outp.output( topology, new FileOutputStream( new File("BPEL4Chor/topology.xml") ) );
	}
	
	
	//findet zu einer Liste von Elementen die zugehörigen Aktivitätsverbindungen
	//wobei in einer Aktivitätsverbindung nur ein Element dieser Liste vorkommen muss
	private static List<ActivityLink> getActLinks(List<Element> elems)
	{
		List<ActivityLink> ActLinks = new ArrayList<ActivityLink>();
		for (int m = 0; m < global_variable.ActivityLinks.size(); m++)
		{
			for (int n = 0; n < elems.size(); n++)
			{
				if (global_variable.ActivityLinks.get(m).rec_acts.contains(elems.get(n)))
				{
					if (!ActLinks.contains(global_variable.ActivityLinks.get(m)))
					{
						ActLinks.add(global_variable.ActivityLinks.get(m));
					}
				}
			}
		}
		return ActLinks;
	}
	
	
	//iteriert eine forEach-Schleife über eine Teilnehmermenge?
	//siehe Ausarbeitung Abschnitt 3.7
	@SuppressWarnings("unchecked")
	private static boolean iteratesSet(Element elem, String counterName, List<String> PartLinks){
		String type = elem.getName();
		if (type == "process" || type=="sequence" || type=="flow" || type=="if" || type=="while" || type=="repeatUntil" || type=="forEach" || type=="pick" || type=="onMessage" || type=="onAlarm" || type=="scope" || type=="elseif" || type=="else" || type == "assign")
		{
			List<Element> children = new ArrayList<Element>();
			children = elem.getChildren();
			if (children.isEmpty() == false)
				{
				Iterator<Element> list = children.iterator();
				while (list.hasNext())
					{
						Element act_element = list.next();
						boolean k = iteratesSet(act_element, counterName, PartLinks);
						if (k)
						{
							return true;
						}
					}
				}
			return false;
		}
		else if (type=="copy")
		{
			//Element from;
			boolean test = false;
			List<Element> children = new ArrayList<Element>();
			children = elem.getChildren();
			if (children.isEmpty() == false)
			{
				Iterator<Element> list = children.iterator();
				while (list.hasNext())
				{
					Element act_element = list.next();
					if (act_element.getName() == "to")
					{
						if (act_element.getAttributeValue("partnerLink") != null)
						{
							if (PartLinks.contains(act_element.getAttributeValue("partnerLink")))
							{
								test = true;
							}
						}
					}
				}
			}
			
			if (children.isEmpty() == false)
			{
				Iterator<Element> list = children.iterator();
				while (list.hasNext() && test)
				{
					Element act_element = list.next();
					if (act_element.getName() == "from")
					{
						List<Element> children2 = new ArrayList<Element>();
						children2 = act_element.getChildren();
						if (children2.isEmpty() == false)
						{
							Iterator<Element> list2 = children2.iterator();
							while(list2.hasNext())
							{
								Element current_child = list2.next();
								if (current_child.getName() == "query")
								{
									String tmp = current_child.getText();
									if (tmp.contains(counterName))
									{
									return true;
									}
								}
							}
						}
					}
				}
			}
					
		}
		
		return false;
	}
	
	//findet ein participantSet Element anhand des Namens
	@SuppressWarnings("unchecked")
	private static Element get_superset(Element elem, String name)
	{
		String type = elem.getName();
		if (type == "participants"){
			List<Element> children = new ArrayList<Element>();
			children = elem.getChildren();
			if (children.isEmpty() == false)
			{
				Iterator<Element> list = children.iterator();
				while (list.hasNext())
				{
					Element act_element = list.next();
					Element temp = null;
					temp = get_superset(act_element, name);
					if (temp != null)
					{
						return temp;
					}
				}
			}
		}
		else if (type=="participantSet")
		{	
			if (elem.getAttributeValue("name") == name)
			{
				return elem;
			}
			else
			{
				List<Element> children = new ArrayList<Element>();
				children = elem.getChildren();
				if (children.isEmpty() == false)
				{
					Iterator<Element> list = children.iterator();
					while (list.hasNext())
					{
						Element act_element = list.next();
						Element temp = null;
						temp = get_superset(act_element, name);
						if (temp != null)
						{
							return temp;
						}
					}
				}
			}
		}
		return null;
	}
	
	
	//die ParticipantGroundings generieren
	@SuppressWarnings("unchecked")
	private static void generate_groundings() throws FileNotFoundException, IOException, JDOMException
	{
		Document groundings = new Document( new Element("groundings") );
		Element root = groundings.getRootElement();
		root.setAttribute("topology", "top:topology");
		
		Namespace top;
		top = Namespace.getNamespace("top", "urn:chor");
		root.addNamespaceDeclaration(top);
		
		Element messageLinks = new Element("messageLinks");
		Element properties = new Element("properties");
		
		root.addContent(messageLinks);
		root.addContent(properties);
		
		int ns_declarations = 0;
		
		//alle Namensraumdeklarationen in den Groundings durchführen
		for (int z = 0; z < global_variable.URIs.size(); z++)
		{
			Namespace new_NS;
			new_NS = Namespace.getNamespace("ns"+(z+1), global_variable.URIs.get(z));
			root.addNamespaceDeclaration(new_NS);
			ns_declarations = z+1;
		}
			
		//alle ActivityLinks durchgehen
		for (int z = 0; z < global_variable.ActivityLinks.size(); z++)
		{
			Element tmp_elem;
			tmp_elem = global_variable.ActivityLinks.get(z).X;
			
			PartnerLink tmp_PL = null;
			String pl = tmp_elem.getAttributeValue("partnerLink");
			
			//finden des richtigen portTypes:
			
			//PartnerLinks nach passendem Partner Link durchsuchen
			for (int y = 0; y < global_variable.PartnerLinks.size(); y++)
			{
				String process_name1 = global_variable.PartnerLinks.get(y).process;
				String process_name2 = tmp_elem.getDocument().getRootElement().getAttributeValue("targetNamespace") + ":" + tmp_elem.getDocument().getRootElement().getAttributeValue("name");
				if (process_name1.equals(process_name2))
				{
					if (pl.equals(global_variable.PartnerLinks.get(y).PL_name))
					{
						tmp_PL = global_variable.PartnerLinks.get(y);
					}
				}
			}
			//String, nach dem wir suchen müssen suchen
			String search_string = ""; 
			if (tmp_elem.getName() == "invoke")
			{
				search_string = tmp_PL.partner_role;
			}
			else
			{
				search_string = tmp_PL.my_role;
			}
			
			Element wsdl_root = null;
			//suchen der richtigen WSDL-Datei
			for (int y = 0; y < global_variable.WSDL_files.size(); y++)
			{
				if (tmp_PL.namespace_URI.equals(global_variable.WSDL_files.get(y).getRootElement().getAttributeValue("targetNamespace")))
				{
					wsdl_root = global_variable.WSDL_files.get(y).getRootElement();
				}
			}
			
			//durchsuchen des WSDL-files nach dem richtigen partnerLinkType-Element
			Element p_link_type = null;
			List<Element> list = wsdl_root.getChildren();
			for (int y = 0; y < list.size(); y++)
			{
				if (list.get(y).getName() == "partnerLinkType")
				{
					if (list.get(y).getAttributeValue("name").equals(tmp_PL.PL_typ))
					{
						p_link_type = list.get(y);
					}
				}
			}
			
			String port_name = "";
			String port_uri = "";
			
			list = p_link_type.getChildren();
			for (int y = 0; y < list.size(); y++)
			{
				if (list.get(y).getName() == "role")
				{
					if (list.get(y).getAttributeValue("name").equals(search_string))
					{
						String tmp_string = list.get(y).getAttributeValue("portType");
						
						int l = tmp_string.indexOf(":");
						port_name = tmp_string.substring(l+1);
						String temp2;
						temp2 = tmp_string.substring(0, l);
						port_uri = wsdl_root.getNamespace(temp2).getURI();
						if (!global_variable.URIs.contains(port_uri))
						{
							ns_declarations++;
							Namespace new_NS;
							new_NS = Namespace.getNamespace("ns"+ns_declarations, port_uri);
							global_variable.URIs.add(port_uri);
							root.addNamespaceDeclaration(new_NS);
						}
					}
				}
			}
			
			//Element messageLink erzeugen
			//belegen der Attribute mit Werten
			//siehe Ausarbeitung Abschnitt 3.8
			Element ML_tmp = new Element("messageLink");
			ML_tmp.setAttribute("name", ""+(z+1));
			String PT_temp = "";
			for (int y = 1; y <= ns_declarations; y++)
			{
				if (root.getNamespace("ns"+y).getURI().equals(port_uri))
				{
					PT_temp = "ns"+y;
				}
				
			}
			PT_temp = PT_temp + ":" + port_name;
			ML_tmp.setAttribute("portType",PT_temp);
			ML_tmp.setAttribute("operation",tmp_elem.getAttributeValue("operation"));
			
			messageLinks.addContent(ML_tmp);
		}
		
	
		//corr_prop Elemente durchsuchen
		//property Elemente erzeugen
		//siehe Ausarbeitung Abschnitt 3.8
		for (int z = 0; z < global_variable.CORR_PROPs.size(); z++)
		{
			Element corr_tmp = new Element("property");
			corr_tmp.setAttribute("name", global_variable.CORR_PROPs.get(z).NCname);
			
			String prop_tmp = "";
			for (int y = 1; y <= ns_declarations; y++)
			{
				if (root.getNamespace("ns"+y).getURI().equals(global_variable.CORR_PROPs.get(z).namespace_URI))
				{
					prop_tmp = "ns"+y;
				}
				
			}
			prop_tmp = prop_tmp + ":" + global_variable.CORR_PROPs.get(z).property_name;
			corr_tmp.setAttribute("WSDLproperty", prop_tmp);
			
			properties.addContent(corr_tmp);
		}
		
		XMLOutputter outp = new XMLOutputter();
        outp.setFormat( Format.getPrettyFormat() );
        outp.output( groundings, new FileOutputStream( new File("BPEL4Chor/groundings.xml") ) );
	}
	
	
	//eliminiert die übergebenen Elemente, die für die PBDs entfernt werden müssen
	@SuppressWarnings("unchecked")
	private static void eliminate_elements(Element elem)
	{
		List<Element> list1 = elem.getChildren();
		List<Element> list2 = new ArrayList<Element>();
		boolean test = false;
		for (int z = 0; z < list1.size(); z++)
		{
			if (!global_variable.remove_elems.contains(list1.get(z)))
			{
				list2.add(list1.get(z));
			}
			else
			{
				test = true;
			}
		}
		if (test)
		{
		elem.removeContent();
		elem.addContent(list2);
		}
		
		List<Element> children = new ArrayList<Element>();
		children = elem.getChildren();
		if (children.isEmpty() == false)
			{
				Iterator<Element> list = children.iterator();
				while (list.hasNext())
					{
					Element act_element = list.next();
					eliminate_elements(act_element);
					}
			}
	}
	
	//sorgt für die Entfernung von Elementen aus den BPEL-Prozessen
	//so dass sich PBDs ergeben
	//siehe Ausarbeitung Abschnitt 3.9
	@SuppressWarnings("unchecked")
	private static void generate_PBD(Element elem)
	{
		String type = elem.getName();
		if (type == "process" || type=="sequence" || type=="flow" || type=="if" || type=="while" || type=="repeatUntil" || type=="forEach" || type=="pick" || type=="onAlarm" || type=="scope" || type=="elseif" || type=="else")
		{
			if (global_variable.FE.contains(elem))
			{
				List<Element> list1 = elem.getChildren();
				List<Element> list2 = new ArrayList<Element>();
				for (int z = 0; z < list1.size(); z++)
				{
					if (list1.get(z).getName() != "startCounterValue" && list1.get(z).getName() != "finalCounterValue")
					{
						list2.add(list1.get(z));
					}
				}
				elem.removeContent();
				elem.addContent(list2);
				elem.removeAttribute("counterName");
			}
			List<Element> list1 = elem.getChildren();
			List<Element> list2 = new ArrayList<Element>();
			for (int z = 0; z < list1.size(); z++)
			{
				if (list1.get(z).getName() != "partnerLinks")
				{
					list2.add(list1.get(z));
				}
			}
			elem.removeContent();
			elem.addContent(list2);
			List<Element> children = new ArrayList<Element>();
			children = elem.getChildren();
			if (children.isEmpty() == false)
				{
					Iterator<Element> list = children.iterator();
					while (list.hasNext())
						{
						Element act_element = list.next();
						generate_PBD(act_element);
						}
				}
		}
		else if (type == "onMessage")
		{
			elem.removeAttribute("partnerLink");
			elem.removeAttribute("operation");
			elem.removeAttribute("portType");
			boolean test = true;
			//testen
			if (getALset(elem, false).isEmpty())
			{
				test = false;
				global_variable.remove_elems.add(elem);
			}
			if (test)
			{
				List<Element> children = new ArrayList<Element>();
				children = elem.getChildren();
				if (children.isEmpty() == false)
					{
						Iterator<Element> list = children.iterator();
						while (list.hasNext())
							{
							Element act_element = list.next();
							generate_PBD(act_element);
							}
					}
			}
			
		}
		else if (type == "assign")
		{
			List<Element> remove = new ArrayList<Element>();
			List<Element> children = elem.getChildren();
			for (int z = 0; z < children.size(); z++)
			{
				Element act_elem = children.get(z);
				if (act_elem.getName() == "copy")
				{
					List<Element> list2 = act_elem.getChildren();
					for (int y = 0; y < list2.size(); y++)
					{
						if (list2.get(y).getAttributeValue("partnerLink") != null)
						{
							remove.add(act_elem);
						}
					}
				}
			}
			if (remove.containsAll(children))
			{
				global_variable.remove_elems.add(elem);
			}
			else
			{
				List<Element> list1 = elem.getChildren();
				List<Element> list2 = new ArrayList<Element>();
				for (int z = 0; z < list1.size(); z++)
				{
					if (!remove.contains(list1.get(z)))
					{
						list2.add(list1.get(z));
					}
				}
				elem.removeContent();
				elem.addContent(list2);
			}
		}
		else if (type=="receive" || type=="reply" || type=="invoke")
		{
			elem.removeAttribute("partnerLink");
			elem.removeAttribute("operation");
			elem.removeAttribute("portType");
			if (getALset(elem, false).isEmpty())
			{
				global_variable.remove_elems.add(elem);
			}
		}
	}
	
	//liefert eine Menge von Activity Links zurück, die zu einem Element gehören
	//der Boolean receiving_invoke gibt dabei an, ob bei einem invoke die Activity Links gesucht sind,
	//in denen das invoke die empfangende Aktivität ist oder diejenigen gesucht sind,
	//bei denen dieses invoke die sendende Aktivität ist
	private static List<ActivityLink> getALset (Element elem, boolean receiving_invoke)
	{
		List<ActivityLink> ALset;
		ALset = new ArrayList<ActivityLink>();
		ActivityLink al;
		Iterator<ActivityLink> list = global_variable.ActivityLinks.iterator();
		if (elem.getName() == "receive" || (elem.getName() == "invoke" && receiving_invoke) || elem.getName() == "onMessage" )
		{
			while (list.hasNext())
			{
				al = list.next();
				if (elem == al.Y)
				{
					ALset.add(al);
				}
			}
		}
		if (elem.getName() == "reply" || (elem.getName() == "invoke" && !receiving_invoke))
		{
			while (list.hasNext())
			{
				al = list.next();
				if (elem == al.X)
				{
					ALset.add(al);
				}
			}
		}
		return ALset;
	}
	
	public static void generate_choreography() throws FileNotFoundException, IOException, JDOMException
	{
		//Participant Topology erzeugen
		generate_topology();
		
		//Participant Groundings erzeugen
		generate_groundings();
		
		//für jeden gegebenen Prozess eine PBD erzeugen
		for (int j = 0; j < global_variable.i; j++)
		{
			generate_PBD(global_variable.roots[j]);
			eliminate_elements(global_variable.roots[j]);
			global_variable.roots[j].setAttribute("abstractProcessProfile","urn:HPI_IAAS:choreography:profile:2006/12-X");
			Namespace new_NS;
			new_NS = Namespace.getNamespace("http://docs.oasis-open.org/wsbpel/2.0/process/abstract");
			global_variable.roots[j].setNamespace(new_NS);
			XMLOutputter outp = new XMLOutputter();
	        outp.setFormat( Format.getPrettyFormat() );
	        outp.output( global_variable.roots[j], new FileOutputStream( new File("BPEL4Chor/"+global_variable.roots[j].getAttributeValue("name")+".bpel") ) );
		}
	}
}
