package monitoring.analyse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import monitoring.model.HealthDataFeatures;
import monitoring.utils.ExportUtils;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Controller Class for the calls which take the specified .CSV files (the
 * .CSV files are now hardcoded in the viewResults() function => should be a
 * parameter from the app) and then uses the first to train the recognition
 * algorithm and the second to test it and find the recognition result
 * percentage.
 * 
 * @author adriana
 *
 */

@RestController
@RequestMapping({ "/stepAnalyse" })
public class StepAnalyseController {

	/*
	 * Variable which holds the values for the features which are to be
	 * considered in the training and in the testing phase. The data in the .CSV
	 * files comes with more features extracted from the extraction step.
	 */
	public static final String[] FEATURES_STRINGS = { "currentMeanHRV",
			"currentStdDevHRV", "currentMedianHRV", "currentNn50",
			"currentNn20", "currentRmssd", "currentSdsd", "currentMinNN",
			"currentMaxNN", "currentSd1", "currentSd2", "nextMeanHR",
			"nextMeanHRV", "nextStdDevHRV", "nextMedianHRV", "nextNn50",
			"nextNn20", "nextRmssd", "nextSdsd", "nextMinNN", "nextMaxNN",
			"nextSd1", "nextSd2" };

	/*
	 * Variable which holds every threshold corresponding to a feature in the
	 * FEATURES_STRINGS variable.
	 * 
	 * meanHRV - 20 - stdDevHRV - 40 - medianHRV - 5 - nn50 - 5 - nn20 - 5 -
	 * rmssd - 50 - sdsd - 20 - minNN - 20 - maxNN - 40 - sd1 - 50 - sd2 - 50 -
	 * meanHR - 20
	 * 
	 * TO DO - why these thresholds
	 */
	public static final int[] FEATURES_TRESHOLDS = { 20, 40, 5, 5, 5, 50, 20,
			20, 40, 50, 50, 20, 20, 40, 5, 5, 5, 50, 20, 20, 40, 50, 50 };

	/*
	 * Number of features taken into consideration for the training phase from
	 * the FEATURES_STRINGS variable
	 */
	public static final int FEATURES_CONSIDERED = 11;

	/*
	 * Number of features taken into consideration for the testing phase from
	 * the FEATURES_STRINGS variable
	 */
	public static final int FEATURES_CONSIDERED_TEST = 23;

	/*
	 * Variables which hold every possible distinct meanHR and all the values
	 * which can come after it.
	 * 
	 * Format: the first (most outer) HashMap has as the key the distinct meanHR
	 * and the value is a second HashMap which has as the key the distinct
	 * nextMeanHR which can occur and the value of this one is a List of
	 * HashMaps. It's a List because we have to memorise all the components for
	 * the nextMeanHR which can appear more than once. And the HashMap is
	 * composed of all the features for that specific entry of the same
	 * nextMeanHR. The key is a String: the name of the feature, and the value
	 * is an Integer: the value of the feature.
	 */
	public HashMap<Integer, HashMap<Integer, List<HashMap<String, Integer>>>> trainAttribuesForProbabilities = new HashMap<Integer, HashMap<Integer, List<HashMap<String, Integer>>>>();
	public HashMap<Integer, HashMap<Integer, List<HashMap<String, Integer>>>> testAttribuesForProbabilities = new HashMap<Integer, HashMap<Integer, List<HashMap<String, Integer>>>>();

	/*
	 * Method which can be called from a UI application to use the algorithm
	 * which does the recognition.
	 */
	@RequestMapping(value = "/healths", method = RequestMethod.GET)
	@ResponseBody
	public void viewResults(HttpServletResponse response,
			@RequestParam MultiValueMap<String, String> messageParams) {

		/*
		 * Headers to allow calls from another Tomcat server instance running on
		 * the same machine
		 */
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods",
				"POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept");

		/*
		 * Variables which hold the data from the .CSV files
		 */
		List<HealthDataFeatures> heartrateDataTrain = new ArrayList<HealthDataFeatures>();
		List<HealthDataFeatures> heartrateDataTest = new ArrayList<HealthDataFeatures>();

		heartrateDataTrain = ExportUtils
				.readFromCSVFeatures("TEST/EXTRACTION-Bogdan-INT-1.csv");
		heartrateDataTest = ExportUtils
				.readFromCSVFeatures("TEST/EXTRACTION-Adriana-INT.csv");

		/*
		 * Variables which hold all the learned attributes. This means:
		 * 
		 * The form: a HashMap which has as the key the distinct meanHR and the
		 * value is a List of HashMaps. The List holds the attribute for each of
		 * the meanHR because they can occur more than once and then they have
		 * more components which are saved in this List. Every component has its
		 * features -> the HashMap which has as key the name of the feature and
		 * then as value the value of this feature.
		 */
		HashMap<Integer, List<HashMap<String, Integer>>> attributesLearned = learnPhase(heartrateDataTrain);
		HashMap<Integer, List<HashMap<String, Integer>>> attributesLearnedForTest = learnPhase(heartrateDataTest);

		/*
		 * Variables which hold the probabilities (%) for every meanHR that a
		 * nextMeanHR can occur. This means:
		 * 
		 * The form: a HashMap which has as the key the distinct meanHR and the
		 * value is another HashMap whose key is represented by the nextMeanHR
		 * and the value is a Double which represents the percentage (%) with
		 * which that nextMeanHR occurs.
		 */
		HashMap<Integer, HashMap<Integer, Double>> probabilitiesTest = getProbabilities(
				attributesLearnedForTest, false);
		HashMap<Integer, HashMap<Integer, Double>> probabilitiesTrain = getProbabilities(
				attributesLearned, true);

		testPhase1(attributesLearned, heartrateDataTest);
		// looking at all features of the learned meanHR every time
		testPhase2(attributesLearned, heartrateDataTest);
		// looking only at the features of the learned meanHR
		// which correspond to every feature of the test data
		testPhase3(attributesLearned, probabilitiesTrain,
				attributesLearnedForTest, probabilitiesTest);
		// looking at the probability with which nextHR appears
	}

	/**
	 * The learning phase creates a HashMap which holds, for every distinct
	 * meanHR component which comes from the .CSV file, a List with its
	 * corresponding current features (for every occurrence -> that's why I need
	 * a List) and also its corresponding next features (also for every
	 * occurrence)
	 * 
	 * @param heartrateDataTrain
	 *            - the data parsed from the .CSV file
	 * @return - the generated HashMap with all the learned values (described at
	 *         variable)
	 */
	public HashMap<Integer, List<HashMap<String, Integer>>> learnPhase(
			List<HealthDataFeatures> heartrateDataTrain) {

		HashMap<Integer, List<HashMap<String, Integer>>> attributes = new HashMap<Integer, List<HashMap<String, Integer>>>();

		// for all the components in the .CSV file
		for (int i = 0; i < heartrateDataTrain.size(); i++) {

			// get the current features for the current component instance
			HealthDataFeatures currentHealthDataFeatures = heartrateDataTrain
					.get(i);

			// get the current meanHR of the current component instance
			int currentHR = currentHealthDataFeatures.getMeanHR();

			// get the List with the components already saved for this meanHR
			// value if something was already saved
			List<HashMap<String, Integer>> currentValuesList = attributes
					.get(currentHR);

			if (currentValuesList == null) {
				currentValuesList = new ArrayList<HashMap<String, Integer>>();
			}

			// retrieve all the current values in this variable and the next
			// values if they exist and are relevant
			HashMap<String, Integer> currentValues = new HashMap<String, Integer>();

			currentValues.put("currentStdDevHR",
					currentHealthDataFeatures.getStdDevHR());
			currentValues.put("currentHrvEct",
					currentHealthDataFeatures.getHrvEct());
			currentValues.put("currentMeanHRV",
					currentHealthDataFeatures.getMeanHRV());
			currentValues.put("currentStdDevHRV",
					currentHealthDataFeatures.getStdDevHRV());
			currentValues.put("currentMedianHRV",
					currentHealthDataFeatures.getMedianHRV());
			currentValues.put("currentNn50",
					currentHealthDataFeatures.getNn50());
			currentValues.put("currentNn20",
					currentHealthDataFeatures.getNn20());
			currentValues.put("currentRmssd",
					currentHealthDataFeatures.getRmssd());
			currentValues.put("currentSdsd",
					currentHealthDataFeatures.getSdsd());
			currentValues.put("currentMinNN",
					currentHealthDataFeatures.getMinNN());
			currentValues.put("currentMaxNN",
					currentHealthDataFeatures.getMaxNN());
			currentValues.put("currentHrvTi",
					currentHealthDataFeatures.getHrvTi());
			currentValues.put("currentTinn",
					currentHealthDataFeatures.getTinn());
			currentValues.put("currentSd1", currentHealthDataFeatures.getSd1());
			currentValues.put("currentSd2", currentHealthDataFeatures.getSd2());
			currentValues.put("currentSteps",
					currentHealthDataFeatures.getSteps());
			currentValues.put("currentSpEn",
					currentHealthDataFeatures.getSpEn());

			// if there exists a next component
			if (i + 1 < heartrateDataTrain.size()) {

				// retrieve the features of the next component
				HealthDataFeatures nextHealthDataFeatures = heartrateDataTrain
						.get(i + 1);

				// if the next component is relevant, and really comes at a next
				// moment, or it is after a pause period
				// this is calculated now if the next meanHR is in the range
				// currentMeanHR - 10 <= nextMeanHR <= currentMeanHR + 10
				// => TO DO: change this and see if it is relevant by looking
				// directly at the time difference
				if (nextHealthDataFeatures.getMeanHR() <= currentHealthDataFeatures
						.getMeanHR() + 10
						&& nextHealthDataFeatures.getMeanHR() >= currentHealthDataFeatures
								.getMeanHR() - 10) {

					currentValues.put("nextMeanHR",
							nextHealthDataFeatures.getMeanHR());
					currentValues.put("nextStdDevHR",
							nextHealthDataFeatures.getStdDevHR());
					currentValues.put("nextHrvEct",
							nextHealthDataFeatures.getHrvEct());
					currentValues.put("nextMeanHRV",
							nextHealthDataFeatures.getMeanHRV());
					currentValues.put("nextStdDevHRV",
							nextHealthDataFeatures.getStdDevHRV());
					currentValues.put("nextMedianHRV",
							nextHealthDataFeatures.getMedianHRV());
					currentValues.put("nextNn50",
							nextHealthDataFeatures.getNn50());
					currentValues.put("nextNn20",
							nextHealthDataFeatures.getNn20());
					currentValues.put("nextRmssd",
							nextHealthDataFeatures.getRmssd());
					currentValues.put("nextSdsd",
							nextHealthDataFeatures.getSdsd());
					currentValues.put("nextMinNN",
							nextHealthDataFeatures.getMinNN());
					currentValues.put("nextMaxNN",
							nextHealthDataFeatures.getMaxNN());
					currentValues.put("nextHrvTi",
							nextHealthDataFeatures.getHrvTi());
					currentValues.put("nextTinn",
							nextHealthDataFeatures.getTinn());
					currentValues.put("nextSd1",
							nextHealthDataFeatures.getSd1());
					currentValues.put("nextSd2",
							nextHealthDataFeatures.getSd2());
					currentValues.put("nextSteps",
							nextHealthDataFeatures.getSteps());
					currentValues.put("nextSpEn",
							nextHealthDataFeatures.getSpEn());
				}
			}

			// add the new created component with current values +/- and next
			// values to
			// the already existing list with components for the current meanHR
			currentValuesList.add(currentValues);

			// save for the current meanHR the new list of value components
			attributes.put(currentHR, currentValuesList);
		}

		System.out.println("Attributes: " + attributes);

		return attributes;
	}

	/**
	 * This method is also used in the "learning phase". It generates a HashMap
	 * with the percentages that a nextMeanHR occurs, for every distinct
	 * currentMeanHR.
	 * 
	 * In this method, another generated HashMap, is the one which has the same
	 * structure as the one mentioned above, just that instead of holding the
	 * percentages for every nextMeanHR to occur, it holds, for every
	 * nextMeanHR, the values (features: name + value) with which it occurs.
	 * 
	 * @param attributesLearned
	 *            - the new generated HashMap from the learn phase
	 * @param learnPhase
	 *            - if it is training mode or testing mode
	 * @return - a HashMap with all the probabilities (%) (described at
	 *         variable)
	 */
	public HashMap<Integer, HashMap<Integer, Double>> getProbabilities(
			HashMap<Integer, List<HashMap<String, Integer>>> attributesLearned,
			boolean learnPhase) {

		HashMap<Integer, HashMap<Integer, List<HashMap<String, Integer>>>> attribuesForProbabilities = new HashMap<Integer, HashMap<Integer, List<HashMap<String, Integer>>>>();

		HashMap<Integer, HashMap<Integer, Double>> probabilities = new HashMap<Integer, HashMap<Integer, Double>>();

		// get all the distinct currentMeanHR values
		Set<Integer> keys = attributesLearned.keySet();

		Iterator<Integer> iterator = keys.iterator();

		// for every distinct currentMeanHR value
		while (iterator.hasNext()) {

			// retreive the currentMeanHR
			Integer key = iterator.next();

			// get all the attributes for the currentMeanHR (this means also the
			// "next" attributes)
			List<HashMap<String, Integer>> currentAttributes = attributesLearned
					.get(key);
			
			
			HashMap<Integer, Double> currentMeanHRValues = new HashMap<Integer, Double>();
			HashMap<Integer, List<HashMap<String, Integer>>> currentAttributesForProbabilities = new HashMap<Integer, List<HashMap<String, Integer>>>();
			int meanHRNumber = 0;

			for (int i = 0; i < currentAttributes.size(); i++) {

				HashMap<String, Integer> currentAttributesInstance = currentAttributes
						.get(i);

				if (currentAttributesInstance.get("nextMeanHR") != null) {

					meanHRNumber++;

					int currentMeanHR = currentAttributesInstance
							.get("nextMeanHR");

					if (currentMeanHRValues.get(currentMeanHR) == null) {
						currentMeanHRValues.put(currentMeanHR, 1.0);
					} else {
						currentMeanHRValues.put(currentMeanHR,
								currentMeanHRValues.get(currentMeanHR) + 1.0);
					}

					List<HashMap<String, Integer>> currentAttributesForProbabilitiesList = currentAttributesForProbabilities
							.get(currentMeanHR);
					if (currentAttributesForProbabilitiesList == null) {
						currentAttributesForProbabilitiesList = new ArrayList<HashMap<String, Integer>>();
					}
					currentAttributesForProbabilitiesList
							.add(currentAttributesInstance);
					currentAttributesForProbabilities.put(currentMeanHR,
							currentAttributesForProbabilitiesList);
				}
			}

			Set<Integer> currentKeys = currentMeanHRValues.keySet();

			Iterator<Integer> iteratorCurrentKeys = currentKeys.iterator();

			while (iteratorCurrentKeys.hasNext()) {
				Integer currentKey = iteratorCurrentKeys.next();
				Double currentValue = currentMeanHRValues.get(currentKey);
				Double currentProcent = currentValue * 100 / meanHRNumber;

				currentMeanHRValues.put(currentKey, currentProcent);
			}

			// probabilities.put(key, currentMeanHRValues);
			probabilities.put(key,
					getRelevantProbabilities(currentMeanHRValues));

			attribuesForProbabilities.put(key,
					currentAttributesForProbabilities);
		}

		System.out.println("Probabilities: " + probabilities);
		System.out.println("Probabilities Attributes: "
				+ attribuesForProbabilities);

		if (learnPhase) {
			trainAttribuesForProbabilities = attribuesForProbabilities;
		} else {
			testAttribuesForProbabilities = attribuesForProbabilities;
		}

		return probabilities;
	}

	public HashMap<Integer, Double> getRelevantProbabilities(
			HashMap<Integer, Double> currentMeanHRValues) {

		HashMap<Integer, Double> newMeanHRValues = new HashMap<Integer, Double>();
		newMeanHRValues.putAll(currentMeanHRValues);

		int checkSize = newMeanHRValues.size();

		int procent = 0;

		if (checkSize < 7) { // under 7% not relevant
			procent = 7;
		} else if (checkSize < 9) { // under 5% not relevant
			procent = 5;
		}

		Iterator<Integer> meanHRValuesIterator = currentMeanHRValues.keySet()
				.iterator();

		while (meanHRValuesIterator.hasNext() && procent != 0) {
			Integer key = meanHRValuesIterator.next();
			if (newMeanHRValues.get(key) < procent) {
				newMeanHRValues.remove(key);
			}
		}

		return newMeanHRValues;
	}

	public void testPhase1(
			HashMap<Integer, List<HashMap<String, Integer>>> attributesLearned,
			List<HealthDataFeatures> heartrateDataTest) {

		HashMap<Integer, Double> meanProcentages = new HashMap<Integer, Double>();
		HashMap<Integer, List<Double>> procentages = new HashMap<Integer, List<Double>>();

		for (int i = 0; i < heartrateDataTest.size(); i++) {

			HealthDataFeatures currentHealthDataFeatures = heartrateDataTest
					.get(i);

			Integer currentMeanHR = currentHealthDataFeatures.getMeanHR();

			List<HashMap<String, Integer>> healthDataFromTraining = attributesLearned
					.get(currentMeanHR);

			if (healthDataFromTraining != null) {

				// the comment below

				List<String> features = new ArrayList<String>();

				for (int j = 0; j < FEATURES_CONSIDERED; j++) {
					String currentFeature = FEATURES_STRINGS[j];
					int currentTreshold = FEATURES_TRESHOLDS[j];

					boolean feature = false;

					for (int k = 0; k < healthDataFromTraining.size()
							&& healthDataFromTraining.size() > 2; k++) {

						HashMap<String, Integer> currentValues = healthDataFromTraining
								.get(k);

						if (currentHealthDataFeatures
								.getFeature(currentFeature) <= currentValues
								.get(currentFeature) + currentTreshold
								&& currentHealthDataFeatures
										.getFeature(currentFeature) >= currentValues
										.get(currentFeature) - currentTreshold) {
							feature = true;
							break;
						}
					}

					if (feature) {
						features.add(currentFeature);
					}
				}

				if (!features.isEmpty()) {

					List<Double> procentagesList = procentages
							.get(currentMeanHR);

					if (procentagesList == null) {
						procentagesList = new ArrayList<Double>();
					}

					procentagesList.add((double) features.size() * 100
							/ FEATURES_CONSIDERED);

					procentages.put(currentMeanHR, procentagesList);
				}
			}
		}

		Iterator<Integer> procentagesIterator = procentages.keySet().iterator();

		while (procentagesIterator.hasNext()) {
			Integer key = procentagesIterator.next();
			List<Double> currentProcentages = procentages.get(key);

			if (currentProcentages.size() > 2) {
				currentProcentages = removeMinMax(currentProcentages);
			}
			meanProcentages.put(key,
					calculateMeanProcentage(currentProcentages));
		}

		System.out.println("Procentages: " + procentages);
		System.out.println("Mean Procentages: " + meanProcentages);

		Iterator<Integer> meanProcentagesIterator = meanProcentages.keySet()
				.iterator();
		List<Double> meanPorcentagesList = new ArrayList<Double>();

		while (meanProcentagesIterator.hasNext()) {
			Integer key = meanProcentagesIterator.next();
			meanPorcentagesList.add(meanProcentages.get(key));
		}

		System.out.println("Mean Procentage: "
				+ calculateMeanProcentage(meanPorcentagesList));
	}

	public void testPhase2(
			HashMap<Integer, List<HashMap<String, Integer>>> attributesLearned,
			List<HealthDataFeatures> heartrateDataTest) {

		HashMap<Integer, Double> meanProcentages = new HashMap<Integer, Double>();
		HashMap<Integer, List<Double>> procentages = new HashMap<Integer, List<Double>>();

		for (int i = 0; i < heartrateDataTest.size(); i++) {

			HealthDataFeatures currentHealthDataFeatures = heartrateDataTest
					.get(i);

			Integer currentMeanHR = currentHealthDataFeatures.getMeanHR();

			List<HashMap<String, Integer>> healthDataFromTraining = attributesLearned
					.get(currentMeanHR);

			if (healthDataFromTraining != null) {

				List<String> features = new ArrayList<String>();

				List<HashMap<String, Integer>> foundValuesList = new ArrayList<HashMap<String, Integer>>();

				for (int j = 0; j < FEATURES_CONSIDERED; j++) {
					String currentFeature = FEATURES_STRINGS[j];
					int currentTreshold = FEATURES_TRESHOLDS[j];

					boolean feature = false;

					if (foundValuesList.isEmpty()) {
						for (int k = 0; k < healthDataFromTraining.size()
								&& healthDataFromTraining.size() > 2; k++) {

							HashMap<String, Integer> currentValues = healthDataFromTraining
									.get(k);

							if (currentHealthDataFeatures
									.getFeature(currentFeature) <= currentValues
									.get(currentFeature) + currentTreshold
									&& currentHealthDataFeatures
											.getFeature(currentFeature) >= currentValues
											.get(currentFeature)
											- currentTreshold) {
								feature = true;
								foundValuesList.add(currentValues);
							}
						}
					} else {

						for (int k = 0; k < foundValuesList.size(); k++) {

							HashMap<String, Integer> currentValues = foundValuesList
									.get(k);

							if (currentHealthDataFeatures
									.getFeature(currentFeature) <= currentValues
									.get(currentFeature) + currentTreshold
									&& currentHealthDataFeatures
											.getFeature(currentFeature) >= currentValues
											.get(currentFeature)
											- currentTreshold) {
								feature = true;
								continue;
							}
							foundValuesList.remove(currentValues);
							k--;
						}
					}

					if (feature) {
						features.add(currentFeature);
					}
				}

				if (!features.isEmpty()) {

					List<Double> procentagesList = procentages
							.get(currentMeanHR);

					if (procentagesList == null) {
						procentagesList = new ArrayList<Double>();
					}

					procentagesList.add((double) features.size() * 100
							/ FEATURES_CONSIDERED);

					procentages.put(currentMeanHR, procentagesList);
				}
			}
		}

		Iterator<Integer> procentagesIterator = procentages.keySet().iterator();

		while (procentagesIterator.hasNext()) {
			Integer key = procentagesIterator.next();
			List<Double> currentProcentages = procentages.get(key);

			if (currentProcentages.size() > 2) {
				currentProcentages = removeMinMax(currentProcentages);
			}
			meanProcentages.put(key,
					calculateMeanProcentage(currentProcentages));
		}

		System.out.println("Procentages: " + procentages);
		System.out.println("Mean Procentages: " + meanProcentages);

		Iterator<Integer> meanProcentagesIterator = meanProcentages.keySet()
				.iterator();
		List<Double> meanPorcentagesList = new ArrayList<Double>();

		while (meanProcentagesIterator.hasNext()) {
			Integer key = meanProcentagesIterator.next();
			meanPorcentagesList.add(meanProcentages.get(key));
		}

		System.out.println("Mean Procentage: "
				+ calculateMeanProcentage(meanPorcentagesList));
	}

	public void testPhase3(
			HashMap<Integer, List<HashMap<String, Integer>>> attributesLearned,
			HashMap<Integer, HashMap<Integer, Double>> probabilitiesTrain,
			HashMap<Integer, List<HashMap<String, Integer>>> attributesLearnedForTest,
			HashMap<Integer, HashMap<Integer, Double>> probabilitiesTest) {

		HashMap<Integer, Double> meanProcentages = new HashMap<Integer, Double>();

		Iterator<Integer> probabilitiesTestIterator = probabilitiesTest
				.keySet().iterator();

		List<Double> includedNumbersList = new ArrayList<Double>();
		List<Double> inclusionProcentagesList = new ArrayList<Double>();
		List<HashMap<Integer, Double>> currentIncludedProbabilitiesList = new ArrayList<HashMap<Integer, Double>>();

		while (probabilitiesTestIterator.hasNext()) {

			Integer probabilitiesTestKey = probabilitiesTestIterator.next();

			if (probabilitiesTrain.get(probabilitiesTestKey) != null) {

				if (!probabilitiesTest.get(probabilitiesTestKey).isEmpty()
						&& !probabilitiesTrain.get(probabilitiesTestKey)
								.isEmpty()) {

					Integer maxKeyProbTest = getMaxMin(
							probabilitiesTrain.get(probabilitiesTestKey)
									.keySet(), true) + 1;
					Integer minKeyProbTest = getMaxMin(
							probabilitiesTrain.get(probabilitiesTestKey)
									.keySet(), false) - 1;

					Iterator<Integer> currentProbabilitiesTestIterator = probabilitiesTest
							.get(probabilitiesTestKey).keySet().iterator();

					double included = 0;
					HashMap<Integer, Double> currentIncludedProbabilities = new HashMap<Integer, Double>();

					while (currentProbabilitiesTestIterator.hasNext()) {
						Integer currentProbabilitiesTestKey = currentProbabilitiesTestIterator
								.next();
						if (currentProbabilitiesTestKey <= maxKeyProbTest
								&& currentProbabilitiesTestKey >= minKeyProbTest) {

							if (currentProbabilitiesTestKey < maxKeyProbTest
									&& currentProbabilitiesTestKey > minKeyProbTest) {

								int differenceNextPossibilities = probabilitiesTest
										.get(probabilitiesTestKey).size()
										- probabilitiesTrain.get(
												probabilitiesTestKey).size();

								if (differenceNextPossibilities < 0) {
									differenceNextPossibilities = 0 - differenceNextPossibilities;
								}

								double difference = 0;
								double testWithTrain = 0;
								int newKeyTrain = 0;

								if (probabilitiesTrain
										.get(probabilitiesTestKey).get(
												currentProbabilitiesTestKey) != null) {
									testWithTrain = probabilitiesTrain.get(
											probabilitiesTestKey).get(
											currentProbabilitiesTestKey);
									newKeyTrain = currentProbabilitiesTestKey;
								} else {
									int i = 0;
									while (true) {
										if (probabilitiesTrain
												.get(probabilitiesTestKey)
												.get(currentProbabilitiesTestKey
														+ i) != null) {
											testWithTrain = probabilitiesTrain
													.get(probabilitiesTestKey)
													.get(currentProbabilitiesTestKey
															+ i);
											newKeyTrain = currentProbabilitiesTestKey
													+ i;
											break;
										} else if (probabilitiesTrain
												.get(probabilitiesTestKey)
												.get(currentProbabilitiesTestKey
														- i) != null) {
											testWithTrain = probabilitiesTrain
													.get(probabilitiesTestKey)
													.get(currentProbabilitiesTestKey
															- i);
											newKeyTrain = currentProbabilitiesTestKey
													- i;
											break;
										}
										i++;
									}
								}

								if (differenceNextPossibilities < 4) {

									difference = probabilitiesTest.get(
											probabilitiesTestKey).get(
											currentProbabilitiesTestKey)
											- testWithTrain;
								} else {
									difference = probabilitiesTest.get(
											probabilitiesTestKey).get(
											currentProbabilitiesTestKey)
											- (testWithTrain * 1.5);
								}
								if (difference < 0) {
									difference = 0 - difference;
								}

								if (difference == 0) {

									double procentage = calculateMeanProcentage(getProcentagesList(
											probabilitiesTestKey, newKeyTrain,
											currentProbabilitiesTestKey));

									if (procentage >= 85) {
										included += 1;
									} else if (procentage < 85
											&& procentage >= 70) {
										included += 0.9;
									} else if (procentage < 50) {
										included += 0.7;
									} else {
										included += 0.8;
									}
									currentIncludedProbabilities
											.put(currentProbabilitiesTestKey,
													probabilitiesTest
															.get(probabilitiesTestKey)
															.get(currentProbabilitiesTestKey));

								} else if (difference <= 40) {

									double procentage = calculateMeanProcentage(getProcentagesList(
											probabilitiesTestKey, newKeyTrain,
											currentProbabilitiesTestKey));

									if (procentage >= 10) {
										included += 1;
									} else if (procentage < 5) {
										included += 0.7;
									} else {
										included += 0.8;
									}
									currentIncludedProbabilities
											.put(currentProbabilitiesTestKey,
													probabilitiesTest
															.get(probabilitiesTestKey)
															.get(currentProbabilitiesTestKey));
								} else {

									double procentage = calculateMeanProcentage(getProcentagesList(
											probabilitiesTestKey, newKeyTrain,
											currentProbabilitiesTestKey));

									if (procentage >= 90) {
										included += 1;
									} else if (procentage >= 60
											&& procentage < 90) {
										included += 0.9;
									} else if (procentage < 60
											&& procentage >= 40) {
										included += 0.7;
									} else if (procentage < 10) {
										included += 0.5;
									} else {
										included += 0.6;
									}
									currentIncludedProbabilities
											.put(currentProbabilitiesTestKey,
													probabilitiesTest
															.get(probabilitiesTestKey)
															.get(currentProbabilitiesTestKey));
								}
							} else {
								int newKeyTrain = 0;

								if (trainAttribuesForProbabilities.get(
										probabilitiesTestKey).get(
										currentProbabilitiesTestKey + 1) != null) {
									newKeyTrain = currentProbabilitiesTestKey + 1;
								} else {
									newKeyTrain = currentProbabilitiesTestKey - 1;
								}

								double procentage = calculateMeanProcentage(getProcentagesList(
										probabilitiesTestKey, newKeyTrain,
										currentProbabilitiesTestKey));

								if (procentage >= 50) {
									included += 1;
								} else if (procentage < 60 && procentage >= 20) {
									included += 0.9;
								} else if (procentage < 10) {
									included += 0.7;
								} else {
									included += 0.8;
								}
								currentIncludedProbabilities.put(
										currentProbabilitiesTestKey,
										probabilitiesTest.get(
												probabilitiesTestKey).get(
												currentProbabilitiesTestKey));
							}
						} else {

							double differenceMax = currentProbabilitiesTestKey
									- maxKeyProbTest;
							double differenceMin = currentProbabilitiesTestKey
									- minKeyProbTest;

							if (differenceMax < 0) {
								differenceMax = 0 - differenceMax;
							}
							if (differenceMin < 0) {
								differenceMin = 0 - differenceMin;
							}

							double difference = differenceMax < differenceMin ? differenceMax
									: differenceMin;

							if (difference <= 3) {
								included -= 0.1;
							} else if (difference > 3 && difference <= 5) {
								included -= 0.5;
							} else {
								included -= 1;
							}
						}
					}

					double inclusionProcentage = 0;

					if (included > 0.5
							|| (included < -0.1 && isRelevant(
									includedNumbersList, included))) {
						inclusionProcentage = included
								* 100
								/ probabilitiesTest.get(probabilitiesTestKey)
										.keySet().size();
					}

					includedNumbersList.add(included);
					inclusionProcentagesList.add(inclusionProcentage);
					currentIncludedProbabilitiesList
							.add(currentIncludedProbabilities);

					// meanProcentages.put(probabilitiesTestKey,
					// inclusionProcentage);

					// if (inclusionProcentage >= 55) {
					//
					// int negatives = 0;
					// int positives = 0;
					// int high = 0;
					//
					// for (Double includedNumber : includedNumbersList) {
					// if (includedNumber < 0) {
					// negatives++;
					// } else if (includedNumber < 6) {
					// positives++;
					// } else {
					// high++;
					// }
					// }
					// if (high > positives) {
					// inclusionProcentage = 100;
					// } else if ((positives + high) > negatives + 5) {
					// inclusionProcentage = 100;
					// } else if (positives > negatives) {
					// inclusionProcentage = 80;
					// } else {
					// inclusionProcentage = 50;
					// }
					//
					// // meanProcentages.put(probabilitiesTestKey,
					// // Double.valueOf(100));
					// } else {
					//
					// if (inclusionProcentage >= 45) {
					//
					// inclusionProcentage = getAdjustedInclusionProcentage(
					// inclusionProcentage,
					// currentIncludedProbabilities,
					// probabilitiesTrain, probabilitiesTestKey,
					// 4, 80, 7, 50);
					//
					// } else if (inclusionProcentage > 20) {
					//
					// inclusionProcentage = getAdjustedInclusionProcentage(
					// inclusionProcentage,
					// currentIncludedProbabilities,
					// probabilitiesTrain, probabilitiesTestKey,
					// 5, 90, 8, 40);
					// } else {
					// inclusionProcentage = 0;
					// }
					//
					// // meanProcentages.put(probabilitiesTestKey,
					// // inclusionProcentage);
					// }
					//
					// meanProcentages.put(probabilitiesTestKey,
					// inclusionProcentage);
				}
			}
		}

		Iterator<Integer> probabilitiesTestIteratorNew = probabilitiesTest
				.keySet().iterator();

		// Collections.sort(inclusionProcentagesList);
		//
		// System.out.println(inclusionProcentagesList);

		boolean noConsideration = false;
		boolean condiderHundreds = true;
		int noConsiderationNumber = 0;
		int allNegatives = 0;
		int hundreds = 0;
		int under50 = 0;

		for (int i = 0; i < inclusionProcentagesList.size(); i++) {
			double inclusionProcentage = inclusionProcentagesList.get(i);
			if (inclusionProcentage <= -95) {
				condiderHundreds = false;
			}
		}

		for (int i = 0; i < inclusionProcentagesList.size(); i++) {
			double inclusionProcentage = inclusionProcentagesList.get(i);
			if (inclusionProcentage < -60) {
				noConsiderationNumber++;
			}
			if (inclusionProcentage < -10) {
				allNegatives++;
			}
			if (noConsiderationNumber > 1 || allNegatives >= 5) {
				noConsideration = true;
				break;
			}
			if (inclusionProcentage >= 95 && condiderHundreds) {
				hundreds++;
			}
			if (inclusionProcentage <= 70) {
				under50++;
			}
		}
		if (!noConsideration) {
			if (under50 > hundreds) {
				noConsideration = true;
			}
		}

		for (int i = 0; i < inclusionProcentagesList.size(); i++) {

			double inclusionProcentage = inclusionProcentagesList.get(i);
			Integer probabilitiesTestKey = probabilitiesTestIteratorNew.next();

			if (inclusionProcentage >= 55 && !noConsideration) {

				int negatives = 0;
				int positives = 0;
				int high = 0;

				for (Double includedNumber : includedNumbersList) {
					if (includedNumber < 0) {
						negatives++;
					} else if (includedNumber < 6) {
						positives++;
					} else {
						high++;
					}
				}
				if (high > positives) {
					inclusionProcentage = 100.0;
				} else if ((positives + high) > negatives + 5) {
					inclusionProcentage = 100.0;
				} else if (positives > negatives) {
					inclusionProcentage = 80.0;
				} else {
					inclusionProcentage = 50.0;
				}

				// meanProcentages.put(probabilitiesTestKey,
				// Double.valueOf(100));
			} else {
				if (inclusionProcentage >= 55 && noConsideration) {
					inclusionProcentage = 0;
				} else if (inclusionProcentage >= 45) {

					inclusionProcentage = getAdjustedInclusionProcentage(
							inclusionProcentage,
							currentIncludedProbabilitiesList.get(i),
							probabilitiesTrain, probabilitiesTestKey, 4, 80, 7,
							50);

				} else if (inclusionProcentage > 20) {

					inclusionProcentage = getAdjustedInclusionProcentage(
							inclusionProcentage,
							currentIncludedProbabilitiesList.get(i),
							probabilitiesTrain, probabilitiesTestKey, 5, 90, 8,
							40);
				} else { // if (inclusionProcentage >= 0)
					inclusionProcentage = 0;
				}

				// meanProcentages.put(probabilitiesTestKey,
				// inclusionProcentage);
			}

			meanProcentages.put(probabilitiesTestKey, inclusionProcentage);

		}

		Iterator<Integer> meanProcentagesIterator = meanProcentages.keySet()
				.iterator();
		List<Double> meanPorcentagesList = new ArrayList<Double>();

		while (meanProcentagesIterator.hasNext()) {
			Integer key = meanProcentagesIterator.next();
			meanPorcentagesList.add(meanProcentages.get(key));
		}

		System.out.println("Mean Procentage new: "
				+ calculateMeanProcentage(meanPorcentagesList));
	}

	public boolean isRelevant(List<Double> includedNumbersList, double included) {

		for (Double includedNumber : includedNumbersList) {
			if (includedNumber < -0.1) {
				return true;
			}
		}

		return false;
	}

	public List<Double> getProcentagesList(Integer probabilitiesTestKey,
			Integer newKeyTrain, Integer currentProbabilitiesTestKey) {

		List<HashMap<String, Integer>> trainAttributes = trainAttribuesForProbabilities
				.get(probabilitiesTestKey).get(newKeyTrain);
		List<HashMap<String, Integer>> testAttributes = testAttribuesForProbabilities
				.get(probabilitiesTestKey).get(currentProbabilitiesTestKey);

		int i = 0;
		List<HashMap<String, Integer>> newTrainAttributes = new ArrayList<HashMap<String, Integer>>();
		List<Double> procentagesList = new ArrayList<Double>();

		for (HashMap<String, Integer> attributesTe : testAttributes) {

			if (i < trainAttributes.size()) {

				List<String> features = new ArrayList<String>();

				for (int j = 0; j < FEATURES_CONSIDERED_TEST; j++) {
					Integer currentValueTest = attributesTe
							.get(FEATURES_STRINGS[j]);

					boolean feature = false;

					if (newTrainAttributes.isEmpty()) {

						for (int k = 0; k < trainAttributes.size(); k++) {

							HashMap<String, Integer> currentValues = trainAttributes
									.get(k);

							Integer currentValueTrain = currentValues
									.get(FEATURES_STRINGS[j]);

							if (currentValueTest <= currentValueTrain
									+ FEATURES_TRESHOLDS[j]
									&& currentValueTest >= currentValueTrain
											- FEATURES_TRESHOLDS[j]) {
								newTrainAttributes.add(currentValues);
								feature = true;
							}
						}
					} else {

						for (int k = 0; k < newTrainAttributes.size(); k++) {

							HashMap<String, Integer> currentValues = newTrainAttributes
									.get(k);

							Integer currentValueTrain = currentValues
									.get(FEATURES_STRINGS[j]);

							if (currentValueTest <= currentValueTrain
									+ FEATURES_TRESHOLDS[j]
									&& currentValueTest >= currentValueTrain
											- FEATURES_TRESHOLDS[j]) {
								feature = true;
								continue;
							}
							newTrainAttributes.remove(currentValues);
						}
					}

					if (feature) {
						features.add(FEATURES_STRINGS[j]);
					}

					i++;
				}

				procentagesList.add((double) features.size() * 100
						/ FEATURES_CONSIDERED_TEST);

			}
		}

		return procentagesList;
	}

	public double getAdjustedInclusionProcentage(double inclusionProcentage,
			HashMap<Integer, Double> currentIncludedProbabilities,
			HashMap<Integer, HashMap<Integer, Double>> probabilitiesTrain,
			Integer probabilitiesTestKey, int tresholdDifference1,
			int tresholdProcent1, int tresholdDifference2, int tresholdProcent2) {

		Iterator<Integer> includedProbabilitiesInterator = currentIncludedProbabilities
				.keySet().iterator();
		List<Double> differences = new ArrayList<Double>();

		if (probabilitiesTrain.get(probabilitiesTestKey) != null) {

			while (includedProbabilitiesInterator.hasNext()) {

				Integer currentIncludedKey = includedProbabilitiesInterator
						.next();
				Double currentIncludedKeyValue = currentIncludedProbabilities
						.get(currentIncludedKey);
				Double trainIncludedKeyValue = probabilitiesTrain.get(
						probabilitiesTestKey).get(currentIncludedKey);

				if (trainIncludedKeyValue != null) {

					double difference = currentIncludedKeyValue
							- trainIncludedKeyValue;
					if (difference < 0) {
						difference = 0 - difference;
					}

					differences.add(difference);
				}
			}

			for (Double difference : differences) {
				if (difference < tresholdDifference1
						&& inclusionProcentage <= tresholdProcent1) {
					inclusionProcentage += (100 - tresholdProcent1);
				} else if (difference > tresholdDifference2
						&& inclusionProcentage >= tresholdProcent2) {
					inclusionProcentage -= tresholdProcent2;
				}
			}
		}
		return inclusionProcentage;
	}

	public Integer getMaxMin(Set<Integer> probabilitiesKeys, boolean max) {

		Integer maxMin = 0;

		Iterator<Integer> probabilitiesKeysIterator = probabilitiesKeys
				.iterator();

		if (probabilitiesKeysIterator.hasNext()) {
			maxMin = probabilitiesKeysIterator.next();
		}
		while (probabilitiesKeysIterator.hasNext()) {
			Integer nextValue = probabilitiesKeysIterator.next();
			if (max) {
				if (maxMin < nextValue) {
					maxMin = nextValue;
				}
			} else {
				if (maxMin > nextValue) {
					maxMin = nextValue;
				}
			}
		}

		return maxMin;
	}

	public Double calculateMeanProcentage(List<Double> procentages) {

		double sum = 0;

		for (int i = 0; i < procentages.size(); i++) {
			sum += procentages.get(i);
		}

		return sum / procentages.size();
	}

	public List<Double> removeMinMax(List<Double> procentages) {

		double max = procentages.get(0);
		double min = procentages.get(0);

		for (int i = 1; i < procentages.size(); i++) {

			double procent = procentages.get(i);

			if (procent > max) {
				max = procent;
			}
			if (procent < min) {
				min = procent;
			}
		}

		int maxNr = 0;
		int minNr = 0;

		for (Double procent : procentages) {

			if (procent == max) {
				maxNr++;
			}
			if (procent == min) {
				minNr++;
			}
		}

		// if (maxNr == 1) {
		// procentages.remove(max);
		// }
		// if (minNr == 1) {
		// procentages.remove(min);
		// }

		if (maxNr > minNr) {
			while (procentages.size() > 1) {
				if (!procentages.remove(min)) {
					break;
				}
			}
		} else {
			while (procentages.size() > 1) {
				if (!procentages.remove(max)) {
					break;
				}
			}
		}

		return procentages;
	}

	// List<String> features = new ArrayList<String>();
	// int totalFeatures = 0;
	//
	// for (int j = 0; j < healthDataFromTraining.size(); j++) {
	//
	// String currentFeature = FEATURES_STRINGS[totalFeatures];
	// totalFeatures++;
	//
	// if (totalFeatures > 16) {
	// break;
	// }
	//
	// boolean feature = false;
	//
	// for (int k = 0; k < healthDataFromTraining.size(); k++) {
	//
	// HashMap<String, Integer> currentValues = healthDataFromTraining
	// .get(k);
	//
	// if (currentHealthDataFeatures
	// .getFeature(currentFeature) <= currentValues
	// .get(currentFeature) + 5
	// && currentHealthDataFeatures.getMeanHRV() >= currentValues
	// .get(currentFeature) - 5) {
	// feature = true;
	// }
	// }
	//
	// if (feature) {
	// features.add(currentFeature);
	// }
	// }
	//
	// List<Double> procentagesList = procentages.get(currentMeanHR);
	//
	// if (procentagesList == null) {
	// procentagesList = new ArrayList<Double>();
	// }
	//
	// procentagesList.add((double) features.size() * 100
	// / (double) totalFeatures);
	//
	// procentages.put(currentMeanHR, procentagesList);
}
