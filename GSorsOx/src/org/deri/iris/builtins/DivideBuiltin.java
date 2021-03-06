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
package org.deri.iris.builtins;

import static org.deri.iris.factory.Factory.BASIC;

import java.util.Set;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.basics.EqualityTypeItem;

/**
 * <p>
 * Represents a multiply operation. In at the evaluation time there must be only
 * one variable be left for computation, otherwise an exception will be thrown.
 * </p>
 * <p>
 * $Id: DivideBuiltin.java,v 1.15 2007-10-12 12:40:58 bazbishop237 Exp $
 * </p>
 * 
 * @author Richard Pöttler, richard dot poettler at deri dot org
 * @version $Revision: 1.15 $
 */
public class DivideBuiltin extends ArithmeticBuiltin {

    /** The predicate defining this builtin. */
    private static final IPredicate PREDICATE = BASIC.createPredicate("DIVIDE",
	    3);

    /**
     * Constructs a builtin. Three terms must be passed to the constructor,
     * otherwise an exception will be thrown.
     * 
     * @param t
     *            the terms
     * @throws NullPointerException
     *             if one of the terms is {@code null}
     * @throws IllegalArgumentException
     *             if the number of terms submitted is not 3
     * @throws NullPointerException
     *             if t is <code>null</code>
     */
    public DivideBuiltin(final ITerm... t) {
	super(PREDICATE, t);
    }

    protected ITerm computeMissingTerm(int missingTermIndex, ITerm[] terms)
	    throws EvaluationException {
	switch (missingTermIndex) {
	case 0:
	    return BuiltinHelper.multiply(terms[2], terms[1]);

	case 1:
	    return BuiltinHelper.divide(terms[0], terms[2]);

	default:
	    return BuiltinHelper.divide(terms[0], terms[1]);
	}
    }
    
    public Set<EqualityTypeItem> getEqualityType() {
	return this.getEqualityType();
    }
}
