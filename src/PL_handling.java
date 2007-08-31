import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import new_types.PL_pair;
import new_types.PartnerLink;

import org.jdom.Element;


public class PL_handling {

	
	
	//alle Namen der Partner Links sammeln unterhalb von elem
	@SuppressWarnings("unchecked")
	private static void get_PL_names(Element elem)
	{
		String type = elem.getName();
		if (type == "process" || type=="sequence" || type=="flow" || type=="if" || type=="while" || type=="repeatUntil" || type=="forEach" || type=="pick" || type=="onMessage" || type=="onAlarm" || type=="scope" || type=="elseif" || type=="else" || type=="partnerLinks"){
			List<Element> children = new ArrayList<Element>();
			children = elem.getChildren();
			if (children.isEmpty() == false)
			{
				Iterator<Element> list = children.iterator();
				while (list.hasNext())
				{
					Element current_element = list.next();
					get_PL_names(current_element);
				}
			}
		}
		else if (type=="partnerLink")
		{	
			global_variable.name_list.add(elem.getAttributeValue("name"));
		}
		
	}
	
	
	//Partner Link umbenennen
	@SuppressWarnings("unchecked")
	private static void rename_PartnerLink(String old_name, String new_name, Element elem)
	{
		if (elem.getAttributeValue("partnerLink") != null)
		{
			if (elem.getAttributeValue("partnerLink").equals(old_name))
			{
				elem.setAttribute("partnerLink", new_name);
			}
		}
		List<Element> children = new ArrayList<Element>();
		children = elem.getChildren();
		if (children.isEmpty() == false)
		{
			Iterator<Element> list = children.iterator();
			while (list.hasNext())
			{
				Element current_element = list.next();
				rename_PartnerLink(old_name, new_name, current_element);
			}
		}
	}
	
	//findet die Partner Links, für jeden Partner Link wird ein Objekt PartnerLink erzeugt
	@SuppressWarnings("unchecked")
	private static void find_PartnerLinks(Element root, String process_name, int j) {
		String type = root.getName();
		if (type == "process" || type=="sequence" || type=="flow" || type=="if" || type=="while" || type=="repeatUntil" || type=="forEach" || type=="pick" || type=="onMessage" || type=="onAlarm" || type=="scope" || type=="elseif" || type=="else" || type=="partnerLinks"){
			List<Element> children = new ArrayList<Element>();
			children = root.getChildren();
			if (children.isEmpty() == false)
			{
			Iterator<Element> list = children.iterator();
			while (list.hasNext())
				{Element current_element = list.next();
				find_PartnerLinks(current_element, process_name, j);
				}
			}
		}
		else if (type=="partnerLink")
		{	
			PartnerLink tmp_PL;
			tmp_PL = new PartnerLink();
			
			int cnt = 10000;
			String new_name = root.getAttributeValue("name");
			boolean rename = false;
			boolean already_exists = false;
			while (global_variable.PL_names.contains(new_name) || already_exists)
			{
				rename = true;
				already_exists = false;
				new_name = root.getAttributeValue("name") + cnt;
				cnt++;
				global_variable.name_list = new ArrayList<String>();
				get_PL_names(root.getParentElement().getParentElement());
				if (global_variable.name_list.contains(new_name))
				{
					already_exists = true;
				}
			}
			
			if (rename)
			{
				String old_name = root.getAttributeValue("name");
				root.setAttribute("name", new_name);
				rename_PartnerLink(old_name, new_name, root.getParentElement().getParentElement());
			}
			
			global_variable.PL_names.add(new_name);
			
			tmp_PL.process = process_name;
			tmp_PL.PL_name = root.getAttributeValue("name");
			String temp = root.getAttributeValue("partnerLinkType");
			int l = temp.indexOf(":");
			tmp_PL.PL_typ = temp.substring(l+1);
			String temp2;
			temp2 = temp.substring(0, l);
			tmp_PL.namespace_URI = global_variable.roots[j].getNamespace(temp2).getURI();
			tmp_PL.my_role = root.getAttributeValue("myRole");
			tmp_PL.partner_role = root.getAttributeValue("partnerRole");
			global_variable.PartnerLinks.add(tmp_PL);
		}
	}
	
	//Menge der PartnerLink Paare wird gebildet. In jedem Paar sind zwei Partner Links enthalten, die zueinander passen
	private static void find_PL_pairs()
	{
		for (int j = 0; j < global_variable.PartnerLinks.size(); j++){
			for (int k = 0; k < global_variable.PartnerLinks.size(); k++){
				if (j!=k){
					if (global_variable.PartnerLinks.get(j).PL_typ.equals(global_variable.PartnerLinks.get(k).PL_typ)){
						if (global_variable.PartnerLinks.get(j).namespace_URI.equals(global_variable.PartnerLinks.get(k).namespace_URI)){
							if (!(global_variable.PartnerLinks.get(j).process.equals(global_variable.PartnerLinks.get(k).process))){
								if (global_variable.PartnerLinks.get(j).my_role != null && global_variable.PartnerLinks.get(j).partner_role != null && global_variable.PartnerLinks.get(k).my_role != null && global_variable.PartnerLinks.get(k).partner_role != null)
								{
									if (global_variable.PartnerLinks.get(j).my_role.equals(global_variable.PartnerLinks.get(k).partner_role) && global_variable.PartnerLinks.get(j).partner_role.equals(global_variable.PartnerLinks.get(k).my_role))
									{
										PL_pair temp = new PL_pair();
										temp.PL1 = global_variable.PartnerLinks.get(j);
										temp.PL2 = global_variable.PartnerLinks.get(k);
										global_variable.PL_pairs.add(temp);
									}
								}
								else if (global_variable.PartnerLinks.get(j).my_role == null && global_variable.PartnerLinks.get(j).partner_role != null && global_variable.PartnerLinks.get(k).my_role != null && global_variable.PartnerLinks.get(k).partner_role != null)
								{
									if (global_variable.PartnerLinks.get(j).partner_role.equals(global_variable.PartnerLinks.get(k).my_role))
									{
										PL_pair temp = new PL_pair();
										temp.PL1 = global_variable.PartnerLinks.get(j);
										temp.PL2 = global_variable.PartnerLinks.get(k);
										global_variable.PL_pairs.add(temp);
									}
								}
								else if (global_variable.PartnerLinks.get(j).my_role != null && global_variable.PartnerLinks.get(j).partner_role == null && global_variable.PartnerLinks.get(k).my_role != null && global_variable.PartnerLinks.get(k).partner_role != null)
								{
									if (global_variable.PartnerLinks.get(j).my_role.equals(global_variable.PartnerLinks.get(k).partner_role))
									{
										PL_pair temp = new PL_pair();
										temp.PL1 = global_variable.PartnerLinks.get(j);
										temp.PL2 = global_variable.PartnerLinks.get(k);
										global_variable.PL_pairs.add(temp);
									}
								}
								else if (global_variable.PartnerLinks.get(j).my_role != null && global_variable.PartnerLinks.get(j).partner_role != null && global_variable.PartnerLinks.get(k).my_role == null && global_variable.PartnerLinks.get(k).partner_role != null)
								{
									if (global_variable.PartnerLinks.get(j).my_role.equals(global_variable.PartnerLinks.get(k).partner_role))
									{
										PL_pair temp = new PL_pair();
										temp.PL1 = global_variable.PartnerLinks.get(j);
										temp.PL2 = global_variable.PartnerLinks.get(k);
										global_variable.PL_pairs.add(temp);
									}
								}
								else if (global_variable.PartnerLinks.get(j).my_role != null && global_variable.PartnerLinks.get(j).partner_role != null && global_variable.PartnerLinks.get(k).my_role != null && global_variable.PartnerLinks.get(k).partner_role == null)
								{
									if (global_variable.PartnerLinks.get(j).partner_role.equals(global_variable.PartnerLinks.get(k).my_role))
									{
										PL_pair temp = new PL_pair();
										temp.PL1 = global_variable.PartnerLinks.get(j);
										temp.PL2 = global_variable.PartnerLinks.get(k);
										global_variable.PL_pairs.add(temp);
									}
								}
								else if (global_variable.PartnerLinks.get(j).my_role == null && global_variable.PartnerLinks.get(j).partner_role != null && global_variable.PartnerLinks.get(k).my_role != null && global_variable.PartnerLinks.get(k).partner_role == null)
								{
									if (global_variable.PartnerLinks.get(j).partner_role.equals(global_variable.PartnerLinks.get(k).my_role))
									{
										PL_pair temp = new PL_pair();
										temp.PL1 = global_variable.PartnerLinks.get(j);
										temp.PL2 = global_variable.PartnerLinks.get(k);
										global_variable.PL_pairs.add(temp);
									}
								}
								else if (global_variable.PartnerLinks.get(j).my_role != null && global_variable.PartnerLinks.get(j).partner_role == null && global_variable.PartnerLinks.get(k).my_role == null && global_variable.PartnerLinks.get(k).partner_role != null)
								{
									if (global_variable.PartnerLinks.get(j).my_role.equals(global_variable.PartnerLinks.get(k).partner_role))
									{
										PL_pair temp = new PL_pair();
										temp.PL1 = global_variable.PartnerLinks.get(j);
										temp.PL2 = global_variable.PartnerLinks.get(k);
										global_variable.PL_pairs.add(temp);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	public static void pl_handling()
	{
		//PartnerLinks finden
		for (int j = 0; j < global_variable.i; j++)
		{
		global_variable.PL_names = new ArrayList<String>();
		find_PartnerLinks(global_variable.roots[j], global_variable.processes[j], j);
		}
		
		//passende PartnerLinks finden
		find_PL_pairs();
	}
	
}
