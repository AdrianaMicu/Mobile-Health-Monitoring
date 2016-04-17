package monitoring.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import monitoring.model.HRwithHRV;
import monitoring.model.HealthData;
import monitoring.utils.ExportUtils;
//import monitoring.utils.HealthDataRepository;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.springframework.cglib.core.Local;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import sun.font.LayoutPathImpl.EndType;

@RestController
@RequestMapping({ "/step2CallsController" })
public class Step2Controller {

	@RequestMapping(value = "/healths", method = RequestMethod.GET)
	@ResponseBody
	public List<HealthData> viewAll(HttpServletResponse response,
			@RequestParam MultiValueMap<String, String> messageParams) {

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods",
				"POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept");

		String type = messageParams.getFirst("type");

		List<HealthData> heartrateData = new ArrayList<HealthData>();
		List<HealthData> heartrateData1 = new ArrayList<HealthData>();
		List<HealthData> stepcountData = new ArrayList<HealthData>();

		if (type.equalsIgnoreCase("calculateDifferences")) {

			heartrateData = ExportUtils
					.readFromCSV("TEST/Adriana-hr17may/Adriana-hr17may-restNormal.csv");
			heartrateData1 = ExportUtils.readFromCSV("TEST/test2.csv");

			segmentData(heartrateData, 300);

			List<HealthData> hrNoZeros = removeZeros(heartrateData);
			List<HealthData> hrNoZeros1 = removeZeros(heartrateData1);
			int seconds = 60;

			List<HealthData> meanHRinSegment = meanHRforSegment(hrNoZeros,
					seconds);
			List<HealthData> meanHRinSegment1 = meanHRforSegment(hrNoZeros1,
					seconds);
			callCalculateDifferences(meanHRinSegment, meanHRinSegment1);

			List<HealthData> stdDevHRinSegment = stdDeviationHRforSegment(
					hrNoZeros, meanHRinSegment, seconds);
			List<HealthData> stdDevHRinSegment1 = stdDeviationHRforSegment(
					hrNoZeros1, meanHRinSegment1, seconds);
			callCalculateDifferences(stdDevHRinSegment, stdDevHRinSegment1);

			// List<HealthData> stepsInSegment = stepsForSegment(
			// meanHRinSegment, stepcountData, seconds);
			// List<HealthData> stepsInSegment = stepsForSegment(
			// meanHRinSegment, stepcountData, seconds);

			List<HealthData> hrv = getHRV(hrNoZeros);
			List<HealthData> hrv1 = getHRV(hrNoZeros1);
			List<HealthData> hrvEctopicInSegment = hrvEctopicForSegment(hrv,
					seconds);
			List<HealthData> hrvEctopicInSegment1 = hrvEctopicForSegment(hrv1,
					seconds);
			callCalculateDifferences(hrvEctopicInSegment, hrvEctopicInSegment1);

			List<HealthData> hrvNoEctopic = removeEctopicBeats(hrv,
					heartrateData);
			List<HealthData> hrvNoEctopic1 = removeEctopicBeats(hrv1,
					heartrateData1);

			List<HealthData> meanHRVinSegment = meanHRVforSegment(hrvNoEctopic,
					seconds);
			List<HealthData> meanHRVinSegment1 = meanHRVforSegment(
					hrvNoEctopic1, seconds);
			callCalculateDifferences(meanHRVinSegment, meanHRVinSegment1);

			List<HealthData> stdDevHRVinSegment = stdDeviationHRVforSegment(
					hrvNoEctopic, meanHRVinSegment, seconds);
			List<HealthData> stdDevHRVinSegment1 = stdDeviationHRVforSegment(
					hrvNoEctopic1, meanHRVinSegment1, seconds);
			callCalculateDifferences(stdDevHRVinSegment, stdDevHRVinSegment1);

			List<HealthData> medianHRVinSegment = medianHRVforSegment(
					hrvNoEctopic, seconds);
			List<HealthData> medianHRVinSegment1 = medianHRVforSegment(
					hrvNoEctopic1, seconds);
			callCalculateDifferences(medianHRVinSegment, medianHRVinSegment1);

			List<HealthData> nn50inSegment = nn50forSegment(50, hrvNoEctopic,
					seconds);
			List<HealthData> nn50inSegment1 = nn50forSegment(50, hrvNoEctopic1,
					seconds);
			callCalculateDifferences(nn50inSegment, nn50inSegment1);

			List<HealthData> nn20inSegment = nn50forSegment(20, hrvNoEctopic,
					seconds);
			List<HealthData> nn20inSegment1 = nn50forSegment(20, hrvNoEctopic1,
					seconds);
			callCalculateDifferences(nn20inSegment, nn20inSegment1);

			List<HealthData> rmssdInSegment = rmssdForSegment(hrvNoEctopic,
					seconds);
			List<HealthData> rmssdInSegment1 = rmssdForSegment(hrvNoEctopic1,
					seconds);
			callCalculateDifferences(rmssdInSegment, rmssdInSegment1);

			List<HealthData> sdsdInSegment = sdsdForSegment(hrvNoEctopic,
					meanSDforSegment(hrvNoEctopic, seconds), seconds);
			List<HealthData> sdsdInSegment1 = sdsdForSegment(hrvNoEctopic1,
					meanSDforSegment(hrvNoEctopic1, seconds), seconds);
			callCalculateDifferences(sdsdInSegment, sdsdInSegment1);

			List<HealthData> minNNinSegment = minMaxNNforSegment(hrvNoEctopic,
					seconds, true);
			List<HealthData> minNNinSegment1 = minMaxNNforSegment(
					hrvNoEctopic1, seconds, true);
			callCalculateDifferences(minNNinSegment, minNNinSegment1);

			List<HealthData> maxNNinSegment = minMaxNNforSegment(hrvNoEctopic,
					seconds, false);
			List<HealthData> maxNNinSegment1 = minMaxNNforSegment(
					hrvNoEctopic1, seconds, false);
			callCalculateDifferences(maxNNinSegment, maxNNinSegment1);

			List<HealthData> hrvTriangularIndexInSegment = hrvTriangularIndexForSegment(
					hrvNoEctopic, seconds);
			List<HealthData> hrvTriangularIndexInSegment1 = hrvTriangularIndexForSegment(
					hrvNoEctopic1, seconds);
			callCalculateDifferences(hrvTriangularIndexInSegment,
					hrvTriangularIndexInSegment1);

			List<HealthData> tinnInSegment = tinnForSegment(hrvNoEctopic,
					seconds);
			List<HealthData> tinnInSegment1 = tinnForSegment(hrvNoEctopic1,
					seconds);
			callCalculateDifferences(tinnInSegment, tinnInSegment1);

			List<HealthData> sd1InSegment = sd1ForSegment(sdsdInSegment,
					seconds);
			List<HealthData> sd1InSegment1 = sd1ForSegment(sdsdInSegment1,
					seconds);
			callCalculateDifferences(sd1InSegment, sd1InSegment1);

			List<HealthData> sd2InSegment = sd2ForSegment(stdDevHRVinSegment,
					sdsdInSegment, seconds);
			List<HealthData> sd2InSegment1 = sd2ForSegment(stdDevHRVinSegment1,
					sdsdInSegment1, seconds);
			callCalculateDifferences(sd2InSegment, sd2InSegment1);

			List<HealthData> spEnInSegment = spEnForSegment(hrvNoEctopic,
					seconds);
			List<HealthData> spEnInSegment1 = spEnForSegment(hrvNoEctopic1,
					seconds);
			callCalculateDifferences(spEnInSegment, spEnInSegment1);
		}

		if (type.equalsIgnoreCase("readHR")) {

			String[] filenames2 = { "Adriana-hr15jun", "Adriana-hr17may", "Adriana-hr20apr",
					"Adriana-hr21apr", "Adriana-hr22apr", "Adriana-hr23apr",
					"Bogdan-hr28apr", "Bogdan-hr30apr", "Toni-hr08may",
					"Toni-hr09may", "Toni-hr10may", "Vlad-hr07may" };

			String[] filenamesStep = { "Adriana-sc15jun", "Adriana-sc17may", "Adriana-sc20apr",
					"Adriana-sc21apr", "Adriana-sc22apr", "Adriana-sc23apr",
					"Bogdan-sc28apr", "Bogdan-sc30apr", "Toni-sc08may",
					"Toni-sc09may", "Toni-sc10may", "Vlad-sc07may" };

			String[] filenames1 = { "Adriana-hr17may-restNormal",
					"Adriana-hr21apr-restNormal", "Adriana-hr22apr-restNormal",
					"Adriana-hr23apr-restNormal", "Vlad-hr07may-restNormal",
					"Vlad-hr07may-restStressed", "Vlad-hr07may-trainRecovery",
					"Vlad-hr07may-trainLow", "Adriana-hr17may-restStressed",
					"Adriana-hr17may-trainRecovery",
					"Adriana-hr17may-trainLow", "Adriana-hr17may-trainVLow",
					"Adriana-hr20apr-restNormal",
					"Adriana-hr20apr-trainRecovery",
					"Adriana-hr20apr-trainVLow",
					"Adriana-hr21apr-restStressed",
					"Adriana-hr21apr-trainRecovery",
					"Adriana-hr21apr-trainVLow",
					"Adriana-hr22apr-restStressed",
					"Adriana-hr22apr-trainRecovery",
					"Adriana-hr22apr-trainLow", "Adriana-hr22apr-trainVLow",
					"Adriana-hr23apr-restStressed",
					"Adriana-hr23apr-trainRecovery",
					"Adriana-hr23apr-trainLow", "Adriana-hr23apr-trainVLow",
					"Bogdan-hr01may-restNormal",
					"Bogdan-hr01may-trainRecovery",
					"Bogdan-hr28apr-restNormal",
					"Bogdan-hr28apr-trainRecovery", "Bogdan-hr28apr-trainLow",
					"Bogdan-hr28apr-trainVLow", "Bogdan-hr30apr-restNormal",
					"Bogdan-hr30apr-trainRecovery", "Bogdan-hr30apr-trainLow",
					"Bogdan-hr30apr-trainVLow", "Toni-hr08may-restNormal",
					"Toni-hr08may-restStressed", "Toni-hr08may-trainRecovery",
					"Toni-hr08may-trainLow", "Toni-hr09may-restNormal",
					"Toni-hr09may-restStressed", "Toni-hr09may-trainRecovery",
					"Toni-hr09may-trainLow", "Toni-hr09may-trainVLow",
					"Toni-hr10may-restNormal", "Toni-hr10may-restStressed",
					"Toni-hr10may-trainRecovery", "Toni-hr10may-trainLow",
					"Toni-hr10may-trainVLow", };

			String[] filenames = {
					"Adriana-hr17may/Adriana-hr17may-restNormal",
					"Adriana-hr21apr/Adriana-hr21apr-restNormal",
					"Adriana-hr22apr/Adriana-hr22apr-restNormal",
					"Adriana-hr23apr/Adriana-hr23apr-restNormal",
					"Vlad-hr07may/Vlad-hr07may-restNormal",
					"Vlad-hr07may/Vlad-hr07may-restStressed",
					"Vlad-hr07may/Vlad-hr07may-trainRecovery",
					"Vlad-hr07may/Vlad-hr07may-trainLow",
					"Adriana-hr17may/Adriana-hr17may-restStressed",
					"Adriana-hr17may/Adriana-hr17may-trainRecovery",
					"Adriana-hr17may/Adriana-hr17may-trainLow",
					"Adriana-hr17may/Adriana-hr17may-trainVLow",
					"Adriana-hr20apr/Adriana-hr20apr-restNormal",
					"Adriana-hr20apr/Adriana-hr20apr-trainRecovery",
					"Adriana-hr20apr/Adriana-hr20apr-trainVLow",
					"Adriana-hr21apr/Adriana-hr21apr-restStressed",
					"Adriana-hr21apr/Adriana-hr21apr-trainRecovery",
					"Adriana-hr21apr/Adriana-hr21apr-trainVLow",
					"Adriana-hr22apr/Adriana-hr22apr-restStressed",
					"Adriana-hr22apr/Adriana-hr22apr-trainRecovery",
					"Adriana-hr22apr/Adriana-hr22apr-trainLow",
					"Adriana-hr22apr/Adriana-hr22apr-trainVLow",
					"Adriana-hr23apr/Adriana-hr23apr-restStressed",
					"Adriana-hr23apr/Adriana-hr23apr-trainRecovery",
					"Adriana-hr23apr/Adriana-hr23apr-trainLow",
					"Adriana-hr23apr/Adriana-hr23apr-trainVLow",
					"Bogdan-hr01may/Bogdan-hr01may-restNormal",
					"Bogdan-hr01may/Bogdan-hr01may-trainRecovery",
					"Bogdan-hr28apr/Bogdan-hr28apr-restNormal",
					"Bogdan-hr28apr/Bogdan-hr28apr-trainRecovery",
					"Bogdan-hr28apr/Bogdan-hr28apr-trainLow",
					"Bogdan-hr28apr/Bogdan-hr28apr-trainVLow",
					"Bogdan-hr30apr/Bogdan-hr30apr-restNormal",
					"Bogdan-hr30apr/Bogdan-hr30apr-trainRecovery",
					"Bogdan-hr30apr/Bogdan-hr30apr-trainLow",
					"Bogdan-hr30apr/Bogdan-hr30apr-trainVLow",
					"Toni-hr08may/Toni-hr08may-restNormal",
					"Toni-hr08may/Toni-hr08may-restStressed",
					"Toni-hr08may/Toni-hr08may-trainRecovery",
					"Toni-hr08may/Toni-hr08may-trainLow",
					"Toni-hr09may/Toni-hr09may-restNormal",
					"Toni-hr09may/Toni-hr09may-restStressed",
					"Toni-hr09may/Toni-hr09may-trainRecovery",
					"Toni-hr09may/Toni-hr09may-trainLow",
					"Toni-hr09may/Toni-hr09may-trainVLow",
					"Toni-hr10may/Toni-hr10may-restNormal",
					"Toni-hr10may/Toni-hr10may-restStressed",
					"Toni-hr10may/Toni-hr10may-trainRecovery",
					"Toni-hr10may/Toni-hr10may-trainLow",
					"Toni-hr10may/Toni-hr10may-trainVLow", };

			int seconds = 60;

			for (int i = 0; i < 1; i++) { // filenames2.length
				heartrateData = ExportUtils.readFromCSV("TEST/" + filenames2[i]
						+ ".csv");
				stepcountData = ExportUtils.readFromCSV("TEST/"
						+ filenamesStep[i] + ".csv");

				// if (filenames1[i]
				// .equalsIgnoreCase("Adriana-hr20apr-restNormal")) {
				// System.out.println();
				// }

				List<HealthData> hrNoZeros = removeZeros(heartrateData);

				List<HealthData> meanHRinSegment = meanHRforSegment(hrNoZeros,
						seconds); // ok
				List<HealthData> stdDevHRinSegment = stdDeviationHRforSegment(
						hrNoZeros, meanHRinSegment, seconds); // ok

				List<HealthData> stepsInSegment = stepsForSegment(
						meanHRinSegment, stepcountData, seconds); // ok

				List<HealthData> hrv = getHRV(hrNoZeros);
				List<HealthData> hrvEctopicInSegment = hrvEctopicForSegment(
						hrv, seconds); // ok
				List<HealthData> hrvNoEctopic = removeEctopicBeats(hrv,
						heartrateData);

				List<HealthData> meanHRVinSegment = meanHRVforSegment(
						hrvNoEctopic, seconds); // ok
				List<HealthData> stdDevHRVinSegment = stdDeviationHRVforSegment(
						hrvNoEctopic, meanHRVinSegment, seconds); // ok
				List<HealthData> stdDevHRVinLongSegment = stdDeviationHRVforSegment(
						hrvNoEctopic, meanHRVinSegment, 300);
				List<HealthData> medianHRVinSegment = medianHRVforSegment(
						hrvNoEctopic, seconds); // ok
				List<HealthData> nn50inAllData = nn50forAllDataset(50,
						hrvNoEctopic);
				List<HealthData> nn50inSegment = nn50forSegment(50,
						hrvNoEctopic, seconds); // ok
				List<HealthData> nn20inSegment = nn50forSegment(20,
						hrvNoEctopic, seconds); // ok
				List<HealthData> rmssdInSegment = rmssdForSegment(hrvNoEctopic,
						seconds); // ok
				List<HealthData> sdsdInSegment = sdsdForSegment(hrvNoEctopic,
						meanSDforSegment(hrvNoEctopic, seconds), seconds); // ok
				List<HealthData> minNNinSegment = minMaxNNforSegment(
						hrvNoEctopic, seconds, true); // ok
				List<HealthData> maxNNinSegment = minMaxNNforSegment(
						hrvNoEctopic, seconds, false); // ok
				List<HealthData> hrvTriangularIndexInSegment = hrvTriangularIndexForSegment(
						hrvNoEctopic, seconds); // ok
				List<HealthData> tinnInSegment = tinnForSegment(hrvNoEctopic,
						seconds); // ok

				List<HealthData> sd1InSegment = sd1ForSegment(sdsdInSegment,
						seconds); // ok
				List<HealthData> sd2InSegment = sd2ForSegment(
						stdDevHRVinSegment, sdsdInSegment, seconds); // ok
				List<HealthData> spEnInSegment = spEnForSegment(hrvNoEctopic,
						seconds); // ok

				// ExportUtils.writeToCSV(meanHRinSegment, stdDevHRinSegment,
				// hrvEctopicInSegment, meanHRVinSegment,
				// stdDevHRVinSegment, medianHRVinSegment, nn50inSegment,
				// nn20inSegment, rmssdInSegment, sdsdInSegment,
				// minNNinSegment, maxNNinSegment,
				// hrvTriangularIndexInSegment, tinnInSegment,
				// sd1InSegment, sd2InSegment, spEnInSegment, filenames[i]);

				// ExportUtils.writeToCSVMore(meanHRinSegment,
				// stdDevHRinSegment,
				// hrvEctopicInSegment, meanHRVinSegment,
				// stdDevHRVinSegment, medianHRVinSegment, nn50inSegment,
				// nn20inSegment, rmssdInSegment, sdsdInSegment,
				// minNNinSegment, maxNNinSegment,
				// hrvTriangularIndexInSegment, tinnInSegment,
				// sd1InSegment, sd2InSegment, spEnInSegment, filenames[i]);

				ExportUtils.writeToCSVNoTime(meanHRinSegment,
						stdDevHRinSegment, hrvEctopicInSegment,
						meanHRVinSegment, stdDevHRVinSegment,
						medianHRVinSegment, nn50inSegment, nn20inSegment,
						rmssdInSegment, sdsdInSegment, minNNinSegment,
						maxNNinSegment, hrvTriangularIndexInSegment,
						tinnInSegment, sd1InSegment, sd2InSegment,
						stepsInSegment, spEnInSegment, filenames2[i]);
			}

			System.out.println();
		}

		return null;
	}

	public static void segmentData(List<HealthData> data, int seconds) {

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		LocalDateTime startDateTime = LocalDateTime.parse(data.get(0)
				.getTimestamp(), formatter);
		LocalDateTime endDateTime = startDateTime.plusSeconds(seconds);

		for (HealthData healthData : data) {

			LocalDateTime currentDateTime = LocalDateTime.parse(
					healthData.getTimestamp(), formatter);

			if (currentDateTime.isBefore(endDateTime)) {

			}
		}
	}

	public static void callCalculateDifferences(List<HealthData> data,
			List<HealthData> data1) {

		List<Double> diff1 = calculateDifferences(data, data1, 0);
		List<Double> meanDiff1 = calculateDifferences(data, data1, 1);
		double meanAllDiff = calculateMeanOfAllDifferences(meanDiff1);
		System.out.println(meanAllDiff);
	}

	public static List<Double> calculateDifferences(List<HealthData> list1,
			List<HealthData> list2, int returnType) {

		List<Double> differences = new ArrayList<Double>();
		List<Double> meanDifferences = new ArrayList<Double>();

		for (HealthData healthData : list1) {

			double mean = 0;
			for (int i = 0; i < list2.size(); i++) {
				double difference = Double.valueOf(healthData.getValue())
						- Double.valueOf(list2.get(i).getValue());
				differences.add(difference);
				mean += difference;
				// System.out.println(difference);
			}
			meanDifferences.add(mean / list2.size());
		}

		if (returnType == 0) {
			return differences;
		} else {
			return meanDifferences;
		}
	}

	public static double calculateMeanOfAllDifferences(List<Double> list) {

		double mean = 0;
		for (Double nr : list) {
			if (nr < 0) {
				nr = 0 - nr;
			}
			mean += nr;
		}
		return mean / list.size();
	}

	public static List<HealthData> removeZeros(List<HealthData> heartrateData) {

		List<HealthData> heartrateDataNoZeros = new ArrayList<HealthData>();

		for (HealthData healthData : heartrateData) {
			heartrateDataNoZeros.add(healthData);
		}

		for (HealthData healthData : heartrateData) {
			if (Double.valueOf(healthData.getValue()) <= 0) {
				heartrateDataNoZeros.remove(healthData);
			}
		}

		return heartrateDataNoZeros;

		// List<HealthData> heartrateDataNoZeros = heartrateData;
		//
		// for (HealthData healthData : heartrateDataNoZeros) {
		// if (Double.valueOf(healthData.getValue()) <= 0) {
		// heartrateDataNoZeros.remove(healthData);
		// }
		// }
		//
		// return heartrateDataNoZeros;
	}

	public static List<HealthData> getHRV(List<HealthData> heartrateData) {

		List<HealthData> heartrateDataHRV = new ArrayList<HealthData>();

		DateTimeFormatter formatter1 = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		for (int i = 0; i < heartrateData.size() - 1; i++) {

			HealthData hd = heartrateData.get(i);
			HealthData hdNext = heartrateData.get(i + 1);

			String timeStart = hd.getTimestamp();
			String timeEnd = hdNext.getTimestamp();

			LocalDateTime dateStart = null;
			LocalDateTime dateEnd = null;
			try {
				dateStart = LocalDateTime.parse(timeStart, formatter1);
				dateEnd = LocalDateTime.parse(timeEnd, formatter1);
			} catch (Exception e) {

			}

			try {

				long duration = Duration.between(dateStart, dateEnd).toMillis();
				if (duration < 0) {
					duration = 0 - duration;
				}

				HealthData healthData = new HealthData();
				healthData.setTimestamp(hd.getTimestamp());
				healthData.setValue(String.valueOf(duration));

				heartrateDataHRV.add(healthData);

			} catch (Exception e) {

			}
		}

		// ExportUtils.writeToCSV(heartrateDataHRV, "Bogdan-hr30apr-HRV.csv");
		return heartrateDataHRV;
	}

	public static List<HealthData> hrvEctopicForSegment(
			List<HealthData> hrvData, int seconds) {

		List<HealthData> hrvEctopicInSegment = new ArrayList<HealthData>();

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		LocalDateTime startDateTime = LocalDateTime.parse(hrvData.get(0)
				.getTimestamp(), formatter);
		LocalDateTime endDateTime = startDateTime.plusSeconds(seconds);

		int nrEctopic = 0;
		boolean flagLastSaved = false;

		for (HealthData healthData : hrvData) {

			flagLastSaved = false;

			LocalDateTime currentDateTime = LocalDateTime.parse(
					healthData.getTimestamp(), formatter);

			double hd = Double.valueOf(healthData.getValue());

			if (currentDateTime.isBefore(endDateTime)) {
				if (hd > 2500) {
					nrEctopic++;
				}
			} else {
				HealthData healthDataLocal = new HealthData();
				healthDataLocal.setValue(String.valueOf(nrEctopic));
				healthDataLocal
						.setTimestamp((endDateTime.minusSeconds(seconds))
								.format(formatter));
				hrvEctopicInSegment.add(healthDataLocal);

				nrEctopic = 0;

				if (hd > 2500) {
					nrEctopic++;
				}

				endDateTime = endDateTime.plusSeconds(seconds);

				flagLastSaved = true;
			}
		}

		if (!flagLastSaved) {
			HealthData healthDataLocal = new HealthData();
			healthDataLocal.setValue(String.valueOf(nrEctopic));
			healthDataLocal.setTimestamp((endDateTime.minusSeconds(seconds))
					.format(formatter));
			hrvEctopicInSegment.add(healthDataLocal);
		}

		return hrvEctopicInSegment;
	}

	public static List<HealthData> removeEctopicBeats(
			List<HealthData> heartrateDataHRV, List<HealthData> heartrateData) {

		List<HealthData> heartrateDataLocal = heartrateData;
		List<HealthData> heartrateDataNoEctopic = heartrateDataHRV;

		SplineInterpolator splineInterpolator = new SplineInterpolator();

		for (int i = 0; i < heartrateDataHRV.size(); i++) {

			double hd = Double.valueOf(heartrateDataHRV.get(i).getValue());
			// double hdNext = Double.valueOf(heartrateDataHRV.get(i + 1)
			// .getValue());

			// double procent = hdNext * 100 / hd;
			// double diffProcent = 100 - procent;

			// if (diffProcent < 0) {
			// diffProcent = 0 - diffProcent;
			// }

			// if the difference between the beat intervals in bigger than 300%
			// -> 2 seconds => we have an ectopic beat => cubic spline
			// interpolation to replace the removed ectopic beat
			if (hd > 2500) { // if (diffProcent > 300) {

				int nrValuesToConsider = 120;

				List<HRwithHRV> hrWithHrvValues = new ArrayList<HRwithHRV>();

				for (int j = i; j < nrValuesToConsider / 2 + i; j++) {

					HRwithHRV hrWithHrv = new HRwithHRV();

					try {
						hrWithHrv.setHeartrate(Double
								.valueOf(heartrateDataLocal.get(j + 1)
										.getValue()));
						hrWithHrv
								.setHeartrateVariability(Double
										.valueOf(heartrateDataHRV.get(j + 1)
												.getValue()));

						hrWithHrvValues.add(hrWithHrv);
					} catch (Exception e) {

					}
					try {
						hrWithHrv.setHeartrate(Double
								.valueOf(heartrateDataLocal.get(
										j - (nrValuesToConsider / 2))
										.getValue()));
						hrWithHrv.setHeartrateVariability(Double
								.valueOf(heartrateDataHRV.get(
										j - (nrValuesToConsider / 2))
										.getValue()));

						hrWithHrvValues.add(hrWithHrv);
					} catch (Exception e) {

					}
				}

				Collections.sort(hrWithHrvValues);
				SortedSet<HRwithHRV> uniqueHrWithHrvValues = new TreeSet<HRwithHRV>(
						hrWithHrvValues);

				double[] x = new double[uniqueHrWithHrvValues.size()];
				double[] y = new double[uniqueHrWithHrvValues.size()];

				Iterator<HRwithHRV> iterator = uniqueHrWithHrvValues.iterator();
				int j = 0;
				while (iterator.hasNext()) {
					HRwithHRV hrWithHrv = iterator.next();
					x[j] = hrWithHrv.getHeartrate();
					y[j++] = hrWithHrv.getHeartrateVariability();
				}

				PolynomialSplineFunction func = splineInterpolator.interpolate(
						x, y);

				double res = 0;
				double hrValue = Double.valueOf(heartrateDataLocal.get(i)
						.getValue());

				try {
					res = func.value(hrValue);
				} catch (OutOfRangeException e) {

					if (x[0] - hrValue < 0) {
						res = func.value(x[x.length - 1]);
					} else {
						res = func.value(x[0]);
					}
				}

				heartrateDataNoEctopic.get(i).setValue(String.valueOf(res));
			}
		}

		// ExportUtils.writeToCSV(heartrateDataNoEctopic,
		// "Bogdan-hr30apr-HRV-noEctopic.csv");
		return heartrateDataNoEctopic;
	}

	// extract features
	public static List<HealthData> meanHRforSegment(
			List<HealthData> heartrateData, int seconds) {

		List<HealthData> meanHRinSegment = new ArrayList<HealthData>();

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		LocalDateTime startDateTime = LocalDateTime.parse(heartrateData.get(0)
				.getTimestamp(), formatter);
		LocalDateTime endDateTime = startDateTime.plusSeconds(seconds);

		int nrEntries = 0;
		double sumHR = 0;
		boolean flagLastSaved = false;

		for (HealthData healthData : heartrateData) {

			flagLastSaved = false;

			LocalDateTime currentDateTime = LocalDateTime.parse(
					healthData.getTimestamp(), formatter);

			if (currentDateTime.isBefore(endDateTime)) {

				sumHR += Double.valueOf(healthData.getValue());
				nrEntries++;
			} else {
				HealthData healthDataLocal = new HealthData();
				healthDataLocal.setValue(String.valueOf(sumHR / nrEntries));
				healthDataLocal
						.setTimestamp((endDateTime.minusSeconds(seconds))
								.format(formatter));
				meanHRinSegment.add(healthDataLocal);
				sumHR = Double.valueOf(healthData.getValue());
				;
				nrEntries = 1;
				endDateTime = endDateTime.plusSeconds(seconds);
				flagLastSaved = true;
			}
		}

		if (!flagLastSaved) {
			HealthData healthDataLocal = new HealthData();
			healthDataLocal.setValue(String.valueOf(sumHR / nrEntries));
			healthDataLocal.setTimestamp((endDateTime.minusSeconds(seconds))
					.format(formatter));
			meanHRinSegment.add(healthDataLocal);
		}

		return meanHRinSegment;
	}

	public static List<HealthData> stdDeviationHRforSegment(
			List<HealthData> heartrateData, List<HealthData> meanHRinSegment,
			int seconds) {

		List<HealthData> stdDevHRinSegment = new ArrayList<HealthData>();

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		LocalDateTime startDateTime = LocalDateTime.parse(heartrateData.get(0)
				.getTimestamp(), formatter);
		LocalDateTime endDateTime = startDateTime.plusSeconds(seconds);

		int nrEntries = 0;
		double sumDifferences = 0;
		int i = 0;
		boolean flagLastSaved = false;

		for (HealthData heatlhData : heartrateData) {

			flagLastSaved = false;

			LocalDateTime currentDateTime = LocalDateTime.parse(
					heatlhData.getTimestamp(), formatter);

			if (currentDateTime.isBefore(endDateTime)) {

				sumDifferences += Math.pow(
						Double.valueOf(heatlhData.getValue())
								- Double.valueOf(meanHRinSegment.get(i)
										.getValue()), 2);
				nrEntries++;
			} else {
				HealthData healthDataLocal = new HealthData();
				healthDataLocal
						.setValue(Math.sqrt(sumDifferences / nrEntries) != 0 ? String
								.valueOf(Math.sqrt(sumDifferences / nrEntries))
								: "0");
				healthDataLocal
						.setTimestamp((endDateTime.minusSeconds(seconds))
								.format(formatter));
				stdDevHRinSegment.add(healthDataLocal);
				sumDifferences = Math.pow(Double.valueOf(heatlhData.getValue())
						- Double.valueOf(meanHRinSegment.get(i).getValue()), 2);
				;
				i++;
				nrEntries = 1;
				endDateTime = endDateTime.plusSeconds(seconds);

				flagLastSaved = true;
			}
		}

		if (!flagLastSaved) {
			HealthData healthDataLocal = new HealthData();
			healthDataLocal
					.setValue(Math.sqrt(sumDifferences / nrEntries) != 0 ? String
							.valueOf(Math.sqrt(sumDifferences / nrEntries))
							: "0");
			healthDataLocal.setTimestamp((endDateTime.minusSeconds(seconds))
					.format(formatter));
			stdDevHRinSegment.add(healthDataLocal);
		}

		return stdDevHRinSegment;
	}

	public static List<HealthData> stepsForSegment(
			List<HealthData> meanHRinSegment, List<HealthData> stepcountData,
			int seconds) {

		List<HealthData> stepsInSegment = new ArrayList<HealthData>();

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		// split step interval in seconds intervals
		int i = 0;
		for (HealthData healthData : stepcountData) {

			// healthData.getTimestampStart();
			// healthData.getTimestampEnd();

			LocalDateTime dateStart = null;
			LocalDateTime dateEnd = null;

			dateStart = LocalDateTime.parse(healthData.getTimestampStart(),
					formatter);
			dateEnd = LocalDateTime.parse(healthData.getTimestampEnd(),
					formatter);

			// find out seconds between start and end date
			long secondsSteps = dateStart.until(dateEnd, ChronoUnit.SECONDS);

			// see how many intervals to make
			double numberOfIntervals = (double) secondsSteps / (double) seconds;

			if ((numberOfIntervals - 0.01) < (int) numberOfIntervals) {
				numberOfIntervals = (int) numberOfIntervals;
			} else {
				numberOfIntervals = (int) numberOfIntervals + 1;
			}

			// know how many steps for each interval
			int allSteps = Integer.parseInt(healthData.getValue());
			double numberOfSteps = 0;
			if (allSteps > 0) {
				numberOfSteps = (double) Integer
						.parseInt(healthData.getValue()) / numberOfIntervals;
			}

			while (numberOfIntervals > 0 && i < meanHRinSegment.size()) {

				HealthData newStepcount = new HealthData();
				newStepcount
						.setTimestamp(meanHRinSegment.get(i).getTimestamp());
				newStepcount.setValue(String.valueOf(numberOfSteps));

				stepsInSegment.add(newStepcount);

				i++;
				numberOfIntervals--;
			}

		}

		return stepsInSegment;
	}

	public static List<HealthData> meanHRVforSegment(List<HealthData> hrvData,
			int seconds) {

		return meanHRforSegment(hrvData, seconds);
	}

	public static List<HealthData> medianHRVforSegment(
			List<HealthData> hrvData, int seconds) {

		List<HealthData> medianHRVinSegment = new ArrayList<HealthData>();

		List<Double> currentSegmentValues = new ArrayList<Double>();

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		LocalDateTime startDateTime = LocalDateTime.parse(hrvData.get(0)
				.getTimestamp(), formatter);
		LocalDateTime endDateTime = startDateTime.plusSeconds(seconds);

		boolean flagLastSaved = false;

		for (HealthData heatlhData : hrvData) {

			flagLastSaved = false;

			LocalDateTime currentDateTime = LocalDateTime.parse(
					heatlhData.getTimestamp(), formatter);

			if (currentDateTime.isBefore(endDateTime)) {
				currentSegmentValues.add(Double.valueOf(heatlhData.getValue()));
			} else {
				Collections.sort(currentSegmentValues);

				HealthData healthDataLocal = new HealthData();
				healthDataLocal.setValue(String
						.valueOf(calculateMedian(currentSegmentValues)));
				healthDataLocal
						.setTimestamp((endDateTime.minusSeconds(seconds))
								.format(formatter));
				medianHRVinSegment.add(healthDataLocal);

				currentSegmentValues = new ArrayList<Double>();
				currentSegmentValues.add(Double.valueOf(heatlhData.getValue()));
				endDateTime = endDateTime.plusSeconds(seconds);

				flagLastSaved = true;
			}
		}

		if (!flagLastSaved) {
			HealthData healthDataLocal = new HealthData();
			healthDataLocal.setValue(String
					.valueOf(calculateMedian(currentSegmentValues)));
			healthDataLocal.setTimestamp((endDateTime.minusSeconds(seconds))
					.format(formatter));
			medianHRVinSegment.add(healthDataLocal);
		}

		return medianHRVinSegment;
	}

	public static List<HealthData> stdDeviationHRVforSegment(
			List<HealthData> hrvData, List<HealthData> meanHRVinSegment,
			int seconds) {

		return stdDeviationHRforSegment(hrvData, meanHRVinSegment, seconds);
	}

	public static double calculateMedian(List<Double> values) {

		if (values.size() > 2 && values.size() % 2 == 0) {
			return (values.get(values.size() / 2) + values
					.get((values.size() / 2) - 1)) / 2;
		} else {
			return values.get(values.size() / 2);
		}
	}

	public static List<HealthData> nn50forAllDataset(int nn,
			List<HealthData> hrvData) {

		List<HealthData> nn50inAllData = new ArrayList<HealthData>();

		for (int i = 0; i < hrvData.size() - 1; i++) {

			double hrvCurrent = Double.valueOf(hrvData.get(i).getValue());
			double hrvNext = Double.valueOf(hrvData.get(i + 1).getValue());

			double difference = hrvNext - hrvCurrent;
			if (difference < 0) {
				difference = 0 - difference;
			}

			if (difference > nn) {
				HealthData healthData = new HealthData();
				healthData.setValue(String.valueOf(hrvNext));
				healthData.setTimestamp(hrvData.get(i + 1).getTimestamp());

				nn50inAllData.add(healthData);
			}
		}

		return nn50inAllData;
	}

	public static List<HealthData> nn50forSegment(int nn,
			List<HealthData> hrvData, int seconds) {

		List<HealthData> nn50inSegment = new ArrayList<HealthData>();

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		LocalDateTime startDateTime = LocalDateTime.parse(hrvData.get(0)
				.getTimestamp(), formatter);
		LocalDateTime endDateTime = startDateTime.plusSeconds(seconds);

		double sumValues = 0;
		int nrValues = 0;
		boolean flagLastSaved = false;

		for (int i = 0; i < hrvData.size() - 1; i++) {

			flagLastSaved = false;

			LocalDateTime currentDateTime = LocalDateTime.parse(
					hrvData.get(i + 1).getTimestamp(), formatter);

			double hrvCurrent = Double.valueOf(hrvData.get(i).getValue());
			double hrvNext = Double.valueOf(hrvData.get(i + 1).getValue());

			if (currentDateTime.isBefore(endDateTime)) {

				double difference = hrvNext - hrvCurrent;
				if (difference < 0) {
					difference = 0 - difference;
				}

				if (difference > nn) {
					sumValues += difference;
					nrValues++; // trebuia sa fie nr la toate, nu numai cele cu nn
				}
			} else {

				HealthData healthData = new HealthData();
				healthData.setValue(sumValues > 0 ? String.valueOf(sumValues
						/ nrValues) : "0");
				healthData.setTimestamp((endDateTime.minusSeconds(seconds))
						.format(formatter));

				nn50inSegment.add(healthData);

				endDateTime = endDateTime.plusSeconds(seconds);

				sumValues = 0;

				double difference = hrvNext - hrvCurrent;
				if (difference < 0) {
					difference = 0 - difference;
				}

				if (difference > nn) {
					sumValues += difference;
					nrValues++;
				}

				flagLastSaved = true;
			}
		}

		if (!flagLastSaved) {

			HealthData healthData = new HealthData();
			healthData.setValue(String.valueOf(sumValues / nrValues));
			healthData.setTimestamp((endDateTime.minusSeconds(seconds))
					.format(formatter));

			nn50inSegment.add(healthData);
		}

		return nn50inSegment;
	}

	public static List<HealthData> rmssdForSegment(List<HealthData> hrvData,
			int seconds) {

		List<HealthData> rmssdInSegment = new ArrayList<HealthData>();

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		LocalDateTime startDateTime = LocalDateTime.parse(hrvData.get(0)
				.getTimestamp(), formatter);
		LocalDateTime endDateTime = startDateTime.plusSeconds(seconds);

		double sumDiff = 0;
		int nrValues = 0;
		boolean flagLastSaved = false;

		for (int i = 0; i < hrvData.size() - 1; i++) {

			flagLastSaved = false;

			LocalDateTime currentDateTime = LocalDateTime.parse(
					hrvData.get(i + 1).getTimestamp(), formatter);

			double currentValue = Double.valueOf(hrvData.get(i).getValue());
			double nextValue = Double.valueOf(hrvData.get(i + 1).getValue());

			if (currentDateTime.isBefore(endDateTime)) {

				sumDiff += Math.pow(nextValue - currentValue, 2);
				nrValues++;
			} else {
				HealthData healthData = new HealthData();
				healthData.setValue(sumDiff != 0 ? String.valueOf(Math
						.sqrt(sumDiff / nrValues)) : "0");
				healthData.setTimestamp((endDateTime.minusSeconds(seconds))
						.format(formatter));
				rmssdInSegment.add(healthData);

				endDateTime = endDateTime.plusSeconds(seconds);

				sumDiff = Math.pow(nextValue - currentValue, 2);
				nrValues = 1;

				flagLastSaved = true;
			}

		}

		if (!flagLastSaved) {

			HealthData healthData = new HealthData();
			healthData.setValue(sumDiff != 0 ? String.valueOf(Math.sqrt(sumDiff
					/ nrValues)) : "0");
			healthData.setTimestamp((endDateTime.minusSeconds(seconds))
					.format(formatter));
			rmssdInSegment.add(healthData);
		}

		return rmssdInSegment;
	}

	public static List<HealthData> meanSDforSegment(List<HealthData> hrvData,
			int seconds) {

		List<HealthData> meanSDinSegment = new ArrayList<HealthData>();

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		LocalDateTime startDateTime = LocalDateTime.parse(hrvData.get(0)
				.getTimestamp(), formatter);
		LocalDateTime endDateTime = startDateTime.plusSeconds(seconds);

		double sumDiff = 0;
		int nrValues = 0;
		boolean flagLastSaved = false;

		for (int i = 0; i < hrvData.size() - 1; i++) {

			flagLastSaved = false;

			LocalDateTime currentDateTime = LocalDateTime.parse(
					hrvData.get(i + 1).getTimestamp(), formatter);

			double currentValue = Double.valueOf(hrvData.get(i).getValue());
			double nextValue = Double.valueOf(hrvData.get(i + 1).getValue());

			if (currentDateTime.isBefore(endDateTime)) {

				sumDiff += nextValue - currentValue;
				nrValues++;
			} else {
				HealthData healthData = new HealthData();
				healthData.setValue(sumDiff != 0 ? String.valueOf(sumDiff
						/ nrValues) : "0");
				healthData.setTimestamp((endDateTime.minusSeconds(seconds))
						.format(formatter));
				meanSDinSegment.add(healthData);

				endDateTime = endDateTime.plusSeconds(seconds);

				sumDiff = nextValue - currentValue;
				nrValues = 1;

				flagLastSaved = true;
			}
		}

		if (!flagLastSaved) {
			HealthData healthData = new HealthData();
			healthData.setValue(sumDiff != 0 ? String.valueOf(sumDiff
					/ nrValues) : "0");
			healthData.setTimestamp((endDateTime.minusSeconds(seconds))
					.format(formatter));
			meanSDinSegment.add(healthData);
		}

		return meanSDinSegment;
	}

	public static List<HealthData> sdsdForSegment(List<HealthData> hrvData,
			List<HealthData> hrvSDmean, int seconds) {

		return stdDeviationHRVforSegment(hrvData, hrvSDmean, seconds);
	}

	public static List<HealthData> minMaxNNforSegment(List<HealthData> hrvData,
			int seconds, boolean minimum) {

		List<HealthData> minMaxNNinSegment = new ArrayList<HealthData>();

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		LocalDateTime startDateTime = LocalDateTime.parse(hrvData.get(0)
				.getTimestamp(), formatter);
		LocalDateTime endDateTime = startDateTime.plusSeconds(seconds);

		double minValue = Double.valueOf(hrvData.get(0).getValue());
		double maxValue = Double.valueOf(hrvData.get(0).getValue());
		boolean flagLastSaved = false;

		for (int i = 1; i < hrvData.size(); i++) {

			flagLastSaved = false;

			LocalDateTime currentDateTime = LocalDateTime.parse(hrvData.get(i)
					.getTimestamp(), formatter);

			double currentValue = Double.valueOf(hrvData.get(i).getValue());

			if (currentDateTime.isBefore(endDateTime)) {

				if (minimum) {
					if (currentValue < minValue) {
						minValue = currentValue;
					}
				} else {
					if (currentValue > maxValue) {
						maxValue = currentValue;
					}
				}
			} else {

				HealthData healthData = new HealthData();
				healthData.setValue(String.valueOf(minimum ? minValue
						: maxValue));
				healthData.setTimestamp((endDateTime.minusSeconds(seconds))
						.format(formatter));
				minMaxNNinSegment.add(healthData);

				endDateTime = endDateTime.plusSeconds(seconds);

				minValue = currentValue;
				maxValue = currentValue;

				flagLastSaved = true;
			}
		}

		if (!flagLastSaved) {

			HealthData healthData = new HealthData();
			healthData.setValue(String.valueOf(minimum ? minValue : maxValue));
			healthData.setTimestamp((endDateTime.minusSeconds(seconds))
					.format(formatter));
			minMaxNNinSegment.add(healthData);
		}

		return minMaxNNinSegment;
	}

	public static List<HealthData> hrvTriangularIndexForSegment(
			List<HealthData> hrvData, int seconds) {

		List<HealthData> hrvTrinagularIndexInSegment = new ArrayList<HealthData>();

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		LocalDateTime startDateTime = LocalDateTime.parse(hrvData.get(0)
				.getTimestamp(), formatter);
		LocalDateTime endDateTime = startDateTime.plusSeconds(seconds);

		double binWidth = (double) 10000 / (double) 128;

		int nrValues = 0;
		int[] valuesPerBin = new int[(int) (2500 / binWidth)]; // 32
		boolean flagLastSaved = false;

		for (int i = 0; i < hrvData.size(); i++) {

			flagLastSaved = false;

			LocalDateTime currentDateTime = LocalDateTime.parse(hrvData.get(i)
					.getTimestamp(), formatter);

			double currentValue = Double.valueOf(hrvData.get(i).getValue());

			if (currentDateTime.isBefore(endDateTime)) {

				double place = currentValue / binWidth;
				nrValues++;

				if ((place - 0.5) < (int) place) {

					if (place <= 0) {
						valuesPerBin[0]++;
					} else {
						if (place >= valuesPerBin.length - 1) {
							valuesPerBin[valuesPerBin.length - 1]++;
						} else {
							valuesPerBin[(int) place]++;
						}
					}
				} else {
					if (place >= valuesPerBin.length - 1) {
						valuesPerBin[valuesPerBin.length - 1]++;
					} else {
						valuesPerBin[((int) place) + 1]++;
					}
				}
			} else {

				HealthData healthData = new HealthData();
				healthData.setTimestamp((endDateTime.minusSeconds(seconds))
						.format(formatter));
				healthData.setValue(String
						.valueOf(nrValues == 0 ? 0 : (double) nrValues
								/ (double) maximum(valuesPerBin)[1]));
				hrvTrinagularIndexInSegment.add(healthData);

				nrValues = 0;
				valuesPerBin = new int[(int) (2500 / binWidth)]; // 32
				endDateTime = endDateTime.plusSeconds(seconds);

				flagLastSaved = true;
			}
		}

		if (!flagLastSaved) {

			HealthData healthData = new HealthData();
			healthData.setTimestamp((endDateTime.minusSeconds(seconds))
					.format(formatter));
			healthData.setValue(String.valueOf(valuesPerBin.length
					/ maximum(valuesPerBin)[1]));
			hrvTrinagularIndexInSegment.add(healthData);
		}

		return hrvTrinagularIndexInSegment;
	}

	public static int[] maximum(int[] values) {

		int[] maximum = new int[2];

		for (int i = 0; i < values.length; i++) {

			if (values[i] > maximum[1]) {
				maximum[1] = values[i];
				maximum[0] = i;
			}
		}

		return maximum;
	}

	public static List<HealthData> tinnForSegment(List<HealthData> hrvData,
			int seconds) {

		List<HealthData> tinnInSegment = new ArrayList<HealthData>();

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		LocalDateTime startDateTime = LocalDateTime.parse(hrvData.get(0)
				.getTimestamp(), formatter);
		LocalDateTime endDateTime = startDateTime.plusSeconds(seconds);

		double binWidth = (double) 10000 / (double) 128;

		int nrValues = 0;
		int[] valuesPerBin = new int[(int) (2500 / binWidth)]; // 32
		boolean flagLastSaved = false;

		for (int i = 0; i < hrvData.size(); i++) {

			flagLastSaved = false;

			LocalDateTime currentDateTime = LocalDateTime.parse(hrvData.get(i)
					.getTimestamp(), formatter);

			double currentValue = Double.valueOf(hrvData.get(i).getValue());

			if (currentDateTime.isBefore(endDateTime)) {

				double place = currentValue / binWidth;
				nrValues++;

				if ((place - 0.5) < (int) place) {

					if (place <= 0) {
						valuesPerBin[0]++;
					} else {
						if (place >= valuesPerBin.length - 1) {
							valuesPerBin[valuesPerBin.length - 1]++;
						} else {
							valuesPerBin[(int) place]++;
						}
					}
				} else {
					if (place >= valuesPerBin.length - 1) {
						valuesPerBin[valuesPerBin.length - 1]++;
					} else {
						valuesPerBin[((int) place) + 1]++;
					}
				}
			} else {

				int[] indexAndMax = maximum(valuesPerBin);
				int index = indexAndMax[0];
				int start[] = new int[2];
				start[0] = index;
				start[1] = valuesPerBin[index];
				if (index != 0) {
					start[0] = index - 1;
					start[1] = valuesPerBin[index - 1];
				}
				int end[] = new int[2];
				end[0] = index;
				end[1] = valuesPerBin[index];
				if (index != valuesPerBin.length - 1) {
					end[0] = index + 1;
					end[1] = valuesPerBin[index + 1];
				}

				for (int j = index - 2; j > 0; j--) {

					if (valuesPerBin[j] <= start[1]) {
						start[0] = j;
						start[1] = valuesPerBin[j];
					} else {
						break;
					}
				}
				for (int j = index + 2; j < valuesPerBin.length; j++) {

					if (valuesPerBin[j] <= end[1]) {
						end[0] = j;
						end[1] = valuesPerBin[j];
						;
					} else {
						break;
					}
				}

				double startPoint = binWidth * start[0];
				double endPoint = binWidth * end[0];
				double baselineDifference = endPoint - startPoint;

				HealthData healthData = new HealthData();
				healthData.setTimestamp((endDateTime.minusSeconds(seconds))
						.format(formatter));
				healthData.setValue(String.valueOf(baselineDifference));
				tinnInSegment.add(healthData);

				nrValues = 0;
				valuesPerBin = new int[(int) (2500 / binWidth)]; // 32
				endDateTime = endDateTime.plusSeconds(seconds);

				flagLastSaved = true;
			}
		}

		if (!flagLastSaved) {

			int[] indexAndMax = maximum(valuesPerBin);
			int index = indexAndMax[0];
			int start[] = new int[2];
			start[0] = index;
			start[1] = valuesPerBin[index];
			if (index != 0) {
				start[0] = index - 1;
				start[1] = valuesPerBin[index - 1];
			}
			int end[] = new int[2];
			end[0] = index;
			end[1] = valuesPerBin[index];
			if (index != valuesPerBin.length - 1) {
				end[0] = index + 1;
				end[1] = valuesPerBin[index + 1];
			}

			for (int j = index - 2; j > 0; j--) {

				if (valuesPerBin[j] <= start[1]) {
					start[0] = j;
					start[1] = valuesPerBin[j];
				} else {
					break;
				}
			}
			for (int j = index + 2; j < valuesPerBin.length; j++) {

				if (valuesPerBin[j] <= end[1]) {
					end[0] = j;
					end[1] = valuesPerBin[j];
					;
				} else {
					break;
				}
			}

			double startPoint = binWidth * start[0];
			double endPoint = binWidth * end[0];
			double baselineDifference = endPoint - startPoint;

			HealthData healthData = new HealthData();
			healthData.setTimestamp((endDateTime.minusSeconds(seconds))
					.format(formatter));
			healthData.setValue(String.valueOf(baselineDifference));
			tinnInSegment.add(healthData);
		}

		return tinnInSegment;
	}

	public static List<HealthData> sd1ForSegment(List<HealthData> sdsdData,
			int seconds) {

		List<HealthData> sd1InSegment = new ArrayList<HealthData>();

		for (HealthData healthData : sdsdData) {

			double currentValue = Double.valueOf(healthData.getValue());
			double sdValue = Math.sqrt(Math.pow(currentValue, 2) / 2);

			HealthData healthDataLocal = new HealthData();
			healthDataLocal.setTimestamp(healthData.getTimestamp());
			healthDataLocal.setValue(sdValue != 0 ? String.valueOf(sdValue)
					: "0");

			sd1InSegment.add(healthDataLocal);
		}

		return sd1InSegment;
	}

	public static List<HealthData> sd2ForSegment(List<HealthData> sdnnData,
			List<HealthData> sdsdData, int seconds) {

		List<HealthData> sd2InSegment = new ArrayList<HealthData>();

		for (int i = 0; i < sdnnData.size(); i++) {

			double currentValue1 = Double.valueOf(sdnnData.get(i).getValue());
			double currentValue2 = Double.valueOf(sdsdData.get(i).getValue());
			double formulaValue = (2 * Math.pow(currentValue1, 2))
					- (Math.pow(currentValue2, 2) / 2);

			if (formulaValue < 0) {
				formulaValue = 0 - formulaValue;
			}
			double sdValue = Math.sqrt(formulaValue);

			HealthData healthDataLocal = new HealthData();
			healthDataLocal.setTimestamp(sdnnData.get(i).getTimestamp());
			healthDataLocal.setValue(sdValue != 0 ? String.valueOf(sdValue)
					: "0");

			sd2InSegment.add(healthDataLocal);
		}

		return sd2InSegment;
	}

	public static List<HealthData> spEnForSegment(List<HealthData> hrvData,
			int seconds) {

		List<HealthData> spEnInSegment = new ArrayList<HealthData>();

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		LocalDateTime startDateTime = LocalDateTime.parse(hrvData.get(0)
				.getTimestamp(), formatter);
		LocalDateTime endDateTime = startDateTime.plusSeconds(seconds);

		List<Double> currentValues = new ArrayList<Double>();

		boolean flagLastSaved = false;

		for (int i = 0; i < hrvData.size(); i++) {

			flagLastSaved = false;

			LocalDateTime currentDateTime = LocalDateTime.parse(hrvData.get(i)
					.getTimestamp(), formatter);

			double currentValue = Double.valueOf(hrvData.get(i).getValue());

			if (currentDateTime.isBefore(endDateTime)) {
				currentValues.add(currentValue);
			} else {

				List<Double> probabilities = getProbabilities(currentValues);

				double spEnValue = 0;
				for (Double probability : probabilities) {
					spEnValue += probability
							* (Math.log(probability) / Math.log(2));
				}

				spEnValue = 0 - spEnValue;

				HealthData healthDataLocal = new HealthData();
				healthDataLocal
						.setTimestamp((endDateTime.minusSeconds(seconds))
								.format(formatter));
				healthDataLocal.setValue(String.valueOf(spEnValue));
				spEnInSegment.add(healthDataLocal);

				endDateTime = endDateTime.plusSeconds(seconds);
				currentValues = new ArrayList<Double>();

				flagLastSaved = true;
			}
		}

		if (!flagLastSaved) {

			List<Double> probabilities = getProbabilities(currentValues);

			double spEnValue = 0;
			for (Double probability : probabilities) {
				spEnValue += probability
						* (Math.log(probability) / Math.log(2));
			}

			spEnValue = 0 - spEnValue;

			HealthData healthDataLocal = new HealthData();
			healthDataLocal.setTimestamp((endDateTime.minusSeconds(seconds))
					.format(formatter));
			healthDataLocal.setValue(String.valueOf(spEnValue));
			spEnInSegment.add(healthDataLocal);
		}

		return spEnInSegment;
	}

	public static List<Double> getProbabilities(List<Double> values) {

		List<Double> probabilities = new ArrayList<Double>();

		List<Double> currentValues = values;
		Collections.sort(values);
		SortedSet<Double> uniqueValues = new TreeSet<Double>(values);

		Iterator<Double> iterator = uniqueValues.iterator();
		while (iterator.hasNext()) {

			double currentValue = iterator.next();
			int currentValueNr = 0;
			for (int i = 0; i < currentValues.size(); i++) {

				if (currentValue == currentValues.get(i)) {
					currentValueNr++;
				}
			}

			probabilities.add((double) currentValueNr
					/ (double) currentValues.size());
		}

		return probabilities;
	}
}
