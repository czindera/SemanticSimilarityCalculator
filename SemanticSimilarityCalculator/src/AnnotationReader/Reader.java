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
		//findRootOfDag(annotDagBP);
		System.out.println(findCommonAncestor("GO:0051252","GO:0045935").getID());
		/*System.out.println("Distance between GO:0050779 and GO:0051252 "+distanceOfEdges("GO:0051252","GO:0050779"));
		System.out.println("Distance between GO:0051252 and GO:0050779 "+distanceOfEdges("GO:0050779","GO:0051252"));
		System.out.println("Distance between GO:0050779 and GO:0051254 "+distanceOfEdges("GO:0050779","GO:0051254"));
		System.out.println("Distance between GO:0051254 and GO:0050779 "+distanceOfEdges("GO:0051254","GO:0050779"));*/
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
	        //double loop in the same set to find all existing edges between Terms in original dag
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
	 * Calls printDagEdges with relevant DAG
	 * @param choosenDag
	 */
	public void printEdges(String choosenDag){
		switch (choosenDag){
		case "BP": case "1":{
			printDagEdges(this.annotDagBP);
			break;
		}
		case "MF": case "2":{
			printDagEdges(this.annotDagMF);
			break;
		}
		case "CC": case "3":{
			printDagEdges(this.annotDagCC);
			break;
		}
		}
	}
	
	/**
	 * Prints out the edges of a DAG in (sourceVertex --> targetVertex) format
	 * @param thisDAG
	 */
	
	private void printDagEdges(DirectedAcyclicGraph<Term, DefaultEdge> thisDAG) {
		for(DefaultEdge e : thisDAG.edgeSet()){
		    System.out.println(thisDAG.getEdgeSource(e).getID() + " --> " + thisDAG.getEdgeTarget(e).getID());
		}
	}
	
	
	/**
	 * This method finds the root of the DAG.
	 * @param thisDAG
	 */
	@SuppressWarnings("unused")
	private Term findRootOfDag(DirectedAcyclicGraph<Term, DefaultEdge> thisDAG){
		Term result = null;
		for(Term t : thisDAG.vertexSet()){
			if(thisDAG.incomingEdgesOf(t).size()==0){
				System.out.println("This term is the root: "+t.getID());
				result = t;
			}
		}
		return result;
	}
	
	private Term findCommonAncestor(String termA, String termB){
		Term thisTerm1 = dags.getTerms().get(termA);
		Term thisTerm2 = dags.getTerms().get(termB);
		DirectedAcyclicGraph<Term, DefaultEdge> thisDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
			if (thisTerm1.getNamespace().equals("biological_process")){
				thisDag = this.annotDagBP;
			} 
			else if (thisTerm1.getNamespace().equals("cellular_component")){
				thisDag = this.annotDagCC;
			} 
			else if (thisTerm1.getNamespace().equals("molecular_function")){
				thisDag = this.annotDagMF;
			} 
		Set<Term> ancestorSetA = thisDag.getAncestors(thisDag, thisTerm1);
		Set<Term> ancestorSetB = thisDag.getAncestors(thisDag, thisTerm2);
		//if one term's ancestorSet contains the other term
		if (ancestorSetA.size() < ancestorSetB.size()){   // term1 is closer to root
			if(ancestorSetB.contains(thisTerm1)){
				return thisTerm1;
			}
		} else if(ancestorSetA.contains(thisTerm2)){ //term2 is closer to root
				return thisTerm2;
		} 
			Set<Term> mutualTermSet = new HashSet<Term>(ancestorSetB);	    
			mutualTermSet.retainAll(ancestorSetA);
			Term result = null;
			for (Term t : mutualTermSet){ 
				Iterator<DefaultEdge> edgeIterator = thisDag.outgoingEdgesOf(t).iterator();
				boolean allEdgesPointOut = false;
				while (edgeIterator.hasNext()){
					DefaultEdge thisEdge = edgeIterator.next();
					if (mutualTermSet.contains(thisDag.getEdgeTarget(thisEdge))){
						allEdgesPointOut = true;
					}
				}
				if(!allEdgesPointOut){
					result = t;
				}
			}
			return result;
		
		/*
		 * Computation of lowest common ancestors may be useful, for instance, as part of a procedure 
		 * for determining the distance between pairs of nodes in a tree: the distance from v to w can 
		 * be computed as the distance from the root to v, plus the distance from the root to w, minus 
		 * twice the distance from the root to their lowest common ancestor
		 * */	
	}
	
	@SuppressWarnings("unused")
	private int distanceOfEdges(String term1, String term2){
		int result=-1;
		Term thisTerm1 = dags.getTerms().get(term1);
		Term thisTerm2 = dags.getTerms().get(term2);
		DirectedAcyclicGraph<Term, DefaultEdge> temporaryDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
		if (thisTerm1!=null && thisTerm2!=null){
			if (thisTerm1.getNamespace().equals("biological_process")){
				temporaryDag = this.annotDagBP;
			} 
			else if (thisTerm1.getNamespace().equals("cellular_component")){
				temporaryDag = this.annotDagCC;
			} 
			else if (thisTerm1.getNamespace().equals("molecular_function")){
				temporaryDag = this.annotDagMF;
			} 
			if(term1.equals(term2)) {result = 0;}
			else {
				//result = 
			}
		}
		return result;
	}
	
}
