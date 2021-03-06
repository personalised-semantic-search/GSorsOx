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

// TODO: implement equals, hashCode an clone.

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.iris.api.basics.ITuple;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graphs;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DirectedMultigraph;

import user.models.Pair;

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
public class PreferencesGraph implements Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2302977473188237968L;
	private final Logger LOGGER = Logger.getLogger(PreferencesGraph.class);

	private enum Color {
		white, grey, black
	};

	private Map<ITuple, Color> colorMap;

	public DirectedMultigraph<ITuple, DefaultEdge> g = new DirectedMultigraph<ITuple, DefaultEdge>(
			new PreferenceEdgeFactory());

	/**
	 * Constructs an empty graph object.
	 */
	public PreferencesGraph() {
		g = new DirectedMultigraph<ITuple, DefaultEdge>(
				new PreferenceEdgeFactory());
	}
	
	public void transitiveClosure(){
		TransitiveClosure c = TransitiveClosure.INSTANCE;
		c.closeSimpleDirectedGraph(g);
	}

	/**
	 * Constructs a new graph with a given json.
	 * 
	 * @param object
	 *            the rules with which to initialize the graph
	 */
	PreferencesGraph(final List<Pair<ITuple, ITuple>> prefs) {
		if (prefs != null) {
			for (Pair<ITuple, ITuple> pre : prefs) {
				this.addPreference(pre.getElement0(), pre.getElement1());

			}
		}
	}

	public boolean hasPreference(ITuple s, ITuple t) {
		return g.containsEdge(s, t);
	}

	public Set<ITuple> vertexSet() {
		return g.vertexSet();
	}
	public Set<ITuple> vertexSetNoIncoming() {
		Set<ITuple> s= new HashSet<ITuple>();
		for (ITuple t: g.vertexSet()){
			if (g.inDegreeOf(t)!=0) s.add(t);
		}
		return s;
	}
	public Set<DefaultEdge> edgeSet() {
		return g.edgeSet();
	}

	public PreferencesGraph(PreferencesGraph copy) {
		if (copy.g != null) {
			this.g = new DirectedMultigraph<ITuple, DefaultEdge>(
					new PreferenceEdgeFactory());
			for (ITuple s : copy.g.vertexSet()) {
				this.g.addVertex(s);
			}
			for (DefaultEdge e : copy.g.edgeSet()) {

				DefaultEdge curEdge = new DefaultEdge();
				curEdge.setSource(e.getSource());
				curEdge.setTarget(e.getTarget());
				if (e.getSource() != null && e.getTarget() != null) {
					this.g.addEdge((ITuple) e.getSource(),
							(ITuple) e.getTarget(), curEdge);
				} else {
					// System.out.println("Null"+e);
				}
			}
		}
	}

	public boolean hasCycles() {
		CycleDetector<ITuple, DefaultEdge> cd = new CycleDetector<ITuple, DefaultEdge>(
				g);
		return cd.detectCycles();
	}

	public Set<ITuple> findVertexesForCycle() {

		StrongConnectivityInspector<ITuple, DefaultEdge> inspector = new StrongConnectivityInspector<ITuple, DefaultEdge>(
				g);

		List<Set<ITuple>> components = inspector.stronglyConnectedSets();

		// A vertex participates in a cycle if either of the following is
		// true: (a) it is in a component whose size is greater than 1
		// or (b) it is a self-loop

		Set<ITuple> set = new HashSet<ITuple>();
		for (Set<ITuple> component : components) {
			if (component.size() > 1) {
				// cycle
				set.addAll(component);
			} else {
				ITuple v = component.iterator().next();
				if (g.containsEdge(v, v)) {
					// self-loop
					set.add(v);
				}
			}
		}
		return set;

	}

	public boolean findVertexesForCycle(ITuple s) {
		CycleDetector det = new CycleDetector(g);
		return det.detectCyclesContainingVertex(s);

	}

	public int getEdgesSize() {

		return g.edgeSet().size();
	}

	public int getVertexesSize() {

		return g.vertexSet().size();
	}

	public Set<DefaultEdge> findEdgesForCycle() {
		final Set<ITuple> cycle = findVertexesForCycle();
		final Set<DefaultEdge> edges = new HashSet<DefaultEdge>();
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

	public void addVertex(final ITuple tuple) {
		if (!g.vertexSet().contains(tuple)) {
			g.addVertex(tuple);
		}
	}

	public boolean isSkiline(ITuple tuple) {
		return (g.inDegreeOf(tuple) < 1 ? true : false);
	}

	public void addVertexes(final Set<ITuple> set) {
		for (ITuple tuple : set) {
			if (!g.vertexSet().contains(tuple)) {
				g.addVertex(tuple);
			}
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
	public void addPreference(final ITuple preffered, final ITuple lessPreffred) {
		if (preffered == null || lessPreffred == null) {
			throw new NullPointerException("The Literals must not be null");
		}
		if (!g.containsVertex(preffered)) {
			g.addVertex(preffered);
		}
		if (!g.containsVertex(lessPreffred)) {
			g.addVertex(lessPreffred);
		}
		final DefaultEdge e = new DefaultEdge();
		e.setSource(preffered);
		e.setTarget(lessPreffred);
		g.addEdge(preffered, lessPreffred, e);

	}

	public void removePreference(final ITuple preffered,
			final ITuple lessPreffred) {
		if (preffered == null || lessPreffred == null)
			throw new NullPointerException("The Literals must not be null");
		g.removeEdge(preffered, lessPreffred);

	}

	public void addPreference(final List<Pair<ITuple, ITuple>> r) {
		if ((r == null) || r.contains(null))
			throw new NullPointerException(
					"The rules must not be, or contain null");
		for (final Pair<ITuple, ITuple> pair : r) {
			addPreference(pair.getElement0(), pair.getElement1());
		}

	}

	public void removeNodes(final Set<ITuple> vertToDelete) {
		if ((vertToDelete == null) || vertToDelete.contains(null))
			throw new NullPointerException(
					"The rules must not be, or contain null");

		this.g.removeAllVertices(vertToDelete);

	}

	public void removePreference(final List<Pair<ITuple, ITuple>> r) {
		if ((r == null) || r.contains(null))
			throw new NullPointerException(
					"The rules must not be, or contain null");
		for (final Pair<ITuple, ITuple> pair : r) {
			removePreference(pair.getElement0(), pair.getElement1());
		}
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
	private Set<ITuple> getDepends(final ITuple p) {
		if (p == null)
			throw new NullPointerException("The Literal must not be null");
		if (!g.containsVertex(p))
			return Collections.EMPTY_SET;

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

	public List<ITuple> getNeighBour(final ITuple p) {
		if (p == null)
			throw new NullPointerException("The Literal must not be null");
		if (!g.containsVertex(p))
			return null;
		return Graphs.successorListOf(g, p);

	}

	/**
	 * <p>
	 * Computes a short description of this object. <b>The format of the
	 * returned string is undocumented and subject to change.</b>.
	 * </p>
	 * <p>
	 * And example return string could be:
	 * </p>
	 * <p>
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * a-&gt;(false)-&gt;b
	 * b-&gt;(true)-&gt;c
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @return the string description
	 */
	@Override
	public String toString() {
		return g.toString();
	}

	public String edgesString() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append('{');
		boolean first = true;
		for (final DefaultEdge t : g.edgeSet() ) {
			if (first) {
				first = false;
			} else {
				buffer.append(", ");
			}
			buffer.append(t.getSource()+"->"+t.getTarget());
		}
		buffer.append("}\n");
		return buffer.toString();
	}
	
	List<DefaultEdge> path(ITuple s, ITuple t) {

		// BreadthFirstIterator<ITuple, DefaultEdge> i = new
		// BreadthFirstIterator<ITuple, DefaultEdge>(
		// g, s);
		// boolean foundTarget = false;
		// while (i.hasNext() && !foundTarget) {
		// ITuple v = i.next();
		// foundTarget = v.equals(t);
		// connectedSet.add(v);
		// }
		DijkstraShortestPath<ITuple, DefaultEdge> ds = new DijkstraShortestPath<ITuple, DefaultEdge>(
				g, s, t);
		return ds.getPath().getEdgeList();

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
	private static class PreferenceEdgeFactory implements
			EdgeFactory<ITuple, DefaultEdge> {

		@Override
		public DefaultEdge createEdge(final ITuple s, final ITuple t) {
			if ((s == null) || (t == null))
				throw new NullPointerException("The vertices must not be null");
			return new DefaultEdge();
		}
	}

	public void removeCycles(String info) {
		if (hasCycles()){
			System.out.println("Cycle-------"+info);
			try {
			    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("cycles.txt", true)));
			    out.println(info);
			    out.close();
			} catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
			LOGGER.info("There is a cycle");
		}
		DefaultEdge edgeC = null;
		for (DefaultEdge edge : new HashSet<DefaultEdge>(g.edgeSet())) {
			Set<DefaultEdge> pIn = g.getAllEdges((ITuple) edge.getSource(),
					(ITuple) edge.getTarget());
			Set<DefaultEdge> pOut = g.getAllEdges((ITuple) edge.getTarget(),
					(ITuple) edge.getSource());
			if (pIn.size() == pOut.size()) {
				g.removeAllEdges((ITuple) edge.getTarget(),
						(ITuple) edge.getSource());
				g.removeAllEdges((ITuple) edge.getSource(),
						(ITuple) edge.getTarget());

			}
		}
		while ((edgeC = findCycle()) != null) {
			g.removeEdge(edgeC);
		}
	}

	public void eliminateDuplicates() {
		Set<DefaultEdge> e = g.edgeSet();
		for (DefaultEdge preferenceEdge : e) {
			Set<DefaultEdge> pe = g.getAllEdges(
					(ITuple) preferenceEdge.getSource(),
					(ITuple) preferenceEdge.getTarget());
			if (pe.size() > 1) {
				g.removeEdge(preferenceEdge);
			}
		}
	}

	private DefaultEdge findCycle() {
		colorMap = new HashMap<ITuple, Color>();
		// initialize all nodes with white
		for (ITuple node : g.vertexSet()) {
			colorMap.put(node, Color.white);
		}

		for (ITuple node : g.vertexSet()) {
			if (colorMap.get(node).equals(Color.white)) {
				DefaultEdge e = visit(node);
				if (e != null) {
					return e;
				}
			}
		}
		return null;
	}

	private DefaultEdge visit(ITuple node) {
		colorMap.put(node, Color.grey);
		Set<DefaultEdge> outgoingEdges = g.outgoingEdgesOf(node);
		for (DefaultEdge edge : outgoingEdges) {
			ITuple outNode = g.getEdgeTarget(edge);
			if (colorMap.get(outNode).equals(Color.grey)) {
				return edge;
			} else if (colorMap.get(outNode).equals(Color.white)) {
				DefaultEdge e = visit(outNode);
				if (e != null) {
					return e;
				}
			}
		}
		colorMap.put(node, Color.black);
		return null;
	}

}
