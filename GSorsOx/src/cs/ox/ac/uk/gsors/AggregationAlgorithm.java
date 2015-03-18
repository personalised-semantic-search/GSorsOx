package cs.ox.ac.uk.gsors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.ITuple;
import org.jgraph.graph.DefaultEdge;

import cs.ox.ac.uk.experiments.toit2014.IOHelper;
import cs.ox.ac.uk.sors.PreferencesGraph;
import cs.ox.ac.uk.sors.TopKAlgorithms;

public class AggregationAlgorithm {

	public static List<ITuple> topK(AggregateStrategy strategy,
			GPreferencesGraph pg, int k, boolean printSolution,
			String folderrint) {
		if (strategy == AggregateStrategy.CSU)
			return collapseToSingleUser(pg, k, folderrint);
		if (strategy == AggregateStrategy.Plurality)
			return pluralityVoting(pg, k, false, folderrint);
		return pluralityVotingMisery(pg, k, printSolution, folderrint);

	}

	public static List<ITuple> collapseToSingleUser(GPreferencesGraph pg, int k, String folderPrint) {
		PreferencesGraph collapsed = pg.collapseEdges();
		collapsed.removeCycles(folderPrint+"collapse_cycle");
		return TopKAlgorithms.getTopK(collapsed, k);
	}

	public static List<ITuple> pluralityVoting(GPreferencesGraph pg, int k,
			boolean printSolution, String folderPrint) {
		pg.removeCycles(folderPrint);
		List<ITuple> result = new ArrayList<ITuple>();
		Map<ITuple, Integer> tupleCount = new HashMap<ITuple, Integer>();
		int index = 0;
		for (PreferencesGraph p : pg) {
			List<ITuple> topk = TopKAlgorithms.getTopK(p, k);
			if (printSolution) {
				IOHelper.writeSolution(folderPrint + "/" + index + ".txt", topk);
			}
			for (ITuple tuple : topk) {
				if (!tupleCount.containsKey(tuple)) {
					tupleCount.put(tuple, 1);
				} else {
					int c = tupleCount.get(tuple).intValue();
					tupleCount.put(tuple, c + 1);
				}
			}
			index++;
		}
		int i = 0;
		while (i <= k && tupleCount.size() > 0) {
			int max = 0;
			List<ITuple> toDelete = new ArrayList<ITuple>();
			for (ITuple e : tupleCount.keySet()) {
				int v = tupleCount.get(e);
				if (v > max) {
					max = v;
				}
			}
			for (ITuple tuple : tupleCount.keySet()) {
				int v = tupleCount.get(tuple);
				if (v == max) {
					i++;
					result.add(tuple);
					toDelete.add(tuple);
					if (i == k)
						return result;

				}
			}
			for (ITuple tuple : toDelete) {
				tupleCount.remove(tuple);
			}
		}
		return result;
	}

	public static List<ITuple> pluralityVotingMisery(GPreferencesGraph pg,
			int k, boolean printSolution, String folderPrint) {
		pg.removeCycles(folderPrint);
		List<ITuple> result = new ArrayList<ITuple>();
		Map<ITuple, Integer> tupleCount = new HashMap<ITuple, Integer>();
		Map<Integer, HashSet<ITuple>> misery = new HashMap<Integer, HashSet<ITuple>>();
		int count = 0;
		for (PreferencesGraph p : pg) {
			misery.put(count, new HashSet<ITuple>());
			List<ITuple> topk = TopKAlgorithms.getTopK(p, k);
			if (printSolution) {
				IOHelper.writeSolution(folderPrint + "/" + count + ".txt", topk);
			}
			misery.get(count).addAll(p.vertexSetNoIncoming());
			for (ITuple tuple : topk) {

				if (!tupleCount.containsKey(tuple)) {
					tupleCount.put(tuple, 1);
				} else {
					int c = tupleCount.get(tuple).intValue();
					tupleCount.put(tuple, c + 1);
				}
			}
			for (DefaultEdge edge : p.edgeSet()) {
				ITuple s = ((ITuple) edge.getSource());
				if (misery.get(count).contains(s)) {
					misery.get(count).remove(s);
				}
			}
			count++;
		}
		for (Integer j : misery.keySet()) {
			for (ITuple t : misery.get(j))
				tupleCount.remove(t);
		}
		int i = 0;
		while (i <= k && tupleCount.size() > 0) {
			int max = 0;
			List<ITuple> toDelete = new ArrayList<ITuple>();
			for (ITuple e : tupleCount.keySet()) {
				int v = tupleCount.get(e);
				if (v > max) {
					max = v;
				}
			}
			for (ITuple tuple : tupleCount.keySet()) {
				int v = tupleCount.get(tuple);
				if (v == max) {
					i++;
					result.add(tuple);
					toDelete.add(tuple);
					if (i == k)
						return result;
				}
			}
			for (ITuple tuple : toDelete) {
				tupleCount.remove(tuple);
			}
		}
		return result;
	}
}
