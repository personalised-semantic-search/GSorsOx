package cs.ox.ac.uk.gsors;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CyclesCount {

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("cycles.txt"));
		String line;
		Set<String> scenariosCycle = new HashSet<String>();
		Set<String> collapseCycle = new HashSet<String>();
		Map<String, Integer> users = new HashMap<String, Integer>();
		while ((line = br.readLine()) != null) {
			String[] words = line.split(" ");
			String scenario = words[1];
			int count = 1;
			if (words[2].contains("User")) {
				if (users.containsKey(scenario)) {
					count = users.get(scenario) + 1;

				}
				users.put(scenario, count);
			} else {
				collapseCycle.add(scenario);
			}
			// System.out.println("|" + scenario + "|");
			scenariosCycle.add(scenario);
		}

		// System.out.print(scenariosCycle.size() + " " + collapseCycle.size()
		// + " " + users.size());

		// for (String key : users.keySet()) {
		// System.out.println(key + "	" + users.get(key) / 8);
		// }
		for (String key : users.keySet()) {
			if (collapseCycle.contains(key)) {
				
			}else
			{
				System.out.println(key);
			}
		}
	}
}