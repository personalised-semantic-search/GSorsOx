package cs.ox.ac.uk.experiments.toit2014;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.deri.iris.EvaluationException;
import org.deri.iris.Expressivity;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.queryrewriting.IQueryRewriter;
import org.deri.iris.compiler.Parser;
import org.deri.iris.queryrewriting.DepGraphUtils;
import org.deri.iris.queryrewriting.NDMRewriter;
import org.deri.iris.queryrewriting.ParallelRewriter;
import org.deri.iris.queryrewriting.RewritingUtils;
import org.deri.iris.queryrewriting.caching.CacheManager;
import org.deri.iris.queryrewriting.configuration.DecompositionStrategy;
import org.deri.iris.queryrewriting.configuration.NCCheck;
import org.deri.iris.queryrewriting.configuration.RewritingLanguage;
import org.deri.iris.queryrewriting.configuration.SubCheckStrategy;
import org.deri.iris.rules.IRuleSafetyProcessor;
import org.deri.iris.rules.safety.StandardRuleSafetyProcessor;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.StorageManager;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.Sets;

import cs.ox.ac.uk.experiments.reader.JsonHelper;
import cs.ox.ac.uk.gsors.AggregateStrategy;
import cs.ox.ac.uk.gsors.AggregationAlgorithm;
import cs.ox.ac.uk.gsors.GCombinationAlgorithms;
import cs.ox.ac.uk.gsors.GPreferencesGraph;
import cs.ox.ac.uk.gsors.User;
import cs.ox.ac.uk.sors.ProbabilisticModel;
import cs.ox.ac.uk.sors.UReportingUtils;
/**
 * Represents the class used to run all the experiments of the paper
 * ************ 
 * Ontology-Based Query Answering with Group Preferences
 * Thomas Lukasiewicz, Maria Vanina Martinez, Gerardo I. Simari and Oana Tifrea-Marciuska
 * In ACM Transactions on Internet Technology (TOIT). Vol.14. No. 4. Pages 25:1-25:24. December, 2014.
 * ***********
 */

public class ExperimentAllTOIT {

	// TODO Auto-generated method stub

	private final Logger LOGGER = Logger.getLogger(ExperimentAllTOIT.class);

	private final String _DEFAULT_SUMMARY_DIR = "results";
	private final String _DEFAULT_INPUT_PATH = "datasets/toit2014_dataset/input/";
	private final static File _WORKING_DIR = FileUtils.getFile(System
			.getProperty("user.dir"));

	public static long distance(int[] a, int[] b) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("Array dimensions disagree");
		}
		int N = a.length;

		int[] ainv = new int[N];
		for (int i = 0; i < N; i++)
			ainv[a[i]] = i;

		Integer[] bnew = new Integer[N];
		for (int i = 0; i < N; i++)
			bnew[i] = ainv[b[i]];

		return 1;
	}

	public static void main(String[] args) {

		Double t = Double.parseDouble(args[0]); // t
		String folderPrefs = String.valueOf(args[1]);
		// folder prefs e.g
		// datasets/toit2014_dataset/input/group_preferences/gr1_sc1
		ExperimentAllTOIT myexp = new ExperimentAllTOIT();
		System.out.println("--------Test: " + folderPrefs);
		List<Integer> ks = new ArrayList<Integer>();
		ks.add(5);
		ks.add(10);
		ks.add(15);
		ks.add(20);
		//List<String> prefsFolders = IOHelper.folders(folderPrefs);
		List<AggregateStrategy> str = new ArrayList<AggregateStrategy>();
		str.add(AggregateStrategy.CSU);
		str.add(AggregateStrategy.Plurality);
		str.add(AggregateStrategy.PluralityMisery);
		try {
			// for (String fp : prefsFolders) {
			// folderPrefs = fp;
			for (AggregateStrategy strategy : str) {
				for (Integer k : ks) {
					String foldername = _WORKING_DIR.getPath() + "/"
							+ "Gilbert" + "/" + k + "/" + folderPrefs;
					IOHelper.createFolder(foldername);
					myexp.runAll(t, k, folderPrefs, "Gilbert", 163, foldername,
							strategy);

				}
			}
			// }
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			StorageManager.close();
		}
	}

	public void runAll(Double t, int k, String folderPreferences, String city,
			int nbBs, String folderResults, AggregateStrategy strategy)
			throws Exception {
		/**
		 * Performs one run with the following parameters int runNumber - id of
		 * this run int numberNodes - number of nodes the select should have int
		 * k - topk answers to be returned boolean runCSU - true if we run
		 * algorithm boolean runPlurality - true if we run algorithm boolean
		 * runPluralityMisery - true if we run algorithm String
		 * folderPreferences String city int nbBs - nb bsuinesses in the city
		 */

		// Configuration.
		final DecompositionStrategy decomposition = DecompositionStrategy.DECOMPOSE;
		final RewritingLanguage rewLang = RewritingLanguage.UCQ;
		final SubCheckStrategy subchkStrategy = SubCheckStrategy.INTRADEC;
		final NCCheck ncCheckStrategy = NCCheck.NONE;

		System.out.println("Decomposition: " + decomposition.name());

		// Parse the program
		final Parser parser = new Parser();
		parser.parse(getStringFile(_DEFAULT_INPUT_PATH + "prefDB-ontology.dtg"));

		// Get the rules
		final List<IRule> rules = parser.getRules();

		// Get the queries
		final List<IQuery> queryHeads = parser.getQueries();
		final Map<IPredicate, IRelation> conf = parser.getDirectives();
		if (!conf.isEmpty()) {
			StorageManager.getInstance();
			StorageManager.configure(conf);
		}

		// Get the TGDs from the set of rules
		final List<IRule> tgds = RewritingUtils.getTGDs(rules, queryHeads);

		final List<IRule> mSBox = RewritingUtils
				.getSBoxRules(rules, queryHeads);
		final IRuleSafetyProcessor ruleProc = new StandardRuleSafetyProcessor();
		ruleProc.process(mSBox);
		final IQueryRewriter ndmRewriter = new NDMRewriter(mSBox);

		// Convert the query bodies in rules
		final List<IRule> bodies = new LinkedList<IRule>(rules);
		bodies.removeAll(tgds);

		final List<IRule> queries = RewritingUtils.getQueries(bodies,
				queryHeads);

		// get the constraints from the set of rules
		final Set<IRule> constraints = RewritingUtils.getConstraints(rules,
				queryHeads);

		final Set<Expressivity> exprs = RewritingUtils.getExpressivity(tgds);
		LOGGER.info("Expressivity: " + exprs.toString());

		if (!exprs.contains(Expressivity.LINEAR)
				&& !exprs.contains(Expressivity.STICKY)) {
			extracted();
		}

		// compute the dependency graph
		LOGGER.debug("Computing position dependencies.");
		System.out.println("Computing position dependencies.");
		long posDepTime = System.currentTimeMillis();
		Map<Pair<IPosition, IPosition>, Set<List<IRule>>> deps = DepGraphUtils
				.computePositionDependencyGraph(tgds);
		posDepTime = System.currentTimeMillis() - posDepTime;
		CacheManager.setupCaching();

		// if linear TGDs, compute the atom coverage graph.
		LOGGER.debug("Computing atom coverage graph.");
		System.out.println("Computing atom coverage graph.");
		long atomCoverGraphTime = System.currentTimeMillis();
		if (exprs.contains(Expressivity.LINEAR)) {
			deps = DepGraphUtils.computeAtomCoverageGraph(deps);
		}
		atomCoverGraphTime = System.currentTimeMillis() - atomCoverGraphTime;

		final ParallelRewriter cnsRewriter = new ParallelRewriter(
				DecompositionStrategy.MONOLITIC, RewritingLanguage.UCQ,
				SubCheckStrategy.NONE, NCCheck.NONE);
		long ncRewTime = System.currentTimeMillis();
		final Set<IRule> rewrittenConstraints = Sets.newHashSet();
		if (!ncCheckStrategy.equals(NCCheck.NONE)) {
			for (final IRule c : constraints) {
				rewrittenConstraints.addAll(cnsRewriter.getRewriting(c, tgds,
						new HashSet<IRule>(), deps, exprs));
			}
		}
		ncRewTime = System.currentTimeMillis() - ncRewTime;

		LOGGER.debug("Finished rewriting constraints.");
		System.out.println("Finished rewriting constraints.");

		// Compute the Rewriting
		final ParallelRewriter rewriter = new ParallelRewriter(decomposition,
				rewLang, subchkStrategy, ncCheckStrategy);

		LOGGER.trace("Starting...");
		// System.out.println("Starting...");

		// creating output files
		final String summaryPrefix;

		summaryPrefix = StringUtils
				.join("Run" + folderPreferences + "-k" + k + "-City" + city
						+ "-T" + t + "-K" + k).replace(".", "_")
				.replace("-", "_").replace("/", "_");

		final File exp2cSummaryFile = FileUtils.getFile(_WORKING_DIR,
				FilenameUtils.separatorsToSystem(_DEFAULT_SUMMARY_DIR),
				StringUtils.join(summaryPrefix, ".csv"));
		final CSVWriter expSummaryWriter = new CSVWriter(new FileWriter(
				exp2cSummaryFile), ',');
		System.out.print(exp2cSummaryFile.getCanonicalPath());
		expSummaryWriter.writeNext(UReportingUtils
				.getSummaryRewritingExpReportHeader());

		// setting parameters for the preference graph
		GPrefParameters parameters = new GPrefParameters(folderPreferences, k,
				city, nbBs);
		IRule q = null;
		if (parameters.getScenario() == Scenario.BREAKFAST_FOOD
				|| parameters.getScenario() == Scenario.LUNCH_FOOD
				|| parameters.getScenario() == Scenario.DINNER_FOOD) {
			q = queries.get(0);
		}
		if (parameters.getScenario() == Scenario.BREAKFAST_CUSINE
				|| parameters.getScenario() == Scenario.LUNCH_CUSINE
				|| parameters.getScenario() == Scenario.DINNER_CUSINE) {
			q = queries.get(1);
		}
		if (parameters.getScenario() == Scenario.BREAKFAST_PLACE
				|| parameters.getScenario() == Scenario.LUNCH_PLACE
				|| parameters.getScenario() == Scenario.DINNER_PLACE) {
			q = queries.get(2);
		}

		CacheManager.setupCaching();

		final String queryPredicate = q.getHead().iterator().next().getAtom()
				.getPredicate().getPredicateSymbol();
		// List<AggregateStrategy> str = new ArrayList<AggregateStrategy>();
		// //str.add(AggregateStrategy.CSU);
		// str.add(AggregateStrategy.Plurality);
		// str.add(AggregateStrategy.PluralityMisery);

		ToitReporter.setScenario(Scenario.BREAKFAST_CUSINE);

		LOGGER.info("Processing query: ".concat(q.toString()));
		System.out.println("Processing query: ".concat(q.toString()));
		long overallTime = System.currentTimeMillis();
		final Set<IRule> rewriting = rewriter.getRewriting(q, tgds,
				rewrittenConstraints, deps, exprs);
		overallTime = System.currentTimeMillis() - overallTime;

		// CONSTRUCT graph
		System.out.println("Read preferences...");
		Map<User, List<user.models.Pair<IPredicate, IPredicate>>> prefs = JsonHelper
				.getGPreferences(parameters.getPrefs(), tgds);
		System.out.println("Construct graph...");
		long constPrefGraphTime = System.currentTimeMillis();
		HelperQuery hp = new HelperQuery(rewriting, parameters, ndmRewriter);
		final GPreferencesGraph prefGraph = hp.constructGraph(prefs);
		constPrefGraphTime = System.currentTimeMillis() - constPrefGraphTime;
		long prefVert = (long) prefGraph.getVertexesSize();
		long prefEdges = (long) prefGraph.getEdgesSize();

		// System.out.println(prefVert + " " + prefEdges);
		// TRANSITIVE graph
		long transitiveClosureTime = System.currentTimeMillis();
		System.out.println("Doing transitive closure");
		prefGraph.transitiveClosure();
		transitiveClosureTime = System.currentTimeMillis()
				- transitiveClosureTime;
		long prefTraVert = (long) prefGraph.getVertexesSize();
		long prefTraEdges = (long) prefGraph.getEdgesSize();
		// System.out.println("Trans" + "Num edges in closed graph: "
		// + prefTraEdges + "-" + "Time for closure: "
		// + transitiveClosureTime);

		System.out.println("calling garbagge collector?");
		System.gc();
		System.out.println("Garbagge collector finished?");

		Map<String, Double> probModel = ProbabilisticModel
				.get(_DEFAULT_INPUT_PATH + "reviews.txt");
		System.out.println("Combining preferences");
		long mergeOperatorTime = System.currentTimeMillis();
		GPreferencesGraph mergedGraph = GCombinationAlgorithms.combPrefsGen(
				prefGraph, probModel, t);
		mergedGraph.transitiveClosure();
		mergeOperatorTime = System.currentTimeMillis() - mergeOperatorTime;
		long prefMergeVert = (long) prefGraph.getVertexesSize();
		long prefMergeEdges = (long) prefGraph.getEdgesSize();

		// compute top-k
		System.out.println("Computing top-k ");

		// for (AggregateStrategy strategy : str) {
		// Setup reporting
		final ToitReporter rep = ToitReporter.getInstance(true);
		ToitReporter.setupReporting();
		ToitReporter.setQuery(queryPredicate);
		ToitReporter.setTest("test" + folderPreferences);
		ToitReporter.setK(k);
		ToitReporter.setGroupID(parameters.getGroupId());
		ToitReporter.setCity(city);
		ToitReporter.setNbUsers(parameters.getMaxNbUsers());
		ToitReporter.setStrategy(strategy);
		rep.setValue(GRewMetric.PREFGRAPH_MERGE_TIME, mergeOperatorTime);
		rep.setValue(GRewMetric.PREFGRAPH_MERGE_SIZE_V, prefMergeVert);
		rep.setValue(GRewMetric.PREFGRAPH_MERGE_SIZE_E, prefMergeEdges);
		rep.setValue(GRewMetric.PREFGRAPH_CONST_TIME, constPrefGraphTime);
		rep.setValue(GRewMetric.PREFGRAPH_CONST_SIZE_V, prefVert);
		rep.setValue(GRewMetric.PREFGRAPH_CONST_SIZE_E, prefEdges);
		rep.setValue(GRewMetric.REW_TIME, overallTime);
		rep.setValue(GRewMetric.REW_SIZE, (long) rewriting.size());
		rep.setValue(GRewMetric.DEPGRAPH_TIME, posDepTime);
		rep.setValue(GRewMetric.REW_CNS_TIME, ncRewTime);
		rep.setValue(GRewMetric.TRANSITIVE_CLOSURE_TIME, transitiveClosureTime);
		rep.setValue(GRewMetric.PREFGRAPH_TRA_SIZE_V, prefTraVert);
		rep.setValue(GRewMetric.PREFGRAPH_TRA_SIZE_E, prefTraEdges);
		// System.out.print("Ruuning----" + rep.toString());
		long topKTime = System.currentTimeMillis();
		// GPreferencesGraph graph = new GPreferencesGraph(mergedGraph);
		List<ITuple> topk = AggregationAlgorithm
				.topK(ToitReporter.getStrategy(), mergedGraph, k, true,
						folderResults);
		IOHelper.writeSolution(folderResults + "/" + strategy.toString()
				+ ".txt", topk);
		topKTime = System.currentTimeMillis() - topKTime;
		rep.setValue(GRewMetric.PREFGRAPH_TOPK_TIME, topKTime);
		int sizeAnswer = (topk != null) ? topk.size() : 0;
		rep.setValue(GRewMetric.ANSWER_SIZE, (long) sizeAnswer);

		mergeOperatorTime = System.currentTimeMillis() - mergeOperatorTime;
		rep.setValue(GRewMetric.PREFGRAPH_TOPK_TIME, topKTime);
		rep.setValue(GRewMetric.PREFGRAPH_TOPK_SIZE_V,
				(long) prefGraph.getVertexesSize());
		rep.setValue(GRewMetric.PREFGRAPH_TOPK_SIZE_E,
				(long) prefGraph.getEdgesSize());
		String[] resultsize = rep.getSummarySizeMetrics();
		String[] resulttime = rep.getSummaryTimeMetrics();
		String[] both = ArrayUtils.addAll(resultsize, resulttime);

		expSummaryWriter.writeNext(both);
		expSummaryWriter.flush();
		// }
		expSummaryWriter.close();

	}

	public String getStringFile(String input) throws IOException {
		// Read the content of the current program
		final FileReader fr = new FileReader(input);
		final StringBuilder sb = new StringBuilder();
		int ch = -1;
		while ((ch = fr.read()) >= 0) {
			sb.append((char) ch);
		}
		final String program = sb.toString();
		fr.close();
		return program;
	}

	private void extracted() {
		throw new EvaluationException(
				"Only Linear and Sticky TGDs are supported for rewriting.");
	}

}
