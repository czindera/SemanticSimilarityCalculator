package OBOReader;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Terms {
	HashMap<String,Term> Terms;
	Term term;
	
	Terms() {
		Terms = new LinkedHashMap<String,Term>();
	}
	
	public Term addTerm(Term t) {
		return Terms.put(t.id, t);
	}
	
	public Term get(String s) {
		return Terms.get(s);
	}
	
	public void printTerms() {
		Terms.forEach((key, value) -> System.out.println(key + " : " + value));
	}

}
