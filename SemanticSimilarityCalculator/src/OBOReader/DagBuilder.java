package OBOReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;

public class DagBuilder {
	private BufferedReader in,in2;
	private String buffer;
	private DirectedAcyclicGraph<Term,ConnectionType> dag;
	private Terms terms;

	DagBuilder() {
		this.buffer = null;
		this.terms = new Terms();
		this.dag = new DirectedAcyclicGraph<Term,ConnectionType>(ConnectionType.class);
			try {
				parse();
			} catch (IOException | CycleFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/**
	 * 
	 * @return the next line
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
	 * parse one Term
	 * @throws IOException
	 */
	private void parseTerm() throws IOException {
		String line;
		Term newTerm = new Term();
		while((line=next())!=null) {
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
				continue;
				}
			System.out.println("NEW TERM ADDED TO COLLECTION!\n"+terms.addTerm(newTerm));
			System.out.println("New Node added to DAG: "+dag.addVertex(newTerm));
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
		while((line=next())!=null) {
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
				Term fromNode = terms.get(fromVertex);
				Term toNode = terms.get(toVertex);
				System.out.println("New EDGE added: "+dag.addDagEdge(fromNode, toNode, ConnectionType.IS_A));
				continue;
				}
		}
	}
	
	/**
	 * parse the whole file
	 * @throws IOException
	 * @throws CycleFoundException 
	 */
	private void parse() throws IOException, CycleFoundException {
		InputStream input = getClass().getResourceAsStream("go_basic.obo");
		in=new BufferedReader(new InputStreamReader(input));
		String line;
		while((line=next())!=null) {
			if(line.equals("[Term]")) parseTerm();
		}
		in.close();
		buffer=null;
		in2=new BufferedReader(new InputStreamReader(input));
		while((line=next())!=null) {
			if(line.equals("[Term]")) createEdges();
		}
		in2.close();
	}
}