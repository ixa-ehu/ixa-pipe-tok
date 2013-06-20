package ixa.pipe.resources;

public class Formats {
	
	public String cleanWeirdChars(String line) {
	    line = line.replace("’","'");
	    line = line.replace("‘","'");
	    line = line.replace('“','"');
	    line = line.replace('”','"');
	    line = line.replace("`","'");
	    
	    return line; 
	    
	  }

}
