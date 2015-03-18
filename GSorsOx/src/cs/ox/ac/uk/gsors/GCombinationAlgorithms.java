package cs.ox.ac.uk.gsors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cs.ox.ac.uk.sors.CombinationAlgorithms;
import cs.ox.ac.uk.sors.PreferencesGraph;

public class GCombinationAlgorithms {

	public static <T extends Comparable<? super T>> List<T> asSortedListDesc(
			Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list, Collections.reverseOrder());
		return list;
	}

	public static <T extends Comparable<? super T>> List<T> asSortedListAsc(
			Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}

	public static GPreferencesGraph combPrefsGen(GPreferencesGraph pg,
			Map<String, Double> probabilisticModel, List<Double> treashold) {
		List<PreferencesGraph> l = new ArrayList<PreferencesGraph>();
		for (int i=0;i<pg.size();i++) {
			PreferencesGraph p= pg.get(i);
			PreferencesGraph preferenceModel = CombinationAlgorithms
					.combPrefsGen(p, probabilisticModel,treashold.get(i));
			l.add(preferenceModel);
		}
		return new GPreferencesGraph(l);
	}
	public static GPreferencesGraph combPrefsGen(GPreferencesGraph pg,
			Map<String, Double> probabilisticModel, Double treashold) {
		List<PreferencesGraph> l = new ArrayList<PreferencesGraph>();
		for (int i=0;i<pg.size();i++) {
			PreferencesGraph p= pg.get(i);
			PreferencesGraph preferenceModel = CombinationAlgorithms
					.combPrefsGen(p, probabilisticModel,treashold);
			l.add(preferenceModel);
		}
		return new GPreferencesGraph(l);
	}
}
