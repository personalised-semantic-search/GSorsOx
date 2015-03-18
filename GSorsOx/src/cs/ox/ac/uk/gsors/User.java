package cs.ox.ac.uk.gsors;


public class User {
	private String name;
	private String id;
	private double t;

	public User(String name, String id, double t) {
		this.name = name;
		this.id = id;
		this.t = t;
	}

	public User(User u) {
		this.name = u.name;
		this.id = u.id;
		this.t = u.t;
	}

	public double getT() {
		return t;
	}

	public void setT(double t) {
		this.t = t;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name + "(" + id + "): " + t;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof User)) {
			return false;
		}
		User u = (User) o;
		return u.getId().equals(getId()) && u.getName().equals(getName())
				&& (u.getT() == getT());
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = (result * 37) + id.hashCode();

		return result;
	}

}
