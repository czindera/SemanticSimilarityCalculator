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
	
	public Term(String id) {
		this.id = id;
		this.name = null;
		this.namespace = null;
		this.def = null;
	}
	
	public String getNamespace(){
		return this.namespace;
	}
	
	public String getID(){
		return this.id;
	}
	
	public String toString() {
		return "id: "+this.id+"\nname: "+this.name+"\nnamespace: "+this.namespace+"\ndef: "+this.def;
	}
}
