package AnnotationReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

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
	//private DirectedAcyclicGraph<Term, DefaultEdge> dagMF,dagBP,dagCC;
	DagBuilder dags;
	
	public Reader(){
		in=null;
		buffer = null;
		this.annotDagBP = new DirectedAcyclicGraph<>(DefaultEdge.class);
		this.annotDagMF = new DirectedAcyclicGraph<>(DefaultEdge.class);
		this.annotDagCC = new DirectedAcyclicGraph<>(DefaultEdge.class);
		this.dags = new DagBuilder();
		try {
			parse();
		} catch (IOException | CycleFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
						temporaryDag.addVertex(thisTerm);
						Iterator<DefaultEdge> edgeIterator = thisDag.incomingEdgesOf(thisTerm).iterator();
						while (edgeIterator.hasNext()){
							DefaultEdge currentEdge = edgeIterator.next();
							Term ancestor = thisDag.getEdgeSource(currentEdge);//this has to be added to new dag
							temporaryDag.addVertex(ancestor);
							temporaryDag.addDagEdge(ancestor, thisTerm);
							System.out.println("Edge added with ancesor: "+ancestor.getID()+" and child "+thisTerm.getID());
						}
						
						//get Vertex and put it into new Dag
						//get ancestor and put it into new Dag....can it be multiple?
						//connect with Edges
					}
				}
			}
		}
		//System.out.println(temporaryDag.toString());
	}
}
