package AnnotationReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;

import OBOReader.DagBuilder;
import OBOReader.Term;
import OBOReader.Terms;

public class Reader {
	private BufferedReader in;
	private String buffer;
	private DirectedAcyclicGraph<Term, DefaultEdge> annotDagBP,annotDagMF,annotDagCC,tempDag;
	private Stack<Term> nextStack;
	private Term nextTerm;
	private DagBuilder dags;
	
	public Reader(){
		in=null;
		buffer = null;
		this.annotDagBP = new DirectedAcyclicGraph<>(DefaultEdge.class);
		this.annotDagMF = new DirectedAcyclicGraph<>(DefaultEdge.class);
		this.annotDagCC = new DirectedAcyclicGraph<>(DefaultEdge.class);
		this.tempDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
		this.nextStack = new Stack<Term>();
		this.dags = new DagBuilder();
		try {
			parse();
		} catch (IOException | CycleFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Uppropagation finished!");
		//printEdges(annotDagCC);
		//listAncestors("GO:0019688");
	}
	
	private String next() throws IOException {
		if(buffer!=null) {
			String s=buffer;
			buffer=null;
			return s;
		}
		return in.readLine();
	}
	
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
				DirectedAcyclicGraph<Term, DefaultEdge> temporaryDag = new DirectedAcyclicGraph<>(DefaultEdge.class);
				if (goID.startsWith("GO")){
					DirectedAcyclicGraph<Term, DefaultEdge> thisDag = this.dags.dagDecider(goID); 
					if (thisDag!=null){
						Term thisTerm = allTerms.get(goID);
						
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
						tempDag = temporaryDag;
						
						//add this term to termStack
						nextStack.push(thisTerm);
						while(ancestorFinder(tempDag,thisTerm,nextStack)){
							
						}
						temporaryDag = tempDag;
						
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
			}
		}
		//System.out.println(temporaryDag.toString());
	}
	
	private boolean ancestorFinder(DirectedAcyclicGraph<Term, DefaultEdge> thisDag, Term thisTerm, Stack<Term> termStack) throws CycleFoundException{
		DirectedAcyclicGraph<Term, DefaultEdge> currentDag = this.dags.dagDecider(thisTerm.getID());
		Set<DefaultEdge> ancestorEdgeSet = currentDag.incomingEdgesOf(thisTerm);//get the ancestor edges
		Iterator<DefaultEdge> edgeIterator = ancestorEdgeSet.iterator();
		nextStack = termStack;
		//Term nextTerm = null;
		while (edgeIterator.hasNext()){
			DefaultEdge currentEdge = edgeIterator.next();
			Term ancestor = currentDag.getEdgeSource(currentEdge);//this ancestor has to be added to new dag
			//check if it is already in the Dag
			if (!tempDag.containsVertex(ancestor)){
				tempDag.addVertex(ancestor);
				nextStack.push(ancestor);	
			} else {
				nextStack.remove(ancestor);
				//System.out.println("Ancestor already in the Dag, but Edge was still added with ancestor: "+ancestor.getID()+" and child "+thisTerm.getID());
			}
			tempDag.addDagEdge(ancestor, thisTerm); //edges also added to the dag
			nextStack.remove(thisTerm);		//remove the term from stack as all the edges being discovered
			
		}
		//check if nextStack is not empty
		if (!nextStack.isEmpty()){
			nextTerm = nextStack.pop();
			if (currentDag.incomingEdgesOf(nextTerm).isEmpty() && !nextStack.isEmpty()){//if there is no ancestor for nextTerm, peek
				nextTerm = nextStack.peek();
			}
			return true;
		} else return false;
		
	}
	
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
			
			System.out.print("The ancestor/s of the term: [ ");
			getAncestors(temporaryDag,thisTerm).stream().forEach(theTerm -> System.out.print(theTerm.getID()+" "));
			System.out.print("]");
		} else { 
			System.out.println("No such term exists!");
			return;
		}
	}
	
	
	/**
	 * Prints out the edges of a DAG in (from Vertex --> to Vertex) format
	 * @param thisDAG
	 */
	public void printEdges(DirectedAcyclicGraph<Term, DefaultEdge> thisDAG) {
		for(DefaultEdge e : thisDAG.edgeSet()){
		    System.out.println(thisDAG.getEdgeSource(e).getID() + " --> " + thisDAG.getEdgeTarget(e).getID());
		}
	}
}
