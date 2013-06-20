package ixa.pipe.resources;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class NonBreakingPrefixes {
	
	

	/** any single upper case letter  followed by a period is not a sentence ender 
	 * (excluding I occasionally, but we leave it in)
	usually upper case letters are initials in a name
	**/
	
	public final Set<String> upperLetters = new HashSet<String>(Arrays.asList("A", "B",
			"C", "D", "E" , "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", 
			"Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"));
	
	/** 
	 * List of titles. These are often followed by upper-case names, but do
	 *  not indicate sentence breaks
	 */
	
	public final Set<String> titles = new HashSet<String>(Arrays.asList("Adj","Adm", 
			"Adv", "Asst", "Bart", "Bldg", "Brig", "Bros","Capt", "Cmdr", "Col", 
			"Comdr", "Con", "Corp", "Cpl", "DR", "Dr", "Drs", "Ens", "Gen", "Gov",
			"Hon", "Hr", "Hosp", "Insp", "Lt","MM", "MR", "MRS", "MS", "Maj", "Messrs",
			"Mlle", "Mme", "Mr", "Mrs", "Ms", "Msgr", "Op","Ord", "Pfc", "Ph", "Prof",
			"Pvt", "Rep","Reps","Res","Rev","Rt","Sen","Sens","Sfc","Sgt","Sr","St",
			"Supt", "Surg"));
		
	/** 
	 * misc - odd period-ending items that NEVER indicate breaks 
	 * (p.m. does NOT fall into this category - it sometimes ends a sentence)
	 */
	
	public final Set<String> periodEnding = new HashSet<String>(Arrays.asList("v","vs",
			"i.e", "rev","e.g"));
	
	/**
	 * Numbers only. These should only induce breaks when followed by a 
	 * numeric sequence
	 * This case is mostly for the english "No." 
	 * which can either be a sentence of its own, or if followed by a number, 
	 * a non-breaking prefix
	 */
	
	public final Set<String> numericOnly = new HashSet<String>(Arrays.asList("No","Art","pp"));
	
	public final Set<String> numbers = new HashSet<String>(Arrays.asList("Nos","Nr"));

}
