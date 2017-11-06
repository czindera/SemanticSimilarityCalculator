package OBOReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

public class DagBuilder {
	private BufferedReader in;
	private String buffer;
	private DirectedAcyclicGraph<Term,ConnectionType> dag;

	DagBuilder() {
		this.buffer = null;
		this.dag = new DirectedAcyclicGraph<Term,ConnectionType>(ConnectionType.class);
		try {
			parse();
		} catch (IOException e) {
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
			//if(t==null) continue;
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
			dag.addVertex(newTerm);
		}
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	/*private void createEdges() throws IOException {
		if(line.startsWith("is_a:"))
		{
		String rel=nocomment(line.substring(colon+1));
		t.is_a.add(rel);
		Term parent=getTermById(rel,true);
		parent.children.add(t.id);
		continue;
		}
	}*/
	
	/**
	 * parse the whole file
	 * @throws IOException
	 */
	private void parse() throws IOException {
		InputStream input = getClass().getResourceAsStream("go_basic.obo");
		in=new BufferedReader(new InputStreamReader(input));
		String line;
		while((line=next())!=null) {
			if(line.equals("[Term]")) parseTerm();
		}
		in.close();
	}
}