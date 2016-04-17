package monitoring.controller;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import monitoring.model.HealthData;
import monitoring.utils.ExportUtils;
import monitoring.utils.HealthDataRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({ "/step1CallsController" })
public class Step1Controller {

	// for step 2
	List<HealthData> restNormal = new ArrayList<HealthData>();
	List<HealthData> restStressed = new ArrayList<HealthData>();
	List<HealthData> trainVLow = new ArrayList<HealthData>();
	List<HealthData> trainLow = new ArrayList<HealthData>();
	List<HealthData> trainMed = new ArrayList<HealthData>();
	List<HealthData> trainHigh = new ArrayList<HealthData>();
	List<HealthData> trainVHigh = new ArrayList<HealthData>();
	List<HealthData> trainRecovery = new ArrayList<HealthData>();

	List<HealthData> syncedHeartrateData = new ArrayList<HealthData>();
	List<HealthData> syncedStepcountData = new ArrayList<HealthData>();
	HashMap<Integer, List<Double>> meansIntensitySegments = new HashMap<Integer, List<Double>>();
	HashMap<Integer, Integer> meansIntensityTimesSegments = new HashMap<Integer, Integer>();
	double meanRestingHR = 0;
	double meanTrainingHR = 0;
	int restingValues = 0;
	int trainingValues = 0;

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
		List<HealthData> stepcountData = new ArrayList<HealthData>();

		if (type.equals("getChartData")) {
			heartrateData = ExportUtils.readFromCSV("TEST/Adriana-hr15jun.csv");
			stepcountData = ExportUtils.readFromCSV("TEST/Adriana-sc15jun.csv");
			synchroniseHRWithSteps(heartrateData, stepcountData, 30);
		}

		if (type.equalsIgnoreCase("chartHeart")) {
			return syncedHeartrateData;
		}

		if (type.equalsIgnoreCase("chartSteps")) {
			return syncedStepcountData;
		}

		return null;
	}

	int totalHours = 0;
	int totalMinutes = 0;

	int restingHours = 0;
	int restingMinutes = 0;

	@RequestMapping(value = "/healthc", method = RequestMethod.GET)
	@ResponseBody
	public String viewChart(HttpServletResponse response,
			@RequestParam MultiValueMap<String, String> messageParams) {

		String type = messageParams.getFirst("type");

		double returnMeanValue = 0;
		int nrValues = 0;

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		if (type.equalsIgnoreCase("totalTime")) {

			LocalDateTime startDateTime = LocalDateTime.parse(
					syncedHeartrateData.get(0).getTimestamp(), formatter);
			LocalDateTime endDateTime = LocalDateTime.parse(syncedHeartrateData
					.get(syncedHeartrateData.size() - 1).getTimestamp(),
					formatter);

			long duration = Duration.between(startDateTime, endDateTime)
					.toMillis();

			long seconds = duration / 1000;
			// int minutes = (int) seconds / 60;
			int minutes = (restingValues + trainingValues) / 2;
			int hours = minutes >= 60 ? minutes / 60 : 0;
			int remainingMinutes = minutes - (hours * 60);

			totalHours = hours;
			totalMinutes = remainingMinutes;

			return hours + ":" + remainingMinutes;
		}

		if (type.equalsIgnoreCase("totalRestingTime")) {

			// int second30Intervals = meansIntensityTimesSegments.get(1) ==
			// null ? 0
			// : meansIntensityTimesSegments.get(1);
			// second30Intervals += meansIntensityTimesSegments.get(2) == null ?
			// 0
			// : meansIntensityTimesSegments.get(2);
			// second30Intervals += meansIntensityTimesSegments.get(3) == null ?
			// 0
			// : meansIntensityTimesSegments.get(3);

			int minutes = restingValues / 2;
			int hours = minutes >= 60 ? minutes / 60 : 0;
			int remainingMinutes = minutes - (hours * 60);

			restingHours = hours;
			restingMinutes = remainingMinutes;

			return hours + ":" + remainingMinutes;
			//return hours + " hours, " + remainingMinutes + " minutes";
		}

		if (type.equalsIgnoreCase("totalMovingTime")) {

			// return (totalHours - restingHours) + " hours, "
			// + (totalMinutes - restingMinutes) + " minutes";

			int minutes = trainingValues / 2;
			int hours = minutes >= 60 ? minutes / 60 : 0;
			int remainingMinutes = minutes - (hours * 60);

			return hours + ":" + remainingMinutes;
			//return hours + " hours, " + remainingMinutes + " minutes";
		}

		if (type.equalsIgnoreCase("meanRestingHR")) {

			return String.valueOf(meanRestingHR);
		}

		if (type.equalsIgnoreCase("meanTrainingHR")) {

			return String.valueOf(meanTrainingHR);
		}

		if (type.equalsIgnoreCase("periodsTime")) {

			String intensity = messageParams.getFirst("intensity");

			int minutes = 0;

			if (intensity.equalsIgnoreCase("vlow")) {
				minutes = meansIntensityTimesSegments.get(10) != null ? (double) meansIntensityTimesSegments
						.get(10) % 2 != 0 ? meansIntensityTimesSegments.get(10) / 2 + 1
						: meansIntensityTimesSegments.get(10) / 2
						: 0;
			} else if (intensity.equalsIgnoreCase("low")) {
				minutes = meansIntensityTimesSegments.get(20) != null ? (double) meansIntensityTimesSegments
						.get(20) % 2 != 0 ? meansIntensityTimesSegments.get(20) / 2 + 1
						: meansIntensityTimesSegments.get(20) / 2
						: 0;
				minutes += meansIntensityTimesSegments.get(30) != null ? (double) meansIntensityTimesSegments
						.get(30) % 2 != 0 ? meansIntensityTimesSegments.get(30) / 2 + 1
						: meansIntensityTimesSegments.get(30) / 2
						: 0;
			} else if (intensity.equalsIgnoreCase("medium")) {

				minutes = meansIntensityTimesSegments.get(40) != null ? (double) meansIntensityTimesSegments
						.get(40) % 2 != 0 ? meansIntensityTimesSegments.get(40) / 2 + 1
						: meansIntensityTimesSegments.get(40) / 2
						: 0;
				minutes += meansIntensityTimesSegments.get(50) != null ? (double) meansIntensityTimesSegments
						.get(50) % 2 != 0 ? meansIntensityTimesSegments.get(50) / 2 + 1
						: meansIntensityTimesSegments.get(50) / 2
						: 0;
				minutes += meansIntensityTimesSegments.get(60) != null ? (double) meansIntensityTimesSegments
						.get(60) % 2 != 0 ? meansIntensityTimesSegments.get(60) / 2 + 1
						: meansIntensityTimesSegments.get(60) / 2
						: 0;
			} else if (intensity.equalsIgnoreCase("high")) {

				minutes = meansIntensityTimesSegments.get(65) != null ? (double) meansIntensityTimesSegments
						.get(65) % 2 != 0 ? meansIntensityTimesSegments.get(65) / 2 + 1
						: meansIntensityTimesSegments.get(65) / 2
						: 0;
				minutes += meansIntensityTimesSegments.get(70) != null ? (double) meansIntensityTimesSegments
						.get(70) % 2 != 0 ? meansIntensityTimesSegments.get(70) / 2 + 1
						: meansIntensityTimesSegments.get(70) / 2
						: 0;
				minutes += meansIntensityTimesSegments.get(75) != null ? (double) meansIntensityTimesSegments
						.get(75) % 2 != 0 ? meansIntensityTimesSegments.get(75) / 2 + 1
						: meansIntensityTimesSegments.get(75) / 2
						: 0;
			} else if (intensity.equalsIgnoreCase("vhigh")) {

				minutes = meansIntensityTimesSegments.get(80) != null ? (double) meansIntensityTimesSegments
						.get(80) % 2 != 0 ? meansIntensityTimesSegments.get(80) / 2 + 1
						: meansIntensityTimesSegments.get(80) / 2
						: 0;
				minutes += meansIntensityTimesSegments.get(90) != null ? (double) meansIntensityTimesSegments
						.get(90) % 2 != 0 ? meansIntensityTimesSegments.get(90) / 2 + 1
						: meansIntensityTimesSegments.get(90) / 2
						: 0;
			} else if (intensity.equalsIgnoreCase("rest")) {

				minutes = meansIntensityTimesSegments.get(1) != null ? (double) meansIntensityTimesSegments
						.get(1) % 2 != 0 ? meansIntensityTimesSegments.get(1) / 2 + 1
						: meansIntensityTimesSegments.get(1) / 2
						: 0;
			} else if (intensity.equalsIgnoreCase("stress")) {

				minutes = meansIntensityTimesSegments.get(3) != null ? (double) meansIntensityTimesSegments
						.get(3) % 2 != 0 ? meansIntensityTimesSegments.get(3) / 2 + 1
						: meansIntensityTimesSegments.get(3) / 2
						: 0;
			} else if (intensity.equalsIgnoreCase("sleep")) {
				minutes = meansIntensityTimesSegments.get(2) != null ? (double) meansIntensityTimesSegments
						.get(2) % 2 != 0 ? meansIntensityTimesSegments.get(2) / 2 + 1
						: meansIntensityTimesSegments.get(2) / 2
						: 0;
			}

			if (minutes > 0) {
				int hours = minutes >= 60 ? minutes / 60 : 0;
				int remainingMinutes = minutes - (hours * 60);

				String min = remainingMinutes + "";
				if (remainingMinutes < 10) {
					min = "0" + remainingMinutes;
				}
				
				return hours + ":" + min;
				//return hours + " hours, " + (remainingMinutes + 1) + " minutes";
			}
			return "-";
		}

		if (type.equalsIgnoreCase("periodsNumber")) {

			String intensity = messageParams.getFirst("intensity");

			if (intensity.equalsIgnoreCase("vlow")) {

				return meansIntensitySegments.get(10) == null ? "-" : String
						.valueOf(meansIntensitySegments.get(10).size());
			} else if (intensity.equalsIgnoreCase("low")) {

				int number = meansIntensitySegments.get(20) == null ? 0
						: meansIntensitySegments.get(20).size();
				number += meansIntensitySegments.get(30) == null ? 0
						: meansIntensitySegments.get(30).size();

				return String.valueOf(number);
			} else if (intensity.equalsIgnoreCase("medium")) {

				int number = meansIntensitySegments.get(40) == null ? 0
						: meansIntensitySegments.get(40).size();
				number += meansIntensitySegments.get(50) == null ? 0
						: meansIntensitySegments.get(50).size();
				number += meansIntensitySegments.get(60) == null ? 0
						: meansIntensitySegments.get(60).size();

				return String.valueOf(number);
			} else if (intensity.equalsIgnoreCase("high")) {

				int number = meansIntensitySegments.get(65) == null ? 0
						: meansIntensitySegments.get(65).size();
				number += meansIntensitySegments.get(70) == null ? 0
						: meansIntensitySegments.get(70).size();
				number += meansIntensitySegments.get(75) == null ? 0
						: meansIntensitySegments.get(75).size();

				return String.valueOf(number);
			} else if (intensity.equalsIgnoreCase("vhigh")) {

				int number = meansIntensitySegments.get(80) == null ? 0
						: meansIntensitySegments.get(80).size();
				number += meansIntensitySegments.get(90) == null ? 0
						: meansIntensitySegments.get(90).size();

				return String.valueOf(number);
			} else if (intensity.equalsIgnoreCase("rest")) {

				return meansIntensitySegments.get(1) == null ? "-" : String
						.valueOf(meansIntensitySegments.get(1).size());
			} else if (intensity.equalsIgnoreCase("stress")) {

				return meansIntensitySegments.get(3) == null ? "-" : String
						.valueOf(meansIntensitySegments.get(3).size());
			} else if (intensity.equalsIgnoreCase("sleep")) {
				return meansIntensitySegments.get(2) == null ? "-" : String
						.valueOf(meansIntensitySegments.get(2).size());
			}
		}

		if (type.equalsIgnoreCase("intensity")) {

			String intensity = messageParams.getFirst("intensity");

			if (intensity.equalsIgnoreCase("vlow")) {

				if (meansIntensitySegments.get(10) != null) {
					for (Double mean : meansIntensitySegments.get(10)) {
						returnMeanValue += mean;
						nrValues++;
					}
				}
			} else if (intensity.equalsIgnoreCase("low")) {

				if (meansIntensitySegments.get(20) != null) {
					for (Double mean : meansIntensitySegments.get(20)) {
						returnMeanValue += mean;
						nrValues++;
					}
				}
				if (meansIntensitySegments.get(30) != null) {
					for (Double mean : meansIntensitySegments.get(30)) {
						returnMeanValue += mean;
						nrValues++;
					}
				}
			} else if (intensity.equalsIgnoreCase("medium")) {

				if (meansIntensitySegments.get(40) != null) {
					for (Double mean : meansIntensitySegments.get(40)) {
						returnMeanValue += mean;
						nrValues++;
					}
				}
				if (meansIntensitySegments.get(50) != null) {
					for (Double mean : meansIntensitySegments.get(50)) {
						returnMeanValue += mean;
						nrValues++;
					}
				}
				if (meansIntensitySegments.get(60) != null) {
					for (Double mean : meansIntensitySegments.get(60)) {
						returnMeanValue += mean;
						nrValues++;
					}
				}
			} else if (intensity.equalsIgnoreCase("high")) {

				if (meansIntensitySegments.get(65) != null) {
					for (Double mean : meansIntensitySegments.get(65)) {
						returnMeanValue += mean;
						nrValues++;
					}
				}
				if (meansIntensitySegments.get(70) != null) {
					for (Double mean : meansIntensitySegments.get(70)) {
						returnMeanValue += mean;
						nrValues++;
					}
				}
				if (meansIntensitySegments.get(75) != null) {
					for (Double mean : meansIntensitySegments.get(75)) {
						returnMeanValue += mean;
						nrValues++;
					}
				}
			} else if (intensity.equalsIgnoreCase("vhigh")) {

				if (meansIntensitySegments.get(80) != null) {
					for (Double mean : meansIntensitySegments.get(80)) {
						returnMeanValue += mean;
						nrValues++;
					}
				}
				if (meansIntensitySegments.get(90) != null) {
					for (Double mean : meansIntensitySegments.get(90)) {
						returnMeanValue += mean;
						nrValues++;
					}
				}
			} else if (intensity.equalsIgnoreCase("rest")) {

				if (meansIntensitySegments.get(1) != null) {
					for (Double mean : meansIntensitySegments.get(1)) {
						returnMeanValue += mean;
						nrValues++;
					}
				}
			} else if (intensity.equalsIgnoreCase("stress")) {

				if (meansIntensitySegments.get(3) != null) {
					for (Double mean : meansIntensitySegments.get(3)) {
						returnMeanValue += mean;
						nrValues++;
					}
				}
			} else if (intensity.equalsIgnoreCase("sleep")) {

				if (meansIntensitySegments.get(2) != null) {
					for (Double mean : meansIntensitySegments.get(2)) {
						returnMeanValue += mean;
						nrValues++;
					}
				}
			}
		}

		if (returnMeanValue > 0) {
			return String.valueOf(new DecimalFormat("##.##")
					.format(returnMeanValue / nrValues));
		} else if (returnMeanValue < 0) {
			return "-";
		}

		
		if (type.equalsIgnoreCase("fitnessLevel")) {
			return "Excellent";
		}
		if (type.equalsIgnoreCase("vlowoptim")) {
			return "Optimal";
		}
		if (type.equalsIgnoreCase("lowoptim")) {
			return "Optimal";
		}
		if (type.equalsIgnoreCase("medoptim")) {
			return "Optimal";
		}
		if (type.equalsIgnoreCase("highoptim")) {
			return "-";
		}
		if (type.equalsIgnoreCase("vhighoptim")) {
			return "-";
		}
		return "-";
	}

	public void synchroniseHRWithSteps(List<HealthData> heartrateData,
			List<HealthData> stepcountData, int seconds) {

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		// split step interval in seconds intervals
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
			long secondsLocal = dateStart.until(dateEnd, ChronoUnit.SECONDS);

			// see how many intervals to make
			int numberOfIntervals = (int) secondsLocal / seconds;

			// know how many steps for each interval
			int allSteps = Integer.parseInt(healthData.getValue());
			double numberOfSteps = 0;
			if (allSteps > 0) {
				numberOfSteps = (double) Integer
						.parseInt(healthData.getValue()) / numberOfIntervals;
			}

			while (numberOfIntervals > 0) {

				// calculate mean for hr values for this interval
				double meanHR = calculateMeanHRInInterval(dateStart,
						dateStart.plusSeconds(seconds), heartrateData);

				if (meanHR > 0) {
					LocalDateTime newDateStart = dateStart
							.plusSeconds(seconds / 2);

					HealthData newStepcount = new HealthData();
					newStepcount.setTimestamp(newDateStart.format(formatter));
					newStepcount.setType("stepcount");
					newStepcount.setValue(String.valueOf(numberOfSteps));

					syncedStepcountData.add(newStepcount);

					HealthData newHeartrate = new HealthData();
					newHeartrate.setTimestamp(newDateStart.format(formatter));
					newHeartrate.setType("heartrate");
					newHeartrate.setValue(String.valueOf(meanHR));

					syncedHeartrateData.add(newHeartrate);
				}

				dateStart = dateStart.plusSeconds(seconds);
				numberOfIntervals--;
			}

		}
		ExportUtils.writeToCSV(syncedHeartrateData, "HeartDataSync15");
		ExportUtils.writeToCSV(syncedStepcountData, "StepcountSync15");

		// overallRestTrainHR(newHeartrateData, newStepcountData);
		// segmentation(newHeartrateData, newStepcountData);

		List<HealthData> heartrateDataP = new ArrayList<HealthData>();
		List<HealthData> stepdataP = new ArrayList<HealthData>();

		for (HealthData healthData : syncedHeartrateData) {
			heartrateDataP.add(healthData);
		}
		for (HealthData healthData : syncedStepcountData) {
			stepdataP.add(healthData);
		}

		meansIntensitySegments = segmentationIntensityList(heartrateDataP,
				stepdataP);

		ExportUtils.writeToCSV(restNormal, "Adriana-hr15jun-restNormal");
		ExportUtils.writeToCSV(restStressed, "Adriana-hr15jun-restStressed");
		ExportUtils.writeToCSV(trainVLow, "Adriana-hr15jun-trainVLow");
		ExportUtils.writeToCSV(trainLow, "Adriana-hr15jun-trainLow");
		ExportUtils.writeToCSV(trainMed, "Adriana-hr15jun-trainMed");
		ExportUtils.writeToCSV(trainHigh, "Adriana-hr15jun-trainHigh");
		ExportUtils.writeToCSV(trainVHigh, "Adriana-hr15jun-trainVHigh");
		ExportUtils.writeToCSV(trainRecovery, "Adriana-hr15jun-trainRecovery");
	}

	public double calculateMeanHRInInterval(LocalDateTime dateStart,
			LocalDateTime dateEnd, List<HealthData> heartrateData) {

		int index = 0;
		int sumHR = 0;

		List<HealthData> toDeleteList = new ArrayList<HealthData>();

		for (HealthData healthData : heartrateData) {

			DateTimeFormatter formatter = DateTimeFormatter
					.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

			LocalDateTime currentDateTime = LocalDateTime.parse(
					healthData.getTimestamp(), formatter);

			if (currentDateTime.isBefore(dateEnd)) {

				int value = Integer.parseInt(healthData.getValue());
				if (value > 0) {
					sumHR += value;
				}

				toDeleteList.add(healthData);
				// heartrateData.remove(index);
				index++;
			} else {
				break;
			}
		}

		double mean = 0;

		if (sumHR > 0) {

			for (HealthData healthData : toDeleteList) {
				heartrateData.remove(healthData);
			}
			mean = (double) sumHR / index;
		}

		return mean;
	}

	public HashMap<Integer, List<Double>> segmentationIntensityList(
			List<HealthData> heartRateData, List<HealthData> stepCountData) {

		// valid for 30 second intervals

		HashMap<Integer, List<Double>> meansIntensitySegments = new HashMap<Integer, List<Double>>();

		int numberMeanHRTrain = 0;
		double currentMeanHRTrain = 0;
		int numberHRTrain = 0;
		int numberHRRest = 0;
		double currentHRTrain = 0;
		double currentHRRest = 0;
		double currentStep = 0;
		int flagPrevious = 0;
		int flagCurrent = 0;
		int flagRest = 0;
		List<HealthData> restingHrData = new ArrayList<HealthData>();

		while (!stepCountData.isEmpty()) {

			currentStep = Double.valueOf(stepCountData.get(0).getValue());

			if (currentStep != 0) {

				// training
				trainingValues++;
				flagRest = 0;

				if (currentStep > 0 && currentStep < 15) { // 10

					flagCurrent = 10;
					trainVLow.add(heartRateData.get(0));
				} else if (currentStep >= 15 && currentStep < 40) { // 20%

					flagCurrent = 20;
					trainLow.add(heartRateData.get(0));
				} else if (currentStep >= 40 && currentStep < 60) { // 30%

					flagCurrent = 30;
					trainLow.add(heartRateData.get(0));
				} else if (currentStep >= 60 && currentStep < 85) { // 40%

					flagCurrent = 40;
					trainMed.add(heartRateData.get(0));
				} else if (currentStep >= 85 && currentStep < 100) { // 50%

					flagCurrent = 50;
					trainMed.add(heartRateData.get(0));
				} else if (currentStep >= 100 && currentStep < 110) { // 60%

					flagCurrent = 60;
					trainMed.add(heartRateData.get(0));
				} else if (currentStep >= 110 && currentStep < 120) { // 65%

					flagCurrent = 65;
					trainHigh.add(heartRateData.get(0));
				} else if (currentStep >= 120 && currentStep < 130) { // 70%

					flagCurrent = 70;
					trainHigh.add(heartRateData.get(0));
				} else if (currentStep >= 130 && currentStep < 140) { // 75%

					flagCurrent = 75;
					trainHigh.add(heartRateData.get(0));
				} else if (currentStep >= 140 && currentStep < 150) { // 80%

					flagCurrent = 80;
					trainVHigh.add(heartRateData.get(0));
				} else if (currentStep >= 150) { // 90%

					flagCurrent = 90;
					trainVHigh.add(heartRateData.get(0));
				}

				if (flagPrevious == 0 || flagCurrent == flagPrevious) {

					flagPrevious = flagCurrent;
					currentHRTrain += Double.valueOf(heartRateData.get(0)
							.getValue());
					currentMeanHRTrain += Double.valueOf(heartRateData.get(0)
							.getValue());
					numberHRTrain++;
					numberMeanHRTrain++;

					stepCountData.remove(0);
					heartRateData.remove(0);
				} else {

					// if (currentHR > 0) {

					List<Double> means = meansIntensitySegments
							.get(flagPrevious);
					if (means == null) {
						means = new ArrayList<Double>();
					}
					means.add(currentHRTrain / numberHRTrain);

					meansIntensitySegments.put(flagPrevious, means);

					if (meansIntensityTimesSegments.get(flagPrevious) != null) {
						meansIntensityTimesSegments.put(flagPrevious,
								meansIntensityTimesSegments.get(flagPrevious)
										+ numberHRTrain);
					} else {
						meansIntensityTimesSegments.put(flagPrevious,
								numberHRTrain);
					}

					flagPrevious = flagCurrent;
					currentHRTrain = Double.valueOf(heartRateData.get(0)
							.getValue());
					currentMeanHRTrain += Double.valueOf(heartRateData.get(0)
							.getValue());
					numberHRTrain = 1;
					numberMeanHRTrain++;

					stepCountData.remove(0);
					heartRateData.remove(0);
				}
			} else {
				// resting
				restingValues++;
				flagRest++;

				heartRateData.get(0).setUser(String.valueOf(flagRest));

				restingHrData.add(heartRateData.get(0));
				currentHRRest += Double
						.valueOf(heartRateData.get(0).getValue());

				stepCountData.remove(0);
				heartRateData.remove(0);
			}
		}

		// training mean
		meanTrainingHR = currentMeanHRTrain / numberMeanHRTrain;

		// resting
		double meanHr = currentHRRest / restingHrData.size();
		meanRestingHR = meanHr;
		flagPrevious = 0;
		currentHRRest = 0;

		// System.out.println(meanHr);
		// for (HealthData h : restingHrData) {
		// System.out.println(h.getValue());
		// }

		while (!restingHrData.isEmpty()) {

			double currentHR = Double.valueOf(restingHrData.get(0).getValue());

//			restingHrData.get(0).setValue(
//					String.valueOf(new DecimalFormat("##.##")
//							.format(Double.valueOf(restingHrData.get(0)
//									.getValue()))));
			
			if (currentHR > meanHr + 12) { // stress

				flagCurrent = 3;
				if (Integer.valueOf(restingHrData.get(0).getUser()) > 8) {
					restStressed.add(restingHrData.get(0));
				} else {
					trainRecovery.add(restingHrData.get(0));
				}
			} else if (currentHR < 50) { // sleep

				flagCurrent = 2;
			} else {
				flagCurrent = 1; // normal
				if (Integer.valueOf(restingHrData.get(0).getUser()) > 8) {
					restNormal.add(restingHrData.get(0));
				} else {
					trainRecovery.add(restingHrData.get(0));
				}
			}

			if (flagPrevious == 0 || flagCurrent == flagPrevious) {

				flagPrevious = flagCurrent;
				currentHRRest += Double
						.valueOf(restingHrData.get(0).getValue());
				numberHRRest++;

				restingHrData.remove(0);
			} else {
				if (numberHRRest > 4) {

					List<Double> means = meansIntensitySegments
							.get(flagPrevious);
					if (means == null) {
						means = new ArrayList<Double>();
					}
					means.add(currentHRRest / numberHRRest);

					meansIntensitySegments.put(flagPrevious, means);

					if (meansIntensityTimesSegments.get(flagPrevious) != null) {
						meansIntensityTimesSegments.put(flagPrevious,
								meansIntensityTimesSegments.get(flagPrevious)
										+ numberHRRest);
					} else {
						meansIntensityTimesSegments.put(flagPrevious,
								numberHRRest);
					}
				}
				flagPrevious = flagCurrent;
				currentHRRest = Double.valueOf(restingHrData.get(0).getValue());
				numberHRRest = 1;

				restingHrData.remove(0);
			}
		}

		System.out.println("Mean: " + calculateMean(restNormal));
		
		System.out.println(meansIntensitySegments);
		System.out.println(meansIntensityTimesSegments);
		return meansIntensitySegments;
	}
	
	public static double calculateMean(List<HealthData> healthData) {
		
		double sum = 0;
		
		for (HealthData hd : healthData) {
			sum += Double.valueOf(hd.getValue());
		}
		
		return sum / healthData.size();
	}

}
