package new_types;

import org.jdom.Element;

//Erweiterung des Elements, um direkt zu wissen, zu welchem Prozess
//dieses Element gehört und welche id dieser Prozess zugewiesen bekommen
//hat
public class Element_enhanced {
	public Element element;
	public String process;
	public int process_id;
}
