package user.models;

public class Pair<K, V> {

	private final K element0;
	private final V element1;

	public static <K, V> Pair<K, V> createPair(K element0, V element1) {
		return new Pair<K, V>(element0, element1);
	}

	public Pair(K element0, V element1) {
		this.element0 = element0;
		this.element1 = element1;
	}

	public K getElement0() {
		return element0;
	}

	public V getElement1() {
		return element1;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = result * prime + element0.toString().hashCode();
		return result * prime + element1.toString().hashCode();
	}

	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (o == null)
			return false;
		else if (!(o instanceof Pair))
			return false;
		else {
			Pair<K, V> p = (Pair<K, V>) o;
			if (p.getElement0().equals(element0)
					&& p.getElement1().equals(element1)) {
				return true;
			}
		}
		return false;
	}

}