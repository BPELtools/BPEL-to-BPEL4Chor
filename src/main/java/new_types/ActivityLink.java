package new_types;


import java.util.List;
import org.jdom.Element;

//Aktivitätsverbindung (siehe Ausarbeitung)
public class ActivityLink {
	public Element X;
	public Element Y;
	
	public String processX;
	public int processX_id; 
	
	public String processY;
	public int processY_id; 
	
	public int sX;
	public int sY;
	
	public int instX;
	public int instY;
	
	public List<partSet> send_part_sets;
	public List<part> send_parts;
	
	public List<part> rec_parts;
	
	public part bindSenderTo;
	public List<part> bindSendersToRefs;
	
	public List<Element> send_acts;
	public List<Element> rec_acts;
}
