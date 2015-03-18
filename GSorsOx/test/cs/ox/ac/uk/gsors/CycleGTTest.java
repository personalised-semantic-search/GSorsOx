
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

import cs.ox.ac.uk.gsors2.Cycle;

public class CycleGTTest extends TestCase {
	private GPreferencesGraph pg1;
	private Map<String, Double> probModel;
	private static final ITuple pasta = BASIC.createTuple(TERM.createString("pasta"));
	private static final ITuple salad = BASIC.createTuple(TERM.createString("salad"));
	private static final ITuple sushi = BASIC.createTuple(TERM.createString("sushi"));
	private static final ITuple fish = BASIC.createTuple(TERM.createString("fish"));
	private static final ITuple pizza = BASIC.createTuple(TERM.createString("pizza"));
	private static final ITuple p1 = BASIC.createTuple(TERM.createString("p1"));
	private static final ITuple p2 = BASIC.createTuple(TERM.createString("p2"));
private List<User> lu;
	public static Test suite() {
		return new TestSuite(CycleGTTest.class,
				CycleGTTest.class.getSimpleName());
	}

	public void testCycle() throws NumberFormatException {
		System.out.print(pg1);
//		pg1.TransitiveClosure();
//		System.out.print(pg1);
//		GCombinationAlgorithms.combPrefsGen(pg1, probModel);
//		System.out.println("combPrefsGen 0.3 " + pg1);
		Cycle c= new Cycle();
		System.out.print("Has cycle"+c.hasCycles());
	}

	
	public void setUp() {
		probModel = new HashMap<String, Double>();
		probModel.put("pasta", 0.7);
		probModel.put("pizza", 0.45);
		probModel.put("sushi", 0.87);
		probModel.put("fish", 0.68);
		probModel.put("p1", 0.75);
		probModel.put("p2", 0.95);

		User u1= new User("u1", "u1", 0);
		User u2= new User("u2", "u2", 0.1);
		User u3= new User("u3", "u3", 0.1);
		
		pg1 = new GPreferencesGraph(pg1);
//		pg1.addPreference(pasta, sushi,u1);
//		pg1.addPreference(sushi, pizza,u1);
//		pg1.addPreference(pizza, fish,u1);
//		pg1.addPreference(pasta, fish,u1);
//		pg1.addPreference(p1, p2,u1);
//		
//		pg1.addPreference(pasta, sushi,u2);
//		pg1.addPreference(pasta, fish,u2);
//		pg1.addPreference(p1, p2,u2);
//		
//		
//		pg1.addPreference(pizza, pasta,u3);
//		pg1.addPreference(pasta, salad,u3);
//		pg1.addPreference(p1, p2,u3);
		
	lu= new ArrayList<User>();
		lu.add(u1);
		lu.add(u2);
		lu.add(u3);
		
		
		
	}
}
