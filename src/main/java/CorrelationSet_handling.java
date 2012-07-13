import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import new_types.corr_prop;

import org.jdom.Element;


public class CorrelationSet_handling {

	
	//findet alle <correlationSet>-Elemente für einen Teilbaum, dessen
	//Wurzel gegeben ist
	//für jede property wird ein corr_prop Element erzeugt
	//und der Menge CORR_PROPs hinzugefügt
	//siehe Ausarbeitung Abschnitt 3.6
	@SuppressWarnings("unchecked")
	private static void get_CORR_PROPs(Element elem, int process_id)
	{
		String type = elem.getName();
		if (type == "process" || type=="sequence" || type=="flow" || type=="if" || type=="while" || type=="repeatUntil" || type=="forEach" || type=="pick" || type=="onMessage" || type=="onAlarm" || type=="scope" || type=="elseif" || type=="else" || type=="correlationSets")
		{
			List<Element> children = new ArrayList<Element>();
			children = elem.getChildren();
			if (children.isEmpty() == false)
			{
				Iterator<Element> list = children.iterator();
				while (list.hasNext())
					{
					Element current_element = list.next();
					get_CORR_PROPs(current_element, process_id);
					}
			}
		}
		else if (type=="correlationSet")
		{	
			String tmp_string;
			List<String> tmp_list;
			tmp_list = new ArrayList<String>();
			tmp_string = elem.getAttributeValue("properties");
			
			if (tmp_string.contains(" "))
			{
				while (tmp_string.contains(" "))
				{
					int l = tmp_string.indexOf(" ");
					String tmp_string2;
					tmp_string2 = tmp_string.substring(0, l);
					tmp_list.add(tmp_string2);
					
					tmp_string = tmp_string.substring(l+1);
				}
				tmp_list.add(tmp_string);
	
			}
			else
			{
				tmp_list.add(tmp_string);
			}
			//tmp_list enthält die einzelnen properties
			String new_prop_value = "";
			
			for (int cntr = 0; cntr < tmp_list.size(); cntr++)
			{
				String tmp = tmp_list.get(cntr);
				int l = tmp.indexOf(":");
				String ncname = tmp.substring(l+1);
				
				String URI;
				String prefix;
				prefix = tmp.substring(0, l);
				URI = global_variable.roots[process_id].getNamespace(prefix).getURI();
	
				corr_prop temp_corr_prop;
				temp_corr_prop = exists_corr_prop(ncname, URI);
				
				if(temp_corr_prop == null)
				{
					corr_prop tmp_corr_prop = new corr_prop();
					tmp_corr_prop.property_name = ncname;
					tmp_corr_prop.namespace_URI = URI;
					String new_name = ncname;
					int cnt = 10000;
					while (global_variable.NCn.contains(new_name))
					{
						new_name = ncname+cnt;
						cnt++;
					}
					global_variable.NCn.add(new_name);
					tmp_corr_prop.NCname = new_name;
					global_variable.CORR_PROPs.add(tmp_corr_prop);
					
					if (new_prop_value == "")
					{
						new_prop_value = new_name;
					}
					else
					{
						new_prop_value = new_prop_value + " " + new_name;
					}
					
				}
				else
				{
					if (new_prop_value == "")
					{
						new_prop_value = temp_corr_prop.NCname;
					}
					else
					{
						new_prop_value = new_prop_value + " " + temp_corr_prop.NCname;
					}
				}
				
				if (!global_variable.URIs.contains(URI))
				{
					global_variable.URIs.add(URI);
				}
				
				
			}
			
			elem.setAttribute("properties", new_prop_value);
		}
	
	}
	
	
	//existiert für dieselbe property mit derselben NameSpace_URI bereits ein corr_propp-Element
	//so wird es zurückgeliefert
	private static corr_prop exists_corr_prop(String ncname, String URI)
	{
		for (int cntr = 0; cntr < global_variable.CORR_PROPs.size(); cntr++)
		{
			
			if (global_variable.CORR_PROPs.get(cntr).property_name.equals(ncname) && global_variable.CORR_PROPs.get(cntr).namespace_URI.equals(URI))
			{
				return global_variable.CORR_PROPs.get(cntr);
			}
		}
		return null;
	}

	
	public static void correlationSet_handling()
	{
		//Menge CORR_PROPs erzeugen
		for (int cntr = 0; cntr < global_variable.i; cntr++)
		{
			get_CORR_PROPs(global_variable.roots[cntr], cntr);
		}
	}
}
