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
package org.deri.iris.evaluation.topdown.sldnf;

import java.util.List;

import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.evaluation.IEvaluationStrategy;
import org.deri.iris.evaluation.IEvaluationStrategyFactory;
import org.deri.iris.facts.IFacts;

/**
 * Factory for SLDNF evaluation strategy
 * 
 * @author gigi
 * 
 */
public class SLDNFEvaluationStrategyFactory implements
	IEvaluationStrategyFactory {
    public IEvaluationStrategy createEvaluator(IFacts facts, List<IRule> rules,
	    Configuration configuration) throws EvaluationException {
	return new SLDNFEvaluationStrategy(facts, rules, configuration);
    }

    /* (non-Javadoc)
     * @see org.deri.iris.evaluation.IEvaluationStrategyFactory#createEvaluator(org.deri.iris.facts.IFacts, java.util.List, java.util.List, org.deri.iris.Configuration)
     */
    @Override
    public IEvaluationStrategy createEvaluator(IFacts facts, List<IRule> rules,
	    List<IQuery> queries, Configuration configuration)
	    throws EvaluationException {
	
	return createEvaluator(facts, rules, configuration);
    }

}
