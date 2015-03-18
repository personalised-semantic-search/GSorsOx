package cs.ox.ac.uk.experiments.toit2014;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.deri.iris.api.basics.ITuple;

public class IOHelper {

	public static void writeSolution(String fileName, List<ITuple> result) {
		try {
			File file = new File(fileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			for (ITuple t : result) {
				output.write(t.get(0).toString().replaceAll("'", "") + "\n");
			}

			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<String> readFile(String fileName)
			throws FileNotFoundException {
		Scanner s = new Scanner(new File(fileName));
		List<String> list = new ArrayList<String>();
		while (s.hasNext()) {
			list.add(s.next());
		}
		s.close();
		return list;

	}

	public static Map<String, List<String>> readFolder(String folderName,
			Map<Integer, String> users) throws IOException {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		File folder = new File(folderName);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String files = listOfFiles[i].getName();
				List<String> l = readFile(listOfFiles[i].getCanonicalPath());
				String key = files.replaceAll(".txt", "");
				if (key.equals("CSU") || key.equals("Plurality")
						|| key.equals("PluralityMisery")) {
					map.put(key, l);
				} else {
					map.put(users.get(Integer.parseInt(key)), l);
				}
			}
		}
		return map;
	}

	public static List<String> folders(String link) {
		List<String> l = new ArrayList<String>();
		File folder = new File(link);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {

			if (listOfFiles[i].isDirectory()) {
				String files = listOfFiles[i].getName();
				l.add(files);
			}
		}
		return l;
	}

	public static boolean createFolder(String path) {
		File files = new File(path);
		if (!files.exists()) {
			if (files.mkdirs()) {
				System.out.println("Multiple directories are created!");
				return true;

			} else {
				System.out.println("Failed to create multiple directories!");
				return false;
			}
		}
		return false;
	}

}
