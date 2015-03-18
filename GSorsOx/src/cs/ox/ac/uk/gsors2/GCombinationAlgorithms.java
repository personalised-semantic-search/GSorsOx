package cs.ox.ac.uk.gsors2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.ITuple;

public class GCombinationAlgorithms {

	public static <T extends Comparable<? super T>> List<T> asSortedListDesc(
			Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list, Collections.reverseOrder());
		return list;
	}

	public static <T extends Comparable<? super T>> List<T> asSortedListAsc(
			Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}

	public static GPreferencesGraph combPrefsGen(GPreferencesGraph pg,
			Map<String, Double> probabilisticModel) {
		GPreferencesGraph preferenceModel = new GPreferencesGraph(pg);
		for (GPreferenceEdge e : new HashSet<GPreferenceEdge>(
				pg.edgeSet())) {
			if (e.getLabel() != null) {
				double target = probabilisticModel.get(((ITuple) e.getTarget())
						.get(0).toString().replace("'", ""));
				double source = probabilisticModel.get(((ITuple) e.getSource())
						.get(0).toString().replace("'", ""));

				if (target - source > e.getLabel().getT())// 0.7
				{
				
					pg.addPreference(e.getTarget(), e.getSource(), e.getLabel());
					
					pg.removePreference(e.getTarget(), e.getSource(), e.getLabel());	
					// if (preferenceModel.findVertexesForCycle((ITuple)e.getTarget())) {
					// //if (preferenceModel.findVertexesForCycle().size() > 0) {
					// // System.out.println("Remove (cycle) this and added inverse "
					// // + ((ITuple) e.getSource()).get(0).toString()
					// // .replace("'", "")
					// // + " "
					// // + ((ITuple) e.getTarget()).get(0).toString()
					// // .replace("'", ""));
					// //System.out.println("Cycle");
					// preferenceModel.removePreference((ITuple) e.getTarget(),
					// (ITuple) e.getSource());
					// preferenceModel.addPreference((ITuple) e.getSource(),
					// (ITuple) e.getTarget());
					// }
					// }
					// //count++;
					//

				}
			} else {
				System.out.print(e.toString());
			}

			// c.closeSimpleDirectedGraph(preferenceModel.g);
			return preferenceModel;

		}
	
		return preferenceModel;
	}

}
