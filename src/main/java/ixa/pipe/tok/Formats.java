package ixa.pipe.tok;

public class Formats {
	
	public String cleanWeirdChars(String line) {
	    line = line.replace("’","'");
	    line = line.replace("‘","'");
	    line = line.replace('“','"');
	    line = line.replace('”','"');
	    return line; 
	    
	  }

}
