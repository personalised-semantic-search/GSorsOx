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
package org.deri.iris.evaluation.wellfounded;

import java.util.List;

import junit.framework.TestCase;

import org.deri.iris.Configuration;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.compiler.Parser;
import org.deri.iris.factory.Factory;
import org.deri.iris.facts.Facts;
import org.deri.iris.storage.RelationFactory;

public class ProgramDoublerTest extends TestCase {
	List<IRule> startingRules;

	@Override
	protected void setUp() throws Exception {
		final String program = "p(?x) :- t(?x, ?y, ?z), not p(?y), not p(?z)." + "p('b') :- not r('a')."
		        + "t( 'a', 'a', 'b')." + "t( 'a', 'b', 'a').";
		final Parser parser = new Parser();
		parser.parse(program);

		startingRules = parser.getRules();
	}

	public void testStartProgram() {
		final ProgramDoubler doubler = new ProgramDoubler(startingRules, new Facts(new RelationFactory()),
		        new Configuration());

		final List<IRule> startingRules = doubler.getStartingRuleBase();

		assertEquals(0, startingRules.size());
	}

	public void testPositiveProgram() {
		final ProgramDoubler doubler = new ProgramDoubler(startingRules, new Facts(new RelationFactory()),
		        new Configuration());

		final List<IRule> positiveRules = doubler.getPositiveRuleBase();

		for (final IRule rule : positiveRules) {
			final ILiteral head = rule.getHead().iterator().next();
			assertEquals(true, head.isPositive());
			assertEquals(Factory.BASIC.createPredicate("p", 1), head.getAtom().getPredicate());
		}
	}

	public void testNegativeProgram() {
		final ProgramDoubler doubler = new ProgramDoubler(startingRules, new Facts(new RelationFactory()),
		        new Configuration());

		final List<IRule> negativeRules = doubler.getNegativeRuleBase();

		for (final IRule rule : negativeRules) {
			final ILiteral head = rule.getHead().iterator().next();
			assertEquals(true, head.isPositive());
			assertEquals(Factory.BASIC.createPredicate("p" + ProgramDoubler.NEGATED_PREDICATE_SUFFIX, 1), head
			        .getAtom().getPredicate());
		}
	}
}
