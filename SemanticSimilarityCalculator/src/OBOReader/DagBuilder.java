package OBOReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;

public class DagBuilder {
	private BufferedReader in,in2;
	private String buffer;
	//private DirectedAcyclicGraph<Term,ConnectionType> dag;
	private DirectedAcyclicGraph<Term, DefaultEdge> dagBP,dagMF,dagCC;
	private Terms terms;
	//private DefaultEdge is_a;

	DagBuilder() {
		this.buffer = null;
		this.terms = new Terms();
		this.dagBP = new DirectedAcyclicGraph<>(DefaultEdge.class);
		this.dagMF = new DirectedAcyclicGraph<>(DefaultEdge.class);
		this.dagCC = new DirectedAcyclicGraph<>(DefaultEdge.class);
			try {
				parse();
			} catch (IOException | CycleFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/**
	 * Reads the next line while making sure that buffer line will be emptied
	 * @return the next line
	 * @throws IOException
	 */
	private String next(int i) throws IOException {
		if(buffer!=null) {
			String s=buffer;
			buffer=null;
			return s;
		}
		if (i==0) {
			return in.readLine();
		} else return in2.readLine();
	}
	
	
	/**
	 * This method is to trim the comments from a line
	 * @param s
	 * @return
	 */
	private String nocomment(String s) {
		int excLocation=s.indexOf('!');
		if(excLocation!=-1) s=s.substring(0,excLocation);
		return s.trim();
	}
	
	/**
	 * parse one Term and add it to the collection of Terms and as a DAG Vertex
	 * @throws IOException
	 */
	private void parseTerm() throws IOException {
		String line;
		Term newTerm = new Term();
		while((line=next(0))!=null) {
			if(line.startsWith("["))
				{
				this.buffer=line;
				break;
				}
			int colon=line.indexOf(':');
			if(colon==-1) continue;
			if(line.startsWith("id:"))
				{
				newTerm.id = line.substring(colon+1).trim();
				continue;
				}
			if(line.startsWith("name:"))
				{
				newTerm.name=nocomment(line.substring(colon+1));
				continue;
				}
			if(line.startsWith("namespace:"))
				{
				newTerm.namespace=nocomment(line.substring(colon+1));
				continue;
				}
			else if(line.startsWith("def:"))
				{
				newTerm.def=nocomment(line.substring(colon+1));
				terms.addTerm(newTerm);
				if (newTerm.namespace.equals("molecular_function")){
					dagMF.addVertex(newTerm);
				}
				else if (newTerm.namespace.equals("biological_process")){
					dagBP.addVertex(newTerm);	
				}
				else if (newTerm.namespace.equals("cellular_component")){
					dagCC.addVertex(newTerm);
				}
				else {
					System.out.println("TERM WAS NOT ADDED, NO NAMESPACE!");
				}
				continue;
				}
		}
	}
	
	/**
	 * This method reads and adds the is_a connections as edges to the DAG
	 * @throws IOException
	 * @throws CycleFoundException 
	 */
	private void createEdges() throws IOException, CycleFoundException {
		String line;
		String fromVertex=null;
		String toVertex=null;
		while((line=next(1))!=null) {
			if(line.startsWith("["))
				{
				this.buffer=line;
				break;
				}
			int colon=line.indexOf(':');
			if(colon==-1) continue;
			if(line.startsWith("id:"))
				{
				fromVertex= line.substring(colon+1).trim();
				continue;
				}
			if(line.startsWith("is_a:"))
				{
				toVertex = nocomment(line.substring(colon+1));
				//System.out.println(fromVertex+" to be connected to: "+toVertex);
				Term fromNode = terms.get(fromVertex);
				Term toNode = terms.get(toVertex);
				if (fromNode.namespace.equals("molecular_function") && toNode.namespace.equals("molecular_function")){
					dagMF.addEdge(fromNode, toNode);
				}
				else if (fromNode.namespace.equals("biological_process") && toNode.namespace.equals("biological_process")){
					dagBP.addEdge(fromNode, toNode);
				} 
				else if (fromNode.namespace.equals("cellular_component") && toNode.namespace.equals("cellular_component")){
					dagCC.addEdge(fromNode, toNode);
				}
				else {
					System.out.println("FAILED TO ADD TO DAG, not belonging to the same NAMESPACE");
				}
				
				continue;
				}
		}
	}
	
	/**
	 * parse the whole file 2 times, first creating all Vertices, then adding the Edges
	 * currently only working for is_a connections
	 * @throws IOException
	 * @throws CycleFoundException 
	 */
	private void parse() throws IOException, CycleFoundException {
		ClassLoader cl = getClass().getClassLoader();
		File file = new File(cl.getResource("./OBOfiles/go-basic.obo").getFile());
		File file2 = new File(cl.getResource("./OBOfiles/go-basic.obo").getFile());
	    FileReader fr = new FileReader(file);
	    FileReader fr2 = new FileReader(file2);
		in=new BufferedReader(fr);
		in2=new BufferedReader(fr2);
		String line;
		while((line=next(0))!=null) {
			if(line.equals("[Term]")) parseTerm();
		}
		in.close();
		//terms.printTerms();
		buffer=null;
		while((line=next(1))!=null) {
			if(line.equals("[Term]")) createEdges();
		}
		in2.close();
		System.out.println("Finished Building DAGs!");
		
		//System.out.println("Printing MF DAG");
		//printEdges(dagMF);
		//System.out.println("Printing CC DAG");
		//printEdges(dagCC);
		//System.out.println("Printing BP DAG");
		//printEdges(dagBP);
	}
	
	@SuppressWarnings("unused")
	private void printEdges(DirectedAcyclicGraph<Term, DefaultEdge> thisDAG) {
		for(DefaultEdge e : thisDAG.edgeSet()){
		    System.out.println(thisDAG.getEdgeSource(e).id + " --> " + thisDAG.getEdgeTarget(e).id);
		}
	}
}