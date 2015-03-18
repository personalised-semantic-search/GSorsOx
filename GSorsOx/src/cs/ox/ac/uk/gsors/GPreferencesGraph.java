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
package cs.ox.ac.uk.gsors;

// TODO: implement equals, hashCode an clone.

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.ITuple;

import cs.ox.ac.uk.sors.PreferencesGraph;



/**
 * <p>
 * A graph of qualitative preferences
 * </p>
 * <p>
 * $Id$
 * </p>
 * 
 * @author Oana Tifrea-Marciuska
 * @version $Revision$
 */
public class GPreferencesGraph extends AbstractList<PreferencesGraph> {

	/** The terms stored in this tuple. */
	private final PreferencesGraph[] graphs;

	/**
	 * Creates a tuple defined by the list of terms.
	 * 
	 * @param terms
	 *            list of terms that create a tuple
	 * @throws NullPointerException
	 *             if terms is <code>null</code>
	 */
	public GPreferencesGraph(final Collection<PreferencesGraph> t) {
		if (t == null)
			throw new NullPointerException("Input argument must not be null");
		graphs = t.toArray(new PreferencesGraph[t.size()]);
	}

	GPreferencesGraph(GPreferencesGraph g) {
		if (g == null)
			throw new NullPointerException("Input argument must not be null");
		List<PreferencesGraph> l = new ArrayList<PreferencesGraph>();
		for (int i = 0; i < g.size(); i++) {
			l.add(new PreferencesGraph(g.get(i)));
		}
		graphs = l.toArray(new PreferencesGraph[l.size()]);
	}

	@Override
	public int size() {
		return graphs.length;
	}

	public PreferencesGraph collapseEdges() {
		PreferencesGraph pg = new PreferencesGraph();
		Map<user.models.Pair<ITuple, ITuple>, Integer> m = new HashMap<user.models.Pair<ITuple, ITuple>, Integer>();
		boolean f = true;
		for (PreferencesGraph p : this) {
			if (f) {
				pg.addVertexes(p.vertexSet());
				f = false;
			}
			for (org.jgraph.graph.DefaultEdge e : p.edgeSet()) {
				user.models.Pair<ITuple, ITuple> rel = new user.models.Pair<ITuple, ITuple>(
						(ITuple) e.getSource(), (ITuple) e.getTarget());
				user.models.Pair<ITuple, ITuple> inverse = new user.models.Pair<ITuple, ITuple>(
						(ITuple) e.getTarget(), (ITuple) e.getSource());
				if (!(m.containsKey(rel) || m.containsKey(inverse))) {
					m.put(rel, 1);
				} else {
					if (m.containsKey(inverse)) {
						m.put(inverse, m.get(inverse) - 1);
					} else {
						m.put(rel, m.get(rel) + 1);
					}
				}

			}
		}
		for (user.models.Pair<ITuple, ITuple> pair : m.keySet()) {
			if (m.get(pair) > 0) {
				pg.addPreference(pair.getElement0(), pair.getElement1());
			} else {
				pg.addPreference(pair.getElement1(), pair.getElement1());
			}
		}
		return pg;
	}

	public void removeCycles(String pref) {
		int userId = 0;
		for (PreferencesGraph p : this) {
			p.removeCycles(pref + "User" + String.valueOf(userId));
			userId++;
		}
	}

	@Override
	public PreferencesGraph get(final int i) {
		if (i < 0)
			throw new IllegalArgumentException(
					"The index must be positive, but was " + i);
		if (i >= graphs.length)
			throw new IllegalArgumentException(
					"The index must not be greater or equal to the size ("
							+ size() + "), but was " + i);
		return graphs[i];
	}

	public GPreferencesGraph append(
			final Collection<? extends PreferencesGraph> t) {
		if (t == null)
			throw new IllegalArgumentException("The term list must not be null");

		if (t.isEmpty())
			return this;

		final List<PreferencesGraph> res = new LinkedList<PreferencesGraph>(
				this);
		for (final PreferencesGraph term : graphs) {
			res.add(term);
		}
		return new GPreferencesGraph(res);
	}

	@Override
	public String toString() {
		if (graphs.length <= 0)
			return "()";
		final StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		boolean first = true;
		for (final PreferencesGraph t : graphs) {
			if (first) {
				first = false;
			} else {
				buffer.append(", ");
			}
			buffer.append(t);
		}
		buffer.append(')');
		return buffer.toString();
	}

	public String edgesSTring() {
		if (graphs.length <= 0)
			return "()";
		final StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		boolean first = true;
		for (final PreferencesGraph t : graphs) {
			if (first) {
				first = false;
			} else {
				buffer.append(", ");
			}
			buffer.append(t.edgesString());
		}
		buffer.append(')');
		return buffer.toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof GPreferencesGraph))
			return false;
		final GPreferencesGraph t = ((GPreferencesGraph) o);
		return Arrays.equals(t.graphs, graphs);
	}

	@Override
	public int hashCode() {
		int result = 17;
		for (final PreferencesGraph t : this) {
			result = (result * 37) + t.hashCode();
		}
		return result;
	}

	public int getEdgesSize() {
		int i = 0;
		for (final PreferencesGraph t : this) {
			i += t.getEdgesSize();
		}
		return i;
	}

	public int getVertexesSize() {
		if (this.size() < 1)
			return 0;
		return this.get(0).getEdgesSize();
	}

	public void transitiveClosure() {
		for (int i = 0; i < this.size(); i++) {
			this.get(i).transitiveClosure();
		}

	}

}
