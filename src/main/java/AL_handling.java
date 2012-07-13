import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import new_types.ActivityLink;
import new_types.Element_enhanced;
import new_types.PL_pair;
import new_types.PartnerLink;
import new_types.branch;
import new_types.part;
import new_types.partSet;

import org.jdom.Element;


public class AL_handling {

	
	
	//überprüft, ob in der gegebenen liste ein Element vom typ "else" vorhanden ist
	private static boolean exists_else(List<Element> element_list)
	{
		if (element_list != null){
			Iterator<Element> list = element_list.iterator();
			while (list.hasNext()){
				Element act_elem = list.next();
				if (act_elem.getName() == "else")
				{
					return true;
				}
			}
			return false;
		}
		return false;
	}
	
	//zu gegebenem Prozess und PartnerLink wird die Menge der passenden PartnerLinks gesucht
	private static List<PartnerLink> find_matching_PLs(String process, String PLink){
		List<PartnerLink> list_of_matching_PLs = new ArrayList<PartnerLink>();
		Iterator<PL_pair> list = global_variable.PL_pairs.iterator();
		while (list.hasNext()){
			PL_pair PLs = list.next();
			if (PLs.PL1.process.equals(process)){
				if (PLs.PL1.PL_name.equals(PLink)){
					list_of_matching_PLs.add(PLs.PL2);
				}
			}
		}
		return list_of_matching_PLs;
	}
	
	//sucht zu zwei gegebenen Aktivitäten den Activity Link, der diese beiden Aktivitäten beinhaltet
	private static ActivityLink getAL(Element X, Element Y, List<ActivityLink> ALs)
	{
		int AL_size = ALs.size();
		for (int z = 0; z < AL_size; z++)
		{
			if ((ALs.get(z).X == X) && (ALs.get(z).Y == Y))
			{
			return ALs.get(z);
			}			
		}
		System.out.println("Fehler bei der Rückgabe des ActivityLinks");
		return null;
	}
	
	//überprüft für die zwei Aktivitäten eines neu gebildeten Activity Links, ob bereits ein Activity Link mit exakt denselben Aktivitäten existiert
	private static boolean is_in_ALs(ActivityLink AL1, List<ActivityLink> ALs)
	{
		int AL_size = ALs.size();
		boolean test = false;
		for (int z = 0; z < AL_size; z++)
		{
			if ((ALs.get(z).X == AL1.X) && (ALs.get(z).Y == AL1.Y))
			{
			test = true;
			}			
		}
			return test;
	}
	
	
	//findet Menge der Activity Links (siehe Ausarbeitung/Pseudocode im Abschnitt 3.2 und Abschnitt 3.3)
	//vereint die Funktionen FIND-ACTIVITY-LINKS, FIND-ACTIVITIES und GENERATE-ACTIIVITY-LINKS in einer einzigen
	//Funktion
	@SuppressWarnings("unchecked")
	private static List<ActivityLink> find_activity_links(List<Element_enhanced> S, List<Element_enhanced> O, List<Element> B, List<ActivityLink> AL, List<Element_enhanced> I, boolean[] active, int[] instances)
	{
		//zunächst suchen wir die kommunizierenden Aktivitäten 
		//(siehe Funktion FIND-ACTIVITIES in der Ausarbeitung in Abschnitt 3.2)
		Iterator<Element_enhanced> search_list = S.iterator();
		List<ActivityLink> ALtemp;
		ALtemp = new ArrayList<ActivityLink>();
		ALtemp.addAll(AL);
				
		while (search_list.hasNext())
		{
			boolean branch = false;
			Element_enhanced act_element;
			act_element = new Element_enhanced();
			act_element = search_list.next();
			S.remove(act_element);
			
			List<ActivityLink> ALs;
			ALs = new ArrayList<ActivityLink>();
			
			String type = act_element.element.getName();
			
			if (type == "process" || type == "sequence" || type == "scope" || type == "elseif" || type == "else" || type == "forEach" || type == "repeatUntil" || type == "while" || type == "onMessage" || type == "onAlarm")
			{
				List<Element> children = new ArrayList<Element>();
				children = act_element.element.getChildren();
				
				Iterator<Element> list = children.iterator();
				while (list.hasNext())
				{
					Element current_child;
					current_child = list.next();
					String type2 = current_child.getName();
					if (!B.contains(current_child))
					{
						if (type2 == "sequence" || type2 == "scope" || type2 == "if" || type2 == "forEach" || type2 == "repeatUntil" || type2 == "while" || type2 == "pick")
						{
							B.add(current_child);
							Element_enhanced temp_elem;
							temp_elem = new Element_enhanced();
							temp_elem.element = current_child;
							temp_elem.process = act_element.process;
							temp_elem.process_id = act_element.process_id;
							S.add(temp_elem);
							ALs = find_activity_links(S, O, B, ALtemp, I, active, instances);
							return ALs;
						}
						else if (type2 == "reply" || type2 == "invoke" || type2 == "receive")
						{
							Element_enhanced temp_elem;
							temp_elem = new Element_enhanced();
							temp_elem.element = current_child;
							temp_elem.process = act_element.process;
							temp_elem.process_id = act_element.process_id;
							O.add(temp_elem);
							B.add(current_child);
							ALs = find_activity_links(S, O, B, ALtemp, I, active, instances);
							return ALs;
						}
						else if (type2 == "exit")
						{
							ALs = find_activity_links(S, O, B, ALtemp, I, active, instances);
							return ALs;
						}
						
					}
				}
				
			}
			
			else if (type == "exit")
			{
				ALs = find_activity_links(S, O, B, ALtemp, I, active, instances);
				return ALs;
			}
			
			else if (type == "receive" || type == "invoke" || type == "reply")
			{
				Element_enhanced temp_elem;
				temp_elem = new Element_enhanced();
				temp_elem.element = act_element.element.getParentElement();
				temp_elem.process = act_element.process;
				temp_elem.process_id = act_element.process_id;
				S.add(temp_elem);
				ALs = find_activity_links(S, O, B, ALtemp, I, active, instances);
				return ALs;
			}
			
			else if (type == "if" || type == "pick")
			{
				List<Element> inB = new ArrayList<Element>();
				Element root1 = global_variable.roots[act_element.process_id];
				for (int cntr = 0; cntr < B.size(); cntr++)
				{
					Element root2;
					root2 = B.get(cntr).getDocument().getRootElement();
					if (root1 == root2)
					{
						inB.add(B.get(cntr));
					}
				}
				
				
				List<Element> children = new ArrayList<Element>();
				children = act_element.element.getChildren();
				
				List<ActivityLink> ALtemp3;
				ALtemp3 = new ArrayList<ActivityLink>();
				
				Iterator<Element> list = children.iterator();
				while (list.hasNext())
				{
					Element current_child;
					current_child = list.next();
					String typ2 = current_child.getName();
					
					if (!B.contains(current_child))
					{
						if (typ2 == "scope" || typ2 == "sequence" || typ2 == "if" || typ2 == "else" || typ2 =="elseif" || typ2 == "forEach" || typ2 == "repeatUntil" || typ2 == "while" || typ2 == "pick" || typ2 == "onAlarm" || typ2 == "assign" || typ2 == "exit" || typ2 == "validate" || typ2 == "wait" || typ2 == "empty")
						{
							
							branch = true;
							List<Element_enhanced> S_temp;
							S_temp = new ArrayList<Element_enhanced>();
							S_temp.addAll(S);
																					
							Element_enhanced temp_elem;
							temp_elem = new Element_enhanced();
							temp_elem.element = current_child;
							temp_elem.process = act_element.process;
							temp_elem.process_id = act_element.process_id;
							S_temp.add(temp_elem);
							
							
							List<Element> B_temp;
							B_temp = new ArrayList<Element>();
							B_temp.addAll(B);
							B_temp.addAll(children);
							
							
							List<Element_enhanced> O_temp;
							O_temp = new ArrayList<Element_enhanced>();
							O_temp.addAll(O);
							
							
							List<Element_enhanced> I_temp;
							I_temp = new ArrayList<Element_enhanced>();
							I_temp.addAll(I);
							
							
							boolean[] active_temp;
							active_temp = new boolean[instances.length];
							for (int a = 0; a < instances.length; a++)
							{
							active_temp[a] = active[a];
							}
							
							
							int[] instances_temp;
							instances_temp = new int[instances.length];
							for (int a = 0; a < instances.length; a++)
							{
							instances_temp[a] = instances[a];
							}
							
							
							List<ActivityLink> ALtemp2;
							ALtemp2 = new ArrayList<ActivityLink>();
							ALtemp.addAll(ALtemp3);
							ALtemp2 = find_activity_links(S_temp, O_temp, B_temp, ALtemp, I_temp, active_temp, instances_temp);
							if (!ALtemp2.isEmpty())
							{
								branch branch_temp = new branch();
								branch_temp.element = current_child;
								branch_temp.processed = inB;
								global_variable.branches.add(branch_temp);
							}
							ALtemp3.addAll(ALtemp2);
						}
						else if (typ2 == "invoke" || typ2 == "receive" || typ2 == "onMessage" || typ2 == "reply")
						{
							branch = true;
							
							List<Element_enhanced> S_temp;
							S_temp = new ArrayList<Element_enhanced>();
							S_temp.addAll(S);
														
							
							List<Element_enhanced> O_temp;
							O_temp = new ArrayList<Element_enhanced>();
							O_temp.addAll(O);
							
							Element_enhanced temp_elem;
							temp_elem = new Element_enhanced();
							temp_elem.element = current_child;
							temp_elem.process = act_element.process;
							temp_elem.process_id = act_element.process_id;
							O_temp.add(temp_elem);
														
							
							List<Element> B_temp;
							B_temp = new ArrayList<Element>();
							B_temp.addAll(B);
							B_temp.addAll(children);
							
							List<Element_enhanced> I_temp;
							I_temp = new ArrayList<Element_enhanced>();
							I_temp.addAll(I);
														
							boolean[] active_temp;
							active_temp = new boolean[instances.length];
							for (int a = 0; a < instances.length; a++)
							{
							active_temp[a] = active[a];
							}
							
							int[] instances_temp;
							instances_temp = new int[instances.length];
							for (int a = 0; a < instances.length; a++)
							{
							instances_temp[a] = instances[a];
							}
							
							List<ActivityLink> ALtemp2;
							ALtemp2 = new ArrayList<ActivityLink>();
							ALtemp.addAll(ALtemp3);
							ALtemp2 = find_activity_links(S_temp, O_temp, B_temp, ALtemp, I_temp, active_temp, instances_temp);
							if (!ALtemp2.isEmpty())
							{
								branch branch_temp = new branch();
								branch_temp.element = current_child;
								branch_temp.processed = inB;
								global_variable.branches.add(branch_temp);
							}
							ALtemp3.addAll(ALtemp2);
						}
					}
					
				}
				if (type == "if" && !exists_else(children) && branch)
				{
					Element_enhanced temp_elem;
					temp_elem = new Element_enhanced();
					temp_elem.element = act_element.element.getParentElement();
					temp_elem.process = act_element.process;
					temp_elem.process_id = act_element.process_id;
					S.add(temp_elem);
					
					List<ActivityLink> ALtemp2;
					ALtemp2 = new ArrayList<ActivityLink>();
					ALtemp.addAll(ALtemp3);
					ALtemp2 = find_activity_links(S, O, B, ALtemp, I, active, instances);
					if (!ALtemp2.isEmpty())
					{
						branch branch_temp = new branch();
						branch_temp.element = act_element.element.getParentElement();
						branch_temp.processed = inB;
						global_variable.branches.add(branch_temp);
					}
					ALtemp3.addAll(ALtemp2);
				
				}
				if (branch)
				{
					return ALtemp3;
				}
				
			}
						
			
			if (branch == false)
			{
				if (type == "process")
				{
					ALs = find_activity_links(S, O, B, ALtemp, I, active, instances);
					return ALs;
				}
				else
				{
					Element_enhanced temp_elem;
					temp_elem = new Element_enhanced();
					temp_elem.element = act_element.element.getParentElement();
					temp_elem.process = act_element.process;
					temp_elem.process_id = act_element.process_id;
					S.add(temp_elem);
					ALs = find_activity_links(S, O, B, ALtemp, I, active, instances);
					return ALs;
				}
			}
			
		}
		
		//wenn wir bei einem Aufruf der Funktion hier her kommen,
		//dann ist die Menge S leer
		
		//erzeugen von Aktivitätsverbindungen
		//(siehe Ausarbeitung Abschnitt 3.2 Funktion GENERATE-ACTIVITY-LINKS)
		//Bestimmung der Werte für active[], instances[], Matrix B und Matrix D 
		//siehe Ausarbeitung Abschnitt 3.3  
		int m = O.size();
		int cntr = 0;
		boolean new_ALs = false;
		List<ActivityLink> newALs;
		newALs = new ArrayList<ActivityLink>();
		List<Element_enhanced> temp_list;
		temp_list = new ArrayList<Element_enhanced>();
		for (int x = 0; x < m; x++)
		{
			Element_enhanced X;
			X = new Element_enhanced();
			X = O.get(x);
			for (int y = 0; y < m; y++)
			{
				Element_enhanced Y;
				Y = new Element_enhanced();
				Y = O.get(y);
				
				if (match(X, Y))
				{
					
					new_ALs = true;
					
					ActivityLink Alink;
					Alink = new ActivityLink();
					
					Alink.X = X.element;
					Alink.Y = Y.element;
					Alink.processX = X.process;
					Alink.processY = Y.process;
					Alink.processX_id = X.process_id;
					Alink.processY_id = Y.process_id;
					if (!is_in_ALs(Alink, ALtemp))
					{
					ALtemp.add(Alink);
					newALs.add(Alink);
					cntr++;
					}
					else
					{
						ActivityLink Alink2;
						Alink2 = getAL(X.element, Y.element, ALtemp);
						instances[Alink2.processX_id] = Alink2.instX;
						instances[Alink2.processY_id] = Alink2.instY;
						active[Alink2.processX_id] = true;
						active[Alink2.processY_id] = true;
					}
					
					//überprüfen, ob <fromParts> Element existiert
					boolean from_parts = false;
					List<Element> children;
					children = X.element.getChildren();
					Iterator<Element> list = children.iterator();
					while (list.hasNext())
					{
						Element current_child;
						current_child = list.next();
						String type = current_child.getName();
						if (type == "fromParts")
						{
							from_parts = true;
						}
					}
					if (X.element.getAttributeValue("outputVariable") != null || from_parts)
					{
						I.add(X);
						S.add(Y);
					}
					else
					{
						S.add(X);
						S.add(Y);
					}
					
					
					temp_list.add(X);
					temp_list.add(Y);

				}
				
			}
		}
		O.removeAll(temp_list);
		m = O.size();
		
		
		//Extrabehandlung für reply-Aktivitäten in O
		List<Element_enhanced> temp_list2;
		temp_list2 = new ArrayList<Element_enhanced>();
		List<Element_enhanced> temp_list3;
		temp_list3 = new ArrayList<Element_enhanced>();
		for (int j = 0; j < m; j++)
		{
			if (O.get(j).element.getName() == "reply")
			{
				Element_enhanced X;
				X = new Element_enhanced();
				X = O.get(j);
				
				int l = I.size();
				for (int k = 0; k < l; k++)
				{
					Element_enhanced Y;
					Y = new Element_enhanced();
					Y = I.get(k);
					if (match2(X,Y))
					{
						new_ALs = true;
						
						ActivityLink Alink;
						Alink = new ActivityLink();
						
						Alink.X = X.element;
						Alink.Y = Y.element;
						Alink.processX = X.process;
						Alink.processY = Y.process;
						Alink.processX_id = X.process_id;
						Alink.processY_id = Y.process_id;
						if (!is_in_ALs(Alink, ALtemp))
						{
						ALtemp.add(Alink);
						newALs.add(Alink);
						cntr++;
						}
						else
						{
							ActivityLink Alink2;
							Alink2 = getAL(X.element, Y.element, ALtemp);
							instances[Alink2.processX_id] = Alink2.instX;
							instances[Alink2.processY_id] = Alink2.instY;
							active[Alink2.processX_id] = true;
							active[Alink2.processY_id] = true;
						}
					
						
						
						temp_list2.add(X);
						temp_list3.add(Y);

						S.add(X);
						S.add(Y);
					}
				}

			}
		}
		O.removeAll(temp_list2);
		I.removeAll(temp_list3);
		m = O.size();
		
		//Elemente aus off_akt entfernen, die keinen passenden PartnerLink haben 
		List<Element_enhanced> temp_elements;
		temp_elements = new ArrayList<Element_enhanced>();
		for (int j = 0; j < m; j++)
		{
			String PartLink = O.get(j).element.getAttributeValue("partnerLink");
			String Process = O.get(j).process;
			
			List<PartnerLink> PL_menge = new ArrayList<PartnerLink>();
			PL_menge = find_matching_PLs(Process, PartLink);
			if (PL_menge.isEmpty())
				{
				temp_elements.add(O.get(j));
				if (instances[O.get(j).process_id] == 32000)
				{
					instances[O.get(j).process_id] = 0;
					active[O.get(j).process_id] = true;
				}
				new_ALs = true;
				}
		}
		O.removeAll(temp_elements);
		S.addAll(temp_elements);
		
		//für alle neuen ActivityLinks die Bestimmung von instances[] durchführen 
		for (int c = 0; c < newALs.size(); c++)
		{
			ActivityLink tempAL;
			tempAL = newALs.get(c);
			int x;
			int y;
			x = tempAL.processX_id;
			y = tempAL.processY_id;
			int sx = count_loops(tempAL.X);
			int sy = count_loops(tempAL.Y);
			if (!active[x] && !active[y])
			{
				active[x] = true;
				active[y] = true;
				instances[x] = 0;
				if (sx > 0)
				{
					Element tmp_element;
					tmp_element = find_next_loop(tempAL.X);
					if (find_assign(tmp_element, tempAL.X.getAttributeValue("partnerLink")))
					{
						instances[y] = 1;
					}
					else
					{
						instances[y] = 0;
					}
				}
				else
				{
					instances[y] = 0;
				}
				global_variable.D[x][y] = sx;
				global_variable.B[x][y] = instances[y];
				global_variable.D[y][x] = - global_variable.D[x][y];
				global_variable.B[y][x] = - global_variable.B[x][y];
				update_matrix();
			}
			else if (active[x] && !active[y])
			{
				active[y]=true;
				if (sx > 0)
				{
					Element tmp_element;
					tmp_element = find_next_loop(tempAL.X);
					if (find_assign(tmp_element, tempAL.X.getAttributeValue("partnerLink")))
					{
						instances[y] = instances[x]+1;
					}
					else
					{
						instances[y] = instances[x];
					}
				}
				else
				{
				instances[y] = instances[x];
				}
				global_variable.D[x][y] = sx;
				global_variable.B[x][y] = instances[y]-instances[x];
				global_variable.D[y][x] = - global_variable.D[x][y];
				global_variable.B[y][x] = - global_variable.B[x][y];
				update_matrix();
			}
			else if (!active[x] && active[y])
			{
				System.out.println("Something is wrong!");
			}
			else if (active[x] && active[y])
			{
				int z = global_variable.D[x][y] - sx + sy; 
				int bez = global_variable.B[x][y] - z;
				if (instances[x] + bez < instances[y])
				{
					instances[y] = instances[x] + bez;
				}
				else if (instances[x] + bez > instances[y])
				{
					instances[x] = instances[y] - bez;
				}
			}
			tempAL.sX = sx;
			tempAL.sY = sy;

			tempAL.instX = instances[x];
			tempAL.instY = instances[y];
			
		}
		
		//überprüfen, ob cntr = 0; ist new_ALs = true, dann wissen wir, dass AL gefunden wurde, aber schon vorhanden war
		//durch exit_condition und exit_condition_invokes wissen wir
		//ob wir erfolgreich abbrechen können (siehe Ausarbeitung Abschnitt 3.2, Unterabschnitt Abbruchbedingung)
		if (new_ALs && cntr == 0 && exit_condition(O, ALtemp) && exit_condition_invokes(I, ALtemp))
		{
			return ALtemp;
		}
		
		//new_ALs gibt an, ob neuer AL generiert werden konnte
		if (new_ALs)
		{
			List<ActivityLink> ALs;
			ALs = new ArrayList<ActivityLink>();
			ALs = find_activity_links(S, O, B, ALtemp, I, active, instances);
			return ALs;
		}
		else
		{
			if (O.isEmpty() && I.isEmpty())
			{
				return ALtemp;
			}
			else
			{
				if (noInstance(O) && I.isEmpty())
				{
					return ALtemp;
				}
				else
				{
				List<ActivityLink> ALs;
				ALs = new ArrayList<ActivityLink>();
				return ALs;
				}
			}
		}
		
	}
	
	
	//überprüft für Menge von Aktivitäten, ob bei allen gilt: die Aktivitäten sind instanzerzeugend
	private static boolean noInstance(List<Element_enhanced> Elemente){
		int el_size = Elemente.size();
		for (int e = 0; e < el_size; e++)
		{
			String type = Elemente.get(e).element.getName();
			if (type.equals("receive"))
			{
				if (Elemente.get(e).element.getAttributeValue("createInstance") == null)
				{
					return false;
				}
			}
			else if (type.equals("onMessage"))
			{
				Element tmp_elem;
				tmp_elem = Elemente.get(e).element.getParentElement();
				if (tmp_elem.getAttributeValue("createInstance") == null)
				{
					return false;
				}
			}
			else
			return false;

		}
		return true;
	}
	
	
	//überprüft für eine Menge von Element_enhanced, ob alle schon in der Menge von Aktivitätsverbindungen enthalten sind
	private static boolean exit_condition(List<Element_enhanced> elements, List<ActivityLink> Alinks){
		int el_size = elements.size();
		int al_size = Alinks.size();
		boolean test;
		for (int e = 0; e < el_size; e++)
		{
			test = false;
			for (int m = 0; m < al_size; m++)
			{
				if ((elements.get(e).element.equals(Alinks.get(m).X)) || (elements.get(e).element.equals(Alinks.get(m).Y)))
				{
				test = true;
				} 
			}
			if (test == false)
			{
				return false;
			}
		}
		
		return true;
	}
	
	//überprüft für Menge von Invoke-Aktivitäten, die noch auf Antwort von Reply warten, ob die schon als Empfänger in einem AL vorkommen
	private static boolean exit_condition_invokes(List<Element_enhanced> elements, List<ActivityLink> Alinks)
	{
		int el_size = elements.size();
		int al_size = Alinks.size();
		boolean test;
		for (int e = 0; e < el_size; e++)
		{
			test = false;
			for (int m = 0; m < al_size; m++)
			{
				if (elements.get(e).element.equals(Alinks.get(m).Y))
				{
				test = true;
				} 
			}
			if (test == false)
			{
				return false;
			}
		}
		
		return true;
	}
	
	
	//Matrizen D und B aktualisieren
	private static void update_matrix()
	{
		boolean change = true;
		while(change)
		{
			change = false;
			for (int o=0; o <= global_variable.processes.length-3; o++)
			{
				for (int p = o+1; p <= global_variable.processes.length-2; p++)
				{
					if (global_variable.D[o][p] != 32000)
					{
						for (int q = p+1; q <= global_variable.processes.length-1; q++)
						{
							if (global_variable.D[o][q] != 32000 && global_variable.D[p][q] == 32000)
							{
								global_variable.D[p][q] = global_variable.D[o][q] - global_variable.D[o][p];
								global_variable.D[q][p] = -global_variable.D[p][q];
								global_variable.B[p][q] = global_variable.B[o][q] - global_variable.B[o][p];
								global_variable.B[q][p] = -global_variable.B[p][q];
								change = true;
							}
						}
					}
				}
			}
			for (int o=0; o <= global_variable.processes.length-3; o++)
			{
				for (int p = o+1; p <= global_variable.processes.length-1; p++)
				{
					if (global_variable.D[o][p] != 32000)
					{
						for (int q = 0; q <= global_variable.processes.length-1; q++)
						{
							if (global_variable.D[p][q] != 32000 && global_variable.D[o][q] == 32000 && p != q)
							{
								global_variable.D[o][q] = global_variable.D[o][p] + global_variable.D[p][q];
								global_variable.D[q][o] = -global_variable.D[o][q];
								global_variable.B[o][q] = global_variable.B[o][p] + global_variable.B[p][q];
								global_variable.B[q][o] = -global_variable.B[o][q];
								change = true;
							}
						}
					}
				}
			}
		}
	}
	
	
	//gibt an, ob zwei Aktivitäten zueinander passen und Aktivitätsverbindung gebildet werden kann
	private static boolean match (Element_enhanced elem1, Element_enhanced elem2)
	{
		String type1;
		type1 = elem1.element.getName();

		String type2;
		type2 = elem2.element.getName();
		if (type1 == "invoke" && (type2 == "receive" || type2 == "onMessage"))
		{
			String process = elem1.process;
			String PartLink = elem1.element.getAttributeValue("partnerLink");
			List<PartnerLink> PLList;
			PLList = new ArrayList<PartnerLink>();
			PLList = find_matching_PLs(process, PartLink);

			String PartLink2 = elem2.element.getAttributeValue("partnerLink");	
			String process2 = elem2.process;
			String operation1 = elem1.element.getAttributeValue("operation");
			String operation2 = elem2.element.getAttributeValue("operation");
			if (is_in_PLset(process2, PartLink2, PLList) && operation1.equals(operation2))
			{
				return true;
			}			
		}
		return false;
	}
	
	//überprüft für eine gegebene reply-Aktivität, ob die gegebene invoke-Aktivität dazu passt
	private static boolean match2 (Element_enhanced elem1, Element_enhanced elem2)
	{
			String process = elem1.process;
			String PartLink = elem1.element.getAttributeValue("partnerLink");
			List<PartnerLink> PLList;
			PLList = new ArrayList<PartnerLink>();
			PLList = find_matching_PLs(process, PartLink);

			String PartLink2 = elem2.element.getAttributeValue("partnerLink");	
			String process2 = elem2.process;
			String operation1 = elem1.element.getAttributeValue("operation");
			String operation2 = elem2.element.getAttributeValue("operation");
			if (is_in_PLset(process2, PartLink2, PLList) && operation1.equals(operation2))
			{
				return true;
			}			
		
		return false;
	}
	
	
	//gibt an, ob ein mittels seines Namens gegebener Partner Link in Menge von Partner Links enthalten ist
	private static boolean is_in_PLset(String process, String PLname, List<PartnerLink> mPL)
	{
		Iterator<PartnerLink> list = mPL.iterator();
		while (list.hasNext()){
			PartnerLink PL = list.next();
			if (PL.PL_name.equals(PLname))
			{
				if (PL.process.equals(process))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	//liefert die Schleifentiefe einer Aktivität
	private static int count_loops(Element e){
		int cntr = 0;
		while (e.getName() != "process")
		{
		if (e.getName() == "forEach" || e.getName() == "while" || e.getName() == "repeatUntil")
		{
		cntr++;
		}
		e=e.getParentElement();
		}
		return cntr;
	}
	
	
	//überprüft, ob eine Endpunktreferenz in den gegebenen Partner Link kopiert wird
	//die Wurzel des betrachteten Teilbaums ist das Element e
	@SuppressWarnings("unchecked")
	private static boolean find_assign(Element e, String Plink){
		String type = e.getName();
		if (type == "process" || type=="sequence" || type=="flow" || type=="if" || type=="while" || type=="repeatUntil" || type=="forEach" || type=="pick" || type=="onMessage" || type=="onAlarm" || type=="scope" || type=="elseif" || type=="else" || type == "assign" || type=="copy")
					{
						List<Element> children = new ArrayList<Element>();
						children = e.getChildren();
						if (children.isEmpty() == false)
							{Iterator<Element> list = children.iterator();
							while (list.hasNext())
								{
									Element current_element = list.next();
									boolean k = find_assign(current_element, Plink);
									if (k)
									{
										return true;
									}
								}
							}
					return false;
					}
		else if (type=="to")
		{
			String test;
			test = e.getAttributeValue("partnerLink");
			if (test != null)
			{
				if (test.equals(Plink))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}

		}
		else
		{
			return false;
		}

	}
	
	
	//findet zu einem Element die nächste Schleife Richtung Wurzel
	//nur aufrufen, wenn Schleife sicher existiert
	private static Element find_next_loop(Element e){
		while (e.getName() != "while" && e.getName() != "forEach" && e.getName() != "repeatUntil")
		{
			if (e.getName() == "process")
			{
				global_variable.Error = global_variable.Error + " Keine Schleife gefunden, obwohl eine erwartet wurde.";
			}
			e=e.getParentElement();
		}
		return e;
	}
	
	//initialisiert die bisher nicht benötigten Konstrukte der Aktivitätsverbindungen
	private static void initialize_activity_links_attributes()
	{
		for (int k = 0; k < global_variable.ActivityLinks.size(); k++)
		{
			global_variable.ActivityLinks.get(k).send_part_sets = new ArrayList<partSet>();
			global_variable.ActivityLinks.get(k).send_parts = new ArrayList<part>();
			global_variable.ActivityLinks.get(k).rec_parts = new ArrayList<part>();
			global_variable.ActivityLinks.get(k).bindSenderTo = null;
			global_variable.ActivityLinks.get(k).bindSendersToRefs = new ArrayList<part>();
			global_variable.ActivityLinks.get(k).send_acts = new ArrayList<Element>();
			global_variable.ActivityLinks.get(k).rec_acts = new ArrayList<Element>();
			global_variable.ActivityLinks.get(k).send_acts.add(global_variable.ActivityLinks.get(k).X);
			global_variable.ActivityLinks.get(k).rec_acts.add(global_variable.ActivityLinks.get(k).Y);
		}
	}
	
	
	
	public static void al_handling()
	{
		//active[] erzeugen
		boolean[] active;
		active = new boolean[global_variable.i];
		for (int j = 0; j < global_variable.i; j++)
		{
			active[j]=false;
		}
		
		//instances[] erzeugen, 32000 ist default wert
		int[] instances;
		instances = new int[global_variable.i];
		for (int j = 0; j < global_variable.i; j++)
		{
			instances[j]=32000;
		}
		
		//Matrix A, 32000 ist default wert
		global_variable.D = new int[global_variable.i][global_variable.i];
		for (int j = 0; j < global_variable.i; j++)
		{
			for (int k = 0; k < global_variable.i; k++)
			{
				if (j == k)
				{
					global_variable.D[j][k]=0;
				}
				else
				{
					global_variable.D[j][k]=32000;
				}
			}
		}
		
		//Matrix B, 32000 ist default wert
		global_variable.B = new int[global_variable.i][global_variable.i];
		for (int j = 0; j < global_variable.i; j++)
		{
			for (int k = 0; k < global_variable.i; k++)
			{
				if (j == k)
				{
					global_variable.B[j][k]=0;
				}
				else
				{
					global_variable.B[j][k]=32000;
				}
			}
		}
		
				
		//initialisieren (liste der bereits besuchten Elemente)
		List<Element> B;
		B = new ArrayList<Element>();
		
		//initialisieren (liste der Activity Links)
		List<ActivityLink> ALs;
		ALs = new ArrayList<ActivityLink>();
		
		//initialisieren (liste der offenen Aktivitäten)
		List<Element_enhanced> O;
		O = new ArrayList<Element_enhanced>();
		
		//initialisieren (liste der Startpunkte (der <process> Konstrukte jedes Prozesses))
		List<Element_enhanced> start_elements;
		start_elements = new ArrayList<Element_enhanced>();
		for (int j = 0; j < global_variable.i; j++)
		{
			Element_enhanced temp;
			temp = new Element_enhanced();
			temp.element = global_variable.roots[j];
			temp.process = global_variable.processes[j];
			temp.process_id = j;
			start_elements.add(temp);		
		}
		
		//initialisieren (liste der invokes mit outputVariable bzw. fromParts-Element)
		List<Element_enhanced> I;
		I = new ArrayList<Element_enhanced>();
		
		//Activity Links erzeugen
		List<ActivityLink> ActivityLinks2;
		ActivityLinks2 = new ArrayList<ActivityLink>();
		ActivityLinks2 = find_activity_links(start_elements, O, B, ALs, I, active, instances);
		//doppelte Activity Links streichen
		for (int ze = 0; ze < ActivityLinks2.size(); ze++)
		{
			ActivityLink tmp = ActivityLinks2.get(ze);
			if (is_in_ALs(tmp, global_variable.ActivityLinks) == false)
			{
				global_variable.ActivityLinks.add(tmp);
			} 
		}
		
		
		//initialisieren der bisher noch nicht benötigten Konstrukte in den Activity Links
		initialize_activity_links_attributes();
		
	}
	
}
