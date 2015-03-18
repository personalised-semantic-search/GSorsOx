/*
 * IRIS+/- Engine:
 * An extensible rule inference system for Datalog with extensions.
 * 
 * Copyright (C) 2009 ICT Institute - Dipartimento di Elettronica e Informazione (DEI), 
 * Politecnico di Milano, Via Ponzio 34/5, 20133 Milan, Italy.
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
package cs.ox.ac.uk.gsors;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.deri.iris.EvaluationException;
import org.deri.iris.Expressivity;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.queryrewriting.IQueryRewriter;
import org.deri.iris.compiler.Parser;
import org.deri.iris.factory.Factory;
import org.deri.iris.queryrewriting.DepGraphUtils;
import org.deri.iris.queryrewriting.NDMRewriter;
import org.deri.iris.queryrewriting.ParallelRewriter;
import org.deri.iris.queryrewriting.RewritingUtils;
import org.deri.iris.queryrewriting.SQLRewriter;
import org.deri.iris.queryrewriting.caching.CacheManager;
import org.deri.iris.queryrewriting.configuration.DecompositionStrategy;
import org.deri.iris.queryrewriting.configuration.NCCheck;
import org.deri.iris.queryrewriting.configuration.RewritingLanguage;
import org.deri.iris.queryrewriting.configuration.SubCheckStrategy;
import org.deri.iris.rules.IRuleSafetyProcessor;
import org.deri.iris.rules.safety.StandardRuleSafetyProcessor;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.RelationFactory;
import org.deri.iris.storage.StorageManager;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.Sets;

import cs.ox.ac.uk.experiments.reader.JsonHelper;
import cs.ox.ac.uk.experiments.toit2014.GPrefParameters;
import cs.ox.ac.uk.experiments.toit2014.GReportingUtils;
import cs.ox.ac.uk.experiments.toit2014.GRewMetric;
import cs.ox.ac.uk.experiments.toit2014.Scenario;
import cs.ox.ac.uk.experiments.toit2014.ToitReporter;
import cs.ox.ac.uk.gsors2.GPreferenceEdge;
import cs.ox.ac.uk.gsors2.GTopKAlgorithms;


public class GroupPreferencesTest1 extends TestCase {

	private final Logger LOGGER = Logger.getLogger(GroupPreferencesTest1.class);

	private final String _DEFAULT_OUTPUT_PATH = "examples/group_test/output/";
	private final String _DEFAULT_SUMMARY_DIR = "summary";
	private final String _DEFAULT_INPUT_PATH = "examples/group_test/input/";
	private final File _WORKING_DIR = FileUtils.getFile(System
			.getProperty("user.dir"));
	private final DateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'-'HH:mm:ss");

	static {
		// Load the logging configuration
		PropertyConfigurator.configure("config/logging.properties");
	}

	public static Test suite() {

		return new TestSuite(GroupPreferencesTest1.class,
				GroupPreferencesTest1.class.getSimpleName());
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

	public void testFORewriting() throws Exception {

		// Configuration.
		final DecompositionStrategy decomposition = DecompositionStrategy.DECOMPOSE;
		final RewritingLanguage rewLang = RewritingLanguage.UCQ;
		final SubCheckStrategy subchkStrategy = SubCheckStrategy.INTRADEC;
		final NCCheck ncCheckStrategy = NCCheck.NONE;

		LOGGER.info("Decomposition: " + decomposition.name());
		LOGGER.info("Rewriting Language: " + rewLang.name());
		LOGGER.info("Subsumption Check Strategy: " + subchkStrategy.name());
		LOGGER.info("Negative Constraints Check Strategy "
				+ ncCheckStrategy.name());

		final File testSuiteFile = FileUtils.getFile(_WORKING_DIR,
				FilenameUtils.separatorsToSystem(_DEFAULT_INPUT_PATH),
				"test-cases1.txt");

		final List<String> tests = IOUtils.readLines(new FileReader(
				testSuiteFile));

		final String creationDate = dateFormat.format(new Date());

		// Parse the program
		final Parser parser = new Parser();
		parser.parse(getStringFile(_DEFAULT_INPUT_PATH+"prefDB-ontology.dtg"));

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
		final IRelationFactory rf = new RelationFactory();

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
		// long depGraphMem = MonitoringUtils.getHeapUsage();
		long posDepTime = System.currentTimeMillis();
		Map<Pair<IPosition, IPosition>, Set<List<IRule>>> deps = DepGraphUtils
				.computePositionDependencyGraph(tgds);
		posDepTime = System.currentTimeMillis() - posDepTime;
		// depGraphMem = depGraphMem - MonitoringUtils.getHeapUsage();
		// Setup caching
		CacheManager.setupCaching();

		// if linear TGDs, compute the atom coverage graph.
		LOGGER.debug("Computing atom coverage graph.");
		long atomCoverGraphTime = System.currentTimeMillis();
		if (exprs.contains(Expressivity.LINEAR)) {
			deps = DepGraphUtils.computeAtomCoverageGraph(deps);
		}
		atomCoverGraphTime = System.currentTimeMillis() - atomCoverGraphTime;
		// depGraphMem = MonitoringUtils.getHeapUsage() - depGraphMem;

		// rewriting constraints
		// long ncRewMem = MonitoringUtils.getHeapUsage();
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
		// ncRewMem = ncRewMem - MonitoringUtils.getHeapUsage();
		LOGGER.debug("Finished rewriting constraints.");

		// Compute the Rewriting
		final ParallelRewriter rewriter = new ParallelRewriter(decomposition,
				rewLang, subchkStrategy, ncCheckStrategy);

		Map<String, Integer> cities = new HashMap<String, Integer>();
//		cities.put("Peoria", 109);
//		 cities.put("Gilbert", 163);
//		 cities.put("Glendale", 242);
//		 cities.put("Chandler", 349);
		cities.put("Tempe", 465);
		//cities.put("Scottsdale", 780);
		// cities.put("Phoenix", 1683);
		List<Integer> ks = new ArrayList<Integer>();
		ks.add(1);
		ks.add(2);
		ks.add(3);

		List<AggregateStrategy> str = new ArrayList<AggregateStrategy>();
		str.add(AggregateStrategy.CSU);
		str.add(AggregateStrategy.Plurality);
		str.add(AggregateStrategy.PluralityMisery);

		for (AggregateStrategy strategyQA : str) {

			final String summaryPrefix = StringUtils.join(creationDate, "-",
					strategyQA.toString());

			final File sizeSummaryFile = FileUtils.getFile(
					_WORKING_DIR,
					FilenameUtils.separatorsToSystem(_DEFAULT_OUTPUT_PATH + "/"
							+ strategyQA.toString()),
					FilenameUtils.separatorsToSystem(_DEFAULT_SUMMARY_DIR),
					StringUtils.join(summaryPrefix, "-", "size-summary.csv"));
			final CSVWriter sizeSummaryWriter = new CSVWriter(new FileWriter(
					sizeSummaryFile), ',');

			final File timeSummaryFile = FileUtils.getFile(
					_WORKING_DIR,
					FilenameUtils.separatorsToSystem(_DEFAULT_OUTPUT_PATH + "/"
							+ strategyQA.toString()),
					FilenameUtils.separatorsToSystem(_DEFAULT_SUMMARY_DIR),
					StringUtils.join(summaryPrefix, "-", "time-summary.csv"));
			final CSVWriter timeSummaryWriter = new CSVWriter(new FileWriter(
					timeSummaryFile), ',');

			// final File cacheSummaryFile = FileUtils.getFile(
			// _WORKING_DIR,
			// FilenameUtils.separatorsToSystem(_DEFAULT_OUTPUT_PATH + "/"
			// + strategyQA.toString()),
			// FilenameUtils.separatorsToSystem(_DEFAULT_SUMMARY_DIR),
			// StringUtils.join(summaryPrefix, "-", "cache-summary.csv"));
			// final CSVWriter cacheSummaryWriter = new CSVWriter(new
			// FileWriter(
			// cacheSummaryFile), ',');
			//
			// final File memorySummaryFile = FileUtils.getFile(
			// _WORKING_DIR,
			// FilenameUtils.separatorsToSystem(_DEFAULT_OUTPUT_PATH + "/"
			// + strategyQA.toString()),
			// FilenameUtils.separatorsToSystem(_DEFAULT_SUMMARY_DIR),
			// StringUtils.join(summaryPrefix, "-", "memory-summary.csv"));
			// final CSVWriter memorySummaryWriter = new CSVWriter(new
			// FileWriter(
			// memorySummaryFile), ',');

			sizeSummaryWriter.writeNext(GReportingUtils
					.getSummaryRewritingSizeReportHeader());
			timeSummaryWriter.writeNext(GReportingUtils
					.getSummaryRewritingTimeReportHeader());
			// cacheSummaryWriter.writeNext(GReportingUtils
			// .getSummaryCachingReportHeader());
			// memorySummaryWriter.writeNext(GReportingUtils
			// .getSummaryMemoryReportHeader());
			for (Integer k : ks) {
				for (String city : cities.keySet()) {
					for (int con = 0; con < 1; con++) {
						LOGGER.info("con-city-k: " + con + "-" + city + "-" + k
								+ "-" + strategyQA.toString());
						// top k for each preferences
						for (final String testName : tests) {
							// Create a buffer for the output
							final IRelation result = rf.createRelation();
							GPrefParameters parameters = new GPrefParameters(
									testName, k, city, cities.get(city));
							// Create the Directory where to store the test
							// results
							// final File outTestDir = FileUtils
							// .getFile(
							// _WORKING_DIR,
							// FilenameUtils
							// .separatorsToSystem(_DEFAULT_OUTPUT_PATH
							// + "/"
							// + strategyQA
							// .toString()
							// + k + city),
							// testName);
							// if (!outTestDir.exists()) {
							// if (outTestDir.mkdirs()) {
							// LOGGER.info("Created output directory: "
							// + testName);
							// } else {
							// LOGGER.fatal("Error creating output directory");
							// }
							// }

							LOGGER.info("Processing file: " + testName);
							// dump the rewritten constraints:
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

							final String queryPredicate = q.getHead()
									.iterator().next().getAtom().getPredicate()
									.getPredicateSymbol();

							// Setup reporting
							final ToitReporter rep = ToitReporter
									.getInstance(true);
							ToitReporter.setupReporting();
							ToitReporter.setQuery(queryPredicate);
							ToitReporter.setTest(testName);
							ToitReporter.setK(parameters.getK());
					//		GroupReporter.setStrategy(parameters.getStrategy());
							ToitReporter.setCity(parameters.getCity());
							ToitReporter.setGroupID(parameters.getGroupId());
							ToitReporter
									.setNbUsers(parameters.getMaxNbUsers());
							ToitReporter.setNbBuss(parameters.getBs());
							ToitReporter.setScenario(parameters.getScenario());

							rep.setValue(GRewMetric.DEPGRAPH_TIME, posDepTime);
							// rep.setValue(GRewMetric.DEPGRAPH_MEM,
							// depGraphMem);
							LOGGER.info("Processing query: ".concat(q
									.toString()));
							// final long rewMem =
							// MonitoringUtils.getHeapUsage();
							final long overallTime = System.currentTimeMillis();
							final Set<IRule> rewriting = rewriter.getRewriting(
									q, tgds, rewrittenConstraints, deps, exprs);
							rep.setValue(GRewMetric.REW_TIME,
									System.currentTimeMillis() - overallTime);
							// rep.setValue(GRewMetric.REW_MEM,
							// MonitoringUtils.getHeapUsage() - rewMem);

							rep.setValue(GRewMetric.REW_SIZE,
									(long) rewriting.size());

							rep.setValue(GRewMetric.REW_CNS_TIME, ncRewTime);
							// rep.setValue(GRewMetric.REW_CNS_MEM, ncRewMem);

							// Other metrics

							// Caching size metrics

							// Create a file to store the rewriting results.

							// File outFile = FileUtils.getFile(outTestDir,
							// queryPredicate.concat("_rew.dtg"));
							// final FileWriter rewFW = new FileWriter(outFile);
							//
							// rewFW.write("/// Query: " + q + "///\n");
							// rewFW.write("/// Ontology: " + testName + "///");
							// rewFW.write("/// Created on: " + creationDate
							// + " ///\n");
							// rewFW.write("/// Rules in the program: "
							// + rules.size() + " ///\n");
							// rewFW.write("/// TGDs in the program: "
							// + tgds.size() + " ///\n");

							// LOGGER.info("Writing the output at: "
							// + outFile.getAbsolutePath());

							// dump metrics for individual queries.
							// rewFW.write(rep.getReport());
							//
							// rewFW.write(IOUtils.LINE_SEPARATOR);
							// rewFW.write(IOUtils.LINE_SEPARATOR);
							//
							// rewFW.write("/// Rewritten Program ///\n");
							final Set<ILiteral> newHeads = new HashSet<ILiteral>();
							Map<IPredicate, IRelation> results = new HashMap<IPredicate, IRelation>();
							for (final IRule qr : rewriting) {
								newHeads.add(qr.getHead().iterator().next());
								// rewFW.write(qr + "\n");

								final Set<IRule> sboxRewriting = new LinkedHashSet<IRule>();

								Set<IRule> rrules = ndmRewriter
										.getRewriting(qr);
								sboxRewriting.addAll(rrules);

								// Produce the SQL rewriting for each query in
								// the
								// program
								final SQLRewriter sqlRewriter = new SQLRewriter(
										sboxRewriting);

								// rewFW.write("Computing SQL Rewriting");
								try {
									// Get the SQL rewriting as Union of
									// Conjunctive
									// Queries
									long duration = -System.nanoTime();
									final List<String> ucqSQLRewriting = sqlRewriter
											.getSQLRewritings(parameters
													.getConstraintsSqlQuery(),
													parameters.getNbNodes(),
													parameters
															.getStartFromRes());
									
									duration = ((duration + System.nanoTime()) / 1000000);
									IRelation resultAux = rf.createRelation();
									for (final String qu : ucqSQLRewriting) {
										IRelation r = StorageManager
												.executeQuery(qu);

										// LOGGER.info("-Query: " +
										// qu+" "+r.size()+" "+c);
										resultAux.addAll(r);
									}
									for (IPredicate predicate : qr
											.getBodyPredicates()) {
										results.put(predicate, resultAux);
									}
									result.addAll(resultAux);
									// LOGGER.info("-R: " +result.size());
								} catch (final SQLException e) {
									e.printStackTrace();
								}
							}
							// write the result in the output
							// rewFW.write(result.toString());

							// construct the graph
							Map<User, List<user.models.Pair<IPredicate, IPredicate>>> prefs = JsonHelper
									.getGPreferences(parameters.getPrefs(), tgds);
							final cs.ox.ac.uk.gsors2.GPreferencesGraph prefGraph = Factory.GPGRAPH.createPreferencesGraph();
							long constPrefGraphTime = System
									.currentTimeMillis();
//							final long constPrefGraphMem = MonitoringUtils
//									.getHeapUsage();

							for (User user : prefs.keySet()) {
								for (user.models.Pair<IPredicate, IPredicate> pairPreference : prefs
										.get(user)) {
									IRelation morePrefs = results
											.get(pairPreference.getElement0());
									IRelation lessPrefs = results
											.get(pairPreference.getElement1());
									for (int j = 0; j < morePrefs.size(); j++) {
										ITuple el1 = morePrefs.get(j);
										if (!lessPrefs.contains(el1)) {
											for (int i = 0; i < lessPrefs
													.size(); i++) {
												ITuple el2 = lessPrefs.get(i);
												GPreferenceEdge edge = new GPreferenceEdge(
														el1, el2, user);
												prefGraph.addPreference(edge);
											}
										}
									}
								}
							}
							for (int i = 0; i < result.size(); i++) {
								ITuple v = result.get(i);
								prefGraph.addVertex(v);

							}
							// LOGGER.info("-----Size--Graph--: " +
							// result.size()+"--"+prefGraph.getVertexesSize() );
							constPrefGraphTime = System.currentTimeMillis()
									- constPrefGraphTime;
							rep.setValue(GRewMetric.PREFGRAPH_CONST_TIME,
									constPrefGraphTime);
							rep.setValue(GRewMetric.PREFGRAPH_CONST_SIZE_V,
									(long) prefGraph.getVertexesSize());
							rep.setValue(GRewMetric.PREFGRAPH_CONST_SIZE_E,
									(long) prefGraph.getEdgesSize());

							// rep.setValue(GRewMetric.PREFGRAPH_CONST_MEM,
							// MonitoringUtils.getHeapUsage()
							// - constPrefGraphMem);

							long mergeOperatorTime = System.currentTimeMillis();
							// final long mergeProbModel = MonitoringUtils
							// .getHeapUsage();
//							prefGraph
//									.mergeProbabilisticModel(_DEFAULT_INPUT_PATH+"reviews.txt");
							mergeOperatorTime = System.currentTimeMillis()
									- mergeOperatorTime;
							// rep.setValue(GRewMetric.PREFGRAPH_MERGE_MEM,
							// MonitoringUtils.getHeapUsage()
							// - mergeProbModel);
							rep.setValue(GRewMetric.PREFGRAPH_MERGE_SIZE_V,
									(long) prefGraph.getVertexesSize());
							rep.setValue(GRewMetric.PREFGRAPH_MERGE_SIZE_E,
									(long) prefGraph.getEdgesSize());
							rep.setValue(GRewMetric.PREFGRAPH_MERGE_TIME,
									(long) mergeOperatorTime);
							// long topKMem = MonitoringUtils
							// .getHeapUsage();
							long topKTime = System.currentTimeMillis();
							IRelation r = GTopKAlgorithms.getTopK(prefGraph,parameters.getK(),
									strategyQA);
							topKTime = System.currentTimeMillis() - topKTime;
							// rep.setValue(GRewMetric.PREFGRAPH_TOPK_MEM,
							// topKMem-MonitoringUtils
							// .getHeapUsage());
							rep.setValue(GRewMetric.PREFGRAPH_TOPK_TIME,
									topKTime);
							rep.setValue(GRewMetric.PREFGRAPH_TOPK_SIZE_V,
									(long) prefGraph.getVertexesSize());
							rep.setValue(GRewMetric.PREFGRAPH_TOPK_SIZE_E,
									(long) prefGraph.getEdgesSize());
							rep.setValue(GRewMetric.ANSWER_SIZE,
									(long) r.size());
							// rewFW.write("\n");
							// for (final ILiteral h : newHeads) {
							// rewFW.write("?- " + h + ".\n");
							// }
							// rewFW.write("\n");
							// rewFW.flush();
							// rewFW.close();

							// dump summary metrics.
							sizeSummaryWriter.writeNext(rep
									.getSummarySizeMetrics());
							timeSummaryWriter.writeNext(rep
									.getSummaryTimeMetrics());
							// cacheSummaryWriter.writeNext(rep
							// .getSummaryCacheMetrics());
							// memorySummaryWriter.writeNext(rep
							// .getSummaryMemoryMetrics());
							sizeSummaryWriter.flush();
							timeSummaryWriter.flush();
							// cacheSummaryWriter.flush();
							// memorySummaryWriter.flush();

						}

					}

				}
			}
			sizeSummaryWriter.close();
			timeSummaryWriter.close();
			// cacheSummaryWriter.close();
			// memorySummaryWriter.close();

		}
	}

	private void extracted() {
		throw new EvaluationException(
				"Only Linear and Sticky TGDs are supported for rewriting.");
	}
}
