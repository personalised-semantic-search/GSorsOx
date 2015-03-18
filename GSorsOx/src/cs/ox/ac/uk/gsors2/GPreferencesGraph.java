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
package cs.ox.ac.uk.gsors2;

// TODO: implement equals, hashCode an clone.

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.iris.api.basics.ITuple;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graphs;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DirectedMultigraph;

import user.models.Pair;
import cs.ox.ac.uk.gsors.User;

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
public class GPreferencesGraph implements Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2302977473188237968L;
	private final Logger LOGGER = Logger.getLogger(GPreferencesGraph.class);
	/** Comparator to order preferences according to their dependencies. */
	private final TupleComparator pc = new TupleComparator();

	private enum Color {
		white, grey, black
	};

	private Map<ITuple, Color> colorMap;

	/** Graph to represent the dependencies of the Literals. */
	private DirectedMultigraph<ITuple, GPreferenceEdge> g;
	private CycleDetector<ITuple, GPreferenceEdge> cd;
	/**
	 * Connectivity inspector to determine, whether paths between vertices
	 * exists.
	 */
	private ConnectivityInspector<ITuple, GPreferenceEdge> ci;

	/**
	 * Constructs an empty graph object.
	 */
	public GPreferencesGraph() {
		/** Graph to represent the dependencies of the Literals. */
		g = new DirectedMultigraph<ITuple, GPreferenceEdge>(
				new PreferenceEdgeFactory<ITuple,GPreferenceEdge>(GPreferenceEdge.class));
		cd = new CycleDetector<ITuple, GPreferenceEdge>(g);
		ci = new ConnectivityInspector<ITuple, GPreferenceEdge>(g);
	}
  public Set<GPreferenceEdge> getOutGoingEdges(ITuple t){
	  return g.outgoingEdgesOf(t);
  }
	GPreferencesGraph(GPreferencesGraph copy) {
		g = new DirectedMultigraph<ITuple, GPreferenceEdge>(
				new PreferenceEdgeFactory<ITuple,GPreferenceEdge>(GPreferenceEdge.class));
		if (copy.g != null) {
			for (GPreferenceEdge edge : copy.g.edgeSet()) {
				ITuple preffered=edge.getSource();
				ITuple lessPreffred=edge.getTarget();
				User user = new User(edge.getLabel().getId(), edge.getLabel()
						.getName(), edge.getLabel().getT());
				if (!g.containsVertex(preffered)) {
					g.addVertex(preffered);
				}
				if (!g.containsVertex(lessPreffred)) {
					g.addVertex(lessPreffred);
				}
				final GPreferenceEdge e = new GPreferenceEdge(
						preffered, lessPreffred, user);
				g.addEdge(preffered, lessPreffred, e);

			}
		}
		cd = new CycleDetector<ITuple, GPreferenceEdge>(g);
		ci = new ConnectivityInspector<ITuple, GPreferenceEdge>(g);
	}

	public void TransitiveClosure() {
		LabelledTransitiveClosure c = LabelledTransitiveClosure.INSTANCE;
		c.closeSimpleDirectedGraph(g);
	}

	public boolean detectCycles() {
		return cd.detectCycles();
	}

	public Set<ITuple> findVertexesForCycle() {

		return cd.findCycles();
	}

	public int getEdgesSize() {

		return g.edgeSet().size();
	}

	public int getVertexesSize() {

		return g.vertexSet().size();
	}

	public Set<GPreferenceEdge> findEdgesForCycle() {
		final Set<ITuple> cycle = findVertexesForCycle();
		final Set<GPreferenceEdge> edges = new HashSet<GPreferenceEdge>();
		for (final ITuple v : cycle) {
			for (final ITuple p : Graphs.successorListOf(g, v)) {
				if (cycle.contains(p)) {
					edges.add(g.getEdge(v, p));
					break;
				}
			}
		}
		assert (edges.size() == cycle.size()) : "the number of edges and vertexes must be equal";
		return edges;
	}

	public void addPreference(final GPreferenceEdge pe) {
		addPreference(pe.getSource(), pe.getTarget(), pe.getLabel());
	}

	public void addVertex(final ITuple tuple) {
		if (!g.vertexSet().contains(tuple)) {
			g.addVertex(tuple);
		}
	}

	/**
	 * Adds a preference to this graph.
	 * 
	 * @param rule
	 *            the rule to add
	 * @throws NullPointerException
	 *             if the rule is <code>null</code>
	 */
	public void addPreference(final ITuple preffered,
			final ITuple lessPreffred, final User user) {
		if (preffered == null || lessPreffred == null)
			throw new NullPointerException("The Literals must not be null");
		if (!g.containsVertex(preffered)) {
			g.addVertex(preffered);
		}
		if (!g.containsVertex(lessPreffred)) {
			g.addVertex(lessPreffred);
		}
		final GPreferenceEdge e = new GPreferenceEdge(
				preffered, lessPreffred, user);
		g.addEdge(preffered, lessPreffred, e);
	}

	public void addPreference(final List<Pair<ITuple, ITuple>> r, User user) {
		if ((r == null) || r.contains(null))
			throw new NullPointerException(
					"The rules must not be, or contain null");
		for (final Pair<ITuple, ITuple> pair : r) {

			addPreference(pair.getElement0(), pair.getElement1(), user);
		}

	}

	public Set<ITuple> vertexSet() {
		return g.vertexSet();
	}

	public Set<GPreferenceEdge> edgeSet() {
		return g.edgeSet();
	}

	public void removePreference(final ITuple preffered,
			final ITuple lessPreffred, User user) {

		if (preffered == null || lessPreffred == null)
			throw new NullPointerException("The Literals must not be null");
		GPreferenceEdge p = new GPreferenceEdge(
				preffered, preffered, user);
		g.removeEdge(p);

	}

	/**
	 * <p>
	 * Returns a comparator which compares two tuples depending on their
	 * preferences
	 * </p>
	 * <p>
	 * If one of the compared tuples isn't in the graph, or there isn't a path
	 * from one tuple to the other {@code 0} will be returned. If the first
	 * tuple more preferred the second one, the first one will be determined to
	 * be bigger, and vice versa.
	 * </p>
	 * 
	 * @return the comparator
	 */
	public Comparator<ITuple> getTupleComparator() {
		return pc;
	}

	/**
	 * Returns a set of Literals the given one depends on.
	 * 
	 * @param p
	 *            Literal for which to check for dependencies
	 * @return the Literals the Literal depends on
	 * @throws NullPointerException
	 *             if the Literal is {@code null}
	 */
	public Set<ITuple> getDepends(final ITuple p) {
		if (p == null)
			throw new NullPointerException("The Literal must not be null");
		if (!g.containsVertex(p))
			return Collections.emptySet();

		final Set<ITuple> todo = new HashSet<ITuple>();
		todo.add(p);
		final Set<ITuple> deps = new HashSet<ITuple>();

		while (!todo.isEmpty()) {
			final ITuple act = todo.iterator().next();
			todo.remove(act);

			for (final ITuple depends : Graphs.predecessorListOf(g, act)) {
				if (deps.add(depends)) {
					todo.add(depends);
				}
			}
		}

		return deps;
	}

	public void removeAllVertices(Set<ITuple> vertToDelete) {
		g.removeAllVertices(vertToDelete);
	}

	public Set<GPreferenceEdge> getAllEdges(ITuple source,
			ITuple target) {
		return g.getAllEdges(source, target);
	}

	public void removeAllEdges(ITuple source, ITuple target) {
		g.removeAllEdges(source, target);
	}

	public boolean noPredecesors(ITuple node) {
		return (Graphs.predecessorListOf(g, node).size() == 0);
	}

	public void removeEdge(GPreferenceEdge e) {
		g.removeEdge(e);
	}

	/*
	 * 
	 * @return the string description
	 */
	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		for (final GPreferenceEdge e : g.edgeSet()) {
			b.append(e).append(System.getProperty("line.separator"));
		}
		return b.toString();
	}

	/**
	 * <p>
	 * Compares two Literals depending on their dependencies of their
	 * preferences.
	 * </p>
	 * <p>
	 * If one of the compared Literal isn't in the graph, or there isn't a path
	 * from one Literal to the other {@code 0} will be returned. If the first
	 * Literal depends on the second one, the first one will be determined to be
	 * bigger, and vice versa.
	 * </p>
	 * <p>
	 * $Id$
	 * </p>
	 * 
	 * @author richi
	 * @version $Revision$
	 */
	private class TupleComparator implements Comparator<ITuple> {

		@Override
		public int compare(final ITuple o1, final ITuple o2) {
			if ((o1 == null) || (o2 == null))
				throw new NullPointerException(
						"None of the Literals must be null");

			// one of the vertices is not in the graph, or there is no
			// connection of the vertices -> return 0
			if (!g.containsVertex(o1) || !g.containsVertex(o2)
					|| !ci.pathExists(o1, o2))
				return 0;
			// determine who depends on who
			return getDepends(o1).contains(o2) ? 1 : -1;
		}
	}

	/**
	 * <p>
	 * The simple factory to create default edges for the PreferenceGraph. The
	 * label of the edge will be <code>true</code>.
	 * </p>
	 * <p>
	 * $Id$
	 * </p>
	 * 
	 * @author Oana Tfrea-Marciuska
	 * @version $Revision$
	 * @since 0.1
	 */
	private static class PreferenceEdgeFactory<V,E> implements EdgeFactory<V, E>,
    Serializable
{


private static final long serialVersionUID = 3618135658586388792L;



private final Class<? extends E> edgeClass;



public PreferenceEdgeFactory(Class<? extends E> edgeClass)
{
    this.edgeClass = edgeClass;
}



/**
 * @see EdgeFactory#createEdge(Object, Object)
 */
public E createEdge(V source, V target)
{
    try {
        return edgeClass.newInstance();
    } catch (Exception ex) {
        throw new RuntimeException("Edge factory failed", ex);
    }
}
	}

	public void removeCycles() {
		if (detectCycles())
			LOGGER.info("There is a cycle");
		GPreferenceEdge edgeC = null;
		for (GPreferenceEdge edge : new HashSet<GPreferenceEdge>(
				g.edgeSet())) {
			Set<GPreferenceEdge> pIn = g.getAllEdges(
					edge.getSource(), edge.getTarget());
			Set<GPreferenceEdge> pOut = g.getAllEdges(
					edge.getTarget(), edge.getSource());
			if (pIn.size() == pOut.size()) {
				g.removeAllEdges(edge.getTarget(), edge.getSource());
				g.removeAllEdges(edge.getSource(), edge.getTarget());

			}
		}
		while ((edgeC = findCycle()) != null) {
			g.removeEdge(edgeC);
		}
	}

	void collapseEdges() {
		Set<GPreferenceEdge> edgeSet = g.edgeSet();
		for (GPreferenceEdge edge : new HashSet<GPreferenceEdge>(
				edgeSet)) {
			Set<GPreferenceEdge> pIn = g.getAllEdges(
					edge.getSource(), edge.getTarget());
			Set<GPreferenceEdge> pOut = g.getAllEdges(
					edge.getTarget(), edge.getSource());
			if (pIn.size() == pOut.size()) {
				g.removeAllEdges(edge.getTarget(), edge.getSource());
				g.removeAllEdges(edge.getSource(), edge.getTarget());

			} else if (pIn.size() > pOut.size()) {
				g.removeAllEdges(edge.getTarget(), edge.getSource());
				GPreferenceEdge eaux = null;
				for (GPreferenceEdge ee : new HashSet<GPreferenceEdge>(
						pIn)) {
					if (eaux == null) {
						eaux = ee;
					}
					if (!eaux.equals(ee)) {
						g.removeEdge(ee);
					}
				}

			} else {
				g.removeAllEdges(edge.getSource(), edge.getTarget());
				GPreferenceEdge eaux = null;
				for (GPreferenceEdge ee : new HashSet<GPreferenceEdge>(
						pOut)) {
					if (eaux == null) {
						eaux = ee;
					}
					if (!eaux.equals(ee)) {
						g.removeEdge(ee);
					}
				}

			}
		}
	}

	private GPreferenceEdge findCycle() {
		colorMap = new HashMap<ITuple, Color>();
		// initialize all nodes with white
		for (ITuple node : g.vertexSet()) {
			colorMap.put(node, Color.white);
		}

		for (ITuple node : g.vertexSet()) {
			if (colorMap.get(node).equals(Color.white)) {
				GPreferenceEdge e = visit(node);
				if (e != null) {
					return e;
				}
			}
		}
		return null;
	}

	private GPreferenceEdge visit(ITuple node) {
		colorMap.put(node, Color.grey);
		Set<GPreferenceEdge> outgoingEdges = g
				.outgoingEdgesOf(node);
		for (GPreferenceEdge edge : outgoingEdges) {
			ITuple outNode = g.getEdgeTarget(edge);
			if (colorMap.get(outNode).equals(Color.grey)) {
				return edge;
			} else if (colorMap.get(outNode).equals(Color.white)) {
				GPreferenceEdge e = visit(outNode);
				if (e != null) {
					return e;
				}
			}
		}
		colorMap.put(node, Color.black);
		return null;
	}

}
