package monitoring.model;

public class HRwithHRV implements Comparable<HRwithHRV> {
	
	private double heartrate;
	private double heartrateVariability;
	
	@Override
	public int compareTo(HRwithHRV hrWithHrv) {
		if (hrWithHrv.getHeartrate() > this.getHeartrate()) {
			return -1;
		} else if (hrWithHrv.getHeartrate() == this.getHeartrate()) {
			return 0;
		}

		return 1;
	}

	public double getHeartrate() {
		return heartrate;
	}

	public void setHeartrate(double heartrate) {
		this.heartrate = heartrate;
	}

	public double getHeartrateVariability() {
		return heartrateVariability;
	}

	public void setHeartrateVariability(double heartrateVariability) {
		this.heartrateVariability = heartrateVariability;
	}
}
