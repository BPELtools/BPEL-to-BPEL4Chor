import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import new_types.ActivityLink;
import new_types.PL_pair;
import new_types.PartnerLink;
import new_types.branch;
import new_types.part;
import new_types.partSet;
import new_types.part_ref;

import org.jdom.Element;


public class Participant_handling {

	
	//generieren der Teilnehmermengen und Teilnehmerreferenzen
	@SuppressWarnings("unchecked")
	private static void generate_parts(List<Element> S, List<Element> O, List<Element> B)
	{
		//die Suche nach kommunizierenden Aktivitäten geschieht wie in der Funktion find_activity_links
		//(siehe auch Ausarbeitung Abschnitt 3.2 Funktion FIND-ACTIVITIES)
		//siehe Ausarbeitung Abschnitt 3.4 für das Verhalten bei einer Verzweigung
		boolean branch = false;
		boolean bool_test = false;
		Iterator<Element> search_list = S.iterator();
		while (search_list.hasNext())
		{
			Element act_element = search_list.next();
			S.remove(act_element);
			String type = act_element.getName();
			
			if (type == "process" || type == "sequence" || type == "scope" || type == "elseif" || type == "else" || type == "forEach" || type == "repeatUntil" || type == "while" || type == "onMessage" || type == "onAlarm")
			{
				List<Element> children = new ArrayList<Element>();
				children = act_element.getChildren();
				
				Iterator<Element> list = children.iterator();
				while (list.hasNext())
				{
					Element act_child;
					act_child = list.next();
					String type2 = act_child.getName();
					if (!B.contains(act_child))
					{
						if (type2 == "sequence" || type2 == "scope" || type2 == "if" || type2 == "forEach" || type2 == "repeatUntil" || type2 == "while" || type2 == "pick")
						{
							B.add(act_child);
							S.add(act_child);
							generate_parts(S,O,B);
							return;
						}
						else if (type2 == "reply" || type2 == "invoke" || type2 == "receive")
						{
							B.add(act_child);
							O.add(act_child);
							generate_parts(S,O,B);
							return;
						}
						else if (type2 == "exit")
						{
							return;
						}
						
					}
				}
				
			}
			
			else if (type == "exit")
			{
				return;
			}
			
			else if (type == "receive" || type == "invoke" || type == "reply")
			{
				Element temp_elem = act_element.getParentElement();
				S.add(temp_elem);
				generate_parts(S,O,B);
				return;
			}
			
			
			else if (type == "if" || type == "pick")
			{
				List<Element> children = new ArrayList<Element>();
				children = act_element.getChildren();
				
				Iterator<Element> list = children.iterator();
				while (list.hasNext())
				{
					Element act_child;
					act_child = list.next();
					String type2 = act_child.getName();
					
					if (!B.contains(act_child))
					{
						bool_test = true;
						List<branch> branch_list = new ArrayList<branch>();
						for (int cntr = 0; cntr < global_variable.branches.size(); cntr++)
						{
							if (act_child == global_variable.branches.get(cntr).element)
							{
								branch_list.add(global_variable.branches.get(cntr));
							}
						}
						boolean test = false;
						for (int cntr = 0; cntr < branch_list.size(); cntr++)
						{
							if (B.containsAll(branch_list.get(cntr).processed) && branch_list.get(cntr).processed.containsAll(B))
							{
								test = true;
							}
						}
						
						//nur diesen Zweig betrachten, wenn er bei der tatsächlichen Ausführung
						//vorkommen kann. Dies ist nur der fall wenn test = true gilt.
						if (test)
						{
						if (type2 == "scope" || type2 == "sequence" || type2 == "if" || type2 == "else" || type2 =="elseif" || type2 == "forEach" || type2 == "repeatUntil" || type2 == "while" || type2 == "pick" || type2 == "onAlarm" || type2 == "assign" || type2 == "exit" || type2 == "validate" || type2 == "wait" || type2 == "empty")
						{
							branch = true;
						
							List<Element> Stemp;
							Stemp = new ArrayList<Element>();
							Stemp.addAll(S);
							Stemp.add(act_child);
							
							List<Element> Btemp;
							Btemp = new ArrayList<Element>();
							Btemp.addAll(B);
							Btemp.addAll(children);
							
							List<Element> Otemp;
							Otemp = new ArrayList<Element>();
							Otemp.addAll(O);
							
							part temp_part;
							temp_part = global_variable.current_part;
														
							List<partSet> bound_PSets;
							bound_PSets = new ArrayList<partSet>();
							
							Iterator<partSet> PSet_list = global_variable.partSets.iterator();
							while (PSet_list.hasNext())
							{
								partSet tmp;
								tmp = PSet_list.next();
								if(tmp.bound)
								{
									bound_PSets.add(tmp);
								}
							}
							
													
							generate_parts(Stemp,Otemp,Btemp);
							
							Iterator<partSet> PSet_list_new = global_variable.partSets.iterator();
							while (PSet_list_new.hasNext())
							{
								partSet tmp;
								tmp = PSet_list_new.next();
								if (!bound_PSets.contains(tmp))
								{
									tmp.bound=false;
								}
							}
							global_variable.current_part = temp_part;
							
							
						}
						else if (type2 == "invoke" || type2 == "receive" || type2 == "onMessage" || type2 == "reply")
						{
							branch = true;
							
							List<Element> Stemp;
							Stemp = new ArrayList<Element>();
							Stemp.addAll(S);
							
							List<Element> Btemp;
							Btemp = new ArrayList<Element>();
							Btemp.addAll(B);
							Btemp.addAll(children);
							
							List<Element> Otemp;
							Otemp = new ArrayList<Element>();
							Otemp.addAll(O);
							Otemp.add(act_child);
							
							part temp_part;
							temp_part = global_variable.current_part;
							
							List<partSet> bound_PSets;
							bound_PSets = new ArrayList<partSet>();
							
							Iterator<partSet> PSet_list = global_variable.partSets.iterator();
							while (PSet_list.hasNext())
							{
								partSet tmp;
								tmp = PSet_list.next();
								if(tmp.bound)
								{
									bound_PSets.add(tmp);
								}
							}
													
							generate_parts(Stemp,Otemp,Btemp);
							
							Iterator<partSet> PSet_list_new = global_variable.partSets.iterator();
							while (PSet_list_new.hasNext())
							{
								partSet tmp;
								tmp = PSet_list_new.next();
								if (!bound_PSets.contains(tmp))
								{
									tmp.bound=false;
								}
							}
							
							global_variable.current_part = temp_part;
						}
						}

					}
				}
					
				if (type == "if" && !exists_else(children) && bool_test)
				{
					List<branch> branch_list = new ArrayList<branch>();
					for (int cntr = 0; cntr < global_variable.branches.size(); cntr++)
					{
						if (act_element.getParentElement() == global_variable.branches.get(cntr).element)
						{
							branch_list.add(global_variable.branches.get(cntr));
						}
					}
					boolean test = false;
					for (int cntr = 0; cntr < branch_list.size(); cntr++)
					{
						if (B.containsAll(branch_list.get(cntr).processed) && branch_list.get(cntr).processed.containsAll(B))
						{
							test = true;
						}
					}
					
					if (test)
					{
					branch = true;
					S.add(act_element.getParentElement());
					
					part temp_part;
					temp_part = global_variable.current_part;
					
					List<partSet> bound_PSets;
					bound_PSets = new ArrayList<partSet>();
					
					Iterator<partSet> PSet_list = global_variable.partSets.iterator();
					while (PSet_list.hasNext())
					{
						partSet tmp;
						tmp = PSet_list.next();
						if(tmp.bound)
						{
							bound_PSets.add(tmp);
						}
					}
											
					generate_parts(S,O,B);
					
					Iterator<partSet> PSet_list_new = global_variable.partSets.iterator();
					while (PSet_list_new.hasNext())
					{
						partSet tmp;
						tmp = PSet_list_new.next();
						if (!bound_PSets.contains(tmp))
						{
								tmp.bound=false;
						}
					}
					
					global_variable.current_part = temp_part;
					}
				}
			}
				
			if (branch || bool_test)
			{
				return;
			}
				
			
			else
			{
				if (type == "process")
				{
					generate_parts(S,O,B);
					return;
				}
				else
				{
					S.add(act_element.getParentElement());
					generate_parts(S,O,B);
					return ;
				}
			}
		
		
		}
		
		//kommen wir hier an, dann ist S leer
		//ab hier werden neue Teilnehmer gebildet
		
		//siehe Ausarbeitung Abschnitt 3.4 für das Vorgehen
		if (!O.isEmpty())
		{
			Element act_element;
			act_element = O.get(0);
			O.remove(act_element);
			S.add(act_element);
		
			List<ActivityLink> ALset;
			ALset = new ArrayList<ActivityLink>();
			ALset = getALset(act_element, false);
			if (ALset.isEmpty())
			{
				String PL = act_element.getAttributeValue("partnerLink");
				String Prozess = global_variable.processes[global_variable.counter];
				
				List<PartnerLink> PL_set = new ArrayList<PartnerLink>();
				PL_set = find_matching_PLs(Prozess, PL);
				if (!PL_set.isEmpty())
				{
					return;
				}
				generate_parts(S,O,B);
				return;
			}
			else
			{
				ActivityLink tempAL = ALset.get(0);
				if (act_element.getName() == "invoke" || act_element.getName() == "reply")
				{
					//Fehler vermerken
					if (tempAL.instX < 0)
					{
						global_variable.Error = global_variable.Error + " Vergessen die Endpunktreferenz in den Partner Link zu kopieren?";
						global_variable.Error = global_variable.Error + " Nicht beachtet, dass unterscheidung der Instanzen nach Correlation Sets nicht beachtet wird?";
					}
						
					if (tempAL.instX == 0)
					{
						if (global_variable.current_part == null)
						{
							part_ref pref_tmp = new part_ref();
							part temp_part = new part();
							temp_part.name = global_variable.roots[global_variable.counter].getAttributeValue("name");
							temp_part.type = global_variable.roots[global_variable.counter].getAttributeValue("name");
							temp_part.L1 = new ArrayList<Element>();
							temp_part.select_parts = new ArrayList<String>();
							temp_part.set = null;
							pref_tmp.pref = temp_part;
							pref_tmp.pset = null;
							
							global_variable.PRefs.get(global_variable.counter).add(pref_tmp);
							
							global_variable.current_part = temp_part;
						}
						else
						{
							if (global_variable.current_part.set != null)
							{
								List<part_ref> prefs_temp = global_variable.PRefs.get(global_variable.counter);
								part_ref tmp_ref = null;
								
								for (int z = 0; z < prefs_temp.size(); z++)
								{
									if (prefs_temp.get(z).pset == global_variable.current_part.set)
									{
										tmp_ref = prefs_temp.get(z);
									}
								}
								
								if (tmp_ref == null)
								{
									part_ref pref_tmp = new part_ref();
									part temp_part = new part();
									temp_part.name = "selected_" + global_variable.roots[global_variable.counter].getAttributeValue("name") + "_" + (prefs_temp.size()+1);
									temp_part.type = global_variable.roots[global_variable.counter].getAttributeValue("name");
									temp_part.L1 = new ArrayList<Element>();
									temp_part.select_parts = new ArrayList<String>();
									temp_part.set = null;
									pref_tmp.pref = temp_part;
									pref_tmp.pset = global_variable.current_part.set;
									
									global_variable.PRefs.get(global_variable.counter).add(pref_tmp);
									
									global_variable.current_part = temp_part;
									
								}
								else
								{
									global_variable.current_part = tmp_ref.pref;
								}
								
								
							}
						}
							
							
						//den Activity Links hinzufügen
						Iterator<ActivityLink> AL_liste = ALset.iterator();
						while (AL_liste.hasNext())
						{
							ActivityLink ALtmp;
							ALtmp = AL_liste.next();
							ALtmp.send_parts.add(global_variable.current_part);						
						}
						
						
					}
					else
					{
						if (tempAL.instX == tempAL.instY)
						{
							if (global_variable.current_part == null)
							{
								List<Element> no_loops;
								no_loops = new ArrayList<Element>();
								global_variable.current_part = generatePTset(no_loops, act_element);
								global_variable.partSets.add(global_variable.current_part.set);
								
								//den Activity Links hinzufügen
								Iterator<ActivityLink> AL_list = ALset.iterator();
								while (AL_list.hasNext())
								{
									ActivityLink ALtmp;
									ALtmp = AL_list.next();
									ALtmp.send_part_sets.add(global_variable.current_part.set);
									ALtmp.bindSenderTo = global_variable.current_part;
								}
							}
							else
							{
								//den Activity Links hinzufügen
								Iterator<ActivityLink> AL_list = ALset.iterator();
								while (AL_list.hasNext())
								{
									ActivityLink ALtmp;
									ALtmp = AL_list.next();
									ALtmp.send_parts.add(global_variable.current_part);
								}
							}
						}
						else
						{
							List<Element> loops;
							loops = new ArrayList<Element>();
							if (tempAL.sX < tempAL.sY)
							{
								Iterator<ActivityLink> AL_list = ALset.iterator();
								while (AL_list.hasNext())
								{
									ActivityLink ALtmp;
									ALtmp = AL_list.next();
									Element loop = getLoop(ALtmp.Y, tempAL.sX);
									if (!loops.contains(loop))
									{
										loops.add(loop);
									}
								}
							}
							else
							{
								Iterator<ActivityLink> AL_list = ALset.iterator();
								while (AL_list.hasNext())
								{
									ActivityLink ALtmp;
									ALtmp = AL_list.next();
									Element loop = getLastLoop(ALtmp.Y);
									if (!loops.contains(loop))
									{
										loops.add(loop);
									}
								}
							}
							if (global_variable.current_part == null)
							{
								global_variable.current_part = generatePTset(loops, act_element);
								global_variable.partSets.add(global_variable.current_part.set);
								
								//den Activity Links hinzufügen
								Iterator<ActivityLink> AL_list = ALset.iterator();
								while (AL_list.hasNext())
								{
									ActivityLink ALtmp;
									ALtmp = AL_list.next();
									ALtmp.send_part_sets.add(global_variable.current_part.set);
									ALtmp.bindSenderTo = global_variable.current_part;
								}
							}				
							else
							{
								List<Element> existing_loops;
								part tmpPart = null;
								
								Iterator subset_list = global_variable.current_part.set.part_sets.iterator();
								while (subset_list.hasNext())
								{
									partSet tmpSet = (partSet) subset_list.next();
									existing_loops = tmpSet.L1;
									if (existing_loops.containsAll(loops)&& loops.containsAll(existing_loops))
									{
										tmpPart = tmpSet.part_ref;
									}
								}
								
								if (tmpPart != null)
								{
									global_variable.current_part = tmpPart;
									if (global_variable.current_part.set.bound)
									{
										//den Activity Links hinzufügen
										Iterator<ActivityLink> AL_list = ALset.iterator();
										while (AL_list.hasNext())
										{
											ActivityLink ALtmp;
											ALtmp = AL_list.next();
											ALtmp.send_parts.add(global_variable.current_part);
										}
									}
									else
									{
										global_variable.current_part.set.bound = true;
										//den Activity Links hinzufügen
										Iterator<ActivityLink> AL_list = ALset.iterator();
										while (AL_list.hasNext())
										{
											ActivityLink ALtmp;
											ALtmp = AL_list.next();
											ALtmp.send_part_sets.add(global_variable.current_part.set);
											ALtmp.bindSenderTo = global_variable.current_part;
											global_variable.current_part.set.initial_acts.add(act_element);
										}
									}
								}
								else
								{
									Iterator<partSet> partSet_list = global_variable.partSets.iterator();
									while (partSet_list.hasNext())
									{
										partSet tmpSet = partSet_list.next();
										existing_loops = tmpSet.L1;
										if (tmpSet.bound && existing_loops.containsAll(loops) && loops.containsAll(existing_loops))
										{
											tmpPart = tmpSet.part_ref;
										}
									}
									if (tmpPart != null)
									{
										global_variable.current_part = tmpPart;
										//den Activity Links hinzufügen
										Iterator<ActivityLink> AL_list = ALset.iterator();
										while (AL_list.hasNext())
										{
											ActivityLink ALtmp;
											ALtmp = AL_list.next();
											ALtmp.send_parts.add(global_variable.current_part);
										}
									}
									else
									{
										tmpPart = generatePTset(loops, act_element);
										global_variable.current_part.set.part_sets.add(tmpPart.set);
										global_variable.current_part = tmpPart;
										global_variable.partSets.add(tmpPart.set);
										//den Activity Links hinzufügen
										Iterator<ActivityLink> AL_list = ALset.iterator();
										while (AL_list.hasNext())
										{
											ActivityLink ALtmp;
											ALtmp = AL_list.next();
											ALtmp.send_part_sets.add(global_variable.current_part.set);
											ALtmp.bindSenderTo = global_variable.current_part;
										}
									}
									
								}
								
							}
						}
					}
				}
				//dasselbe nur für empfangende Aktivitäten
				//siehe Ausarbeitung Abschnitt 3.4
				else if (act_element.getName() == "receive" || act_element.getName() == "onMessage")
				{
					//Fehler vermerken
					if (tempAL.instY < 0)
					{
						global_variable.Error = global_variable.Error + " Vergessen die Endpunktreferenz in den Partner Link zu kopieren?";
						global_variable.Error = global_variable.Error + " Nicht beachtet, dass unterscheidung der Instanzen nach Correlation Sets nicht beachtet wird?";
					}
					if (tempAL.instY == 0)
					{
						if (global_variable.current_part == null)
						{
							part_ref pref_tmp = new part_ref();
							part temp_part = new part();
							temp_part.name = global_variable.roots[global_variable.counter].getAttributeValue("name");
							temp_part.type = global_variable.roots[global_variable.counter].getAttributeValue("name");
							temp_part.L1 = new ArrayList<Element>();
							temp_part.select_parts = new ArrayList<String>();
							temp_part.set = null;
							pref_tmp.pref = temp_part;
							pref_tmp.pset = null;
							
							global_variable.PRefs.get(global_variable.counter).add(pref_tmp);
							
							global_variable.current_part = temp_part;
						}
						else
						{
							if (global_variable.current_part.set != null)
							{
								List<part_ref> prefs_temp = global_variable.PRefs.get(global_variable.counter);
								part_ref tmp_ref = null;
								
								for (int z = 0; z < prefs_temp.size(); z++)
								{
									if (prefs_temp.get(z).pset == global_variable.current_part.set)
									{
										tmp_ref = prefs_temp.get(z);
									}
								}
								
								if (tmp_ref == null)
								{
									part_ref pref_tmp = new part_ref();
									part temp_part = new part();
									temp_part.name = "selected_" + global_variable.roots[global_variable.counter].getAttributeValue("name") + "_" + (prefs_temp.size()+1);
									temp_part.type = global_variable.roots[global_variable.counter].getAttributeValue("name");
									temp_part.L1 = new ArrayList<Element>();
									temp_part.select_parts = new ArrayList<String>();
									temp_part.set = null;
									pref_tmp.pref = temp_part;
									pref_tmp.pset = global_variable.current_part.set;
									
									global_variable.PRefs.get(global_variable.counter).add(pref_tmp);
									
									global_variable.current_part = temp_part;
									
								}
								else
								{
									global_variable.current_part = tmp_ref.pref;
								}
								
								
							}
						}
						//den Activity Links hinzufügen
						Iterator<ActivityLink> AL_list = ALset.iterator();
						while (AL_list.hasNext())
						{
							ActivityLink ALtmp;
							ALtmp = AL_list.next();
							ALtmp.rec_parts.add(global_variable.current_part);						
						}
					}
					else
					{
						if (tempAL.instX == tempAL.instY)
						{
							if (global_variable.current_part == null)
							{
								List<Element> no_loops;
								no_loops = new ArrayList<Element>();
								global_variable.current_part = generatePTset(no_loops, act_element);
								global_variable.partSets.add(global_variable.current_part.set);
								
								//den Activity Links hinzufügen
								Iterator<ActivityLink> AL_list = ALset.iterator();
								while (AL_list.hasNext())
								{
									ActivityLink ALtmp;
									ALtmp = AL_list.next();
									ALtmp.rec_parts.add(global_variable.current_part);
								}
							}
							else
							{
								//den Activity Links hinzufügen
								Iterator<ActivityLink> AL_list = ALset.iterator();
								while (AL_list.hasNext())
								{
									ActivityLink ALtmp;
									ALtmp = AL_list.next();
									ALtmp.rec_parts.add(global_variable.current_part);
								}
							}
						}
						else
						{
							List<Element> loops;
							loops = new ArrayList<Element>();
							if (tempAL.sY < tempAL.sX)
							{
								Iterator<ActivityLink> AL_list = ALset.iterator();
								while (AL_list.hasNext())
								{
									ActivityLink ALtmp;
									ALtmp = AL_list.next();
									Element loop = getLoop(ALtmp.X, tempAL.sY);
									if (!loops.contains(loop))
									{
										loops.add(loop);
									}
								}
							}
							else
							{
								Iterator<ActivityLink> AL_list = ALset.iterator();
								while (AL_list.hasNext())
								{
									ActivityLink ALtmp;
									ALtmp = AL_list.next();
									Element loop = getLastLoop(ALtmp.X);
									if (!loops.contains(loop))
									{
										loops.add(loop);
									}
								}
							}
							if (global_variable.current_part == null)
							{
								global_variable.current_part = generatePTset(loops, act_element);
								global_variable.partSets.add(global_variable.current_part.set);
								//den Activity Links hinzufügen
								Iterator<ActivityLink> AL_list = ALset.iterator();
								while (AL_list.hasNext())
								{
									ActivityLink ALtmp;
									ALtmp = AL_list.next();
									ALtmp.rec_parts.add(global_variable.current_part);
								}
							}
							else
							{
								List<Element> existing_loops;
								part tmpPart = null;
								Iterator subset_list = global_variable.current_part.set.part_sets.iterator();
								while (subset_list.hasNext())
								{
									partSet tmpSet = (partSet) subset_list.next();
									existing_loops = tmpSet.L1;
									if (existing_loops.containsAll(loops) && loops.containsAll(existing_loops))
									{
										tmpPart = tmpSet.part_ref;
									}
								}
								
								if (tmpPart != null)
								{
									global_variable.current_part = tmpPart;
									//den Activity Links hinzufügen
									Iterator<ActivityLink> AL_list = ALset.iterator();
									while (AL_list.hasNext())
									{
										ActivityLink ALtmp;
										ALtmp = AL_list.next();
										ALtmp.rec_parts.add(global_variable.current_part);
										global_variable.current_part.set.bound = true;
									}
								}
								else
								{
									Iterator<partSet> partSet_list = global_variable.partSets.iterator();
									while (partSet_list.hasNext())
									{
										partSet tmpSet = partSet_list.next();
										existing_loops = tmpSet.L1;
										if (tmpSet.bound && existing_loops.containsAll(loops) && loops.containsAll(existing_loops))
										{
											tmpPart = tmpSet.part_ref;
										}
									}
									if (tmpPart != null)
									{
										global_variable.current_part = tmpPart;
										//den Activity Links hinzufügen
										Iterator<ActivityLink> AL_list = ALset.iterator();
										while (AL_list.hasNext())
										{
											ActivityLink ALtmp;
											ALtmp = AL_list.next();
											ALtmp.rec_parts.add(global_variable.current_part);
										}
									}
									else
									{
										tmpPart = generatePTset(loops, act_element);
										global_variable.current_part.set.part_sets.add(tmpPart.set);
										global_variable.current_part = tmpPart;
										global_variable.partSets.add(tmpPart.set);
										//den Activity Links hinzufügen
										Iterator<ActivityLink> AL_list = ALset.iterator();
										while (AL_list.hasNext())
										{
											ActivityLink ALtmp;
											ALtmp = AL_list.next();
											ALtmp.rec_parts.add(global_variable.current_part);
										}
									}
								}
							}
						}
					}
				}
				if (act_element.getName() == "invoke")
				{
					ALset = getALset(act_element, true);
					Iterator<ActivityLink> invokes = ALset.iterator();
					while (invokes.hasNext())
					{
						ActivityLink temp_AL = invokes.next();
						temp_AL.rec_parts.add(global_variable.current_part);
					}
				}
				
				generate_parts(S,O,B);
				return;
			}
		}
		
		//wenn O leer ist;
		return;
		
	}

	
	//überprüft, ob in der gegebenen Liste ein Element vom typ "else" vorhanden ist
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
	
	//liefert eine Menge von Activity Links zurück, die zu einem Element gehören
	//der Boolean receiving_invoke gibt dabei an, ob bei einem invoke die Activity Links gesucht sind,
	//in denen das invoke die empfangende Aktivität ist oder diejenigen gesucht sind,
	//bei denen dieses invoke die sendende Aktivität ist
	private static List<ActivityLink> getALset (Element elem, boolean receiving_invoke)
	{
		List<ActivityLink> ALset;
		ALset = new ArrayList<ActivityLink>();
		ActivityLink AL;
		Iterator<ActivityLink> list = global_variable.ActivityLinks.iterator();
		if (elem.getName() == "receive" || (elem.getName() == "invoke" && receiving_invoke) || elem.getName() == "onMessage" )
		{
			while (list.hasNext())
			{
				AL = list.next();
				if (elem == AL.Y)
				{
					ALset.add(AL);
				}
			}
		}
		if (elem.getName() == "reply" || (elem.getName() == "invoke" && !receiving_invoke))
		{
			while (list.hasNext())
			{
				AL = list.next();
				if (elem == AL.X)
				{
					ALset.add(AL);
				}
			}
		}
		return ALset;
	}
	
	
	//erzeugt Teilnehmermenge mit zugehöriger Teilnehmerreferenz
	private static part generatePTset(List<Element> loops, Element elem){
		partSet tmpSet;
		part tmpPart;
		tmpSet = new partSet();
		tmpPart = new part();
		
		//tmpSet erzeugen
		tmpSet.bound = true;
		tmpSet.initial_acts = new ArrayList<Element>();
		tmpSet.initial_acts.add(elem);
		tmpSet.L1 = new ArrayList<Element>();
		tmpSet.L1.addAll(loops);
		tmpSet.name = global_variable.roots[global_variable.counter].getAttributeValue("name")+"_set_"+(global_variable.partSets.size()+1);
		tmpSet.type = global_variable.roots[global_variable.counter].getAttributeValue("name");
		tmpSet.part_ref = tmpPart;
		tmpSet.part_sets = new ArrayList<partSet>();
		tmpSet.select_parts = new ArrayList<String>();
		
		//tmpPart erzeugen
		tmpPart.L1 = new ArrayList<Element>();
		tmpPart.L1.addAll(loops);
		tmpPart.name = "current_"+global_variable.roots[global_variable.counter].getAttributeValue("name")+"_"+(global_variable.partSets.size()+1);
		tmpPart.select_parts = new ArrayList<String>();
		tmpPart.set = tmpSet;
		tmpPart.type = global_variable.roots[global_variable.counter].getAttributeValue("name");
		
		return tmpPart;
	}
	
	
	//findet die number+1 -te Schleife von elem aus Richtung Wurzel
	private static Element getLoop(Element elem, int number)
	{
		int cntr = 0;
		Element loop;
		
		while (elem.getName() != "process")
		{
			if (elem.getName() == "forEach" || elem.getName() == "while" || elem.getName() == "repeatUntil")
			{
				cntr++;
				loop = elem;
				if (cntr > number)
				{
					return loop;
				}
			}
			elem = elem.getParentElement();
		}
		System.out.println("hier kommen wir nie hin");
		//Fehler vermerken
			global_variable.Error = global_variable.Error + " Schleife nicht gefunden, wo eine erwartet wurde.";
		return null;
	}

	//findet letzte schleife auf dem Pfad zur Wurzel
	private static Element getLastLoop(Element elem)
	{
		Element loop = null;
		while (elem.getName() != "process")
		{
			if (elem.getName() == "forEach" || elem.getName() == "while" || elem.getName() == "repeatUntil")
			{
				loop = elem;
			}
			elem = elem.getParentElement();
		}
		if (loop == null)
		{
			global_variable.Error = global_variable.Error + " Es wurde keine Schleife gefunden, wo eine erwartet wurde?";

		}
		return loop;
	}
	
	//wir fassen Activity Links zusammen, die übereinstimmende sendende oder empfangende Aktivitäten haben
	//(siehe Ausarbeitung Abschnitt 3.4)
	private static void adjust_ActivityLinks(boolean sender)
	{
		List<ActivityLink> ALlist;
		//gleiche Sender
		if (sender)
		{
			for (int k = 0; k < global_variable.ActivityLinks.size(); k++)
			{
				ALlist = new ArrayList<ActivityLink>();
				Element tmp = global_variable.ActivityLinks.get(k).X;
				for (int l = k+1; l < global_variable.ActivityLinks.size(); l++)
				{
					if (tmp == global_variable.ActivityLinks.get(l).X)
					{
						ALlist.add(global_variable.ActivityLinks.get(l));
					}
				}
				if (!ALlist.isEmpty())
				{
					ActivityLink ALtmp1 = global_variable.ActivityLinks.get(k);
					ActivityLink ALtmp2;
					Iterator<ActivityLink> matchingALs = ALlist.iterator();
					while (matchingALs.hasNext())
					{
						ALtmp2 = matchingALs.next();
						ALtmp1.rec_acts.addAll(ALtmp2.rec_acts);
						ALtmp1.rec_parts.addAll(ALtmp2.rec_parts);
						ALtmp1.send_part_sets.addAll(ALtmp2.send_part_sets);
						ALtmp1.send_parts.addAll(ALtmp2.send_parts);
						ALtmp1.bindSendersToRefs.add(ALtmp2.bindSenderTo);
						ALtmp1.bindSendersToRefs.addAll(ALtmp2.bindSendersToRefs);
						ALtmp2.Y.setAttribute("id", ALtmp1.Y.getAttributeValue("id", global_variable.wsu), global_variable.wsu);
					}
					global_variable.ActivityLinks.removeAll(ALlist);
					adjust_ActivityLinks(true);
					return;
				}
			}
		}
		
		sender = false;
		
		//gleiche Empfänger
		for (int k = 0; k < global_variable.ActivityLinks.size(); k++)
		{
			ALlist = new ArrayList<ActivityLink>();
			List<Element> tmp = global_variable.ActivityLinks.get(k).rec_acts;
			for (int l = k+1; l < global_variable.ActivityLinks.size(); l++)
			{
				boolean test = false;
				List<Element> tmp2 = global_variable.ActivityLinks.get(l).rec_acts;
				for (int m = 0; m < tmp2.size(); m++)
				{
					if (tmp.contains(tmp2.get(m)))
					{
						test = true;
					}
				}
				
				if (test)
				{
					ALlist.add(global_variable.ActivityLinks.get(l));
				}
			}
			if (!ALlist.isEmpty())
			{
				ActivityLink ALtmp1 = global_variable.ActivityLinks.get(k);
				ActivityLink ALtmp2;
				Iterator<ActivityLink> matchingALs = ALlist.iterator();
				while (matchingALs.hasNext())
				{
					ALtmp2 = matchingALs.next();
					ALtmp1.send_acts.addAll(ALtmp2.send_acts);
					ALtmp1.rec_acts.addAll(ALtmp2.rec_acts);
					ALtmp1.rec_parts.addAll(ALtmp2.rec_parts);
					ALtmp1.send_part_sets.addAll(ALtmp2.send_part_sets);
					ALtmp1.send_parts.addAll(ALtmp2.send_parts);
					ALtmp2.X.setAttribute("id", ALtmp1.X.getAttributeValue("id", global_variable.wsu), global_variable.wsu);
				}
				global_variable.ActivityLinks.removeAll(ALlist);
				adjust_ActivityLinks(false);
				return;
			}
		}
	}
	
	
	//erzeugen der Werte des select_parts Attributs der parts-Objekte
	private static void participant_selects()
	{
		for (int k = 0; k < global_variable.ActivityLinks.size(); k++)
		{
			boolean test = false;
			if (global_variable.ActivityLinks.get(k).Y.getName() == "receive")
			{
				if (global_variable.ActivityLinks.get(k).Y.getAttributeValue("createInstance") != null)
				{
					test = true;
				}
			}
			else if (global_variable.ActivityLinks.get(k).Y.getName() == "onMessage")
			{
				if (global_variable.ActivityLinks.get(k).Y.getParentElement().getAttributeValue("createInstance") != null)
				{
					test = true;
				}
			}
			if (test)
			{
				part tmp_part = global_variable.ActivityLinks.get(k).rec_parts.get(0);
				if (tmp_part.set == null)
				{
					for (int m = 0; m < global_variable.ActivityLinks.get(k).send_part_sets.size(); m++)
					{
						global_variable.ActivityLinks.get(k).send_part_sets.get(m).part_ref.select_parts.add(tmp_part.name);
					}
					for (int m = 0; m < global_variable.ActivityLinks.get(k).send_parts.size(); m++)
					{
						global_variable.ActivityLinks.get(k).send_parts.get(m).select_parts.add(tmp_part.name);
					}
				}
				else
				{
					for (int m = 0; m < global_variable.ActivityLinks.get(k).send_part_sets.size(); m++)
					{
						global_variable.ActivityLinks.get(k).send_part_sets.get(m).part_ref.select_parts.add(tmp_part.set.name);
					}
					for (int m = 0; m < global_variable.ActivityLinks.get(k).send_parts.size(); m++)
					{
						global_variable.ActivityLinks.get(k).send_parts.get(m).select_parts.add(tmp_part.set.name);
					}
				}
			}
		}
	}

	
	
	
	public static void participant_handling()
	{
		//generieren der Teilnehmer
		global_variable.ParticipantSets =  new ArrayList<List<partSet>>();
		for (global_variable.counter = 0; global_variable.counter < global_variable.i; global_variable.counter++)
		{
			global_variable.ParticipantSets.add(new ArrayList<partSet>());
		}
		
		global_variable.PRefs = new ArrayList<List<part_ref>>();
		for (global_variable.counter = 0; global_variable.counter < global_variable.i; global_variable.counter++)
		{
			global_variable.PRefs.add(new ArrayList<part_ref>());
		}
		
		
		for (global_variable.counter = 0; global_variable.counter < global_variable.i; global_variable.counter++)
		{
		global_variable.current_part = null;
		
		List<Element> S;
		S = new ArrayList<Element>();
		S.add(global_variable.roots[global_variable.counter]);
		
		List<Element> O;
		O = new ArrayList<Element>();
		
		List<Element> B;
		B = new ArrayList<Element>();
		
		global_variable.partSets = new ArrayList<partSet>();
		
		generate_parts(S,O,B);
		
		global_variable.ParticipantSets.get(global_variable.counter).addAll(global_variable.partSets);
		}
		
		//bereinigen der Activity Links
		adjust_ActivityLinks(true);
		
		//select Attribut der Teilnehmerreferenzen belegen
		participant_selects();
	}
}
