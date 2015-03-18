package cs.ox.ac.uk.gsors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;

import user.models.Pair;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cs.ox.ac.uk.experiments.reader.JsonHelper;
import cs.ox.ac.uk.experiments.toit2014.GPrefParameters;
import cs.ox.ac.uk.experiments.toit2014.IOHelper;

public class Correlation {

	public static Double kendallTau(List<String> list1, List<String> list2,
			double p) {
		// if (list1.size() != list2.size())
		// return null;
		Set<String> unSet = new HashSet<String>();
		unSet.addAll(list2);
		unSet.addAll(list1);
		List<String> union = new ArrayList<String>(unSet);
		int size = union.size();
		double kendall = 0;
		// System.out.println(union);
		for (int i = 0; i < size; i++) {
			for (int j = i + 1; j < size; j++) {
				String ei = union.get(i);
				String ej = union.get(j);
				// System.out.print("---------(" + i + " " + j + ")-> " + ei +
				// " "
				// + ej + " ");
				// case 1 : elements are in both lists
				if (list2.contains(ei) && (list2.contains(ej))
						&& list1.contains(ei) && list1.contains(ej)) {
					int i1 = list1.indexOf(ei);
					int j1 = list1.indexOf(ej);
					int i2 = list2.indexOf(ei);
					int j2 = list2.indexOf(ej);
					// System.out.println("Case 1 elements are in both lists("
					// + i1 + "," + j1 + ")->(" + i2 + "," + j2 + ") "
					// + ei + " " + ej + " " + (i1 - j1) * (i2 - j2));
					if ((i1 - j1) * (i2 - j2) >= 0)
						kendall += 0;
					else
						kendall += 1;
				} else {
					// case 2 a: list1 contains both elements and list 2
					// contains one of them
					if (list1.contains(ei) && (list1.contains(ej))
							&& (list2.contains(ei) || list2.contains(ej))) {
						int i1 = list1.indexOf(ei);
						int j1 = list1.indexOf(ej);
						int i2 = list2.contains(ei) ? list2.indexOf(ei)
								: size + 1;
						int j2 = list2.contains(ej) ? list2.indexOf(ej)
								: size + 1;
						// System.out.println("Case 2 L1 both L2 only one(" + i1
						// + "," + j1 + ")->(" + i2 + "," + j2 + ") " + ei
						// + " " + ej + " " + (i1 - j1) * (i2 - j2));
						if ((i1 - j1) * (i2 - j2) >= 0)
							kendall += 0;
						else
							kendall += 1;
					} else {
						// case 2 b: list2 contains both elements and list 1
						// contains one of them
						if (list2.contains(ei) && (list2.contains(ej))
								&& (list1.contains(ei) || list1.contains(ej))) {
							int i2 = list2.indexOf(ei);
							int j2 = list2.indexOf(ej);
							int i1 = list1.contains(ei) ? list1.indexOf(ei)
									: size + 1;
							int j1 = list1.contains(ej) ? list1.indexOf(ej)
									: size + 1;
							// System.out.println("Case 2 L2 both L1 only one("
							// + i1 + "," + j1 + ")->(" + i2 + "," + j2
							// + ") " + ei + " " + ej + " " + (i1 - j1)
							// * (i2 - j2));
							if ((i1 - j1) * (i2 - j2) >= 0)
								kendall += 0;
							else
								kendall += 1;
						} else {
							// case 3a: i in L1 and j in L2 and not in other
							if ((list2.contains(ei) && (!list2.contains(ej)) && (list1
									.contains(ej) && (!list1.contains(ei))))
									|| (list1.contains(ei)
											&& (!list1.contains(ej)) && (list2
											.contains(ej) && (!list2
											.contains(ei))))) {
								// System.out.println("Case 3 L1 one L2 one +1");
								kendall += 1;
							} else {
								// System.out.println("Case 4+ p");
								// case 4
								kendall += p;
							}
						}
					}
				}
			}
		}
		// System.out.print(kendall + " ---K   ");
		return 2 * kendall / (size * (size - 1));
	}

	public static int common(List<String> list1, List<String> list2) {
		int counter = 0;
		for (int i = 0; i < list1.size(); i++) {
			if (list2.contains(list1.get(i)))
				counter++;
		}
		return counter;
	}

	public static Double distanceSpearman(List<String> list1, List<String> list2) {
		// computes Spearman's rho
		double sum = 0;
		// if (list1.size() != list2.size())
		// return null;
		Set<String> unSet = new HashSet<String>();
		unSet.addAll(list1);
		unSet.addAll(list2);
		List<String> union = new ArrayList<String>(unSet);
		int size = union.size();
		for (int i = 0; i < size; i++) {
			String ei = union.get(i);
			int i1 = list1.contains(ei) ? list1.indexOf(ei) : size;
			int i2 = list2.contains(ei) ? list2.indexOf(ei) : size;
			long diff = i1 - i2;
			sum += Math.abs(diff);
		}
		return sum / (size * size);
	}

	public static void executeQuery() throws IOException {
		List<Integer> ks = new ArrayList<Integer>();
		ks.add(5);
		ks.add(10);
		ks.add(15);
		ks.add(20);
		for (int k : ks) {

			List<String> f = folders(k);
			for (String fName : f) {
				// scenario
				GPrefParameters para = new GPrefParameters(fName, 0, "", 3);
				Iterator<JsonElement> msg = para.getPrefs().iterator();
				int user = 0;
				Map<Integer, String> users = new HashMap<Integer, String>();
				while (msg.hasNext()) {
					JsonObject o = msg.next().getAsJsonObject();
					String u = JsonHelper.getIdUser(o);
					// System.out.println(user + " " + u);
					users.put(user, u);
					user++;
				}
				Map<String, List<String>> topK = IOHelper.readFolder("Gilbert/"
						+ k + "/" + fName + "/", users);
				for (String key1 : topK.keySet()) {
					for (String key2 : topK.keySet()) {
						if (!key1.equals(key2)) {
							List<String> l1 = topK.get(key1);
							List<String> l2 = topK.get(key2);
							{
								Double k05 = kendallTau(l1, l2, 0.5);
								Double k0 = kendallTau(l1, l2, 0.0);
								Double k1 = kendallTau(l1, l2, 1);
								Double spearman = distanceSpearman(l1, l2);
								String sk0 = (k0 == null || k0.isNaN()) ? "NULL" : k0
										.toString();
								String sk05 = (k05 == null|| k05.isNaN()) ? "NULL" : k05
										.toString();
								String sk1 = (k1 == null|| k1.isNaN()) ? "NULL" : k1
										.toString();
								String sspearman = (spearman == null|| spearman.isNaN()) ? "NULL"
										: spearman.toString();
								String query = "insert into  toit.experim (gr_id , k , l1, l2 ,  common ,  tau_05,  spearman, tau_0, tau_1 ) values (\'"
										+ fName
										+ "\',"
										+ k
										+ ",\'"
										+ key1
										+ "\',\'"
										+ key2
										+ "\', "
										+ common(l1, l2)
										+ ","
										+ sk05
										+ ","
										+ sspearman
										+ ","
										+ sk0
										+ ","
										+ sk1
										+ ");";
								System.out.println(query);
								try {
									PrintWriter out = new PrintWriter(
											new BufferedWriter(new FileWriter(
													"query_final.txt", true)));
									out.println(query);
									out.close();
								} catch (IOException e) {
									// exception handling left as an exercise
									// for
									// the reader
								}
							}
						}
					}
				}
			}
			// System.out.println(topK);
		}
	}

	public static void main(String[] args) throws IOException {
		List<String> xx = Arrays.asList("b", "s", "n");
		List<String> yy = Arrays.asList("b", "a", "n");

		System.out.println("Kendall's tau: " + kendallTau(xx, yy, 1));
		System.out.println("Common: " + common(xx, yy));
		System.out.println("Spearman: " + distanceSpearman(xx, yy));
		executeQuery();
	}

	private static double flips(int[] p1, int[] p2) {
		/*
		 * I want to count the smallest number of flips of adjacent elements
		 * needed to convert one permutation into the other. The idea is: find
		 * an element in p2 that is the furthest from its position in p1 ... no,
		 * that won't necessarily find the smallest number of flips: there may
		 * be some element that gets pushed further from its home when the first
		 * one moves, because its home lies outside the movement range of the
		 * one that is moving.
		 * 
		 * How about this: suppose i is the first value that is currently to the
		 * right of its home. i has to move to its home, so we can start with
		 * the flips needed to get it there. Is it possible that by doing some
		 * preliminary flips we can save time later? Nothing to the right of i's
		 * current position can be affected by i's move. The elements between i
		 * and its home are all > i, and all will be shifted one spot to the
		 * right by i's move. Suppose i+1 is in position i+1. After i moves, i+1
		 * will be in position i+2 and will need a move to get to its home. Can
		 * there be any advantage to making this move before moving i? I don't
		 * think so.
		 * 
		 * By experimentation and upon reflection, it is apparent that flips and
		 * Kendall tau are equivalent
		 */

		int size = p1.length;

		long f = 0;
		int[] p2Copy = new int[size];
		for (int i = 0; i < size; i++) {
			p2Copy[i] = p2[i];
		}

		for (int i = 0; i < size; i++) {
			int cur = p1[i];
			int j = i;

			while (p2Copy[j] != cur) {
				j++;
			}

			while (j > i) {
				p2Copy[j] = p2Copy[j - 1];
				j--;
				f++;
			}

			p2Copy[i] = cur;
		}

		return 1 - 2 * f / (double) ((size - 1) * size);
	}

	private static int[] findLCS(int[] p1, int[] p2) {
		// returns the Longest Common Subsequence of p1 and p2.
		// the int array returned contains 0's and 1's; it references the
		// elements of p1 for absence/presence in the LCS

		int size = p1.length;
		int[] lcs = new int[size];
		long[][] table = new long[size + 1][size + 1];

		// fill in the rest of the table
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				if (p1[row] == p2[col]) {
					table[row + 1][col + 1] = 1 + table[row][col];
				} else {
					table[row + 1][col + 1] = Math.max(table[row][col + 1],
							table[row + 1][col]);
				}
			}
		}

		// now find the LCS
		long LCSLength = table[size][size];
		int curRow = size;
		int curCol = size;
		while (LCSLength > 0) {
			if (p1[curRow - 1] == p2[curCol - 1]) {
				lcs[curRow - 1] = 1;
				curRow--;
				curCol--;
				LCSLength--;
			} else if (table[curRow][curCol] == table[curRow][curCol - 1]) {
				curCol--;
			} else {
				curRow--;
			}
		}
		return lcs;
	}

	public static List<String> folders(int k) {
		List<String> l = new ArrayList<String>();
		File folder = new File("Gilbert/" + k + "/");
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {

			if (listOfFiles[i].isDirectory()) {
				String files = listOfFiles[i].getName();
				l.add(files);
			}
		}
		return l;
	}
}