/**
 * 
 */
package cs.ox.ac.uk.experiments.toit2014;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * @author onsa
 * 
 */
public class GPrefParameters {
	public GPrefParameters(String filename, int k, String city, int nbBs) throws IOException {
		this.k = k;
		this.nbNodes = 10000;
		this.startFromRes = 0;
		this.constraintsSqlQuery = " and place.bs_city LIKE '"+city+"'";
		this.city = city;
		this.scenario = Scenario.fromInt(Integer.parseInt(filename
				.substring(filename.indexOf("sc") + 2)));
		this.prefs = loadFolderPrefs(filename);
		this.groupId = filename.substring(2, filename.indexOf("_"));
		this.maxNbUsers = prefs.size();
		this.bs = nbBs;

	}
	private final int k;
	private final int nbNodes;
	private final int startFromRes;
	private final String constraintsSqlQuery;
	private final String city;
	private final JsonArray prefs;
	private final int maxNbUsers;
	private Scenario scenario;
	private final String groupId;
	private final Integer bs;

	public int getK() {
		return k;
	}

	/**
	 * @return the nbNodes: select ... from employee limit nbNodes
	 */
	public int getNbNodes() {
		return nbNodes;
	}

	/**
	 * @return the startFromRes: select ... offset startFromRes
	 */
	public int getStartFromRes() {
		return startFromRes;
	}

	/**
	 * @return the constraintsSqlQuery:e.g where city=""
	 */
	public String getConstraintsSqlQuery() {
		return constraintsSqlQuery;
	}

	/**
	 * @return the folderNamePrefs
	 */
	public JsonArray getPrefs() {
		return prefs;
	}

	/**
	 * @return the maxNbUsers
	 */
	public int getMaxNbUsers() {
		return maxNbUsers;
	}

	/**
	 * @return the scenario
	 */
	public Scenario getScenario() {
		return scenario;
	}


	private final JsonArray loadFolderPrefs(final String folderName)
			throws IOException {
		final File folder = new File(folderName);
		JsonArray jsonList = new JsonArray();
		for (final File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				JsonParser parser = new JsonParser();
				JsonElement jsonElement = parser
						.parse(new FileReader(fileEntry));
				jsonList.add(jsonElement.getAsJsonObject());
			}
		}
		return jsonList;
	}


	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @return the bs
	 */
	public Integer getBs() {
		return bs;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

}
