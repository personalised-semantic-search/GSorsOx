package cs.ox.ac.uk.gsors2;

import java.io.FileNotFoundException;
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

import cs.ox.ac.uk.gsors.User;

public class AggregationAlgorithm {
	public IRelation collapseToSingleUser(GPreferencesGraph pg, int k)
			throws FileNotFoundException {
		final IRelationFactory rf = new RelationFactory();
		final IRelation result = rf.createRelation();
		final IBasicFactory bf = BasicFactory.getInstance();
		final ITermFactory tf = TermFactory.getInstance();
		pg.collapseEdges();
		pg.removeCycles();
		int i = 1;
		while (i <= k) {
			Set<ITuple> vertToDelete = new HashSet<ITuple>();
			for (final ITuple node : new HashSet<ITuple>(pg.vertexSet())) {
				if (pg.noPredecesors(node)) {
					final List<ITerm> terms = new ArrayList<ITerm>();
					for (int j = 0; j < node.size(); j++) {
						terms.add(tf.createString(node.get(j).toString()));
					}
					result.add(bf.createTuple(terms));
					vertToDelete.add(node);
				}
			}
			pg.removeAllVertices(vertToDelete);
			i++;
		}
		return result;
	}

	public IRelation pluralityVoting(GPreferencesGraph pg, int k) throws FileNotFoundException {
		final IRelationFactory rf = new RelationFactory();
		final IRelation result = rf.createRelation();
		final IBasicFactory bf = BasicFactory.getInstance();
		final ITermFactory tf = TermFactory.getInstance();
		int i = 1;
		Map<User, Set<String>> topkUser = new HashMap<User, Set<String>>();
		Map<String, Integer> tupleCount = new HashMap<String, Integer>();

		while (i <= k) {
			Map<User, Set<String>> top1 = new HashMap<User, Set<String>>();
			for (GPreferenceEdge edge : pg.edgeSet()) {
				if (!top1.containsKey(edge.getLabel())) {
					top1.put(edge.getLabel(), new HashSet<String>());
					for (ITuple t : pg.vertexSet()) {
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

	public IRelation pluralityVotingMisery(GPreferencesGraph pg,int k) throws FileNotFoundException {

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
			for (GPreferenceEdge edge :pg.edgeSet()) {
				if (!top1.containsKey(edge.getLabel())) {
					top1.put(edge.getLabel(), new HashSet<String>());
					for (ITuple t : pg.vertexSet()) {
						top1.get(edge.getLabel()).add(t.toString());
					}
				}
				if (k == 1) {
					if (!miseryUser.containsKey(edge.getLabel())) {
						miseryUser.put(edge.getLabel(), new HashSet<String>());
						for (ITuple t : pg.vertexSet()) {
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
