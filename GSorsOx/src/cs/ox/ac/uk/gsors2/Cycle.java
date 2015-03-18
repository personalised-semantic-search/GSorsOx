package cs.ox.ac.uk.gsors2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deri.iris.api.basics.ITuple;

import cs.ox.ac.uk.gsors.User;

public class Cycle

{

	GPreferencesGraph graph;

	private Map<User, List<List<ITuple>>> cycles;
	private Map<User, Set<ITuple>> marked;
	private Map<User, ArrayDeque<ITuple>> markedStack;
	private Map<User, ArrayDeque<ITuple>> pointStack;
	private Map<User, Map<ITuple, Integer>> vToI;
	private Map<User, Map<ITuple, Set<ITuple>>> removed;
	private List<User> users;

	/**
	 * Create a simple cycle finder with an unspecified graph.
	 */
	public Cycle() {
	}

	/**
	 * Create a simple cycle finder for the specified graph.
	 * 
	 * @param graph
	 *            - the DirectedGraph in which to find cycles.
	 * 
	 * @throws IllegalArgumentException
	 *             if the graph argument is <code>
	 * null</code>.
	 */
	public Cycle(GPreferencesGraph graph, List<User> users) {
		if (graph == null) {
			throw new IllegalArgumentException("Null graph argument.");
		}
		this.graph = graph;
		this.users=users;

	}

	/**
	 * {@inheritDoc}
	 */
	public GPreferencesGraph getGraph() {
		return graph;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setGraph(GPreferencesGraph graph) {
		if (graph == null) {
			throw new IllegalArgumentException("Null graph argument.");
		}
		this.graph = graph;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<List<ITuple>> findSimpleCycles(User u) {
		if (graph == null) {
			throw new IllegalArgumentException("Null graph.");
		}
		initState();

		for (ITuple start : graph.vertexSet()) {
			backtrack(start, start, u);
			while (!markedStack.isEmpty()) {
				marked.remove(markedStack.get(u).pop());
			}
		}

		List<List<ITuple>> result = cycles.get(u);
		clearState();
		return result;
	}

	public boolean hasCycles(User u) {
		if (graph == null) {
			throw new IllegalArgumentException("Null graph.");
		}
		initState();
		boolean cycle = false;
		for (ITuple start : graph.vertexSet()) {
			cycle = hasFoundCycle(start, start, u);
			if (cycle)
				return true;
			while (!markedStack.get(u).isEmpty()) {
				marked.get(u).remove(markedStack.get(u).pop());
			}
		}
		clearState();
		return cycle;
	}

	public boolean hasCycles() {
		for (User u : users) {
			if (hasCycles(u))
				return true;
		}
		return false;
	}

	private boolean hasFoundCycle(ITuple start, ITuple vertex, User u) {
		boolean foundCycle = false;
		pointStack.get(u).push(vertex);
		marked.get(u).add(vertex);
		markedStack.get(u).push(vertex);

		for (GPreferenceEdge currentEdge : graph.getOutGoingEdges(vertex)) {
           
			ITuple currentVertex = currentEdge.getTarget();
			if (getRemoved(vertex, u).contains(currentVertex)) {
				continue;
			}
			int comparison = toI(currentVertex, u).compareTo(toI(start, u));
			if (comparison < 0) {
				getRemoved(vertex, u).add(currentVertex);
			} else if (comparison == 0) {
				return true;
			} else if (!marked.get(u).contains(currentVertex)) {
				boolean gotCycle = hasFoundCycle(start, currentVertex, u);
				foundCycle = foundCycle || gotCycle;
			}
		}

		if (foundCycle) {
			return true;
		}

		pointStack.get(u).pop();
		return foundCycle;
	}

	private boolean backtrack(ITuple start, ITuple vertex, User u) {
		boolean foundCycle = false;
		pointStack.get(u).push(vertex);
		marked.get(u).add(vertex);
		markedStack.get(u).push(vertex);

		for (GPreferenceEdge currentEdge : graph.getOutGoingEdges(vertex)) {
            if (currentEdge.getLabel().equals(u)){
			ITuple currentVertex = currentEdge.getTarget();
			if (getRemoved(vertex, u).contains(currentVertex)) {
				continue;
			}
			int comparison = toI(currentVertex, u).compareTo(toI(start, u));
			if (comparison < 0) {
				getRemoved(vertex, u).add(currentVertex);
			} else if (comparison == 0) {
				foundCycle = true;
				List<ITuple> cycle = new ArrayList<ITuple>();
				Iterator<ITuple> it = pointStack.get(u).descendingIterator();
				ITuple v = null;
				while (it.hasNext()) {
					v = it.next();
					if (start.equals(v)) {
						break;
					}
				}
				cycle.add(start);
				while (it.hasNext()) {
					cycle.add(it.next());
				}
				cycles.get(u).add(cycle);
			} else if (!marked.get(u).contains(currentVertex)) {
				boolean gotCycle = backtrack(start, currentVertex, u);
				foundCycle = foundCycle || gotCycle;
			}
            }
		}

		if (foundCycle) {
			while (!markedStack.get(u).peek().equals(vertex)) {
				marked.remove(markedStack.get(u).pop());
			}
			marked.remove(markedStack.get(u).pop());
		}

		pointStack.get(u).pop();
		return foundCycle;
	}

	private void initState() {
		cycles = new HashMap<User, List<List<ITuple>>>();
		marked = new HashMap<User, Set<ITuple>>();
		markedStack = new HashMap<User, ArrayDeque<ITuple>>();
		pointStack = new HashMap<User, ArrayDeque<ITuple>>();
		vToI = new HashMap<User, Map<ITuple, Integer>>();
		removed = new HashMap<User, Map<ITuple, Set<ITuple>>>();
		for (User user : users) {
			int index = 0;
			User u = new User(user);
			removed.put(u,  new HashMap<ITuple, Set<ITuple>>()) ;
			cycles.put(u, new ArrayList<List<ITuple>>());
			marked.put(u, new HashSet<ITuple>());
			markedStack.put(u, new ArrayDeque<ITuple>());
			pointStack .put(u, new ArrayDeque<ITuple>());
			Map<ITuple, Integer> vv = new HashMap<ITuple, Integer>();
			for (ITuple v : graph.vertexSet()) {
				vv.put(v, index++);
			}
			vToI.put(u, vv);

		}
	}

	private void clearState() {
		cycles = null;
		marked = null;
		markedStack = null;
		pointStack = null;
		vToI = null;
	}

	private Integer toI(ITuple v, User u) {
		return vToI.get(u).get(v);
	}

	private Set<ITuple> getRemoved(ITuple v, User u) {
		// Removed sets typically not all
		// needed, so instantiate lazily.
		Set<ITuple> result = removed.get(u).get(v);
		if (result == null) {
			result = new HashSet<ITuple>();
			removed.get(u).put(v, result);
		}
		return result;
	}
}
