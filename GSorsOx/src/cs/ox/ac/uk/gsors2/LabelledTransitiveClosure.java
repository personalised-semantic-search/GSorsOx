package cs.ox.ac.uk.gsors2;

import java.util.HashSet;
import java.util.Set;

import org.deri.iris.api.basics.ITuple;
import org.jgrapht.graph.DirectedMultigraph;

import user.models.Pair;
import cs.ox.ac.uk.gsors.User;

public class LabelledTransitiveClosure {
	// ~ Static fields/initializers
	// ---------------------------------------------

	/**
	 * Singleton instance.
	 */
	public static final LabelledTransitiveClosure INSTANCE = new LabelledTransitiveClosure();

	// ~ Constructors
	// -----------------------------------------------------------

	/**
	 * Private Constructor.
	 */
	private LabelledTransitiveClosure() {
	}

	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * Computes the transitive closure of the given graph.
	 * 
	 * @param graph
	 *            - Graph to compute transitive closure for.
	 */
	public void closeSimpleDirectedGraph(
			DirectedMultigraph<ITuple, GPreferenceEdge> graph) {
		Set<ITuple> vertexSet = graph.vertexSet();

		Set<Pair<ITuple, User>> newEdgeTargets = new HashSet<Pair<ITuple, User>>();

		// At every iteration of the outer loop, we add a path of length 1
		// between nodes that originally had a path of length 2. In the worst
		// case, we need to make floor(log |V|) + 1 iterations. We stop earlier
		// if there is no change to the output graph.

		int bound = computeBinaryLog(vertexSet.size());
		boolean done = false;
		for (int i = 0; !done && (i < bound); ++i) {
			done = true;
			for (ITuple v1 : vertexSet) {
				newEdgeTargets.clear();
				User u = new User("aux", "aux", 0.4);
				for (GPreferenceEdge v1OutEdge : graph
						.outgoingEdgesOf(v1)) {
					u = v1OutEdge.getLabel();
					ITuple v2 = graph.getEdgeTarget(v1OutEdge);
					for (GPreferenceEdge v2OutEdge : graph
							.outgoingEdgesOf(v2)) {
						if (v2OutEdge.getLabel().equals(u)) {
							ITuple v3 = graph.getEdgeTarget(v2OutEdge);

							if (v1.equals(v3)) {
								// Its a simple graph, so no self loops.
								continue;
							}

							if (graph.getEdge(v1, v3) != null) {
								// There is already an edge from v1 ---> v3,
								// skip;
								continue;
							}

							newEdgeTargets.add(new Pair<ITuple, User>(v3, u));
							done = false;
						}
					}
				}

				for (Pair<ITuple, User> v3 : newEdgeTargets) {
					graph.addEdge(
							v1,
							v3.getElement0(),
							new GPreferenceEdge(v1, v3
									.getElement0(), v3.getElement1()));
				}
			}
		}
	}

	/**
	 * Computes floor(log_2(n)) + 1
	 */
	private int computeBinaryLog(int n) {
		assert n >= 0;

		int result = 0;
		while (n > 0) {
			n >>= 1;
			++result;
		}

		return result;
	}
}
