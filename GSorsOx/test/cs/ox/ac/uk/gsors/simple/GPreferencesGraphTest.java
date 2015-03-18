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

import cs.ox.ac.uk.gsors.User;
import cs.ox.ac.uk.gsors2.GPreferenceEdge;
import cs.ox.ac.uk.gsors2.GPreferencesGraph;


/**
 * <p>
 * Tests the PreferencesGraph.
 * </p>
 * <p>
 * $Id$
 * </p>
 * 
 * @author Richard Poettler (richard dot poettler at deri dot org)
 * @version $Revision$
 */
public class GPreferencesGraphTest extends TestCase {

	/** The not cyceled predicate graph. */
	private GPreferencesGraph pg0;

	/** The cycled graph. */
	private GPreferencesGraph pg1;


	private User u1;
	private User u2;
	private User u3;

	private static final ITuple tv = BASIC.createTuple(TERM.createVariable("X"));
	private static final ITuple t1 = BASIC.createTuple(TERM.createString("t1"));
	private static final ITuple t2 = BASIC.createTuple(TERM.createString("t2"));
	private static final ITuple t3 = BASIC.createTuple(TERM.createString("t3"));
	private static final ITuple t4 = BASIC.createTuple(TERM.createString("t4"));
	private static final ITuple t5 = BASIC.createTuple(TERM.createString("t5"));
	private static final ITuple t6 = BASIC.createTuple(TERM.createString("t6"));
//	private static final ITuple t7 = BASIC.createTuple(TERM.createString("t7"));
//	private static final ITuple t8 = BASIC.createTuple(TERM.createString("t8"));
//	private static final ITuple t9 = BASIC.createTuple(TERM.createString("t9"));
//	private static final ITuple t10 = BASIC.createTuple(TERM.createString("t10"));



	public static Test suite() {
		return new TestSuite(GPreferencesGraphTest.class,
				GPreferencesGraphTest.class.getSimpleName());
	}

	public void testDetectCycles() {
		assertTrue("There are cycles in the graph", pg1.detectCycles());
	}

	public void testFindVertexesForCycle() {
		final Set<ITuple> reference = new HashSet<ITuple>();
		reference.add(t1);
		reference.add(t2);
		reference.add(t3);
		reference.add(t4);
		final Set<ITuple> testing = pg1.findVertexesForCycle();
		assertEquals("The predicate sets must be equal", reference, testing);
	}

	public void testFindEdgesForCycle() {
		User u = new User("u1", "1",0.4);
		final Set<GPreferenceEdge> reference = new HashSet<GPreferenceEdge>();
		reference.add(new GPreferenceEdge(t1, t4, u));
		reference.add(new GPreferenceEdge(t4, t1, u));
		reference.add(new GPreferenceEdge(t1, t2, u));
		reference.add(new GPreferenceEdge(t2, t1, u));
		final Set<GPreferenceEdge> testing = pg1
				.findEdgesForCycle();

		assertEquals("The edge sets must be equal", reference, testing);
	}

	public void testPredicateComparator() {
		final List<ITuple> reference = new ArrayList<ITuple>(
				Arrays.asList(new ITuple[] { t1, t2 }));
		final List<ITuple> testing = new ArrayList<ITuple>(
				Arrays.asList(new ITuple[] {t2,t1 }));
		Collections.sort(testing, pg0.getTupleComparator());

		assertEquals("The sort order isn't correct", reference, testing);
	}

	public void testEdges() {
		final GPreferenceEdge e = new GPreferenceEdge(
				t1, t2, u1);
		final GPreferenceEdge e2 = new GPreferenceEdge(
				t1, t2, u1);
		
			assertEquals("The sort order isn't correct", e, e2);
		
	}

	public void testDuplicates() {
		User u2 = new User("u1", "1",0.3);
		final GPreferenceEdge e = new GPreferenceEdge(
				t5, t6, u2);
		final GPreferenceEdge e2 = new GPreferenceEdge(
				t5, t6, u1);
		pg0.addPreference(e);
		pg0.addPreference(e2);
		System.out.println("Before "+pg0);
	//	pg0.eliminateDuplicates();
		System.out.println("After "+pg0);
		assertEquals("The edge sets must be equal", e, e2);
	}

	
	
	
	public void setUp() {
		pg0 = new GPreferencesGraph();
		u1 = new User("u1", "1",0.3);
		u2 = new User("u2", "2",0.3);
		u3 = new User("u3", "3",0.3);
		GPreferenceEdge e12 = new GPreferenceEdge(t1, t2,u1);
		GPreferenceEdge e23 = new GPreferenceEdge(t2, t3,u1);
		GPreferenceEdge e34 = new GPreferenceEdge(t3, t4,u1);
		GPreferenceEdge e21 = new GPreferenceEdge(t2, t1,u1);
		GPreferenceEdge e41 = new GPreferenceEdge(t4, t1,u1);
		pg0.addPreference(e12);
		pg0.addPreference(e23);
		pg0.addPreference(e34);
		
		
		pg1 = new GPreferencesGraph();
		pg1.addPreference(e12);
		pg1.addPreference(e23);
		pg1.addPreference(e34);
		pg1.addPreference(e21);
		pg1.addPreference(e41);
	}
}
