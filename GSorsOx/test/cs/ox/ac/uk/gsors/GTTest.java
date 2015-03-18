package cs.ox.ac.uk.gsors;

import static org.deri.iris.factory.Factory.BASIC;
import static org.deri.iris.factory.Factory.TERM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deri.iris.api.basics.ITuple;

import cs.ox.ac.uk.gsors.GPreferencesGraph;
import cs.ox.ac.uk.sors.PreferencesGraph;

public class GTTest extends TestCase {
	private PreferencesGraph pg1;
	private PreferencesGraph pg2;
	private PreferencesGraph pg3;
	
	private PreferencesGraph pg4;
	
	GPreferencesGraph g;
	private Map<String, Double> probModel;
	private static final ITuple pasta = BASIC.createTuple(TERM
			.createString("pasta"));
	private static final ITuple salad = BASIC.createTuple(TERM
			.createString("salad"));
	private static final ITuple sushi = BASIC.createTuple(TERM
			.createString("sushi"));
	private static final ITuple fish = BASIC.createTuple(TERM
			.createString("fish"));
	private static final ITuple pizza = BASIC.createTuple(TERM
			.createString("pizza"));
	private static final ITuple p1 = BASIC.createTuple(TERM.createString("p1"));
	private static final ITuple p2 = BASIC.createTuple(TERM.createString("p2"));
	private List<Double> tr;

	public static Test suite() {
		return new TestSuite(GTTest.class, GTTest.class.getSimpleName());
	}

	
	
	public void testcombPrefsGen() throws NumberFormatException {
		System.out.print(pg4.hasCycles());
		System.out.print(pg4.edgesString());
		pg4.removeCycles("A");
		System.out.print(pg4.edgesString());
	}
//	public void testcombPrefsGen() throws NumberFormatException {
//		System.out.println("Initial"+g.edgesSTring());
//		g.transitiveClosure();
//		System.out.println("Trans "+g.edgesSTring());
//
//		
//		GPreferencesGraph gg = GCombinationAlgorithms.combPrefsGen(g,
//				probModel, tr);
//		System.out.println("Old "+g.edgesSTring());
//		System.out.println("New "+gg.edgesSTring());
//		gg.transitiveClosure();	
//		System.out.println("Tra "+gg.edgesSTring());
//		PreferencesGraph p = gg.collapseEdges();
//		System.out.println("collapsed " + p.edgesString());
//		for (int i=1; i<probModel.size();i++)
//		{
//			//System.out.println(" "+gg.edgesSTring());
//		System.out.println("top "+i+" " +AggregationAlgorithm.pluralityVotingMisery(gg, i, false,""));
//		}
//	}

	public void setUp() {
		probModel = new HashMap<String, Double>();
		probModel.put("pasta", 0.75);
		probModel.put("pizza", 0.6);
		probModel.put("sushi", 0.4);
		probModel.put("salad", 0.35);
		probModel.put("fish", 0.2);
		probModel.put("p1", 0.8);
		probModel.put("p2", 0.1);

		pg1 = new PreferencesGraph();
		pg1.addPreference(pasta, sushi);
		pg1.addPreference(pizza, sushi);
		pg1.addPreference(pizza, fish);
		pg1.addPreference(pasta, fish);
		pg1.addPreference(p1, p2);
		pg1.addVertex(salad);

		pg2 = new PreferencesGraph();
		pg2.addPreference(sushi, pasta);
		pg2.addPreference(fish, pasta);
		pg2.addPreference(p1, p2);
		pg1.addVertex(salad);
		pg1.addVertex(pizza);
		
		pg3 = new PreferencesGraph();
		pg3.addPreference(pizza, pasta);
		pg3.addPreference(salad, pasta);
		//pg3.addPreference(pasta, sushi);
		pg3.addPreference(p1, p2);
		pg1.addVertex(sushi);
		pg1.addVertex(fish);
		
		pg4 = new PreferencesGraph();
		pg4.addPreference(pasta, sushi);
		pg4.addPreference(sushi, fish);
		pg4.addPreference(fish, pizza);
		pg4.addPreference(pizza, pasta);

		
		
		
		List<PreferencesGraph> aa = new ArrayList<PreferencesGraph>();
		aa.add(pg1);
		aa.add(pg2);
		aa.add(pg3);

		g = new GPreferencesGraph(aa);
		tr = new ArrayList<Double>();
		tr.add(0.0);
		tr.add(0.1);
		tr.add(0.3);
	}
}
