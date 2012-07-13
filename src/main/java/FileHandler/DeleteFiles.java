package FileHandler;

import java.io.File;

//Dateien löschen
public class DeleteFiles {

	  public static void deleteFiles( File path ) 
	  { 
	    for ( File file : path.listFiles() ) 
	    { 
	      if ( file.isDirectory() ) 
	        deleteFiles( file ); 
	      file.delete(); 
	    } 
	  }
	  
}
