/**
 * 
 */
package cs.ox.ac.uk.experiments.toit2014;



import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.deri.iris.Reporter;

import cs.ox.ac.uk.gsors.AggregateStrategy;

public class ToitReporter {

	private final Logger LOGGER = Logger.getLogger(Reporter.class);

	private static Map<GRewMetric, Long> metrics;
	private static ToitReporter _INSTANCE;
	private static String test;
	private static String query;
	private static String groupID;
	private static Integer nbUsers;
	private static Scenario Scenario;
	private static Integer k;
	private static String City;
	private static int nbBuss;
	private static AggregateStrategy strategy;

	public static ToitReporter getInstance() {
		return getInstance(false);
	}

	public static ToitReporter getInstance(final boolean clean) {
		if ((_INSTANCE == null) || clean) {
			_INSTANCE = new ToitReporter();
			setupReporting();
		}
		return _INSTANCE;
	}

	private ToitReporter() {
		super();
		metrics = new ConcurrentHashMap<GRewMetric, Long>();
	}

	public static void setTest(final String test) {
		ToitReporter.test = test;
	}

	public static void setQuery(final String query) {
		ToitReporter.query = query;
	}

	public String getQuery() {
		if ((query == null) || query.isEmpty()) {
			LOGGER.warn("No value set for the query.");
		}
		return query;
	}

	public String getTest() {
		if ((test == null) || test.isEmpty()) {
			LOGGER.warn("No value set for the test.");
		}
		return test;
	}

	public void setValue(final GRewMetric metric, final Long value) {
		metrics.put(metric, value);
	}

	public void addToValue(final GRewMetric metric, final Long value) {
		metrics.put(metric, metrics.get(metric) + value);
	}

	public void incrementValue(final GRewMetric metric) {
		addToValue(metric, (long) 1);
	}

	public Long getValue(final GRewMetric metric) {
		return metrics.get(metric);
	}

	public static void setupReporting() {

		// Counters

		// _INSTANCE.setValue(GRewMetric.ELIM_ATOM_COUNT, (long) 0);

		// Cachings

		// _INSTANCE.setValue(GRewMetric.COVERING_CACHE_HITS, (long) 0);

		// Timing

		_INSTANCE.setValue(GRewMetric.REW_TIME, (long) 0);

		_INSTANCE.setValue(GRewMetric.REW_CNS_TIME, (long) 0);

		_INSTANCE.setValue(GRewMetric.PREFGRAPH_CONST_TIME, (long) 0);
		_INSTANCE.setValue(GRewMetric.PREFGRAPH_MERGE_TIME, (long) 0);
		// _INSTANCE.setValue(GRewMetric.PREFGRAPH_REMOVE_CYCLE_TIME, (long) 0);
		_INSTANCE.setValue(GRewMetric.PREFGRAPH_TOPK_TIME, (long) 0);

		// Memory
		_INSTANCE.setValue(GRewMetric.REW_MEM, (long) 0);

		_INSTANCE.setValue(GRewMetric.DEPGRAPH_MEM, (long) 0);

		_INSTANCE.setValue(GRewMetric.REW_CNS_MEM, (long) 0);

		_INSTANCE.setValue(GRewMetric.PREFGRAPH_CONST_MEM, (long) 0);
		_INSTANCE.setValue(GRewMetric.PREFGRAPH_MERGE_MEM, (long) 0);
		// _INSTANCE.setValue(GRewMetric.PREFGRAPH_REMOVE_CYCLE_MEM, (long) 0);
		_INSTANCE.setValue(GRewMetric.PREFGRAPH_TOPK_MEM, (long) 0);

		// size
		_INSTANCE.setValue(GRewMetric.PREFGRAPH_CONST_SIZE_E, (long) 0);
		_INSTANCE.setValue(GRewMetric.PREFGRAPH_CONST_SIZE_V, (long) 0);
		_INSTANCE.setValue(GRewMetric.PREFGRAPH_MERGE_SIZE_E, (long) 0);
		_INSTANCE.setValue(GRewMetric.PREFGRAPH_MERGE_SIZE_V, (long) 0);
		// _INSTANCE.setValue(GRewMetric.PREFGRAPH_REMOVE_CYCLE_SIZE_E, (long)
		// 0);
		_INSTANCE.setValue(GRewMetric.PREFGRAPH_TOPK_SIZE_V, (long) 0);

	}

	public String getReport() {
		final StringBuffer sb = new StringBuffer();

		sb.append("/// ---------- METRICS ----------");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// ----- SIZE -----");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # of queries: ");
		sb.append(getValue(GRewMetric.REW_SIZE));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// ----- TIME -----");

		// sb.append(IOUtils.LINE_SEPARATOR);
		// sb.append("/// Total: ");
		// sb.append(getValue(GRewMetric.OVERALL_TIME));
		// sb.append(" msec.");
		// sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Backward rewriting: ");
		sb.append(getValue(GRewMetric.REW_TIME));
		sb.append(" msec.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Dependency graph: ");
		sb.append(getValue(GRewMetric.DEPGRAPH_TIME));
		sb.append(" msec (constant).");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Constraints rewriting time: ");
		sb.append(getValue(GRewMetric.REW_CNS_TIME));
		sb.append(" msec (constant).");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// ----- OTHER -----");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// ----- MEMORY -----");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # Rewriting Memory: ");
		sb.append(getValue(GRewMetric.REW_MEM));
		sb.append(" Kb.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # DepGraph Memory: ");
		sb.append(getValue(GRewMetric.DEPGRAPH_MEM));
		sb.append(" Kb.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # NC rewriting Memory: ");
		sb.append(getValue(GRewMetric.REW_CNS_MEM));
		sb.append(" Kb.");
		sb.append(IOUtils.LINE_SEPARATOR);

		return (sb.toString());
	}

	public String[] getSummaryMemoryMetrics() {
		// test, query,strategy, k, groupID,nbUsers,Scenario,City,nbBuss,rew mem
		// [Kb], depgraph [Kb],prefgraph [Kb] .
		final String[] line = { getTest(), getQuery(),
				getStrategy().toString(), Integer.toString(getK()),
				getGroupID().toString(), Integer.toString(getNbUsers()),
				getScenario().toString(), getCity().toString(),
				Integer.toString(getNbBuss()),
				Long.toString(getValue(GRewMetric.REW_MEM)),
				Long.toString(getValue(GRewMetric.DEPGRAPH_MEM)),
				Long.toString(getValue(GRewMetric.PREFGRAPH_CONST_MEM)),
				Long.toString(getValue(GRewMetric.PREFGRAPH_MERGE_MEM)),
				Long.toString(getValue(GRewMetric.PREFGRAPH_TOPK_MEM)),
		// Long.toString(getValue(GRewMetric.PREFGRAPH_REMOVE_CYCLE_MEM))
		};
		return line;
	}

	public String[] getSummarySizeMetrics() {
		// test, query, strategy, k,groupID,nbUsers,Scenario,City,nbBuss, size
		// [#CQs], prefGraph[#vertices],
		// prefGraph[#edges],prefGraphTRANS[#vertices],
		// prefGraphTRANS[#edges],prefGraphCYCLES[#vertices],
		// prefGraphCYCLES[#edges]
		final String[] line = { getTest(), getQuery(),
				getStrategy().toString(), Integer.toString(getK()),
				getGroupID().toString(), Integer.toString(getNbUsers()),
				getScenario().toString(), getCity().toString(),
				Integer.toString(getNbBuss()),
				Long.toString(getValue(GRewMetric.REW_SIZE)),
				Long.toString(getValue(GRewMetric.PREFGRAPH_CONST_SIZE_V)),
				Long.toString(getValue(GRewMetric.PREFGRAPH_CONST_SIZE_E)),
				Long.toString(getValue(GRewMetric.PREFGRAPH_MERGE_SIZE_V)),
				Long.toString(getValue(GRewMetric.PREFGRAPH_MERGE_SIZE_E)),
				Long.toString(getValue(GRewMetric.PREFGRAPH_TOPK_SIZE_V)),
				Long.toString(getValue(GRewMetric.PREFGRAPH_TOPK_SIZE_E)),
				Long.toString(getValue(GRewMetric.ANSWER_SIZE))
				
		// Long.toString(getValue(GRewMetric.PREFGRAPH_REMOVE_CYCLE_SIZE_V)),
		// Long.toString(getValue(GRewMetric.PREFGRAPH_REMOVE_CYCLE_SIZE_E))
		};
		return line;
	}

	public String[] getSummaryTimeMetrics() {
		// test, query, strategy, k,groupID,nbUsers,Scenario,City,nbBuss,
		// depgraph [msec], total rewriting [msec],
		// construct preference graph time[msec]
		final String[] line = { getTest(), getQuery(),
				getStrategy().toString(), Integer.toString(getK()),
				getGroupID().toString(), Integer.toString(getNbUsers()),
				getScenario().toString(),
				getCity().toString(),
				Integer.toString(getNbBuss()),
				Long.toString(getValue(GRewMetric.DEPGRAPH_TIME)),
				// Long.toString(getValue(GRewMetric.OVERALL_TIME)),
				Long.toString(getValue(GRewMetric.REW_TIME)),
				Long.toString(getValue(GRewMetric.PREFGRAPH_CONST_TIME)),
				Long.toString(getValue(GRewMetric.PREFGRAPH_MERGE_TIME)),
				// Long.toString(getValue(GRewMetric.PREFGRAPH_REMOVE_CYCLE_TIME)),
				Long.toString(getValue(GRewMetric.PREFGRAPH_TOPK_TIME)), };
		return line;
	}

	public String[] getSummaryCacheMetrics() {
		// ontology, query, strategy,k,groupID,nbUsers,Scenario,City,nbBuss,

		final String[] line = { getTest(), getQuery(),
				getStrategy().toString(), Integer.toString(getK()), };
		return line;
	}

	/**
	 * @return the k
	 */
	public static int getK() {
		return k;
	}

	/**
	 * @param k
	 *            the k to set
	 */
	public static void setK(int k) {
		ToitReporter.k = k;
	}

	/**
	 * @return the strategy
	 */
	public static AggregateStrategy getStrategy() {
		return strategy;
	}

	/**
	 * @param strategy
	 *            the strategy to set
	 */
	public static void setStrategy(AggregateStrategy strategy) {
		ToitReporter.strategy = strategy;
	}

	/**
	 * @return the scenario
	 */
	public static Scenario getScenario() {
		return Scenario;
	}

	/**
	 * @param scenario
	 *            the scenario to set
	 */
	public static void setScenario(Scenario scenario) {
		Scenario = scenario;
	}

	/**
	 * @return the nbUsers
	 */
	public static int getNbUsers() {
		return nbUsers;
	}

	/**
	 * @param nbUsers
	 *            the nbUsers to set
	 */
	public static void setNbUsers(int nbUsers) {
		ToitReporter.nbUsers = nbUsers;
	}

	/**
	 * @return the groupID
	 */
	public static String getGroupID() {
		return groupID;
	}

	/**
	 * @param groupID
	 *            the groupID to set
	 */
	public static void setGroupID(String groupID) {
		ToitReporter.groupID = groupID;
	}

	/**
	 * @return the city
	 */
	public static String getCity() {
		return City;
	}

	/**
	 * @param city
	 *            the city to set
	 */
	public static void setCity(String city) {
		City = city;
	}

	/**
	 * @return the nbBuss
	 */
	public static int getNbBuss() {
		return nbBuss;
	}

	/**
	 * @param nbBuss
	 *            the nbBuss to set
	 */
	public static void setNbBuss(int nbBuss) {
		ToitReporter.nbBuss = nbBuss;
	}

}
