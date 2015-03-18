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
package org.deri.iris.compiler;

import static org.deri.iris.factory.Factory.BASIC;
import static org.deri.iris.factory.Factory.BUILTIN;
import static org.deri.iris.factory.Factory.CONCRETE;
import static org.deri.iris.factory.Factory.TERM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.parser.analysis.DepthFirstAdapter;
import org.deri.iris.parser.node.ABase64binaryTerm;
import org.deri.iris.parser.node.ABinaryBuiltin;
import org.deri.iris.parser.node.ABooleanTerm;
import org.deri.iris.parser.node.ABuiltinLiteral;
import org.deri.iris.parser.node.ADecimalTerm;
import org.deri.iris.parser.node.ADecimallTerm;
import org.deri.iris.parser.node.ADirective;
import org.deri.iris.parser.node.ADirname;
import org.deri.iris.parser.node.ADoubleTerm;
import org.deri.iris.parser.node.AEqualsBuiltin;
import org.deri.iris.parser.node.AFact;
import org.deri.iris.parser.node.AFloatTerm;
import org.deri.iris.parser.node.AFunctionTerm;
import org.deri.iris.parser.node.AGdayTerm;
import org.deri.iris.parser.node.AGmonthTerm;
import org.deri.iris.parser.node.AGmonthdayTerm;
import org.deri.iris.parser.node.AGyearTerm;
import org.deri.iris.parser.node.AGyearmonthTerm;
import org.deri.iris.parser.node.AHexbinaryTerm;
import org.deri.iris.parser.node.AIntIntlist;
import org.deri.iris.parser.node.AIntegerTerm;
import org.deri.iris.parser.node.AIntegerlTerm;
import org.deri.iris.parser.node.AIntlist;
import org.deri.iris.parser.node.AIriTerm;
import org.deri.iris.parser.node.AIrilTerm;
import org.deri.iris.parser.node.ALiteral;
import org.deri.iris.parser.node.ALitlist;
import org.deri.iris.parser.node.ANegatedLiteral;
import org.deri.iris.parser.node.ANegatedbuiltinLiteral;
import org.deri.iris.parser.node.APredicate;
import org.deri.iris.parser.node.AQuery;
import org.deri.iris.parser.node.ARule;
import org.deri.iris.parser.node.ASqnameTerm;
import org.deri.iris.parser.node.ASqnamelTerm;
import org.deri.iris.parser.node.AStringTerm;
import org.deri.iris.parser.node.AStringlTerm;
import org.deri.iris.parser.node.ATernaryBuiltin;
import org.deri.iris.parser.node.AVarTerm;
import org.deri.iris.parser.node.PIntlist;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.RelationFactory;

/**
 * Traverses the abstract syntax tree generated by SableCC and constructs the
 * components of a logic program, i.e. facts, rules and queries.
 */
public class TreeWalker extends DepthFirstAdapter {

    /** Patterns to replace the escape sequences. */
    private static final List<PatternReplace> escapes = new ArrayList<PatternReplace>();

    /** Pattern to match Unicode escapes. */
    private static final Pattern unicodePattern = Pattern
	    .compile("\\\\u([0-9a-fA-F]{4})");

    static {
	// construction of the pattern:
	// in the java string construction '\\' boils down to a single '\'
	// in the regexp pattern creation '\\' are needed to match a
	// single '\'
	// this results in '\\\\' for a single '\' to match. this also
	// applies, if you want to replace something with only a '\'.
	escapes.add(new PatternReplace("\\\\\\\\", "\\\\"));
	escapes.add(new PatternReplace("\\\\t", "\t"));
	escapes.add(new PatternReplace("\\\\n", "\n"));
	escapes.add(new PatternReplace("\\\\r", "\r"));
	escapes.add(new PatternReplace("\\\\f", "\f"));
	escapes.add(new PatternReplace("\\\\'", "'"));
	escapes.add(new PatternReplace("\\\\\"", "\""));
    }

    public TreeWalker(BuiltinRegister builtinRegister) {
	mBuiltinRegister = builtinRegister;
    }

    public Map<IPredicate, IRelation> getFacts() {
	return mFacts;
    }
    
    public Map<IPredicate, IRelation> getDirectives() {
	return mDirectives;
    }

    public List<IRule> getRuleBase() {
	return mRules;
    }

    public List<IQuery> getQueries() {
	return mQueries;
    }

    public void inALitlist(final ALitlist l) {
	super.inALitlist(l);

	literals = new ArrayList<ILiteral>();
    }

    public void inALiteral(final ALiteral l) {
	super.inALiteral(l);
    }

    @Override
    public void inAPredicate(APredicate arg0) {
	super.inAPredicate(arg0);
	pushTerms();
    }

    public void inADirname(ADirname arg0) {
	super.inADirname(arg0);
	pushTerms();
    }
    
    public void inABuiltinLiteral(final ABuiltinLiteral l) {
	super.inABuiltinLiteral(l);

	pushTerms();
    }

    public void inANegatedLiteral(final ANegatedLiteral l) {
	super.inANegatedLiteral(l);
    }

    public void inANegatedbuiltinLiteral(final ANegatedbuiltinLiteral l) {
	super.inANegatedbuiltinLiteral(l);
	pushTerms();
    }

    public void inAFunctionTerm(final AFunctionTerm t) {
	super.inAFunctionTerm(t);

	pushTerms();
    }

    public void inADirective(final ADirective f) {
	super.inADirective(f);

	literals = new ArrayList<ILiteral>();
    }
    
    public void inAFact(final AFact f) {
	super.inAFact(f);

	literals = new ArrayList<ILiteral>();
    }

    public void inAQuery(final AQuery q) {
	super.inAQuery(q);

	literals = new ArrayList<ILiteral>();
    }

    public void inARule(final ARule r) {
	super.inARule(r);

	literals = new ArrayList<ILiteral>();
    }

    public void outARule(final ARule r) {
	super.outARule(r);

	r.getHead().apply(this);
	final List<ILiteral> head = new ArrayList<ILiteral>(literals);

	List<ILiteral> body = new ArrayList<ILiteral>();
	if (r.getBody() != null) {
	    r.getBody().apply(this);
	    body.addAll(literals);
	}

	// Create a rule
	IRule rule = BASIC.createRule(head, body);

	mRules.add(rule);
    }

    public void outAFact(final AFact f) {
	super.outAFact(f);

	IAtom atom = literals.get(0).getAtom();
	addFact(atom.getPredicate(), atom.getTuple());
    }
    
    public void outADirective(final ADirective f) {
	super.outADirective(f);

	IAtom atom = literals.get(0).getAtom();
	addDirective(atom.getPredicate(), atom.getTuple());
    }

    private void addFact(IPredicate predicate, ITuple tuple) {
	IRelation relation = mFacts.get(predicate);

	if (relation == null) {
	    if ( predicate.getPredicateSymbol().startsWith("@") )
		// Create a stored Relation
		relation = new RelationFactory().createRelation(true, predicate.getPredicateSymbol());
	    else
		relation = new RelationFactory().createRelation();
	    mFacts.put(predicate, relation);
	}

	relation.add(tuple);
    }
    
    private void addDirective(IPredicate predicate, ITuple tuple) {
	IRelation relation = mDirectives.get(predicate);

	if (relation == null) {
	    relation = new RelationFactory().createRelation();
	    mDirectives.put(predicate, relation);
	}

	relation.add(tuple);
    }

    public void outAQuery(final AQuery _q) {
	super.outAQuery(_q);

	mQueries.add(BASIC.createQuery(literals));
    }

    public void outANegatedLiteral(ANegatedLiteral _l) {
	final ILiteral neg = BASIC.createLiteral(false, literals.get(
		literals.size() - 1).getAtom());
	literals.set(literals.size() - 1, neg);
    }

    public void outANegatedbuiltinLiteral(ANegatedbuiltinLiteral arg0) {
	final ILiteral neg = BASIC.createLiteral(false, literals.get(
		literals.size() - 1).getAtom());
	literals.set(literals.size() - 1, neg);
    }

    public void outABinaryBuiltin(final ABinaryBuiltin b) {
	final String op = b.getTBinOp().getText().trim();

	List<ITerm> terms = popTerms();

	if (op.equals(">")) {
	    literals.add(BASIC.createLiteral(true, BUILTIN.createGreater(terms
		    .get(0), terms.get(1))));
	} else if (op.equals(">=")) {
	    literals.add(BASIC.createLiteral(true, BUILTIN.createGreaterEqual(
		    terms.get(0), terms.get(1))));
	} else if (op.equals("<")) {
	    literals.add(BASIC.createLiteral(true, BUILTIN.createLess(terms
		    .get(0), terms.get(1))));
	} else if (op.equals("<=")) {
	    literals.add(BASIC.createLiteral(true, BUILTIN.createLessEqual(
		    terms.get(0), terms.get(1))));
	} else if (op.equals("!=")) {
	    literals.add(BASIC.createLiteral(true, BUILTIN.createUnequal(terms
		    .get(0), terms.get(1))));
	} else {
	    throw new IllegalArgumentException("Couldn't handle the operator "
		    + op);
	}
    }

    public void outAEqualsBuiltin(final AEqualsBuiltin b) {
	List<ITerm> terms = popTerms();
	literals.add(BASIC.createLiteral(true, BUILTIN.createEqual(
		terms.get(0), terms.get(1))));
    }

    public void outATernaryBuiltin(final ATernaryBuiltin b) {
	final String op = b.getTTerOp().getText().trim();

	List<ITerm> terms = popTerms();

	if (op.equals("+")) {
	    literals.add(BASIC.createLiteral(true, BUILTIN.createAddBuiltin(
		    terms.get(0), terms.get(1), terms.get(2))));
	} else if (op.equals("-")) {
	    literals.add(BASIC.createLiteral(true, BUILTIN
		    .createSubtractBuiltin(terms.get(0), terms.get(1), terms
			    .get(2))));
	} else if (op.equals("*")) {
	    literals.add(BASIC.createLiteral(true, BUILTIN
		    .createMultiplyBuiltin(terms.get(0), terms.get(1), terms
			    .get(2))));
	} else if (op.equals("/")) {
	    literals.add(BASIC.createLiteral(true, BUILTIN.createDivideBuiltin(
		    terms.get(0), terms.get(1), terms.get(2))));
	} else if (op.equals("%")) {
	    literals.add(BASIC.createLiteral(true, BUILTIN
		    .createModulusBuiltin(terms.get(0), terms.get(1), terms
			    .get(2))));
	} else {
	    throw new IllegalArgumentException("Couldn't handle the operator "
		    + op);
	}
    }

    public void outAPredicate(final APredicate p) {
	super.outAPredicate(p);
	final String symbol = p.getTId().getText().trim();
	final IAtom a;
	final Class<?> builtinClass = mBuiltinRegister.getBuiltinClass(symbol);

	List<ITerm> terms = popTerms();

	if (builtinClass == null) { // this predicate is not a builtin
	    a = BASIC.createAtom(BASIC.createPredicate(symbol, terms.size()),
		    BASIC.createTuple(terms));
	} else { // the predicate is a builtin
	    try {
		a = (IAtom) builtinClass.getConstructor(ITerm[].class)
			.newInstance(
				new Object[] { terms.toArray(new ITerm[terms
					.size()]) });
	    } catch (Exception e) {
		throw new IllegalArgumentException(
			"Can not get the java class for the built-in: "
				+ builtinClass + ", " + e);
	    }
	}
	literals.add(BASIC.createLiteral(true, a));
    }

    public void outADirname(final ADirname n) {
	super.outADirname(n);
	final String symbol = n.getTId().getText().trim();
	final IAtom a;

	List<ITerm> terms = popTerms();

	a = BASIC.createAtom(BASIC.createPredicate(symbol, terms.size()), BASIC.createTuple(terms));
	
	literals.add(BASIC.createLiteral(true, a));
    }
    
    public void outAVarTerm(AVarTerm v) {
	super.outAVarTerm(v);

	final String n = v.getTVariable().getText().trim();
	addTerm(TERM.createVariable(n.startsWith("?") ? n.substring(1) : n));
    }

    public void outAFunctionTerm(AFunctionTerm ft) {
	super.outAFunctionTerm(ft);

	String name = peeleStr(ft.getTId().getText().trim());
	List<ITerm> terms = popTerms();

	addTerm(TERM.createConstruct(name, terms));
    }

    /**
     * Transforms a intlist to an array of integers.
     * 
     * @param l
     *            the intlist to transform
     * @return the transformed integer array
     * @throws NullPointerException
     *             if the intlist is {@code null}
     * @throws IllegalArgumentException
     *             if somewhere along the transformation an object appears,
     *             which vould not be handeled.
     */
    private static Integer[] transfromIntList(final PIntlist l) {
	if (l == null) {
	    throw new NullPointerException("The intlist must not be null");
	}

	final List<Integer> ints = new LinkedList<Integer>();
	PIntlist a = l;
	while (a != null) {
	    if (a instanceof AIntIntlist) {
		ints.add(Integer.parseInt(((AIntIntlist) a).getTInt().getText()
			.trim()));
		a = null;
	    } else if (a instanceof AIntlist) {
		AIntlist il = (AIntlist) a;
		ints.add(Integer.parseInt(il.getTInt().getText().trim()));
		a = il.getIntlist();
	    } else {
		throw new IllegalArgumentException(
			"Couldn't handle a object of type "
				+ a.getClass().getName());
	    }
	}

	Collections.reverse(ints);
	final Integer[] arr = new Integer[ints.size()];
	return ints.toArray(arr);
    }

    public void outAStringTerm(final AStringTerm st) {
	String string = peeleStr(st.getTStr().toString().trim());
	for (final PatternReplace replace : escapes) {
	    string = replace.replaceAll(string);
	}
	string = replaceUnicodeEscapes(string);
	addTerm(TERM.createString(string));
    }

    public void outAStringlTerm(final AStringlTerm st) {
	addTerm(TERM.createString(peeleStr(st.getTStr().toString().trim())));
    }

    public void outAIntegerTerm(final AIntegerTerm it) {
	addTerm(CONCRETE.createInteger(Integer.parseInt(it.getTInt().toString()
		.trim())));
    }

    public void outAIntegerlTerm(final AIntegerlTerm it) {
	addTerm(CONCRETE.createInteger(Integer.parseInt(it.getTInt().toString()
		.trim())));
    }

    public void outADecimalTerm(final ADecimalTerm dt) {
	addTerm(CONCRETE.createDecimal(Double.parseDouble(dt.getTDec()
		.toString().trim())));
    }

    public void outADecimallTerm(final ADecimallTerm dt) {
	addTerm(CONCRETE.createDecimal(Double.parseDouble(dt.getTDec()
		.toString().trim())));
    }

    public void outASqnameTerm(final ASqnameTerm st) {
	addTerm(CONCRETE.createSqName(st.getTSq().toString().trim()));
    }

    public void outASqnamelTerm(final ASqnamelTerm st) {
	addTerm(CONCRETE.createSqName(st.getTSq().toString().trim()));
    }

    public void outAIriTerm(final AIriTerm i) {
	addTerm(CONCRETE.createIri(peeleStr(i.getTStr().toString().trim())));
    }

    public void outAIrilTerm(final AIrilTerm i) {
	addTerm(CONCRETE.createIri(peeleStr(i.getTStr().toString().trim())));
    }

    public void outABooleanTerm(final ABooleanTerm b) {
	String value = peeleStr(b.getTStr().toString().trim());
	addTerm(CONCRETE.createBoolean(value));
    }

    public void outADoubleTerm(final ADoubleTerm d) {
	addTerm(CONCRETE.createDouble(Double.parseDouble(d.getTDec().toString()
		.trim())));
    }

    public void outAFloatTerm(final AFloatTerm f) {
	addTerm(CONCRETE.createFloat(Float.parseFloat(f.getTDec().toString()
		.trim())));
    }

    public void outABase64binaryTerm(final ABase64binaryTerm b) {
	addTerm(CONCRETE.createBase64Binary(peeleStr(b.getTStr().getText()
		.trim())));
    }

    public void outAHexbinaryTerm(final AHexbinaryTerm h) {
	addTerm(CONCRETE
		.createHexBinary(peeleStr(h.getTStr().getText().trim())));
    }

    private int parseInt(String text) {
	return Integer.parseInt(text.trim());
    }

    private double parseDouble(String text) {
	return Double.parseDouble(text.trim());
    }

    // public void outADateTerm(final ADateTerm d) {
    // int year = parseInt( d.getYear().getText() );
    // int month = parseInt( d.getMonth().getText() );
    // int day = parseInt( d.getDay().getText() );
    //
    // addTerm(CONCRETE.createDate(year, month, day));
    // }
    //
    // public void outADatetzTerm(ADatetzTerm d) {
    // int year = parseInt( d.getYear().getText() );
    // int month = parseInt( d.getMonth().getText() );
    // int day = parseInt( d.getDay().getText() );
    //
    // int tzhour = parseInt( d.getTzhour().getText() );
    // int tzminute = parseInt( d.getTzminute().getText() );
    //
    // addTerm(CONCRETE.createDate(year, month, day, tzhour, tzminute));
    // }
    //
    // public void outATimeisTerm(final ATimeisTerm t) {
    // int hour = parseInt( t.getHour().getText() );
    // int minute = parseInt( t.getMinute().getText() );
    // int second = parseInt( t.getSecond().getText() );
    //
    // addTerm(CONCRETE.createTime(hour, minute, second, 0, 0));
    // }
    //
    // public void outATimeistzTerm(final ATimeistzTerm t) {
    // int hour = parseInt( t.getHour().getText() );
    // int minute = parseInt( t.getMinute().getText() );
    // int second = parseInt( t.getSecond().getText() );
    //
    // int tzhour = parseInt( t.getTzhour().getText() );
    // int tzminute = parseInt( t.getTzminute().getText() );
    //
    // addTerm(CONCRETE.createTime(hour, minute, second, tzhour, tzminute));
    // }
    //
    // public void outATimefsTerm(final ATimefsTerm t) {
    // int hour = parseInt( t.getHour().getText() );
    // int minute = parseInt( t.getMinute().getText() );
    // double second = parseDouble( t.getSecond().getText() );
    //
    // addTerm(CONCRETE.createTime(hour, minute, second, 0, 0));
    // }
    //	
    // public void outATimefstzTerm(final ATimefstzTerm t) {
    // int hour = parseInt( t.getHour().getText() );
    // int minute = parseInt( t.getMinute().getText() );
    // double second = parseDouble( t.getSecond().getText() );
    //
    // int tzhour = parseInt( t.getTzhour().getText() );
    // int tzminute = parseInt( t.getTzminute().getText() );
    //
    // addTerm(CONCRETE.createTime(hour, minute, second, tzhour, tzminute));
    // }
    //	
    // public void outATimemsTerm(final ATimemsTerm t) {
    // int hour = parseInt( t.getHour().getText() );
    // int minute = parseInt( t.getMinute().getText() );
    // int second = parseInt( t.getSecond().getText() );
    // int millisecond = parseInt( t.getMillisecond().getText() );
    //
    // addTerm(CONCRETE.createTime(hour, minute, second, millisecond, 0, 0));
    // }
    //	
    // public void outATimemstzTerm(final ATimemstzTerm t) {
    // int hour = parseInt( t.getHour().getText() );
    // int minute = parseInt( t.getMinute().getText() );
    // int second = parseInt( t.getSecond().getText() );
    // int millisecond = parseInt( t.getMillisecond().getText() );
    //
    // int tzhour = parseInt( t.getTzhour().getText() );
    // int tzminute = parseInt( t.getTzminute().getText() );
    //
    // addTerm(CONCRETE.createTime(hour, minute, second, millisecond, tzhour,
    // tzminute));
    // }
    //
    // public void outADurationisTerm(final ADurationisTerm d) {
    // int year = parseInt( d.getYear().getText() );
    // int month = parseInt( d.getMonth().getText() );
    // int day = parseInt( d.getDay().getText() );
    //	
    // int hour = parseInt( d.getHour().getText() );
    // int minute = parseInt( d.getMinute().getText() );
    // int second = parseInt( d.getSecond().getText() );
    //		
    // addTerm(CONCRETE.createDuration(true, year, month, day, hour, minute,
    // second ) );
    // }
    //
    // public void outADurationfsTerm(final ADurationfsTerm d) {
    // int year = parseInt( d.getYear().getText() );
    // int month = parseInt( d.getMonth().getText() );
    // int day = parseInt( d.getDay().getText() );
    //	
    // int hour = parseInt( d.getHour().getText() );
    // int minute = parseInt( d.getMinute().getText() );
    // double second = parseDouble( d.getSecond().getText() );
    //		
    // addTerm(CONCRETE.createDuration(true, year, month, day, hour, minute,
    // second ) );
    // }
    //
    // public void outADurationmsTerm(final ADurationmsTerm d) {
    // int year = parseInt( d.getYear().getText() );
    // int month = parseInt( d.getMonth().getText() );
    // int day = parseInt( d.getDay().getText() );
    //	
    // int hour = parseInt( d.getHour().getText() );
    // int minute = parseInt( d.getMinute().getText() );
    // int second = parseInt( d.getSecond().getText() );
    // int millisecond = parseInt( d.getMillisecond().getText() );
    //		
    // addTerm(CONCRETE.createDuration(true, year, month, day, hour, minute,
    // second, millisecond ) );
    // }
    //
    // public void outADatetimeisTerm(final ADatetimeisTerm d) {
    // int year = parseInt( d.getYear().getText() );
    // int month = parseInt( d.getMonth().getText() );
    // int day = parseInt( d.getDay().getText() );
    //	
    // int hour = parseInt( d.getHour().getText() );
    // int minute = parseInt( d.getMinute().getText() );
    // int second = parseInt( d.getSecond().getText() );
    //		
    // addTerm(CONCRETE.createDateTime(year, month, day, hour, minute, second,
    // 0, 0 ) );
    // }
    //	
    // public void outADatetimeistzTerm(final ADatetimeistzTerm d) {
    // int year = parseInt( d.getYear().getText() );
    // int month = parseInt( d.getMonth().getText() );
    // int day = parseInt( d.getDay().getText() );
    //	
    // int hour = parseInt( d.getHour().getText() );
    // int minute = parseInt( d.getMinute().getText() );
    // int second = parseInt( d.getSecond().getText() );
    //		
    // int tzhour = parseInt( d.getTzhour().getText() );
    // int tzminute = parseInt( d.getTzminute().getText() );
    //
    // addTerm(CONCRETE.createDateTime(year, month, day, hour, minute, second,
    // tzhour, tzminute ) );
    // }
    //	
    // public void outADatetimefsTerm(final ADatetimefsTerm d) {
    // int year = parseInt( d.getYear().getText() );
    // int month = parseInt( d.getMonth().getText() );
    // int day = parseInt( d.getDay().getText() );
    //	
    // int hour = parseInt( d.getHour().getText() );
    // int minute = parseInt( d.getMinute().getText() );
    // double second = parseDouble( d.getSecond().getText() );
    //		
    // addTerm(CONCRETE.createDateTime(year, month, day, hour, minute, second,
    // 0, 0 ) );
    // }
    //	
    // public void outADatetimefstzTerm(final ADatetimefstzTerm d) {
    // int year = parseInt( d.getYear().getText() );
    // int month = parseInt( d.getMonth().getText() );
    // int day = parseInt( d.getDay().getText() );
    //	
    // int hour = parseInt( d.getHour().getText() );
    // int minute = parseInt( d.getMinute().getText() );
    // double second = parseDouble( d.getSecond().getText() );
    //		
    // int tzhour = parseInt( d.getTzhour().getText() );
    // int tzminute = parseInt( d.getTzminute().getText() );
    //
    // addTerm(CONCRETE.createDateTime(year, month, day, hour, minute, second,
    // tzhour, tzminute ) );
    // }
    //	
    // public void outADatetimemsTerm(final ADatetimemsTerm d) {
    // int year = parseInt( d.getYear().getText() );
    // int month = parseInt( d.getMonth().getText() );
    // int day = parseInt( d.getDay().getText() );
    //	
    // int hour = parseInt( d.getHour().getText() );
    // int minute = parseInt( d.getMinute().getText() );
    // int second = parseInt( d.getSecond().getText() );
    // int millisecond = parseInt( d.getMillisecond().getText() );
    //		
    // addTerm(CONCRETE.createDateTime(year, month, day, hour, minute, second,
    // millisecond, 0, 0 ) );
    // }
    //	
    // public void outADatetimemstzTerm(final ADatetimemstzTerm d) {
    // int year = parseInt( d.getYear().getText() );
    // int month = parseInt( d.getMonth().getText() );
    // int day = parseInt( d.getDay().getText() );
    //	
    // int hour = parseInt( d.getHour().getText() );
    // int minute = parseInt( d.getMinute().getText() );
    // int second = parseInt( d.getSecond().getText() );
    // int millisecond = parseInt( d.getMillisecond().getText() );
    //		
    // int tzhour = parseInt( d.getTzhour().getText() );
    // int tzminute = parseInt( d.getTzminute().getText() );
    //
    // addTerm(CONCRETE.createDateTime(year, month, day, hour, minute, second,
    // millisecond, tzhour, tzminute ) );
    // }
    //	
    // public void outADatetimeTerm(final ADatetimeTerm d) {
    // Integer[] params = transfromIntList(d.getIntlist());
    // if (params.length == 6) {
    // addTerm(CONCRETE.createDateTime(params[0], params[1], params[2],
    // params[3], params[4], params[5], 0, 0));
    // } else if (params.length == 8) {
    // addTerm(CONCRETE.createDateTime(params[0], params[1], params[2],
    // params[3], params[4], params[5],
    // params[6], params[7]));
    // } else if (params.length == 9) {
    // addTerm(CONCRETE.createDateTime(params[0], params[1], params[2],
    // params[3], params[4], params[5],
    // params[6], params[7], params[8]));
    // } else {
    // throw new
    // IllegalArgumentException("The number of integers in a datetime must be 6, 8 or 9, but was "
    // +
    // params.length + ": " + Arrays.toString(params));
    // }
    // }

    public void outAGyearTerm(final AGyearTerm g) {
	addTerm(CONCRETE.createGYear(Integer.parseInt(g.getTInt().getText()
		.trim())));
    }

    public void outAGmonthTerm(final AGmonthTerm g) {
	addTerm(CONCRETE.createGMonth(Integer.parseInt(g.getTInt().getText()
		.trim())));
    }

    public void outAGdayTerm(final AGdayTerm g) {
	addTerm(CONCRETE.createGDay(Integer.parseInt(g.getTInt().getText()
		.trim())));
    }

    public void outAGyearmonthTerm(final AGyearmonthTerm g) {
	Integer[] params = transfromIntList(g.getIntlist());
	if (params.length != 2) {
	    throw new IllegalArgumentException(
		    "The number of integers in a gyearmonth must be 2, but was "
			    + params.length + ": " + Arrays.toString(params));
	}
	addTerm(CONCRETE.createGYearMonth(params[0], params[1]));
    }

    public void outAGmonthdayTerm(final AGmonthdayTerm g) {
	Integer[] params = transfromIntList(g.getIntlist());
	if (params.length != 2) {
	    throw new IllegalArgumentException(
		    "The number of integers in a gmonthday must be 2, but was "
			    + params.length + ": " + Arrays.toString(params));
	}
	addTerm(CONCRETE.createGMonthDay(params[0], params[1]));
    }

    /**
     * Removes the leading and tailing <code>'</code> or <code>&quot;</code> from a
     * string.
     * 
     * @param s
     *            the string to peele
     * @return the peeled stirng if the string started and ended with a <code>'</code>
     *         or <code>&quot;</code>, otherwise the input string will be
     *         returned.
     * @throws NullPointerException
     *             if the string is <code>null</code>
     */
    private static String peeleStr(final String s) {
	if (s == null) {
	    throw new NullPointerException("The string must not be null");
	}
	if (((s.charAt(0) == '\'') && (s.charAt(s.length() - 1) == '\''))
		|| ((s.charAt(0) == '\"') && (s.charAt(s.length() - 1) == '\"'))) {
	    return s.substring(1, s.length() - 1);
	}
	return s;
    }

    private void pushTerms() {
	mTermStack.add(new ArrayList<ITerm>());
    }

    private void addTerm(ITerm term) {
	mTermStack.get(mTermStack.size() - 1).add(term);
    }

    private List<ITerm> popTerms() {
	int last = mTermStack.size() - 1;

	List<ITerm> result = mTermStack.get(last);
	mTermStack.remove(last);

	return result;
    }

    /**
     * Replaces all escaped Unicode appearances in a string with the real
     * Unicode characters.
     * 
     * @param string
     *            the string in which to replace the occurrences
     * @return the string with all escapes replaced
     */
    private static String replaceUnicodeEscapes(final String string) {
	assert string != null : "The string must not be null";

	String workString = string;
	Matcher matcher = unicodePattern.matcher(workString);
	while (matcher.find()) {
	    final String hexString = matcher.group(1);
	    final String unicodeString = String.valueOf(Character
		    .toChars(Integer.parseInt(hexString, 16)));
	    workString = matcher.replaceFirst(unicodeString);
	    matcher = unicodePattern.matcher(workString);
	}
	return workString;
    }

    /**
     * Helperclass to do a faster String.replaceAll(...).
     */
    private static class PatternReplace {

	/** Pattern holding the string to replace. */
	private final Pattern pattern;

	/** String to replace the pattern. */
	private final String replacement;

	/**
	 * Constructor taking a pattern and replacement. The pattern will be
	 * compiled to a <code>java.util.regex.Pattern</code> so the
	 * documentation, how to write a pattern can be found there. The
	 * replacement will be used with the
	 * <code>java.util.regex.Matcher.replaceAll(...)</code> method, so the
	 * construction of such a string is written there, too.
	 * 
	 * @param pattern
	 *            regular expression for which to search
	 * @param replacement
	 *            string with which to replace the found patterns
	 * @throws IllegalArgumentException
	 *             if the pattern is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the replacement is <code>null</code>
	 */
	public PatternReplace(final String pattern, final String replacement) {
	    if (pattern == null) {
		throw new IllegalArgumentException(
			"The pattern must not be null");
	    }
	    if (replacement == null) {
		throw new IllegalArgumentException(
			"The replacement must not be null");
	    }
	    this.pattern = Pattern.compile(pattern);
	    this.replacement = replacement;
	}

	/**
	 * Does the replacement.
	 * 
	 * @param string
	 *            string in which to replace all occurrences of
	 *            <code>pattern</code> with the <code>replacement</code>.
	 * @param return a new string will all occurrences replaced
	 */
	public String replaceAll(final String string) {
	    if (string == null) {
		throw new IllegalArgumentException(
			"The string must not be null");
	    }

	    return pattern.matcher(string).replaceAll(replacement);
	}
    }

    private List<List<ITerm>> mTermStack = new ArrayList<List<ITerm>>();

    private List<ILiteral> literals;

    private Map<IPredicate, IRelation> mFacts = new HashMap<IPredicate, IRelation>();
    
    private Map<IPredicate, IRelation> mDirectives = new HashMap<IPredicate, IRelation>();

    private List<IRule> mRules = new ArrayList<IRule>();

    private List<IQuery> mQueries = new ArrayList<IQuery>();

    private final BuiltinRegister mBuiltinRegister;

}
