package monitoring.model;

public class HealthDataFeatures {

	private int meanHR;
	private int stdDevHR;
	private int hrvEct;
	private int meanHRV;
	private int stdDevHRV;
	private int medianHRV;
	private int nn50;
	private int nn20;
	private int rmssd;
	private int sdsd;
	private int minNN;
	private int maxNN;
	private int hrvTi;
	private int tinn;
	private int sd1;
	private int sd2;
	private int steps;
	private int spEn;
	
	public static final String[] FEATURES_STRINGS = {
		"currentStdDevHR", "currentHrvEct",
		"currentMeanHRV", "currentStdDevHRV",
		"currentMedianHRV", "currentNn50", "currentNn20",
		"currentRmssd", "currentSdsd", "currentMinNN",
		"currentMaxNN", "currentHrvTi", "currentTinn",
		"currentSd1", "currentSd2", "currentSteps",
		"currentSpEn", "nextMeanHR", "nextStdDevHR",
		"nextHrvEct", "nextMeanHRV", "nextStdDevHRV",
		"nextMedianHRV", "nextNn50", "nextNn20",
		"nextRmssd", "nextSdsd", "nextMinNN", "nextMaxNN",
		"nextHrvTi", "nextTinn", "nextSd1", "nextSd2",
		"nextSteps", "nextSpEn" };

	public int getMeanHR() {
		return meanHR;
	}

	public void setMeanHR(int meanHR) {
		this.meanHR = meanHR;
	}

	public int getStdDevHR() {
		return stdDevHR;
	}

	public void setStdDevHR(int stdDevHR) {
		this.stdDevHR = stdDevHR;
	}

	public int getHrvEct() {
		return hrvEct;
	}

	public void setHrvEct(int hrvEct) {
		this.hrvEct = hrvEct;
	}

	public int getMeanHRV() {
		return meanHRV;
	}

	public void setMeanHRV(int meanHRV) {
		this.meanHRV = meanHRV;
	}

	public int getStdDevHRV() {
		return stdDevHRV;
	}

	public void setStdDevHRV(int stdDevHRV) {
		this.stdDevHRV = stdDevHRV;
	}

	public int getMedianHRV() {
		return medianHRV;
	}

	public void setMedianHRV(int medianHRV) {
		this.medianHRV = medianHRV;
	}

	public int getNn50() {
		return nn50;
	}

	public void setNn50(int nn50) {
		this.nn50 = nn50;
	}

	public int getNn20() {
		return nn20;
	}

	public void setNn20(int nn20) {
		this.nn20 = nn20;
	}

	public int getRmssd() {
		return rmssd;
	}

	public void setRmssd(int rmssd) {
		this.rmssd = rmssd;
	}

	public int getSdsd() {
		return sdsd;
	}

	public void setSdsd(int sdsd) {
		this.sdsd = sdsd;
	}

	public int getMinNN() {
		return minNN;
	}

	public void setMinNN(int minNN) {
		this.minNN = minNN;
	}

	public int getMaxNN() {
		return maxNN;
	}

	public void setMaxNN(int maxNN) {
		this.maxNN = maxNN;
	}

	public int getHrvTi() {
		return hrvTi;
	}

	public void setHrvTi(int hrvTi) {
		this.hrvTi = hrvTi;
	}

	public int getTinn() {
		return tinn;
	}

	public void setTinn(int tinn) {
		this.tinn = tinn;
	}

	public int getSd1() {
		return sd1;
	}

	public void setSd1(int sd1) {
		this.sd1 = sd1;
	}

	public int getSd2() {
		return sd2;
	}

	public void setSd2(int sd2) {
		this.sd2 = sd2;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public int getSpEn() {
		return spEn;
	}

	public void setSpEn(int spEn) {
		this.spEn = spEn;
	}
	
	public int getFeature(String feature) {
		
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[0]) || feature.equalsIgnoreCase(FEATURES_STRINGS[18])) {
			return getStdDevHR();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[1]) || feature.equalsIgnoreCase(FEATURES_STRINGS[19])) {
			return getHrvEct();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[2]) || feature.equalsIgnoreCase(FEATURES_STRINGS[20])) {
			return getMeanHRV();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[3]) || feature.equalsIgnoreCase(FEATURES_STRINGS[21])) {
			return getStdDevHRV();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[4]) || feature.equalsIgnoreCase(FEATURES_STRINGS[22])) {
			return getMedianHRV();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[5]) || feature.equalsIgnoreCase(FEATURES_STRINGS[23])) {
			return getNn50();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[6]) || feature.equalsIgnoreCase(FEATURES_STRINGS[24])) {
			return getNn20();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[7]) || feature.equalsIgnoreCase(FEATURES_STRINGS[25])) {
			return getRmssd();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[8]) || feature.equalsIgnoreCase(FEATURES_STRINGS[26])) {
			return getSdsd();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[9]) || feature.equalsIgnoreCase(FEATURES_STRINGS[27])) {
			return getMinNN();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[10]) || feature.equalsIgnoreCase(FEATURES_STRINGS[28])) {
			return getMaxNN();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[11]) || feature.equalsIgnoreCase(FEATURES_STRINGS[29])) {
			return getHrvTi();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[12]) || feature.equalsIgnoreCase(FEATURES_STRINGS[30])) {
			return getTinn();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[13]) || feature.equalsIgnoreCase(FEATURES_STRINGS[31])) {
			return getSd1();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[14]) || feature.equalsIgnoreCase(FEATURES_STRINGS[32])) {
			return getSd2();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[15]) || feature.equalsIgnoreCase(FEATURES_STRINGS[33])) {
			return getSteps();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[16]) || feature.equalsIgnoreCase(FEATURES_STRINGS[34])) {
			return getSpEn();
		}
		if (feature.equalsIgnoreCase(FEATURES_STRINGS[17])) {
			return getMeanHR();
		}
		return 0x0;
	}
}
