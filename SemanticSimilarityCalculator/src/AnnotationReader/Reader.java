package AnnotationReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.BidirectionalDijkstraShortestPath;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.collect.HashMultimap;

import OBOReader.DagBuilder;
import OBOReader.Term;
import OBOReader.Terms;
import controller.MyFormatter;

public class Reader {
	private BufferedReader in;
	private String buffer;
	private DirectedAcyclicGraph<Term, DefaultEdge> annotDagBP,annotDagMF,annotDagCC;
	private Map<Term,Set<Term>> termMap;
	private HashMultimap<String,Set<Term>> geneMap;
	private HashMultimap<String,String> geneAnnotations;
	private DagBuilder dags;
	private HashSet<String> initSet;
	private HashSet<String> geneList;
	private final static Logger LOGGER = Logger.getLogger(Reader.class.getName());
	
	
	public Reader(){
		LOGGER.setUseParentHandlers(false);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new MyFormatter());
		LOGGER.addHandler(handler);
		in=null;
		buffer = null;
		this.annotDagBP = new DirectedAcyclicGraph<>(DefaultEdge.class);
		this.annotDagMF = new DirectedAcyclicGraph<>(DefaultEdge.class);
		this.annotDagCC = new DirectedAcyclicGraph<>(DefaultEdge.class);
		this.termMap = new HashMap<Term,Set<Term>>();
		this.geneAnnotations =  HashMultimap.create();
		this.geneMap = HashMultimap.create();
		this.dags = new DagBuilder();
		this.geneList = new HashSet<>();
		try {
			initSet = Stream.of("EXP", "IDA","IPI","IMP","IGI","IEP").collect(Collectors.toCollection(HashSet::new));
			parse(initSet,"E.Coli(local)");
			dagBuilder();
			//call the uppropagate methods here
			uppropagate(annotDagBP);
			uppropagate(annotDagMF);
			uppropagate(annotDagCC);
			//call methods here to calculate IC for all Terms
			calculateIC(annotDagBP);
			calculateIC(annotDagMF);
			calculateIC(annotDagCC);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		LOGGER.info("Uppropagation finished!");
		//printEdges(annotDagBP);
		//listAncestors("GO:0050779");
		//findRootOfDag(annotDagBP);
		//System.out.println(findCommonAncestor("GO:0051252","GO:0045935").getID());
		
		//LOGGER.info("Root of BP DAG has these genes associated to it: "+findRootOfDag(annotDagBP).getGeneList());
		//LOGGER.info("The term GO:0008150 has these genes associated to it: "+dags.getTerms().get("GO:0008150").getGeneList());
		//LOGGER.info("Root of CC DAG has these genes associated to it: "+findRootOfDag(annotDagCC).getGeneList());
		//LOGGER.info("Root of MF DAG has these genes associated to it: "+findRootOfDag(annotDagMF).getGeneList());
		
		//LOGGER.info("WuPalmer Similarity for GO:0050779 and GO:0019219 :  "+WuPalmerSim("GO:0050779", "GO:0019219"));
		LOGGER.info("Resnik Similarity for GO:0000105 and GO:0000967 :  "+ResnikSim("GO:0000105", "GO:0000967", false));
		LOGGER.info("DekangLin Similarity for GO:0000105 and GO:0000967 :  "+DekangLinSim("GO:0000105", "GO:0000967", false));
		LOGGER.info("JiangConrathSim Similarity for GO:0000105 and GO:0000967 :  "+JiangConrathSim("GO:0000105", "GO:0000967", false));
		LOGGER.info("simUI similarity for gene P05052-appY and P0AGK4-yhbY : "+simUI("P05052-appY","P0AGK4-yhbY"));
		LOGGER.info("simGIC similarity for gene P05052-appY and P0AGK4-yhbY : "+simGIC("P05052-appY","P0AGK4-yhbY"));
		LOGGER.info("Resnik Similarity for GO:0000105 and GO:0000967 using DiShIn : "+ResnikSim("GO:0000105", "GO:0000967", true));
		LOGGER.info("DekangLin Similarity for GO:0000105 and GO:0000967 using DiShIn : "+DekangLinSim("GO:0000105", "GO:0000967", true));
		LOGGER.info("JiangConrathSim Similarity for GO:0000105 and GO:0000967 using DiShIn : "+JiangConrathSim("GO:0000105", "GO:0000967", true));

		//printIC();
		//findLeavesWithLongestPath();
		
	}
	
	/**
	 * to print all IC values in the BP dag
	 */
	public void printIC(){
		Iterator<Term> myiterator =annotDagBP.vertexSet().iterator();
		while(myiterator.hasNext()){
			Term thisterm = myiterator.next();
			LOGGER.info("IC of term "+thisterm.getID()+": "+thisterm.getIC());
		}
	}
	/**
	 * 
	 * @param term
	 * @return
	 */
	public String getName(String term){
		return this.dags.getTerms().get(term).getName();
	}
	
	/**
	 * 
	 * @param term
	 * @return
	 */
	public String getDef(String term){
		return this.dags.getTerms().get(term).getDef();
	}
	
	/**
	 * 
	 * @return
	 */
	public HashSet<String> getGeneList(){
		return this.geneList;
	}
	
	/**
	 * 
	 * @return
	 */
	public HashSet<String> getBPterms(){
		HashSet<String> result = new HashSet<>();
		for (Term t : annotDagBP.vertexSet()){
			result.add(t.getID());
		}
		return result;
	}
	
	/**
	 * 
	 * @return
	 */
	public HashSet<String> getCCterms(){
		HashSet<String> result = new HashSet<>();
		for (Term t : annotDagCC.vertexSet()){
			result.add(t.getID());
		}
		return result;
	}
	
	/**
	 * 
	 * @return
	 */
	public HashSet<String> getMFterms(){
		HashSet<String> result = new HashSet<>();
		for (Term t : annotDagMF.vertexSet()){
			result.add(t.getID());
		}
		return result;
	}
	
	/**
	 * updates the class fields according to a given file, with selected ECodes
	 * @param fileName
	 * @param selectedCodes
	 */
	public void updateReader(String fileName, HashSet<String> selectedCodes){
        in=null;
        buffer = null;
        this.annotDagBP = new DirectedAcyclicGraph<>(DefaultEdge.class);
        this.annotDagMF = new DirectedAcyclicGraph<>(DefaultEdge.class);
        this.annotDagCC = new DirectedAcyclicGraph<>(DefaultEdge.class);
        this.termMap = new HashMap<>();
        //this.dags = new DagBuilder();
        geneAnnotations.clear();
        this.geneAnnotations =  HashMultimap.create();
        geneMap.clear();
		this.geneMap = HashMultimap.create();
		this.geneList = new HashSet<>();
        try {
                LOGGER.info("Rebuilding and propagating DAGs.");
                parse(selectedCodes,fileName);
                dagBuilder();
                
                //call the uppropagate methods here
                uppropagate(annotDagBP);
                uppropagate(annotDagMF);
                uppropagate(annotDagCC);
                //call methods here to calculate IC for all Terms
                calculateIC(annotDagBP);
                calculateIC(annotDagMF);
                calculateIC(annotDagCC);
                LOGGER.info("Root of BP DAG has these genes associated to it: "+findRootOfDag(annotDagBP).getGeneList());
                LOGGER.info("Root of CC DAG has these genes associated to it: "+findRootOfDag(annotDagCC).getGeneList());
                LOGGER.info("Root of MF DAG has these genes associated to it: "+findRootOfDag(annotDagMF).getGeneList());

        } catch (IOException e) {
                e.printStackTrace();
        }

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
	 * It also filters genes according to given evidence codes.
	 * @throws IOException
	 * @throws CycleFoundException
	 */
	private void parse(HashSet<String> filters, String fileName) throws IOException {
		ClassLoader cl = getClass().getClassLoader();
		File annFile;
		if (fileName.equals("E.Coli(local)")){
            annFile = new File(cl.getResource("./AnnotationFiles/E.Coli(local)").getFile());
        } else {
            annFile = new File(System.getProperty("user.dir")+File.separator+fileName);
            //Paths.get(System.getProperty("user.dir")).resolve(fileName)
        }
		FileReader fr = new FileReader(annFile);
		in=new BufferedReader(fr);
		String line;
		while((line=next())!=null) {
			if(!line.startsWith("!")) {
				String[] words = line.split("\\s+");
				String goID = words[3];
				String gene = words[1]+"-"+words[2];
				geneList.add(gene);
				String eCode = words[5];
				Terms allTerms = this.dags.getTerms();
				if (goID.startsWith("GO")){
					DirectedAcyclicGraph<Term, DefaultEdge> thisDag = this.dags.dagDecider(goID);
					if (thisDag!=null){
						Term thisTerm = allTerms.get(goID);
						if (filters.contains(eCode)){
							if (!thisTerm.addGene(gene)) {
								//System.out.println("Gene already exists in the Set.");
							}
							Set<Term> ancestors = thisDag.getAncestors(thisTerm);
							this.termMap.put(thisTerm, ancestors);
							Set<Term> geneAnnotatedTo = ancestors;
							geneAnnotatedTo.add(thisTerm);
							this.geneMap.put(gene, geneAnnotatedTo);
							this.geneAnnotations.put(gene, goID);
						}
					}
				}
			}
		}
		in.close();
		fr.close();
		//System.out.println(temporaryDag.toString());
	}
	
	/**
	 * This method finishes building the annotated DAGs by reading the Map and finding relevant Edges
	 * 
	 */
	private void dagBuilder(){
		DirectedAcyclicGraph<Term, DefaultEdge> temporaryDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
		Iterator<Entry<Term, Set<Term>>> mapIterator = this.termMap.entrySet().iterator();
		while(mapIterator.hasNext()){
			Entry<Term, Set<Term>> entry = mapIterator.next();
	        Term thisTerm = entry.getKey();
	        Set<Term> thisSet = entry.getValue();
	        DirectedAcyclicGraph<Term, DefaultEdge> relevantOBODag = this.dags.dagDecider(thisTerm.getID());
	        temporaryDag = dagSelector(thisTerm);
	        temporaryDag.addVertex(thisTerm);
	        Iterator<Term> setIterator = thisSet.iterator();
	        while(setIterator.hasNext()){
	        	Term thisAncestor = setIterator.next();
	        	temporaryDag.addVertex(thisAncestor);
	        	if(relevantOBODag.containsEdge(thisAncestor, thisTerm)){
	        		//System.out.println("Edge has been found in original DAG.");
	        		temporaryDag.addEdge(thisAncestor, thisTerm);
	        	}	
	        }
	        //double loop in the same set to find all existing edges between Terms in original DAG
	        for(Term t1: thisSet)
	        { 
	            for(Term t2: thisSet)
	            {    
	            	if(relevantOBODag.containsEdge(t1, t2)){
		        		//System.out.println("Edge has been found in original DAG.");
		        		temporaryDag.addEdge(t1, t2);
		        	}
	            	if(relevantOBODag.containsEdge(t2, t1)){
		        		//System.out.println("Edge has been found in original DAG.");
		        		temporaryDag.addEdge(t2, t1);
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
	 * This method sets the genes for all Terms in the DAG => uppropagation
	 * @param dag
	 */
	private void uppropagate(DirectedAcyclicGraph<Term, DefaultEdge> dag){
		for(Term t : dag.vertexSet()){
			for(Term ancestor : dag.getAncestors(t)){
				if (!ancestor.addGenes(t.getGeneList())){
					//System.out.println("Gene Set was not modified for term "+ancestor.getID());
				}
			}
		}
	}
	
	/**
	 * This method should be called in the constructor and in updateReader after uppropagation to calculate 
	 * the information content for each term.
	 * @param thisDag
	 */
	private void calculateIC(DirectedAcyclicGraph<Term, DefaultEdge> thisDag){
		double y = findRootOfDag(thisDag).getGeneList().size();
		for (Term thisTerm : thisDag.vertexSet()) {		
			double x = thisTerm.getGeneList().size();
			thisTerm.setIC(-Math.log(x/y));
		}
	}
	
	/**
	 * this method helps to calculate IC for a node (Log not applyed here yet)
	 * @param thisDag
	 * @param thisTerm
	 * @return
	 
	private double calculateRawIC(DirectedAcyclicGraph<Term, DefaultEdge> thisDag, Term thisTerm){
		double y = findRootOfDag(thisDag).getGeneList().size();
		double x = thisTerm.getGeneList().size();
		return x/y;
	}
	*/
	
	
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
			temporaryDag = dagSelector(thisTerm);			
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
	 * This method finds the set of common ancestors of two nodes.
	 *
		 * Computation of lowest common ancestors may be useful, for instance, as part of a procedure 
		 * for determining the distance between pairs of nodes in a tree: the distance from v to w can 
		 * be computed as the distance from the root to v, plus the distance from the root to w, minus 
		 * twice the distance from the root to their lowest common ancestor
		 * 
	 * @param termA
	 * @param termB
	 * @return LCA term
	 */
	private HashSet<Term> findLowestCommonAncestors(Term thisTerm1, Term thisTerm2){
		DirectedAcyclicGraph<Term, DefaultEdge> thisDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
		HashSet<Term> result = new HashSet<>();
		if (thisTerm1!=null && thisTerm2!=null){
			thisDag = dagSelector(thisTerm1);
			if (thisTerm1.equals(thisTerm2)){
				LOGGER.warning("Two terms are the same.");
				result.add(thisTerm1);
				return result;
			}
			if (!thisDag.equals(dagSelector(thisTerm2))){LOGGER.warning("The two terms are not in the same DAG"); return null;}
			}
		Set<Term> ancestorSetA = thisDag.getAncestors(thisTerm1);
		Set<Term> ancestorSetB = thisDag.getAncestors(thisTerm2);
		//if one term's ancestorSet contains the other term
		if (ancestorSetA.size() < ancestorSetB.size()){   // term1 is closer to root
			if(ancestorSetB.contains(thisTerm1)){
				result.add(thisTerm1);
				return result;
			}
		} else {
			if(ancestorSetA.contains(thisTerm2)){ //term2 is closer to root
				result.add(thisTerm2);	
				return result;
			} 
		}
		Set<Term> mutualTermSet = new HashSet<Term>(ancestorSetB);	    
		mutualTermSet.retainAll(ancestorSetA);  //intersection of two sets
		
		for (Term t : mutualTermSet){ 
			Iterator<DefaultEdge> edgeIterator = thisDag.outgoingEdgesOf(t).iterator();
			boolean allEdgesPointOut = false;
			while (edgeIterator.hasNext()){
				DefaultEdge thisEdge = edgeIterator.next();
				if (mutualTermSet.contains(thisDag.getEdgeTarget(thisEdge))){ 
					//if there is any outgoing edge that leads to a term that is part of the intersection
					allEdgesPointOut = true;
				}
			}
			if(!allEdgesPointOut){ 
				//only consider as a result if there was no outgoing 
				//edge leading to a term in the intersection of the two ancestorSet
				//that will be the LCA
				result.add(t);
				//LOGGER.info("result found for LCA: "+t.getID());
			}
		}
		//LOGGER.info("size of common ancestor set: "+result.size());
		return result;
	}
	
	/**
	 * This method selects the dag that the Term is belonging to.
	 * @param thisTerm
	 * @return
	 */
	private DirectedAcyclicGraph<Term, DefaultEdge> dagSelector (Term thisTerm){
		DirectedAcyclicGraph<Term, DefaultEdge> thisDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
		if (thisTerm.getNamespace().equals("biological_process")){
			thisDag = this.annotDagBP;
		} 
		else if (thisTerm.getNamespace().equals("cellular_component")){
			thisDag = this.annotDagCC;
		} 
		else if (thisTerm.getNamespace().equals("molecular_function")){
			thisDag = this.annotDagMF;
		}
		return thisDag;
	}
	
	private void printPath(GraphPath<Term,DefaultEdge> path){
		//LOGGER.info("Starting vertex: "+path.getStartVertex().getID());
		path.getVertexList().forEach(x -> LOGGER.info(x.getID()));
		//LOGGER.info("Ending vertex: "+path.getEndVertex().getID());
	}
	
	/**
	 * Calculates the distance between two nodes (the ancestor list of one node contains the other).
	 * This method could be used after LCA has been found.
	 * @param thisTerm1
	 * @param thisTerm2
	 * @return
	 */
	private int distanceOnSameWalk(Term thisTerm1, Term thisTerm2){
		int shortestDistance = 100000000;  //initial big distance for testing purposes
		DirectedAcyclicGraph<Term, DefaultEdge> thisDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
		thisDag = dagSelector(thisTerm1);
		if (!thisDag.equals(dagSelector(thisTerm2))){
			LOGGER.warning("Two terms are not in the same DAG");
		}
		Set<Term> ancestorsTerm1 = thisDag.getAncestors(thisTerm1);
		Set<Term> ancestorsTerm2 = thisDag.getAncestors(thisTerm2);
		Term tempTerm = null;
		Term targetTerm = null;
		//is this always true???
		if (ancestorsTerm1.size() < ancestorsTerm2.size()){
			tempTerm = thisTerm2;
			targetTerm = thisTerm1;
		} else {
			tempTerm = thisTerm1;
			targetTerm = thisTerm2;
		}
		GraphPath<Term, DefaultEdge> path = new BidirectionalDijkstraShortestPath<Term, DefaultEdge>(thisDag).getPath( targetTerm, tempTerm);
		//GraphPath<Term, DefaultEdge> reverse = new DijkstraShortestPath<Term, DefaultEdge>(thisDag).getPath( tempTerm, targetTerm);
		if(path!=null){
			LOGGER.info("Shortest path:");
			printPath(path);
		}
		shortestDistance = path.getLength();
		LOGGER.info("Shortest distance= "+shortestDistance);
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
		DirectedAcyclicGraph<Term, DefaultEdge> thisDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
		if (thisTerm1!=null && thisTerm2!=null){
			Term commonAncestor = getMICAfromCAset(findLowestCommonAncestors(thisTerm1,thisTerm2));
			thisDag = dagSelector(thisTerm1);
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
			LOGGER.warning("Term ID's not given correctly or not present/not in the same tree.");
		}
		return result;
	}
	
	/**
	 * returns the MICA from a given term Set
	 * @param commonancestorSet
	 * @return
	 */
	private Term getMICAfromCAset(HashSet<Term> commonancestorSet){
		Term MICA=(Term) commonancestorSet.toArray()[0];
		int size = commonancestorSet.size();
		for(int i=1;i<size;i++){
			Term nextTerm = (Term)commonancestorSet.toArray()[i];
			if(MICA.getIC()<nextTerm.getIC()){
				MICA = nextTerm;
			}
		}
		return MICA;
	}
	
	@SuppressWarnings("unused")
	private void findLeavesWithLongestPath(){
		//Term furthestTerm;
		int pathLength = 0;
		for (Term t : annotDagBP.vertexSet()){		
			if (annotDagBP.getDescendants(t).isEmpty()){
				GraphPath<Term, DefaultEdge> path = new BidirectionalDijkstraShortestPath<Term, DefaultEdge>(annotDagBP).getPath( findRootOfDag(annotDagBP), t );
				if (path != null){
					int distanceFromRoot = path.getLength();
					if (distanceFromRoot>=pathLength){
						//furthestTerm = t;
						pathLength = distanceFromRoot;
						LOGGER.info("new longest has been found: "+t.getID()+" with pathlength "+pathLength);
					}
				}
					
			}
		}
	}
	
	/**
	 * Calculates PD, the number of paths from a term to it's ancestor.
	 * @param term1
	 * @param term2
	 * @return
	 */
	private int calculateNofRoutesToCA(Term term1,Term ancestor){
		DirectedAcyclicGraph<Term, DefaultEdge> thisDag = dagSelector(term1);
		List<GraphPath<Term,DefaultEdge>> realPaths;
		if (!thisDag.equals(dagSelector(ancestor))){LOGGER.warning("Two terms are not in the same DAG!");}
		List<GraphPath<Term,DefaultEdge>> distinctPaths = new AllDirectedPaths<Term, DefaultEdge>(thisDag).getAllPaths(term1,ancestor,true,500);
		List<GraphPath<Term,DefaultEdge>> distinctPathsReverse = new AllDirectedPaths<Term,DefaultEdge>(thisDag).getAllPaths(ancestor,term1,true,500);
		if (distinctPaths.size()>distinctPathsReverse.size()){
			realPaths = distinctPaths;
		} else {realPaths = distinctPathsReverse;}		
		return realPaths.size();
	}
	
	/**
	 * returns the Disjunct Common Ancestor set
	 * @param term1
	 * @param term2
	 * @return
	 */
	private Set<Term> getDishinShare(Term term1,Term term2){
		DirectedAcyclicGraph<Term, DefaultEdge> thisDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
		HashSet<Term> result = new HashSet<>();
		thisDag = dagSelector(term1);
		if (term1.equals(term2)){
			LOGGER.warning("Two terms are the same.");
			result.add(term1);
			return result;
		}
		if (!thisDag.equals(dagSelector(term2))){LOGGER.warning("The two terms are not in the same DAG"); return null;}
		Set<Term> ancestorSetA = thisDag.getAncestors(term1);
		Set<Term> ancestorSetB = thisDag.getAncestors(term2);
		//if one term's ancestorSet contains the other term
		if (ancestorSetA.size() < ancestorSetB.size()){   // term1 is closer to root and term2 ancestor set include term1
			if(ancestorSetB.contains(term1)){
				result.add(term1);
				return result;
			}
		} else {
			if(ancestorSetA.contains(term2)){ //term2 is closer to root and term1 ancestor set include term2
				result.add(term2);	
				return result;
			} 
		}
		Set<Term> mutualTermSet = new HashSet<Term>(ancestorSetB);	    
		mutualTermSet.retainAll(ancestorSetA);  //intersection of two sets
		for (Term t : mutualTermSet){
			if (calculateNofRoutesToCA(term1,t)-calculateNofRoutesToCA(term2,t)!=0){  //this is the PD by definition
				result.add(t);//if the difference is not 0, it means that the common ancestor has to be added to the result set
			}
		}
		//result.forEach(term -> LOGGER.info("common ancestor with DISHIN: "+term));
		result.add(getMICAfromCAset(findLowestCommonAncestors(term1,term2))); //the most informative will be added anyway, except if it was already added
		//we add the MICA to the set anyway (we can not add twice, so it is safe, even if it already contains)
		//if the set was empty as there were only 0 PD values, we need the MICA
		return result;
	}
	
	public double calculateDiShIn(Term term1,Term term2){
		ArrayList<Double> listOfICs = new ArrayList<>();
		getDishinShare(term1,term2).forEach(t -> listOfICs.add(t.getIC()));
		return listOfICs.stream().mapToDouble(val -> val).average().getAsDouble();
	}
	
	/**
	 * Calculates Resnik Similarity.
	 * @param term1
	 * @param term2
	 * @param isDiShIn
	 * @return
	 */
	public double ResnikSim(String term1, String term2, boolean isDiShIn){
		double result = 0;
		Term thisTerm1 = dags.getTerms().get(term1);
		Term thisTerm2 = dags.getTerms().get(term2); 
		if (thisTerm1!=null && thisTerm2!=null){
			Term mica = getMICAfromCAset(findLowestCommonAncestors(thisTerm1,thisTerm2));
			if (term1.equals(term2)){
				LOGGER.warning("Same terms.");
			} 
			if (isDiShIn){
				result = calculateDiShIn(thisTerm1,thisTerm2);
			} else {
				result = mica.getIC();	
			}			
		} else { 
			//LOGGER.warning("Term ID's not given correctly or not present/not in the same tree.(Resnik)");
		}
		return result;
	}
	
	/**
	 * Calculates DekangLin Similarity.
	 * @param term1
	 * @param term2
	 * @return
	 */
	public double DekangLinSim(String term1, String term2, boolean isDiShIn){
		double result =0;
		Term thisTerm1 = dags.getTerms().get(term1);
		Term thisTerm2 = dags.getTerms().get(term2); 
		if (thisTerm1!=null && thisTerm2!=null){
			if (term1.equals(term2)){
				System.out.println("Same terms.");
				result = 1;
			} else {
				double description = thisTerm1.getIC() + thisTerm2.getIC();
				double common =0;
				if (isDiShIn){
					common = 2*calculateDiShIn(thisTerm1,thisTerm2);
				} else {
					common = 2*getMICAfromCAset(findLowestCommonAncestors(thisTerm1,thisTerm2)).getIC();
				}
				result = common/description;
			}			
		} else { 
			//LOGGER.warning("Term ID's not given correctly or not present/not in the same tree.(Dekang)");
		}
		return result;
	}
	
	/**
	 * JiangConrath Similarity measure without using weights
	 * @param term1
	 * @param term2
	 * @param isDiShIn
	 * @return
	 */
	public double JiangConrathSim(String term1, String term2, boolean isDiShIn){
		double result =0;
		Term thisTerm1 = dags.getTerms().get(term1);
		Term thisTerm2 = dags.getTerms().get(term2); 
		DirectedAcyclicGraph<Term, DefaultEdge> thisDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
		if (thisTerm1!=null && thisTerm2!=null){
			Term commonAncestor = getMICAfromCAset(findLowestCommonAncestors(thisTerm1,thisTerm2));
			thisDag = dagSelector(thisTerm1);
			if (term1.equals(term2)){
				System.out.println("Same terms.");
				result = 1;
			} else {
				double nofGenes = findRootOfDag(thisDag).getGeneList().size();
				double value = commonAncestor.getIC();
				if (isDiShIn){
					value = calculateDiShIn(thisTerm1,thisTerm2);
				}
				double dJiang = 2*value - thisTerm1.getIC() - thisTerm2.getIC();
				double max = -2*Math.log(nofGenes/nofGenes)-2*(-Math.log(1/nofGenes));
				//maximum possible value of dJiang if the two terms are only annotated with 1 gene, but their common ancestor is the root
				result = 1-dJiang/max;
			}			
		} else { 
			//LOGGER.warning("Term ID's not given correctly or not present/not in the same tree.(Jiang)");
		}
		return result;
	}
	/**
	 * simUI calculation for genes.
	 * @param gene1
	 * @param gene2
	 * @return
	 */
	public double simUI(String gene1, String gene2){
		double result = 0;
		Set<Term> allTermsforGene1 = new HashSet<Term>();
		Set<Term> allTermsforGene2 = new HashSet<Term>();
		geneMap.get(gene1).forEach(set -> allTermsforGene1.addAll(set));
		geneMap.get(gene2).forEach(set -> allTermsforGene2.addAll(set));	
		Set<Term> union = new HashSet<Term>(allTermsforGene1);
		union.addAll(allTermsforGene2);
		Set<Term> intersection = new HashSet<Term>(allTermsforGene1);
		intersection.retainAll(allTermsforGene2);
		double unionValue = (double)union.size();
		double intersectionValue = (double)intersection.size();
		result = intersectionValue/unionValue;
		return result;
	}
	
	/**
	 * Calculating simGIC similarity for genes.
	 * @param gene1
	 * @param gene2
	 * @return
	 */
	public double simGIC(String gene1,String gene2){
		double result = 0;
		Set<Term> allTermsforGene1 = new HashSet<Term>();
		Set<Term> allTermsforGene2 = new HashSet<Term>();
		geneMap.get(gene1).forEach(set -> allTermsforGene1.addAll(set));
		geneMap.get(gene2).forEach(set -> allTermsforGene2.addAll(set));
		Set<Term> union = new HashSet<Term>(allTermsforGene1);
		union.addAll(allTermsforGene2);
		Set<Term> intersection = new HashSet<Term>(allTermsforGene1);
		intersection.retainAll(allTermsforGene2);
		double unionValue=0;
		double intersectionValue=0;
		for (Term t : union){
			unionValue += t.getIC();
		}
		for (Term t : intersection){
			intersectionValue += t.getIC();
		}
		result = intersectionValue/unionValue;
		return result;
	}
	
	/**
	 * gene wise calculation method for Resnik
	 * @param gene1
	 * @param gene2
	 * @param bestmatch
	 * @param isDiShIn
	 * @return
	 */
	public double geneResnikSim(String gene1, String gene2, boolean bestmatch, boolean isDiShIn){
		double finalResult = 0;
		Set<String> termsForGene1 = geneAnnotations.get(gene1);
		Set<String> termsForGene2 = geneAnnotations.get(gene2);
		ArrayList<Double> resultList = new ArrayList<>();
		ArrayList<Double> bestMatchList = new ArrayList<>();
		double best =0;
		for (String term1  : termsForGene1){
			for (String term2 : termsForGene2){
				if (dags.dagDecider(term1).equals(dags.dagDecider(term2))){
					//normal average
					resultList.add(ResnikSim(term1,term2,isDiShIn));
					//LOGGER.info("normal Resnik: "+term1 +" and "+term2);
					//best-match average
					if (best<ResnikSim(term1,term2,isDiShIn)){
						best = ResnikSim(term1,term2,isDiShIn);
					}
				}
			}
			//end of comparing term 1 to every term2
			bestMatchList.add(best);
			//LOGGER.info("bestMatch Resnik term1: "+term1+" +bestmatch value "+best);
			best=0;
		}
		for (String term2  : termsForGene2){
			for (String term1 : termsForGene1){
				if (dags.dagDecider(term1).equals(dags.dagDecider(term2))){
					//best-match average reverse
					if (best<ResnikSim(term1,term2,isDiShIn)){
						best = ResnikSim(term1,term2,isDiShIn);
					}
				}
			}
			//end of comparing term2 to every term1
			bestMatchList.add(best);
			//LOGGER.info("bestMatch Resnik term2: "+term2+" +bestmatch value "+best);
			best=0;
		}
		if (bestmatch){
			finalResult = bestMatchList.stream().mapToDouble(val -> val).average().getAsDouble();	
		} else {
			finalResult = resultList.stream().mapToDouble(val -> val).average().getAsDouble();	
		}
		return finalResult;
	}
	
	/**
	 * gene wise calculation method for Lin
	 * @param gene1
	 * @param gene2
	 * @param bestmatch
	 * @param isDiShIn
	 * @return
	 */
	public double geneDekangLinSim(String gene1, String gene2, boolean bestmatch, boolean isDiShIn){
		double finalResult = 0;
		Set<String> termsForGene1 = geneAnnotations.get(gene1);
		Set<String> termsForGene2 = geneAnnotations.get(gene2);
		ArrayList<Double> resultList = new ArrayList<>();
		ArrayList<Double> bestMatchList = new ArrayList<>();
		double best =0;
		for (String term1  : termsForGene1){
			for (String term2 : termsForGene2){
				if (dags.dagDecider(term1).equals(dags.dagDecider(term2))){
					//normal average
					resultList.add(DekangLinSim(term1,term2,isDiShIn));
					//best-match average
					if (best<DekangLinSim(term1,term2,isDiShIn)){
						best = DekangLinSim(term1,term2,isDiShIn);
					}
				}
			}
			//end of comparing term 1 to every term2
			bestMatchList.add(best);
			best=0;
		}
		for (String term2  : termsForGene2){
			for (String term1 : termsForGene1){
				if (dags.dagDecider(term1).equals(dags.dagDecider(term2))){
					//best-match average reverse
					if (best<DekangLinSim(term1,term2,isDiShIn)){
						best = DekangLinSim(term1,term2,isDiShIn);
					}
				}
			}
			//end of comparing term2 to every term1
			bestMatchList.add(best);
			best=0;
		}
		if (bestmatch){
			finalResult = bestMatchList.stream().mapToDouble(val -> val).average().getAsDouble();	
		} else {
			finalResult = resultList.stream().mapToDouble(val -> val).average().getAsDouble();	
		}
		return finalResult;
	}
	
	/**
	 * gene wise calculation method for Resnik
	 * @param gene1
	 * @param gene2
	 * @param bestmatch
	 * @param isDiShIn
	 * @return
	 */
	public double geneJiangConrathSim(String gene1, String gene2, boolean bestmatch, boolean isDiShIn){
		double finalResult = 0;
		Set<String> termsForGene1 = geneAnnotations.get(gene1);
		Set<String> termsForGene2 = geneAnnotations.get(gene2);
		ArrayList<Double> resultList = new ArrayList<>();
		ArrayList<Double> bestMatchList = new ArrayList<>();
		double best =0;
		for (String term1  : termsForGene1){
			for (String term2 : termsForGene2){
				if (dags.dagDecider(term1).equals(dags.dagDecider(term2))){
					//normal average
					resultList.add(JiangConrathSim(term1,term2,isDiShIn));
					//best-match average
					if (best<JiangConrathSim(term1,term2,isDiShIn)){
						best = JiangConrathSim(term1,term2,isDiShIn);
					}
				}
			}
			//end of comparing term 1 to every term2
			bestMatchList.add(best);
			best=0;
		}
		for (String term2  : termsForGene2){
			for (String term1 : termsForGene1){
				if (dags.dagDecider(term1).equals(dags.dagDecider(term2))){
					//best-match average reverse
					if (best<JiangConrathSim(term1,term2,isDiShIn)){
						best = JiangConrathSim(term1,term2,isDiShIn);
					}
				}
			}
			//end of comparing term2 to every term1
			bestMatchList.add(best);
			best=0;
		}
		if (bestmatch){
			finalResult = bestMatchList.stream().mapToDouble(val -> val).average().getAsDouble();	
		} else {
			finalResult = resultList.stream().mapToDouble(val -> val).average().getAsDouble();	
		}
		return finalResult;
	}
	
}
