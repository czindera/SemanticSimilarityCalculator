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
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
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
		//System.out.println(findCommonAncestor("GO:0051252","GO:0045935").getID());
		//System.out.println("Similarity for GO:0050779 and GO:0019219 :  "+WuPalmerSim("GO:0050779", "GO:0019219"));
	}
	
	/**
	 * To get the next line of the source file
	 * @return
	 * @throws IOException
	 */
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
		fr.close();
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
			System.out.println("");
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
	 * Prints information about a node.
	 * @param term
	 */
	public void printTermInfo(String term){
		dags.printNodeInfo(term);
	}
	
	
	/**
	 * This method finds the root of the DAG.
	 * @param thisDAG
	 */

	private Term findRootOfDag(DirectedAcyclicGraph<Term, DefaultEdge> thisDAG){
		Term result = null;
		for(Term t : thisDAG.vertexSet()){
			if(thisDAG.incomingEdgesOf(t).size()==0){
				//System.out.println("This term is the root: "+t.getID());
				result = t;
			}
		}
		return result;
	}
	
	/**
	 * This method finds the lowest common ancestor (LCA) of two nodes
	 * @param termA
	 * @param termB
	 * @return LCA term
	 */
	private Term findCommonAncestor(String termA, String termB){
		Term thisTerm1 = dags.getTerms().get(termA);
		Term thisTerm2 = dags.getTerms().get(termB);
		DirectedAcyclicGraph<Term, DefaultEdge> thisDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
		if (thisTerm1!=null && thisTerm2!=null){
			if (thisTerm1.getNamespace().equals("biological_process") && thisTerm2.getNamespace().equals("biological_process")){
				thisDag = this.annotDagBP;
			} 
			else if (thisTerm1.getNamespace().equals("cellular_component") && thisTerm2.getNamespace().equals("cellular_component")){
				thisDag = this.annotDagCC;
			} 
			else if (thisTerm1.getNamespace().equals("molecular_function") && thisTerm2.getNamespace().equals("molecular_function")){
				thisDag = this.annotDagMF;
			} 	
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
	
	
	/**
	 * Calculates the distance between two nodes (the ancestor list of one node contains the other).
	 * This method could be used after LCA has been found.
	 * @param thisTerm1
	 * @param thisTerm2
	 * @return
	 */
	private int distanceOnSameWalk(Term thisTerm1, Term thisTerm2){
		int shortestDistance = 100000000;  //initial big distance
		//Term thisTerm1 = dags.getTerms().get(term1);
		//Term thisTerm2 = dags.getTerms().get(term2);
		DirectedAcyclicGraph<Term, DefaultEdge> thisDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
			if (thisTerm1.getNamespace().equals("biological_process") && thisTerm2.getNamespace().equals("biological_process")){
				thisDag = this.annotDagBP;
			} 
			else if (thisTerm1.getNamespace().equals("cellular_component") && thisTerm2.getNamespace().equals("cellular_component")){
				thisDag = this.annotDagCC;
			} 
			else if (thisTerm1.getNamespace().equals("molecular_function") && thisTerm2.getNamespace().equals("molecular_function")){
				thisDag = this.annotDagMF;
			}
		Set<Term> ancestorsTerm1 = thisDag.getAncestors(thisDag, thisTerm1);
		Set<Term> ancestorsTerm2 = thisDag.getAncestors(thisDag, thisTerm2);
		Term tempTerm = null;
		Term targetTerm = null;
		if (ancestorsTerm1.size() < ancestorsTerm2.size()){
			tempTerm = thisTerm2;
			targetTerm = thisTerm1;
		} else {
			tempTerm = thisTerm1;
			targetTerm = thisTerm2;
		}
		GraphPath<Term, DefaultEdge> path = new DijkstraShortestPath<Term, DefaultEdge>(thisDag).getPath( targetTerm, tempTerm);
		shortestDistance = path.getEdgeList().size();
		/*for(DefaultEdge e : path.getEdgeList()){
			System.out.println(thisDag.getEdgeSource(e).getID()+" --> "+thisDag.getEdgeTarget(e).getID());
		}*/
		return shortestDistance;
	}
	
	/**
	 * Similarity calculation using Wu and Palmer method
	 * @param term1
	 * @param term2
	 * @return a similarity value: [0-1]
	 */
	public double WuPalmerSim(String term1, String term2){
		double result=-1;
		Term thisTerm1 = dags.getTerms().get(term1);
		Term thisTerm2 = dags.getTerms().get(term2);
		Term commonAncestor = findCommonAncestor(term1,term2); 
		DirectedAcyclicGraph<Term, DefaultEdge> thisDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
		if (thisTerm1!=null && thisTerm2!=null){
			if (thisTerm1.getNamespace().equals("biological_process") && thisTerm2.getNamespace().equals("biological_process")){
				thisDag = this.annotDagBP;
			} 
			else if (thisTerm1.getNamespace().equals("cellular_component") && thisTerm2.getNamespace().equals("cellular_component")){
				thisDag = this.annotDagCC;
			} 
			else if (thisTerm1.getNamespace().equals("molecular_function") && thisTerm2.getNamespace().equals("molecular_function")){
				thisDag = this.annotDagMF;
			}
			Term rootTerm = findRootOfDag(thisDag);
			if (term1.equals(term2)){
				System.out.println("Same terms.");
				result = 1;
			} else {
				double n3 = (double)distanceOnSameWalk(commonAncestor,rootTerm);
				double n1 = (double)distanceOnSameWalk(thisTerm1,commonAncestor);
				double n2 = (double)distanceOnSameWalk(thisTerm2,commonAncestor);
				result = (2*n3)/(n1+n2+2*n3);
			}			
		} else { 
			System.out.println("Term ID's not given correctly.");
		}
		return result;
	}
	
}
