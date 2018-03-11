import static org.junit.Assert.*;

import java.util.HashSet;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.Before;
import org.junit.Test;

import AnnotationReader.Reader;
import OBOReader.DagBuilder;
import OBOReader.Term;

/**
 * 
 */

/**
 * @author Atosz
 *
 */
public class testDag {
	private DirectedAcyclicGraph<Term, DefaultEdge> dag;
	private Term term1;
	private DagBuilder dagBuilder;
	private Reader reader;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		dag = new DirectedAcyclicGraph<>(DefaultEdge.class);
		term1 = new Term("test term1");
		dagBuilder = new DagBuilder();
		reader = new Reader();
	}

	@Test
	public void test() {
		dag.addVertex(term1);
		HashSet<Term> vertexSet = new HashSet<Term>();
		vertexSet.add(term1);
		assertEquals("TEST the vertex Set",vertexSet,dag.vertexSet());
	}
	
	@Test
	public void test2() {
		term1 = dagBuilder.getTerms().get("GO:0050779");
		assertEquals("TEST the dagDecider",dagBuilder.getDagBP(),dagBuilder.dagDecider(term1.getID()));
	}
	
	@Test
	public void test3() {	
		assertEquals("TEST the Wu and Palmer method",1.0,reader.WuPalmerSim("GO:0050779", "GO:0050779"),0.001);
	}

}
