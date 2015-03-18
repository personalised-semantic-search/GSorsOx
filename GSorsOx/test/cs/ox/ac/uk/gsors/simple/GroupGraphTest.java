/*
 * Integrated Rule Inference System (IRIS):
 * An extensible rule inference system for datalog with extensions.
 * 
 * Copyright (C) 2008 Semantic Technology Institute (STI) Innsbruck, 
 * University of Innsbruck, Technikerstrasse 21a, 6020 Innsbruck, Austria.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package cs.ox.ac.uk.gsors.simple;

import static org.deri.iris.factory.Factory.BASIC;
import static org.deri.iris.factory.Factory.TERM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deri.iris.api.basics.ITuple;

import cs.ox.ac.uk.gsors.AggregateStrategy;
import cs.ox.ac.uk.gsors.User;
import cs.ox.ac.uk.gsors2.GPreferenceEdge;
import cs.ox.ac.uk.gsors2.GPreferencesGraph;
import cs.ox.ac.uk.gsors2.GTopKAlgorithms;

/**
 * <p>
 * Tests the PredicateGraph.
 * </p>
 * <p>
 * $Id$
 * </p>
 * 
 * @author Richard Poottler (richard dot poettler at deri dot org)
 * @version $Revision$
 */
public class GroupGraphTest extends TestCase {

	/** The cyceled preference graph. */
	private GPreferencesGraph pg0, pmerge;

	/** The unrecursive preference graph. */
	private GPreferencesGraph pg1;
	private GPreferencesGraph pg2;
	private GPreferencesGraph pg3;
	private User u1, u2, u3;
	private static final ITuple pl1 = BASIC.createTuple(TERM
			.createString("pl1"));
	private static final ITuple fpa = BASIC.createTuple(TERM
			.createString("fpa"));
	private static final ITuple fpi = BASIC.createTuple(TERM
			.createString("fpi"));
	private static final ITuple fsu = BASIC.createTuple(TERM
			.createString("fsu"));
	private static final ITuple fsa = BASIC.createTuple(TERM
			.createString("fsa"));
	private static final ITuple ffi = BASIC.createTuple(TERM
			.createString("ffi"));
	private static final ITuple pl2 = BASIC.createTuple(TERM
			.createString("pl2"));

	public static Test suite() {
		return new TestSuite(GroupGraphTest.class,
				GroupGraphTest.class.getSimpleName());
	}

	public void testDetectCycles() {
		// System.out.println("Cycles"+pg1.detectCycles());
		// for (ITuple t : pg1.findVertexesForCycle()) {
		// System.out.print("t"+t.toString());
		// }
		// assertTrue("There are cycles in the graph", pg1.detectCycles());

	}

	public void testFindVertexesForCycle() {
		final Set<ITuple> reference = new HashSet<ITuple>();
		final Set<ITuple> testing = pg1.findVertexesForCycle();

		assertEquals("The predicate sets must be equal", reference, testing);
	}

	public void testFindEdgesForCycle() {
		
		final Set<GPreferenceEdge> testing = pg1
				.findEdgesForCycle();

	}

	public void testAlgorithm() {
		System.out.println("Before " + pg0);

//			pg0.mergeProbabilisticModel("/home/onsa/Dropbox/VGOT/toit13/resources/test.txt");
			System.out.println("After " + pg0);
			System.out.println("After - "
					+ GTopKAlgorithms.getTopK(pg0, 2, AggregateStrategy.CSU));
	
	}

	public void testPredicateComparator() {
		final List<ITuple> reference = new ArrayList<ITuple>(
				Arrays.asList(new ITuple[] { fpa }));
		final List<ITuple> testing = new ArrayList<ITuple>(
				Arrays.asList(new ITuple[] { fpa }));
		Collections.sort(testing, pg1.getTupleComparator());

		assertEquals("The sort order isn't correct", reference, testing);
	}

	public void setUp() {
		pg1 = new GPreferencesGraph();
		u1 = new User("u1", "u1", 0);
		pg1.addPreference(new GPreferenceEdge(fpa, fsu, u1));
		pg1.addPreference(new GPreferenceEdge(fpa, ffi, u1));
		pg1.addPreference(new GPreferenceEdge(fpi, fsu, u1));
		pg1.addPreference(new GPreferenceEdge(fpi, ffi, u1));
		pg1.addPreference(new GPreferenceEdge(pl1, pl2, u1));
		pg1.addVertex(fsa);

		pg2 = new GPreferencesGraph();
		u2 = new User("u2", "u2", 0.1);
		pg2.addPreference(new GPreferenceEdge(fpa, fsu, u2));
		pg2.addPreference(new GPreferenceEdge(fpa, ffi, u2));
		pg2.addPreference(new GPreferenceEdge(pl2, pl1, u2));
		pg2.addVertex(fsa);
		pg2.addVertex(fpi);

		pg3 = new GPreferencesGraph();
		u3 = new User("u3", "u3", 0.3);
		pg3.addPreference(new GPreferenceEdge(fpi, fpa, u3));
		pg3.addPreference(new GPreferenceEdge(fpa, fsa, u3));
		pg3.addPreference(new GPreferenceEdge(pl1, pl2, u3));
		pg3.addVertex(fsu);
		pg3.addVertex(ffi);

		pg0.addPreference(new GPreferenceEdge(fpa, fsu, u1));
		pg0.addPreference(new GPreferenceEdge(fpa, ffi, u1));
		pg0.addPreference(new GPreferenceEdge(fpi, fsu, u1));
		pg0.addPreference(new GPreferenceEdge(fpi, ffi, u1));
		pg0.addPreference(new GPreferenceEdge(pl1, pl2, u1));
		pg0.addPreference(new GPreferenceEdge(fpa, fsu, u2));
		pg0.addPreference(new GPreferenceEdge(fpa, ffi, u2));
		pg0.addPreference(new GPreferenceEdge(pl2, pl1, u2));
		pg0.addPreference(new GPreferenceEdge(fpi, fpa, u3));
		pg0.addPreference(new GPreferenceEdge(fpa, fsa, u3));
		pg0.addPreference(new GPreferenceEdge(pl1, pl2, u3));

	}
}
