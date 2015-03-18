package cs.ox.ac.uk.experiments.toit2014;

public enum Scenario {
	BREAKFAST_CUSINE (1),
	BREAKFAST_FOOD (4),
	BREAKFAST_PLACE (7),
	LUNCH_CUSINE (2),
	LUNCH_FOOD (5),
	LUNCH_PLACE (8),
	DINNER_CUSINE (3),
	DINNER_FOOD (6),
	DINNER_PLACE (9),
	UNDEFINED (10);

	private  int id;   // in kilograms

	Scenario(int id) {
		this.id = id;
	}

	void setId(int id_) {
		this.id = id_;
	}

	int getId(){
		return id;
	}

	public String toString() {
		switch (this) {
		case BREAKFAST_CUSINE:
			return "BREAKFAST_CUSINE";
		case BREAKFAST_FOOD:
			return "BREAKFAST_FOOD";
		case BREAKFAST_PLACE:
			return "BREAKFAST_PLACE";
		case LUNCH_CUSINE:
			return "LUNCH_CUSINE";
		case  LUNCH_FOOD :
			return "LUNCH_FOOD";
		case  LUNCH_PLACE :
			return "LUNCH_PLACE";
		case  DINNER_CUSINE :
			return "DINNER_CUSINE";
		case  DINNER_FOOD :
			return "DINNER_FOOD";
		case  DINNER_PLACE :
			return "DINNER_PLACE";
		default:
			return "Unknown scenario.";
		}
	}
	public static Scenario fromInt(int text) {
	      for (Scenario b : Scenario.values()) {
	        if (text==b.id) {
	          return b;
	        }
	      }
	    return null;
	  }
	}

	

	//for (Scenarios s : Scenarios.values())
	//    System.out.printf(getId());
