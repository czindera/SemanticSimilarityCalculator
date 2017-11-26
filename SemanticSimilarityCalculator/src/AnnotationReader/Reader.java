package AnnotationReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;

import OBOReader.DagBuilder;
import OBOReader.Term;
import OBOReader.Terms;

public class Reader {
	private BufferedReader in;
	private String buffer;
	private DirectedAcyclicGraph<Term, DefaultEdge> annotDagBP,annotDagMF,annotDagCC;
	private Map<Term,Set<Term>> termMap;
	private DagBuilder dags;
	
	public Reader(){
		in=null;
		buffer = null;
		this.annotDagBP = new DirectedAcyclicGraph<>(DefaultEdge.class);
		this.annotDagMF = new DirectedAcyclicGraph<>(DefaultEdge.class);
		this.annotDagCC = new DirectedAcyclicGraph<>(DefaultEdge.class);
		this.termMap = new HashMap<Term,Set<Term>>();
		this.dags = new DagBuilder();
		try {
			parse();
			dagBuilder();
		} catch (IOException | CycleFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Uppropagation finished!");
		//printEdges(annotDagBP);
		//listAncestors("GO:0050779");
	}
	
	private String next() throws IOException {
		if(buffer!=null) {
			String s=buffer;
			buffer=null;
			return s;
		}
		return in.readLine();
	}
	
	/**
	 * This method reads the file and creates a MAP from Terms and all their ancestors.
	 * @throws IOException
	 * @throws CycleFoundException
	 */
	private void parse() throws IOException, CycleFoundException {
		ClassLoader cl = getClass().getClassLoader();
		File annFile = new File(cl.getResource("./AnnotationFiles/gene_association.ecocyc").getFile());
	    FileReader fr = new FileReader(annFile);
		in=new BufferedReader(fr);
		String line;
		while((line=next())!=null) {
			if(!line.startsWith("!")) {
				String[] words = line.split("\\s+");
				String goID = words[3];
				//System.out.println(words[3]); //GO IDs
				Terms allTerms = this.dags.getTerms();
				if (goID.startsWith("GO")){
					DirectedAcyclicGraph<Term, DefaultEdge> thisDag = this.dags.dagDecider(goID); 
					if (thisDag!=null){
						Term thisTerm = allTerms.get(goID);
						this.termMap.put(thisTerm, thisDag.getAncestors(thisDag, thisTerm));
						
					}
				}
			}
		}
		in.close();
		//System.out.println(temporaryDag.toString());
	}
	
	/**
	 * This method finishes the up propagation by reading the Map and finding relevant Edges
	 * @throws CycleFoundException
	 */
	private void dagBuilder() throws CycleFoundException{
		DirectedAcyclicGraph<Term, DefaultEdge> temporaryDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
		Iterator<Entry<Term, Set<Term>>> mapIterator = this.termMap.entrySet().iterator();
		
		while(mapIterator.hasNext()){
			Entry<Term, Set<Term>> entry = mapIterator.next();
	        Term thisTerm = entry.getKey();
	        Set<Term> thisSet = entry.getValue();
	        DirectedAcyclicGraph<Term, DefaultEdge> relevantOBODag = this.dags.dagDecider(thisTerm.getID());
	        if (thisTerm.getNamespace().equals("biological_process")){
    			temporaryDag = this.annotDagBP;
    		} 
    		else if (thisTerm.getNamespace().equals("cellular_component")){
    			temporaryDag = this.annotDagCC;
    		} 
    		else if (thisTerm.getNamespace().equals("molecular_function")){
    			temporaryDag = this.annotDagMF;
    		}
	        temporaryDag.addVertex(thisTerm);
	        Iterator<Term> setIterator = thisSet.iterator();
	        while(setIterator.hasNext()){
	        	Term thisAncestor = setIterator.next();
	        	temporaryDag.addVertex(thisAncestor);
	        	if(relevantOBODag.containsEdge(thisAncestor, thisTerm)){
	        		//System.out.println("Edge has been found in original DAG.");
	        		temporaryDag.addDagEdge(thisAncestor, thisTerm);
	        	}
	        	
	        }
	        //double loop in the same set to find all existing edges between Terms in original
	        for(Term t1: thisSet)
	        { 
	            for(Term t2: thisSet)
	            {    
	            	if(relevantOBODag.containsEdge(t1, t2)){
		        		//System.out.println("Edge has been found in original DAG.");
		        		temporaryDag.addDagEdge(t1, t2);
		        	}
	            	if(relevantOBODag.containsEdge(t2, t1)){
		        		//System.out.println("Edge has been found in original DAG.");
		        		temporaryDag.addDagEdge(t2, t1);
		        	}
	            }
	         }
	        
	        if (thisTerm.getNamespace().equals("biological_process")){
				this.annotDagBP = temporaryDag;
			} 
			else if (thisTerm.getNamespace().equals("cellular_component")){
				this.annotDagCC = temporaryDag;
			} 
			else if (thisTerm.getNamespace().equals("molecular_function")){
				this.annotDagMF = temporaryDag;
			}
		}
		
	}
	
	
	/**
	 * To get the nearest ancestors of a term.
	 * @param dag
	 * @param term
	 * @return a Set of Terms
	 */
	private Set<Term> getAncestors(DirectedAcyclicGraph<Term, DefaultEdge> dag, Term term){
		Set<Term> termSet = new HashSet<Term>();
		Iterator<DefaultEdge> edgeIterator = dag.incomingEdgesOf(term).iterator();
		while (edgeIterator.hasNext()){
			DefaultEdge currentEdge = edgeIterator.next();
			Term ancestor = dag.getEdgeSource(currentEdge);
			termSet.add(ancestor);			
		}	
		return termSet;
	}
	
	/**
	 * This method prints out the nearest ancestors of a Term
	 * @param term
	 */
	public void listAncestors(String term){
		Term thisTerm = dags.getTerms().get(term);
		DirectedAcyclicGraph<Term, DefaultEdge> temporaryDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
		if (thisTerm!=null){
			if (thisTerm.getNamespace().equals("biological_process")){
				temporaryDag = this.annotDagBP;
			} 
			else if (thisTerm.getNamespace().equals("cellular_component")){
				temporaryDag = this.annotDagCC;
			} 
			else if (thisTerm.getNamespace().equals("molecular_function")){
				temporaryDag = this.annotDagMF;
			} 
			
			System.out.print("The ancestor/s of "+term+" term: [ ");
			getAncestors(temporaryDag,thisTerm).stream().forEach(theTerm -> System.out.print(theTerm.getID()+" "));
			System.out.print("]");
		} else { 
			System.out.println("No such term exists!");
			return;
		}
	}
	
	
	/**
	 * Prints out the edges of a DAG in (sourceVertex --> targetVertex) format
	 * @param thisDAG
	 */
	@SuppressWarnings("unused")
	private void printEdges(DirectedAcyclicGraph<Term, DefaultEdge> thisDAG) {
		for(DefaultEdge e : thisDAG.edgeSet()){
		    System.out.println(thisDAG.getEdgeSource(e).getID() + " --> " + thisDAG.getEdgeTarget(e).getID());
		}
	}
}
