package OBOReader;

import java.util.HashSet;

public class Term {
	String id;
	String name;
	String namespace;
	String def;
	HashSet<String> genes;
	double IC;
	
	Term() {
		this.id = null;
		this.name = null;
		this.namespace = null;
		this.def = null;
		this.genes = new HashSet<String>();
		this.IC = 0;
	}
	
	public Term(String id) {
		this.id = id;
		this.name = null;
		this.namespace = null;
		this.def = null;
		this.genes = new HashSet<String>();
		this.IC = 0;
	}
	
	public boolean addGene(String newGene){
		return genes.add(newGene);
	}
	
	public boolean addGenes(HashSet<String> newSet){
		return genes.addAll(newSet);
	}
	
	public String getNamespace(){
		return this.namespace;
	}
	
	public String getID(){
		return this.id;
	}
	
	public double getIC(){
		return this.IC;
	}
	
	public HashSet<String> getGeneList(){
		return this.genes;
	}
	
	public String toString() {
		return "id: "+this.id+"\nname: "+this.name+"\nnamespace: "+this.namespace+"\ndef: "+this.def;
	}
}
