package cs.ox.ac.uk.experiments.toit2014;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.queryrewriting.IQueryRewriter;
import org.deri.iris.queryrewriting.SQLRewriter;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.RelationFactory;
import org.deri.iris.storage.StorageManager;

import cs.ox.ac.uk.gsors.GPreferencesGraph;
import cs.ox.ac.uk.gsors.User;
import cs.ox.ac.uk.sors.PreferencesGraph;

public class HelperQuery {
	public Map<IPredicate, IRelation> mapQueryResult;
	public IRelation allResults;

	public HelperQuery(Set<IRule> rewriting, GPrefParameters parameters,
			IQueryRewriter ndmRewriter) {
		final IRelationFactory rf = new RelationFactory();
		final IRelation result = rf.createRelation();
		// rewFW.write("/// Rewritten Program ///\n");
		final Set<ILiteral> newHeads = new HashSet<ILiteral>();
		Map<IPredicate, IRelation> res = new HashMap<IPredicate, IRelation>();
		for (final IRule qr : rewriting) {
			newHeads.add(qr.getHead().iterator().next());
			final Set<IRule> sboxRewriting = new LinkedHashSet<IRule>();

			Set<IRule> rrules = ndmRewriter.getRewriting(qr);
			sboxRewriting.addAll(rrules);
			final SQLRewriter sqlRewriter = new SQLRewriter(sboxRewriting);
			try {
				long duration = -System.nanoTime();
				final List<String> ucqSQLRewriting = sqlRewriter
						.getSQLRewritings(parameters.getConstraintsSqlQuery(),
								parameters.getNbNodes(),
								parameters.getStartFromRes());

				duration = ((duration + System.nanoTime()) / 1000000);
				IRelation resultAux = rf.createRelation();
				for (final String qu : ucqSQLRewriting) {
					IRelation r = StorageManager.executeQuery(qu);
					// System.out.println("Q" + qu);
					// System.out.println("AA" + r.size());
					resultAux.addAll(r);

				}

				for (IPredicate predicate : qr.getBodyPredicates()) {
					res.put(predicate, resultAux);
				}

				// System.out.println("S" + resultAux.size());
				for (int i = 0; i < resultAux.size(); i++) {
					if (result.size() == parameters.getNbNodes()) {
						mapQueryResult = res;
						allResults = result;
						return;
					}
					result.add(resultAux.get(i));
					// System.out.println("---" + result.size());

				}
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}
		mapQueryResult = res;
		allResults = result;
	}

	public GPreferencesGraph constructGraph(
			Map<User, List<user.models.Pair<IPredicate, IPredicate>>> prefs) {

		List<PreferencesGraph> pfg = new ArrayList<PreferencesGraph>();
		int u = 0;
		for (User user : prefs.keySet()) {
			// System.out.println("User--" + user+"id"+ u);
			PreferencesGraph prefGraph = new PreferencesGraph();
			for (user.models.Pair<IPredicate, IPredicate> pairPreference : prefs
					.get(user)) {
				IRelation morePrefs = mapQueryResult.get(pairPreference
						.getElement0());
				IRelation lessPrefs = mapQueryResult.get(pairPreference
						.getElement1());
				// System.out.println("Pre " + pairPreference.getElement0() +
				// " "
				// + pairPreference.getElement1() + " " + morePrefs.size()
				// + " " + lessPrefs.size());
				for (int j = 0; j < morePrefs.size(); j++) {
					ITuple el1 = morePrefs.get(j);
					if (!lessPrefs.contains(el1)) {
						for (int i = 0; i < lessPrefs.size(); i++) {
							ITuple el2 = lessPrefs.get(i);
							prefGraph.addPreference(el1, el2);
						}
					}
				}
			}
			for (int i = 0; i < allResults.size(); i++) {
				ITuple v = allResults.get(i);
				prefGraph.addVertex(v);

			}
			pfg.add(prefGraph);
			// System.out.println("Prefs" + user + " "
			// + prefGraph.vertexSet().size() + " "
			// + prefGraph.edgeSet().size());
			u++;
		}
		// System.out.print("Prefs" + pfg.size());
		return new GPreferencesGraph(pfg);
	}

}
