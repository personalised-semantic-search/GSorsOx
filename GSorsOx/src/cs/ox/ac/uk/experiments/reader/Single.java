package cs.ox.ac.uk.experiments.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Single {

	static void sendJSONFile() {
		String path = "datasets/jods2014_dataset/input/final/"; // set
		JsonParser parser = new JsonParser();
		File folder = new File(path);
		File[] files = folder.listFiles();
		for (int j = 0; j < files.length; j++) {
			System.out.println(files[j].getAbsolutePath());
			if (files[j].isDirectory()) {
				File[] jsonfiles = getJSONFiles(files[j].getAbsolutePath());
				if (jsonfiles.length > 0) {
					for (int i = 0; i <= jsonfiles.length; i++) {
						File f = new File(path);
						File[] fs = folder.listFiles();
						for (int k = 0; k < files.length; k++) {
							System.out.println(files[j].getAbsolutePath());
							if (files[k].isDirectory()) {
								File[] jfiles = getJSONFiles(files[k]
										.getAbsolutePath());
								if (jsonfiles.length > 0) {
									for (int i2 = 0; i2 <= jsonfiles.length; i2++) {
										try {
											// prints json file names
											System.out.println("File: \t"
													+ jsonfiles[i2]);
											JsonElement jsonElement = parser
													.parse(new FileReader(
															jsonfiles[i2]));
											JsonObject jsonObject = jsonElement
													.getAsJsonObject();
											readJSONFile(jsonObject);
										} catch (FileNotFoundException e) {
											System.out.println(e.getMessage());
											// e.getStackTrace();
										} catch (IOException e) {
											System.out.println(e.getMessage());
											// e.getStackTrace();
										} catch (Exception e) {
											System.out.println(e.getMessage());
											// e.getStackTrace();
										}

									}

								}
							}
						}
					}
				}
			}
		}
	}

	// read a complete json file
	static void readJSONFile(JsonObject jsonObject) {

		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {

			String key = entry.getKey();
			JsonElement value = entry.getValue();

			if (value.isJsonObject()) {
				readJSONFile(value.getAsJsonObject());
			} else if (value.isJsonArray()) {
				JsonArray jsonArray = value.getAsJsonArray();

				if (jsonArray.size() == 1) {
					readJSONFile((JsonObject) jsonArray.get(0));
				} else {
					// prints json array name
					System.out.println(key);
					Iterator<JsonElement> msg = jsonArray.iterator();
					while (msg.hasNext()) {
						// prints json array values
						System.out.println(msg.next());
					}
				}
			} else {
				// //prints json object's keys and values
				if (key.equals("name"))
					System.out.println(key + " - " + value);
			}
		}
	}

	// get only .json files from a directory
	static File[] getJSONFiles(String p) {

		File folder = new File(p);
		File[] files = folder.listFiles();
		File[] jsonfiles = new File[files.length];
		int count = 0;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				if (files[i].getName().endsWith(".json")
						|| files[i].getName().endsWith(".JSON")) {
					jsonfiles[++count] = files[i];
				}
			}
		}

		return files;
	}

	public static void main(String[] args) {
		sendJSONFile();
	}
}
