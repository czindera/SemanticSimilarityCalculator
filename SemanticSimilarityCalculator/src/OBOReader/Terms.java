package OBOReader;

import java.util.HashMap;

public class Terms {
	HashMap<String,Term> Terms;
	Term term;
	
	Terms() {
		Terms = new HashMap<String,Term>();
	}
	
	public Term addTerm(Term t) {
		return Terms.putIfAbsent(t.id, t);
	}
	
	public Term get(String s) {
		return Terms.get(s);
	}

}
