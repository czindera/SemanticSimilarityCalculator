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
	 * parse one Term
	 * @throws IOException
	 */
	private void parseTerm() throws IOException
	{
	Term t=null;
	String line;
	while((line=next())!=null)
		{
		if(line.startsWith("["))
			{
			this.buffer=line;
			break;
			}
		int colon=line.indexOf(':');
		if(colon==-1) continue;
		if(line.startsWith("id:") && t==null)
			{
			t=getTermById(line.substring(colon+1).trim(),true);
			continue;
			}
		if(t==null) continue;
		if(line.startsWith("name:"))
			{
			t.name=nocomment(line.substring(colon+1));
			continue;
			}
		else if(line.startsWith("def:"))
			{
			t.def=nocomment(line.substring(colon+1));
			continue;
			}
		else if(line.startsWith("is_a:"))
			{
			String rel=nocomment(line.substring(colon+1));
			t.is_a.add(rel);
			Term parent=getTermById(rel,true);
			parent.children.add(t.id);
			continue;
			}
		}
	}
	
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