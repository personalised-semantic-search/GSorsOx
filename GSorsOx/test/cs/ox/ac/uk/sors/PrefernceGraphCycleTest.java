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
package cs.ox.ac.uk.sors;

import static org.deri.iris.factory.Factory.BASIC;
import static org.deri.iris.factory.Factory.TERM;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deri.iris.api.basics.ITuple;

import cs.ox.ac.uk.sors.PreferencesGraph;

/**
 * <p>
 * Tests the JODS.
 * </p>
 * <p>
 * $Id$
 * </p>
 * 
 * @author Oana Tifrea-Marciuska
 * @version $Revision$
 */
public class PrefernceGraphCycleTest extends TestCase {

	/** The unrecursive preference graph. */
	private PreferencesGraph pg1;
	private Map<String, Double> probModel;
	

	private static final ITuple m1 = BASIC.createTuple(TERM.createString("m1"));
	private static final ITuple m2 = BASIC.createTuple(TERM.createString("m2"));
	private static final ITuple m3 = BASIC.createTuple(TERM.createString("m3"));
	private static final ITuple m4 = BASIC.createTuple(TERM.createString("m4"));
	private static final ITuple m5 = BASIC.createTuple(TERM.createString("m5"));
	private static final ITuple m6 = BASIC.createTuple(TERM.createString("m6"));
	private static final ITuple m7 = BASIC.createTuple(TERM.createString("m7"));
	private static final ITuple m8 = BASIC.createTuple(TERM.createString("m8"));
	private static final ITuple m9 = BASIC.createTuple(TERM.createString("m9"));
	private static final ITuple m10 = BASIC.createTuple(TERM.createString("m10"));
	private static final ITuple m11 = BASIC.createTuple(TERM.createString("m11"));
	private static final ITuple m12 = BASIC.createTuple(TERM.createString("m12"));
	private static final ITuple m13 = BASIC.createTuple(TERM.createString("m13"));
	private static final ITuple m14 = BASIC.createTuple(TERM.createString("m14"));
	private static final ITuple m15 = BASIC.createTuple(TERM.createString("m15"));

	public static Test suite() {
		return new TestSuite(PrefernceGraphCycleTest.class,
				PrefernceGraphCycleTest.class.getSimpleName());
	}

	public void testCycle() throws NumberFormatException, IOException {
	  System.out.print(pg1.hasCycles());
		
	}



	public void setUp() {
		probModel = new HashMap<String, Double>();
		probModel.put("m1", 0.90);
		probModel.put("m2", 0.39);
		probModel.put("m3", 0.96);
		probModel.put("m4", 0.03);
		probModel.put("m5", 0.90);
		probModel.put("m6", 0.39);
		probModel.put("m7", 0.64);
		probModel.put("m8", 0.69);
		probModel.put("m9", 0.90);
		probModel.put("m10", 0.69);
		probModel.put("m11", 0.98);
		probModel.put("m12", 0.39);
		probModel.put("m13", 0.98);
		probModel.put("m14", 0.39);
		probModel.put("m15", 0.98);
		
		
		pg1 = new PreferencesGraph();
		pg1.addPreference(m1, m2);
		pg1.addPreference(m2, m3);
		pg1.addPreference(m3, m1);
	
		
	}
}
