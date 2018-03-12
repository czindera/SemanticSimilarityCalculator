/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package OBOReader;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Attila
 */
public class DagBuilderTest {
    
    public DagBuilderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getDagBP method, of class DagBuilder.
     */
    @Test
    public void testGetDagBP() {
        System.out.println("getDagBP");
        DagBuilder instance = new DagBuilder();
        DirectedAcyclicGraph<Term, DefaultEdge> expResult = null;
        DirectedAcyclicGraph<Term, DefaultEdge> result = instance.getDagBP();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDagCC method, of class DagBuilder.
     */
    @Test
    public void testGetDagCC() {
        System.out.println("getDagCC");
        DagBuilder instance = new DagBuilder();
        DirectedAcyclicGraph<Term, DefaultEdge> expResult = null;
        DirectedAcyclicGraph<Term, DefaultEdge> result = instance.getDagCC();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDagMF method, of class DagBuilder.
     */
    @Test
    public void testGetDagMF() {
        System.out.println("getDagMF");
        DagBuilder instance = new DagBuilder();
        DirectedAcyclicGraph<Term, DefaultEdge> expResult = null;
        DirectedAcyclicGraph<Term, DefaultEdge> result = instance.getDagMF();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTerms method, of class DagBuilder.
     */
    @Test
    public void testGetTerms() {
        System.out.println("getTerms");
        DagBuilder instance = new DagBuilder();
        Terms expResult = null;
        Terms result = instance.getTerms();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of dagDecider method, of class DagBuilder.
     */
    @Test
    public void testDagDecider() {
        System.out.println("dagDecider");
        String termID = "";
        DagBuilder instance = new DagBuilder();
        DirectedAcyclicGraph<Term, DefaultEdge> expResult = null;
        DirectedAcyclicGraph<Term, DefaultEdge> result = instance.dagDecider(termID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of printNodeInfo method, of class DagBuilder.
     */
    @Test
    public void testPrintNodeInfo() {
        System.out.println("printNodeInfo");
        String nodeID = "";
        DagBuilder instance = new DagBuilder();
        instance.printNodeInfo(nodeID);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
