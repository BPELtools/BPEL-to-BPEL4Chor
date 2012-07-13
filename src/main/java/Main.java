import java.io.IOException;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;


public class Main {

	/**
	* @param args
	*/
	public static void main(String[] args)
	{
		try
		{
			//BPEL Datien einlesen, wsu:id hinzufügen usw.
			FL_handling.fl_handling();
				
			//PartnerLinks finden
			PL_handling.pl_handling();
			
			//Aktivitätsverbindungen generieren
			AL_handling.al_handling();	
			
			//generieren der Teilnehmer
			Participant_handling.participant_handling();
			
			//Korrelationsmengen Umwandlung der QNames
			CorrelationSet_handling.correlationSet_handling();
			
			//generieren der Bestandteile einer BPEL4Chor Beschreibung
			generateChoreography.generate_choreography();
			
			
			//Test Beginn
			for (int k = 0; k < global_variable.ActivityLinks.size(); k++)
			{
				System.out.println();
				System.out.println(global_variable.ActivityLinks.get(k).X.getAttributeValue("id", global_variable.wsu));
				System.out.println(global_variable.ActivityLinks.get(k).processX);
				System.out.println("Instanzklasse: n^" + global_variable.ActivityLinks.get(k).instX);
				if (global_variable.ActivityLinks.get(k).send_part_sets.isEmpty())
				{
					System.out.println(global_variable.ActivityLinks.get(k).send_parts.get(0).name);
				}
				else
				{
					System.out.println(global_variable.ActivityLinks.get(k).send_part_sets.get(0).name);
				}
				System.out.println(global_variable.ActivityLinks.get(k).Y.getAttributeValue("id", global_variable.wsu));
				System.out.println(global_variable.ActivityLinks.get(k).processY);
				System.out.println("Instanzklasse: n^" + global_variable.ActivityLinks.get(k).instY);
				System.out.println(global_variable.ActivityLinks.get(k).rec_parts.get(0).name);
			}
			System.out.println("");
			System.out.println("");
			System.out.println("Anzahl Message Links: " + global_variable.ActivityLinks.size());
			System.out.println("Anzahl corr_props-Elemente: " + global_variable.CORR_PROPs.size());
			//Test Ende
		}
		catch (Exception e)
		{
			try
			{
			 BufferedWriter out = new BufferedWriter(
                     new OutputStreamWriter(
                     new FileOutputStream("BPEL4Chor/ReadMe.txt" ) ) );
			 out.write("Es ist ein Fehler aufgetreten.");
			 out.newLine();
			 out.write("Bitte überprüfen Sie Ihre Eingabedaten.");
			 out.newLine();
			 out.write(global_variable.Error);
			 out.close();
			}
			catch ( IOException f ) 
			{ 
				  System.err.println( "Hier geht auch gar nix!" ); 
			} 
			System.err.println("Es ist ein Fehler aufgetrete. Bitte überprüfen sie ihre Eingabedaten.");
		}
		
	}

}
