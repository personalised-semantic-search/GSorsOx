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

import java.util.Set;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.basics.EqualityTypeItem;

/**
 * <p>
 * Checks whether a term is a string.
 * </p>
 * <p>
 * $Id: IsStringBuiltin.java,v 1.2 2007-10-12 12:40:58 bazbishop237 Exp $
 * </p>
 * 
 * @author Richard Pöttler (richard dot poettler at deri dot at)
 * @version $Revision: 1.2 $
 * @since 0.4
 */
public class IsStringBuiltin extends BooleanBuiltin {
    public IsStringBuiltin(final ITerm... t) {
	super(PREDICATE, t);
    }

    protected boolean computeResult(ITerm[] terms) {
	assert terms.length == 1;

	return terms[0] instanceof IStringTerm;
    }

    /** The predicate defining this built-in. */
    private static final IPredicate PREDICATE = org.deri.iris.factory.Factory.BASIC
	    .createPredicate("IS_STRING", 1);
    
    public Set<EqualityTypeItem> getEqualityType() {
	return this.getEqualityType();
    }
}
