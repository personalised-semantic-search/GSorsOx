package cs.ox.ac.uk.gsors2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.factory.IBasicFactory;
import org.deri.iris.api.factory.ITermFactory;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.basics.BasicFactory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.RelationFactory;
import org.deri.iris.terms.TermFactory;

import cs.ox.ac.uk.gsors.AggregateStrategy;
import cs.ox.ac.uk.gsors.User;

public class GTopKAlgorithms {

	public static IRelation getTopK(GPreferencesGraph preferenceModel, int k,  AggregateStrategy strategy)  {
			if (strategy == AggregateStrategy.CSU) {
				return collapseToSingleUser(preferenceModel,k);
			}
			if (strategy == AggregateStrategy.Plurality) {
				return pluralityVoting(preferenceModel,k);
			}
			if (strategy == AggregateStrategy.PluralityMisery) {
				return pluralityVotingMisery(preferenceModel,k);
			}
			return null;
		}

	static void collapseToSingleUser(GPreferencesGraph preferenceModel ) {
		Set<GPreferenceEdge> edgeSet = preferenceModel.edgeSet();
		for (GPreferenceEdge edge : new HashSet<GPreferenceEdge>(
				edgeSet)) {
			Set<GPreferenceEdge> pIn = preferenceModel.getAllEdges(
					edge.getSource(), edge.getTarget());
			Set<GPreferenceEdge> pOut = preferenceModel.getAllEdges(
					edge.getTarget(), edge.getSource());
			if (pIn.size() == pOut.size()) {
				preferenceModel.removeAllEdges(edge.getTarget(), edge.getSource());
				preferenceModel.removeAllEdges(edge.getSource(), edge.getTarget());

			} else if (pIn.size() > pOut.size()) {
				preferenceModel.removeAllEdges(edge.getTarget(), edge.getSource());
				GPreferenceEdge eaux = null;
				for (GPreferenceEdge ee : new HashSet<GPreferenceEdge>(
						pIn)) {
					if (eaux == null) {
						eaux = ee;
					}
					if (!eaux.equals(ee)) {
						preferenceModel.removeEdge(ee);
					}
				}

			} else {
				preferenceModel.removeAllEdges(edge.getSource(), edge.getTarget());
				GPreferenceEdge eaux = null;
				for (GPreferenceEdge ee : new HashSet<GPreferenceEdge>(
						pOut)) {
					if (eaux == null) {
						eaux = ee;
					}
					if (!eaux.equals(ee)) {
						preferenceModel.removeEdge(ee);
					}
				}

			}
		}
	}

	static  IRelation collapseToSingleUser(GPreferencesGraph preferenceModel, int k)  {
		final IRelationFactory rf = new RelationFactory();
		final IRelation result = rf.createRelation();
		final IBasicFactory bf = BasicFactory.getInstance();
		final ITermFactory tf = TermFactory.getInstance();
		collapseToSingleUser(preferenceModel);
		preferenceModel.removeCycles();
		int i = 1;
		while (i <= k) {
			Set<ITuple> vertToDelete = new HashSet<ITuple>();
			for (final ITuple node : new HashSet<ITuple>(preferenceModel.vertexSet())) {
				if (preferenceModel.noPredecesors(node)) {
					final List<ITerm> terms = new ArrayList<ITerm>();
					for (int j = 0; j < node.size(); j++) {
						terms.add(tf.createString(node.get(j).toString()));
					}
					result.add(bf.createTuple(terms));
					vertToDelete.add(node);
				}
			}
			preferenceModel.removeAllVertices(vertToDelete);
			i++;
		}
		return result;
	}

	static IRelation pluralityVoting(GPreferencesGraph preferenceModel,int k)  {
		final IRelationFactory rf = new RelationFactory();
		final IRelation result = rf.createRelation();
		final IBasicFactory bf = BasicFactory.getInstance();
		final ITermFactory tf = TermFactory.getInstance();
		int i = 1;
		Map<User, Set<String>> topkUser = new HashMap<User, Set<String>>();
		Map<String, Integer> tupleCount = new HashMap<String, Integer>();

		while (i <= k) {
			Map<User, Set<String>> top1 = new HashMap<User, Set<String>>();
			for (GPreferenceEdge edge : preferenceModel.edgeSet()) {
				if (!top1.containsKey(edge.getLabel())) {
					top1.put(edge.getLabel(), new HashSet<String>());
					for (ITuple t : preferenceModel.vertexSet()) {
						top1.get(edge.getLabel()).add(t.toString());
					}
				}
				if (!topkUser.containsKey(edge.getLabel())) {
					topkUser.put(edge.getLabel(), new HashSet<String>());
				}
				if (!topkUser.get(edge.getLabel()).contains(
						edge.getSource().toString())) {
					if (top1.get(edge.getLabel()).contains(
							edge.getTarget().toString())) {
						top1.get(edge.getLabel()).remove(
								edge.getTarget().toString());
					}
				}
			}
			for (User user : top1.keySet()) {
				if (!topkUser.containsKey(user)) {
					topkUser.put(user, new HashSet<String>());
				}
				topkUser.get(user).addAll(top1.get(user));
			}
			i++;
		}
		for (User user : topkUser.keySet()) {
			for (String node : topkUser.get(user)) {
				if (!tupleCount.containsKey(node)) {
					tupleCount.put(node, 1);
				} else {
					int c = tupleCount.get(node).intValue();
					tupleCount.put(node, c + 1);
				}
			}
		}
		final Set<Map.Entry<String, Integer>> entry = tupleCount.entrySet();
		int max = 0;
		for (Entry<String, Integer> e : entry) {
			if (e.getValue() > max) {
				max = e.getValue();
			}
		}
		for (Map.Entry<String, Integer> e : entry) {
			if (e.getValue() == max) {
				final List<ITerm> terms = new ArrayList<ITerm>();
				terms.add(tf.createString(e.getKey().replace("('", "")
						.replace("')", "")));
				result.add(bf.createTuple(terms));
			}

		}
		// make voting
		return result;
	}

	static IRelation pluralityVotingMisery(GPreferencesGraph preferenceModel,int k)   {
		final IRelationFactory rf = new RelationFactory();
		final IRelation result = rf.createRelation();
		final IBasicFactory bf = BasicFactory.getInstance();
		final ITermFactory tf = TermFactory.getInstance();
		int i = 1;
		Map<User, Set<String>> topkUser = new HashMap<User, Set<String>>();
		Map<User, Set<String>> miseryUser = new HashMap<User, Set<String>>();
		Map<String, Integer> tupleCount = new HashMap<String, Integer>();
		while (i <= k) {
			if (k == 1) {
				miseryUser = new HashMap<User, Set<String>>();

			}
			Map<User, Set<String>> top1 = new HashMap<User, Set<String>>();
			for (GPreferenceEdge edge : preferenceModel.edgeSet()) {
				if (!top1.containsKey(edge.getLabel())) {
					top1.put(edge.getLabel(), new HashSet<String>());
					for (ITuple t : preferenceModel.vertexSet()) {
						top1.get(edge.getLabel()).add(t.toString());
					}
				}
				if (k == 1) {
					if (!miseryUser.containsKey(edge.getLabel())) {
						miseryUser.put(edge.getLabel(), new HashSet<String>());
						for (ITuple t : preferenceModel.vertexSet()) {
							miseryUser.get(edge.getLabel()).add(t.toString());
						}
					}
					if (miseryUser.get(edge.getLabel()).contains(
							edge.getSource().toString())) {
						miseryUser.get(edge.getLabel()).remove(
								edge.getSource().toString());

					}
				}
				if (!topkUser.containsKey(edge.getLabel())) {
					topkUser.put(edge.getLabel(), new HashSet<String>());
				}
				if (!topkUser.get(edge.getLabel()).contains(
						edge.getSource().toString())) {
					if (top1.get(edge.getLabel()).contains(
							edge.getTarget().toString())) {
						top1.get(edge.getLabel()).remove(
								edge.getTarget().toString());
					}
				}

			}

			for (User user : top1.keySet()) {
				if (!topkUser.containsKey(user)) {
					topkUser.put(user, new HashSet<String>());
				}
				topkUser.get(user).addAll(top1.get(user));
			}
			i++;
		}

		int max = 0;
		Set<String> misery = new HashSet<String>();

		for (User user : miseryUser.keySet()) {
			for (String string : miseryUser.get(user)) {
				if (!topkUser.get(user).contains(string)) {
					misery.add(string);
				}
			}
		}
		for (User user : topkUser.keySet()) {
			for (String node : topkUser.get(user)) {
				if (!misery.contains(node)) {
					if (!tupleCount.containsKey(node)) {
						tupleCount.put(node, 1);
					} else {
						int c = tupleCount.get(node).intValue();
						tupleCount.put(node, c + 1);
					}
				}
			}
		}
		final Set<Map.Entry<String, Integer>> entry = tupleCount.entrySet();
		for (Entry<String, Integer> e : entry) {
			if (e.getValue() > max) {
				max = e.getValue();
			}
		}
		for (Map.Entry<String, Integer> e : entry) {
			if (e.getValue() == max) {
				final List<ITerm> terms = new ArrayList<ITerm>();
				terms.add(tf.createString(e.getKey().replace("('", "")
						.replace("')", "")));
				result.add(bf.createTuple(terms));
			}

		}
		return result;
	}


	
}
