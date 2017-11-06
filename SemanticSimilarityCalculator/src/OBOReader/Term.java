package OBOReader;


public class Term {
	String id;
	String name;
	String namespace;
	String def;
	
	Term() {
		this.id = null;
		this.name = null;
		this.namespace = null;
		this.def = null;
	}
	
	Term(String id) {
		this.id = id;
		this.name = null;
		this.namespace = null;
		this.def = null;
	}
}