package new_types;

import java.util.List;
import org.jdom.Element;

//siehe Ausarbeitung Abschnitt 3.4
public 	class partSet extends genericParts {
	public part part_ref;
	public boolean bound;
	public List<partSet> part_sets;
	public List<Element> initial_acts;
}
