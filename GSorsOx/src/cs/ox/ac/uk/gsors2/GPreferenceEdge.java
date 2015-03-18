package cs.ox.ac.uk.gsors2;

import org.deri.iris.api.basics.ITuple;
import org.jgrapht.graph.DefaultWeightedEdge;

import cs.ox.ac.uk.gsors.User;

public class GPreferenceEdge extends DefaultWeightedEdge {

	private static final long serialVersionUID = 1L;

	/** Label of this edge. */
	private User label;

	/** The source of the Edge. */
	private final ITuple source;

	/** The target of the Edge. */
	private final ITuple target;

	/**
	 * A constructor which sets the source, taget and the label.
	 * 
	 * @param source
	 *            the source vertex
	 * @param target
	 *            the target vertex
	 * @throws NullPointerException
	 *             if the source or the tages are <code>null</code>
	 */
	public GPreferenceEdge(final ITuple source, final ITuple target) {
		this(source, target, null);
	}

	/**
	 * A constructor which sets the source, taget and the label.
	 * 
	 * @param source
	 *            the source vertex
	 * @param target
	 *            the target vertex
	 * @param label
	 *            the label to set
	 * @throws NullPointerException
	 *             if the source or the tages are <code>null</code>
	 */
	public GPreferenceEdge(final ITuple source, final ITuple target, final User label) {
		if ((source == null) || (target == null)) {
			throw new NullPointerException(
					"The souece and the target must not be null");
		}
		this.label = label;
		this.source = source;
		this.target = target;
	}

	public ITuple getSource() {
		return source;
	}

	public double getMyWeight() {
		return this.getWeight();
	}

	public ITuple getTarget() {
		return target;
	}

	/**
	 * Returns the actual label of the edge.
	 * 
	 * @return the label
	 */
	public User getLabel() {
		return label;
	}

	/**
	 * Returns whether there is actually a label set.
	 * 
	 * @return true if the label is not null, otherwise false
	 */
	public boolean hasLabel() {
		return label != null;
	}

	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof GPreferenceEdge)) {
			return false;
		}
		GPreferenceEdge le = (GPreferenceEdge) o;
		return le.getSource().equals(getSource())
				&& le.getTarget().equals(getTarget())
				&& (label == null ? le.getLabel() == null : label.toString()
						.equals(le.getLabel().toString()));
	}

	public int hashCode() {
		int res = 17;
		res = res * 37 + getSource().hashCode();
		res = res * 37 + getTarget().hashCode();
		res = res * 37 + (label == null ? 0 : label.hashCode());
		return res;
	}

	/**
	 * <p>
	 * Returns a simple string representation of this labeled directed edge.
	 * <b>The subject of the stringrepresentation is to change.</b>
	 * </p>
	 * <p>
	 * An example String could be: <code>source->(label)->target</code>.
	 * </p>
	 * 
	 * @return the string representation
	 */
	public String toString() {
		return getSource() + " ->( " + label + this.getWeight() + " )-> "
				+ getTarget();
	}

	

}