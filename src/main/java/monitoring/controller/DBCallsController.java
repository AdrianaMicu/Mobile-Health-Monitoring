package monitoring.controller;

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
@RequestMapping({ "/dbCallsController" })
public class DBCallsController {

	@Autowired
	HealthDataRepository healthDataRepo;

	List<HealthData> stepcountData = new ArrayList<HealthData>();
	List<HealthData> heartrateData = new ArrayList<HealthData>();

	List<HealthData> hrvData = new ArrayList<HealthData>();
	List<HealthData> newHRVData;

	List<HealthData> newStepcountData;
	List<HealthData> newHeartrateData;

	@RequestMapping(value = "/healths", method = RequestMethod.GET)
	@ResponseBody
	public List<HealthData> viewAll(HttpServletResponse response,
			@RequestParam MultiValueMap<String, String> messageParams) {
		// m.addAttribute(healthDataRepo.getAll());

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods",
				"POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept");
		// response.setHeader(name, value);

		String type = messageParams.getFirst("type");

		if (type.equals("getHRV")) {
			hrvData = calculateHRV(heartrateData);
			synchroniseHRWithSteps(hrvData, stepcountData);
		}

		if (type.equalsIgnoreCase("stepcount")
				|| type.equalsIgnoreCase("heartrate")) {
			List<HealthData> listAll = healthDataRepo.findByType(type); // healthDataRepo.getAll();

			Collections.sort(listAll);

			if (type.equalsIgnoreCase("stepcount")) {
				// return generateRandomSteps("2015-04-08 08:45:01",
				// "2015-04-08 18:48:51");

				//stepcountData = listAll.subList(178, 225);

				// synchroniseData();

				ExportUtils.writeToCSV(listAll, "StepcountAllNew");
				return listAll.subList(178, 225);
			} else {

				heartrateData = listAll.subList(103568, 134878);

				ExportUtils.writeToCSV(listAll, "HeartrateAllNew");
				return heartrateData;
			}
		} else if (type.equalsIgnoreCase("synchronise")) {
			synchroniseHRWithSteps(heartrateData, stepcountData);
			return null;
		} else if (type.equalsIgnoreCase("readfromcsv")) {
			heartrateData = ExportUtils.readFromCSV("hr30apr.csv");
			stepcountData = ExportUtils.readFromCSV("sc30apr.csv");
			return heartrateData;
		} else if (type.equalsIgnoreCase("readfromcsvHR")) {
			return newHeartrateData; // ExportUtils.readFromCSV("allhealthdataHeartDataSync.csv");
		} else if (type.equalsIgnoreCase("readfromcsvSC")) {
			return newStepcountData; // ExportUtils.readFromCSV("allhealthdataStepcountSync.csv");
		}

		return null;
	}

	@RequestMapping(value = "/healthsync", method = RequestMethod.GET)
	@ResponseBody
	public List<HealthData> viewAllSync(HttpServletResponse response,
			@RequestParam MultiValueMap<String, String> messageParams) {

		String type = messageParams.getFirst("type");

		if (type.equalsIgnoreCase("heartrate")) {
			return newHeartrateData;
		} else if (type.equalsIgnoreCase("stepcount")) {
			return newStepcountData;
		}

		return null;
	}

	public List<HealthData> generateRandomSteps(String timeStart, String timeEnd) {

		List<HealthData> listSteps = new ArrayList<HealthData>();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date dateStart = null;
		Date dateEnd = null;
		try {
			dateStart = sdf.parse(timeStart);
			dateEnd = sdf.parse(timeEnd);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Random rand = new Random();

		while (dateStart.before(dateEnd)) {

			HealthData hd = new HealthData();
			hd.setTimestampStart(sdf.format(dateStart));

			Calendar c = Calendar.getInstance();
			c.setTime(dateStart);
			c.add(Calendar.SECOND, 5); // number of days to add
			dateStart = c.getTime();

			hd.setTimestampEnd(sdf.format(dateStart));
			hd.setType("stepcount");

			int randomNum = rand.nextInt(31 - 2) + 2;
			hd.setValue(String.valueOf(randomNum));

			listSteps.add(hd);
		}

		// ExportUtils.writeToCSV(listSteps);
		return listSteps;
	}

	public List<HealthData> getHRV() {

		List<HealthData> healthDataList = new ArrayList<HealthData>();

		DateTimeFormatter formatter1 = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		DateTimeFormatter formatter2 = DateTimeFormatter
				.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
		DateTimeFormatter formatter3 = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss");

		for (int i = 0; i < heartrateData.size() - 2; i++) {

			HealthData hd = heartrateData.get(i);
			HealthData hdNext = heartrateData.get(i + 1);

			String timeStart = hd.getTimestamp();
			String timeEnd = hdNext.getTimestamp();

			if (timeStart.contains("+")) {
				timeStart = timeStart.substring(0, 19);
			}
			if (timeEnd.contains("+")) {
				timeEnd = timeEnd.substring(0, 19);
			}

			LocalDateTime dateStart = null;
			LocalDateTime dateEnd = null;
			try {
				dateStart = LocalDateTime.parse(timeStart, formatter1);
			} catch (Exception e) {
				try {
					dateStart = LocalDateTime.parse(timeStart, formatter2);
				} catch (Exception e1) {
					dateStart = LocalDateTime.parse(timeStart, formatter3);
				}
			}

			try {
				dateEnd = LocalDateTime.parse(timeEnd, formatter1);
			} catch (Exception e) {
				try {
					dateEnd = LocalDateTime.parse(timeEnd, formatter2);
				} catch (Exception e1) {
					dateEnd = LocalDateTime.parse(timeEnd, formatter3);
				}

			}

			try {

				long duration = Duration.between(dateStart, dateEnd).toMillis();
				if (duration < 0) {
					duration = 0 - duration;
				}
				if (duration < 10000) {
					HealthData healthData = new HealthData();
					healthData.setTimestamp(hd.getTimestamp());
					healthData.setValue(String.valueOf(duration));

					healthDataList.add(healthData);
				}

				System.out.println(duration);

			} catch (Exception e) {
				System.out.println();
			}
		}

		ExportUtils.writeToCSV(healthDataList, "HRV");
		return healthDataList;
	}

	public void synchroniseHRWithSteps(List<HealthData> heartrateData,
			List<HealthData> stepcountData) {

		newHeartrateData = new ArrayList<HealthData>();
		newStepcountData = new ArrayList<HealthData>();

		DateTimeFormatter formatter1 = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss");
		DateTimeFormatter formatter2 = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		// split step interval in 30s intervals
		for (HealthData healthData : stepcountData) {

			healthData.getTimestampStart();
			healthData.getTimestampEnd();

			LocalDateTime dateStart = null;
			LocalDateTime dateEnd = null;

			dateStart = LocalDateTime.parse(healthData.getTimestampStart(),
					formatter2);
			dateEnd = LocalDateTime.parse(healthData.getTimestampEnd(),
					formatter2);

			// find out seconds between start and end date
			long seconds = dateStart.until(dateEnd, ChronoUnit.SECONDS);

			// see how many intervals to make
			int numberOfIntervals = (int) seconds / 30;

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
						dateStart.plusSeconds(30), heartrateData);

				if (meanHR > 0) {
					LocalDateTime newDateStart = dateStart.plusSeconds(15);
					dateStart = dateStart.plusSeconds(30);

					HealthData newStepcount = new HealthData();
					newStepcount.setTimestamp(newDateStart.format(formatter2));
					newStepcount.setType("stepcount");
					newStepcount.setValue(String.valueOf(numberOfSteps));
					newStepcount.setUser(healthData.getUser());

					newStepcountData.add(newStepcount);

					HealthData newHeartrate = new HealthData();
					newHeartrate.setTimestamp(newDateStart.format(formatter2));
					newHeartrate.setType("heartrate");
					newHeartrate.setValue(String.valueOf(meanHR));
					newHeartrate.setUser(healthData.getUser());

					newHeartrateData.add(newHeartrate);

					// System.out.println("date: "
					// + newDateStart.format(formatter));
					// System.out.println("HR value: " +
					// String.valueOf(meanHR));
					// System.out.println("SC value: "
					// + String.valueOf(numberOfSteps));
				}

				numberOfIntervals--;
			}

		}

		// System.out.println("new HR lenght: " + newHeartrateData.size());
		// System.out.println("new SC lenght: " + newStepcountData.size());

		ExportUtils.writeToCSV(newHeartrateData, "HeartDataSync");
		ExportUtils.writeToCSV(newStepcountData, "StepcountSync");

		// overallRestTrainHR(newHeartrateData, newStepcountData);
		// segmentation(newHeartrateData, newStepcountData);

		segmentationIntensityList(newHeartrateData, newStepcountData);
	}

	public double calculateMeanHRInInterval(LocalDateTime dateStart,
			LocalDateTime dateEnd, List<HealthData> heartrateData) {

		int index = 0;
		int sumHR = 0;

		List<HealthData> toDeleteList = new ArrayList<HealthData>();

		for (HealthData healthData : heartrateData) {

			DateTimeFormatter formatter2 = DateTimeFormatter
					.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

			LocalDateTime currentDateTime = LocalDateTime.parse(
					healthData.getTimestamp(), formatter2);

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

	// in synchronised data
	public void overallRestTrainHR(List<HealthData> heartRateData,
			List<HealthData> stepCountData) {

		// loadOptimalData("resting");
		// loadOptimalData("training");

		double restingHRMean = 0;
		double trainingHRMean = 0;

		double restingHRSum = 0;
		int restingHRNumber = 0;

		double trainingHRSum = 0;
		int trainingHRNumber = 0;

		List<HealthData> restingHRTimesAndValues = new ArrayList<HealthData>();

		List<Double> restingHRValues = new ArrayList<Double>();

		for (int i = 0; i < heartRateData.size(); i++) {

			double hrValue = Double.valueOf(heartRateData.get(i).getValue());

			if (Double.valueOf(stepCountData.get(i).getValue()) > 0) {

				trainingHRNumber++;
				trainingHRSum += hrValue;
			} else {

				restingHRNumber++;
				restingHRSum += hrValue;
				restingHRValues.add(hrValue);

				HealthData healthData = new HealthData();
				healthData.setTimestamp(heartRateData.get(i).getTimestamp());
				healthData.setValue(String.valueOf(hrValue));
				restingHRTimesAndValues.add(healthData);
			}
		}

		restingHRMean = restingHRSum / restingHRNumber;
		trainingHRMean = trainingHRSum / trainingHRNumber;

		System.out.println("Training: " + trainingHRMean);
		System.out.println("Resting: " + restingHRMean);

		calculateStdDeviation(restingHRValues, restingHRMean);
		// calculateHRV(restingHRTimesAndValues);
	}

	// with sync data
	public void segmentation(List<HealthData> heartRateData,
			List<HealthData> stepCountData) {

		HashMap<String, Double> segmentsMeanHR = new HashMap<String, Double>();

		List<Double> values10 = new ArrayList<Double>();
		List<Double> values20 = new ArrayList<Double>();
		List<Double> values30 = new ArrayList<Double>();
		List<Double> values40 = new ArrayList<Double>();
		List<Double> values50 = new ArrayList<Double>();
		List<Double> values60 = new ArrayList<Double>();
		List<Double> values65 = new ArrayList<Double>();
		List<Double> values70 = new ArrayList<Double>();
		List<Double> values75 = new ArrayList<Double>();
		List<Double> values80 = new ArrayList<Double>();
		List<Double> values90 = new ArrayList<Double>();

		double currentHR10 = 0;
		int numberHR10 = 0;

		double currentHR20 = 0;
		int numberHR20 = 0;

		double currentHR30 = 0;
		int numberHR30 = 0;

		double currentHR40 = 0;
		int numberHR40 = 0;

		double currentHR50 = 0;
		int numberHR50 = 0;

		double currentHR60 = 0;
		int numberHR60 = 0;

		double currentHR65 = 0;
		int numberHR65 = 0;

		double currentHR70 = 0;
		int numberHR70 = 0;

		double currentHR75 = 0;
		int numberHR75 = 0;

		double currentHR80 = 0;
		int numberHR80 = 0;

		double currentHR90 = 0;
		int numberHR90 = 0;

		for (int i = 0; i < stepCountData.size(); i++) {

			double currentStep = Double
					.valueOf(stepCountData.get(i).getValue());
			double currentHR = Double.valueOf(heartRateData.get(i).getValue());

			if (currentStep > 0 && currentStep < 15) { // 10%

				values10.add(currentHR);
				currentHR10 += currentHR;
				numberHR10++;
			} else if (currentStep >= 15 && currentStep < 40) { // 20%

				values20.add(currentHR);
				currentHR20 += currentHR;
				numberHR20++;
			} else if (currentStep >= 40 && currentStep < 60) { // 30%

				values30.add(currentHR);
				currentHR30 += currentHR;
				numberHR30++;
			} else if (currentStep >= 60 && currentStep < 85) { // 40%

				values40.add(currentHR);
				currentHR40 += currentHR;
				numberHR40++;
			} else if (currentStep >= 85 && currentStep < 100) { // 50%

				values50.add(currentHR);
				currentHR50 += currentHR;
				numberHR50++;
			} else if (currentStep >= 100 && currentStep < 110) { // 60%

				values60.add(currentHR);
				currentHR60 += currentHR;
				numberHR60++;
			} else if (currentStep >= 110 && currentStep < 120) { // 65%

				values65.add(currentHR);
				currentHR65 += currentHR;
				numberHR65++;
			} else if (currentStep >= 120 && currentStep < 130) { // 70%

				values70.add(currentHR);
				currentHR70 += currentHR;
				numberHR70++;
			} else if (currentStep >= 130 && currentStep < 140) { // 75%

				values75.add(currentHR);
				currentHR75 += currentHR;
				numberHR75++;
			} else if (currentStep >= 140 && currentStep < 150) { // 80%

				values80.add(currentHR);
				currentHR80 += currentHR;
				numberHR80++;
			} else if (currentStep >= 150) { // 90%

				values90.add(currentHR);
				currentHR90 += currentHR;
				numberHR90++;
			}
		}

		double mean10 = currentHR10 / numberHR10;
		double mean20 = currentHR20 / numberHR20;
		double mean30 = currentHR30 / numberHR30;
		double mean40 = currentHR40 / numberHR40;
		double mean50 = currentHR50 / numberHR50;
		double mean60 = currentHR60 / numberHR60;
		double mean65 = currentHR65 / numberHR65;
		double mean70 = currentHR70 / numberHR70;
		double mean75 = currentHR75 / numberHR75;
		double mean80 = currentHR80 / numberHR80;
		double mean90 = currentHR90 / numberHR90;

		segmentsMeanHR.put("10", mean10);
		segmentsMeanHR.put("20", mean20);
		segmentsMeanHR.put("30", mean30);
		segmentsMeanHR.put("40", mean40);
		segmentsMeanHR.put("50", mean50);
		segmentsMeanHR.put("60", mean60);
		segmentsMeanHR.put("65", mean65);
		segmentsMeanHR.put("70", mean70);
		segmentsMeanHR.put("75", mean75);
		segmentsMeanHR.put("80", mean80);
		segmentsMeanHR.put("90", mean90);

		System.out.println(segmentsMeanHR.get("10"));
		System.out.println(segmentsMeanHR.get("20"));
		System.out.println(segmentsMeanHR.get("30"));
		System.out.println(segmentsMeanHR.get("40"));
		System.out.println(segmentsMeanHR.get("50"));
		System.out.println(segmentsMeanHR.get("60"));
		System.out.println(segmentsMeanHR.get("65"));
		System.out.println(segmentsMeanHR.get("70"));
		System.out.println(segmentsMeanHR.get("75"));
		System.out.println(segmentsMeanHR.get("80"));
		System.out.println(segmentsMeanHR.get("90"));

		calculateStdDeviation(values10, mean10);
		calculateStdDeviation(values20, mean20);
		calculateStdDeviation(values30, mean30);
		calculateStdDeviation(values40, mean40);
		calculateStdDeviation(values50, mean50);
		calculateStdDeviation(values60, mean60);
		calculateStdDeviation(values65, mean65);
		calculateStdDeviation(values70, mean70);
		calculateStdDeviation(values75, mean75);
		calculateStdDeviation(values80, mean80);
		calculateStdDeviation(values90, mean90);
	}

	public void segmentationIntensityList(List<HealthData> heartRateData,
			List<HealthData> stepCountData) {

		HashMap<Integer, List<Double>> meansIntensitySegments = new HashMap<Integer, List<Double>>();
		
		int numberHR = 0;
		double currentHR = 0;
		double currentStep = 0;
		int flagPrevious = 0;
		int flagCurrent = 0;
		
		while (!stepCountData.isEmpty()) {
			
			currentStep = Double.valueOf(stepCountData.get(0).getValue()); 
			
			if (currentStep > 0 && currentStep < 15) { // 10
				
				flagCurrent = 10;
			} else if (currentStep >= 15 && currentStep < 40) { // 20%
				
				flagCurrent = 20;
			} else if (currentStep >= 40 && currentStep < 60) { // 30%
				
				flagCurrent = 30;
			} else if (currentStep >= 60 && currentStep < 85) { // 40%

				flagCurrent = 40;
			} else if (currentStep >= 85 && currentStep < 100) { // 50%

				flagCurrent = 50;
			} else if (currentStep >= 100 && currentStep < 110) { // 60%

				flagCurrent = 60;
			} else if (currentStep >= 110 && currentStep < 120) { // 65%

				flagCurrent = 65;
			} else if (currentStep >= 120 && currentStep < 130) { // 70%

				flagCurrent = 70;
			} else if (currentStep >= 130 && currentStep < 140) { // 75%

				flagCurrent = 75;
			} else if (currentStep >= 140 && currentStep < 150) { // 80%

				flagCurrent = 80;
			} else if (currentStep >= 150) { // 90%

				flagCurrent = 90;
			}
			
			if (flagPrevious == 0 || flagCurrent == flagPrevious) {
				
				flagPrevious = flagCurrent;
				currentHR += Double.valueOf(heartRateData.get(0).getValue()); 
				numberHR++;
				
				stepCountData.remove(0);
				heartRateData.remove(0);
			} else {
				
				if (currentHR > 0) {
					
					List<Double> means = meansIntensitySegments.get(flagPrevious);
					if (means == null) {
						means = new ArrayList<Double>();
					}
					means.add(currentHR / numberHR);

					meansIntensitySegments.put(flagPrevious, means);
				}
				
				flagPrevious = 0;
				currentHR = 0;
				numberHR = 0;
			}
		}
		
		System.out.println(meansIntensitySegments);
	}

	public void calculateStdDeviation(List<Double> values, double mean) {

		double sumDifferences = 0;

		for (Double value : values) {

			sumDifferences += Math.pow(value - mean, 2);
		}

		double variance = sumDifferences / values.size();

		double stdDeviation = Math.sqrt(variance);

		System.out.println("Dev: " + stdDeviation);

		boolean outliers = false;

		for (Double value : values) {

			if (value > (mean + stdDeviation) || value < (mean - stdDeviation)) {

				outliers = true;
				System.out.println("outlier " + value + " for mean: " + mean);
				// break;
			}
		}
	}

	public List<HealthData> calculateHRV(
			List<HealthData> healthDataTimesAndValues) {

		List<HealthData> hrvList = new ArrayList<HealthData>();

		DateTimeFormatter formatter1 = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		for (int i = 0; i < healthDataTimesAndValues.size() - 2; i++) {

			HealthData hd = healthDataTimesAndValues.get(i);
			HealthData hdNext = healthDataTimesAndValues.get(i + 1);

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
				if (duration < 10000) {
					HealthData healthData = new HealthData();
					healthData.setTimestamp(hd.getTimestamp());
					healthData.setValue(String.valueOf(duration));

					hrvList.add(healthData);
				}

			} catch (Exception e) {

			}
		}

		ExportUtils.writeToCSV(hrvList, "HRVList");

		return hrvList;
	}

	@RequestMapping({ "/getAll" })
	public String getAll() {
		return ""; //healthDataRepo.getTest();
	}
	
	
/*	HashMap<String, List<Double>> segmentsIntensityList = new HashMap<String, List<Double>>();

	String intensity = "";
	double currentHR = 0;
	int numberHR = 0;

	while (!stepCountData.isEmpty()) {
		
		while (!stepCountData.isEmpty()
				&& Double.valueOf(stepCountData.get(0).getValue()) > 0
				&& Double.valueOf(stepCountData.get(0).getValue()) < 15) {

			intensity = "10";
			currentHR += Double.valueOf(heartRateData.get(0).getValue());
			numberHR++;
			stepCountData.remove(stepCountData.get(0));
			heartRateData.remove(heartRateData.get(0));
		}

		if (!intensity.equalsIgnoreCase("")) {

			List<Double> means = segmentsIntensityList.get(intensity);
			if (means == null) {
				means = new ArrayList<Double>();
			}
			means.add(currentHR / numberHR);

			segmentsIntensityList.put(intensity, means);
			intensity = "";
			numberHR = 0;
		}

		while (!stepCountData.isEmpty()
				&& Double.valueOf(stepCountData.get(0).getValue()) >= 15
				&& Double.valueOf(stepCountData.get(0).getValue()) < 40) {

			intensity = "20";
			currentHR += Double.valueOf(heartRateData.get(0).getValue());
			numberHR++;
			stepCountData.remove(stepCountData.get(0));
			heartRateData.remove(heartRateData.get(0));
		}

		if (!intensity.equalsIgnoreCase("")) {

			List<Double> means = segmentsIntensityList.get(intensity);
			if (means == null) {
				means = new ArrayList<Double>();
			}
			means.add(currentHR / numberHR);

			segmentsIntensityList.put(intensity, means);
			intensity = "";
			numberHR = 0;
		}

		while (!stepCountData.isEmpty()
				&& Double.valueOf(stepCountData.get(0).getValue()) >= 40
				&& Double.valueOf(stepCountData.get(0).getValue()) < 60) {

			intensity = "30";
			currentHR += Double.valueOf(heartRateData.get(0).getValue());
			numberHR++;
			stepCountData.remove(stepCountData.get(0));
			heartRateData.remove(heartRateData.get(0));
		}

		if (!intensity.equalsIgnoreCase("")) {

			List<Double> means = segmentsIntensityList.get(intensity);
			if (means == null) {
				means = new ArrayList<Double>();
			}
			means.add(currentHR / numberHR);

			segmentsIntensityList.put(intensity, means);
			intensity = "";
			numberHR = 0;
		}

		while (!stepCountData.isEmpty()
				&& Double.valueOf(stepCountData.get(0).getValue()) >= 60
				&& Double.valueOf(stepCountData.get(0).getValue()) < 85) {

			intensity = "40";
			currentHR += Double.valueOf(heartRateData.get(0).getValue());
			numberHR++;
			stepCountData.remove(stepCountData.get(0));
			heartRateData.remove(heartRateData.get(0));
		}

		if (!intensity.equalsIgnoreCase("")) {

			List<Double> means = segmentsIntensityList.get(intensity);
			if (means == null) {
				means = new ArrayList<Double>();
			}
			means.add(currentHR / numberHR);

			segmentsIntensityList.put(intensity, means);
			intensity = "";
			numberHR = 0;
		}

		while (!stepCountData.isEmpty()
				&& Double.valueOf(stepCountData.get(0).getValue()) >= 85
				&& Double.valueOf(stepCountData.get(0).getValue()) < 100) {

			intensity = "50";
			currentHR += Double.valueOf(heartRateData.get(0).getValue());
			numberHR++;
			stepCountData.remove(stepCountData.get(0));
			heartRateData.remove(heartRateData.get(0));
		}

		if (!intensity.equalsIgnoreCase("")) {

			List<Double> means = segmentsIntensityList.get(intensity);
			if (means == null) {
				means = new ArrayList<Double>();
			}
			means.add(currentHR / numberHR);

			segmentsIntensityList.put(intensity, means);
			intensity = "";
			numberHR = 0;
		}

		while (!stepCountData.isEmpty()
				&& Double.valueOf(stepCountData.get(0).getValue()) >= 100
				&& Double.valueOf(stepCountData.get(0).getValue()) < 110) {

			intensity = "60";
			currentHR += Double.valueOf(heartRateData.get(0).getValue());
			numberHR++;
			stepCountData.remove(stepCountData.get(0));
			heartRateData.remove(heartRateData.get(0));
		}

		if (!intensity.equalsIgnoreCase("")) {

			List<Double> means = segmentsIntensityList.get(intensity);
			if (means == null) {
				means = new ArrayList<Double>();
			}
			means.add(currentHR / numberHR);

			segmentsIntensityList.put(intensity, means);
			intensity = "";
			numberHR = 0;
		}

		while (!stepCountData.isEmpty()
				&& Double.valueOf(stepCountData.get(0).getValue()) >= 110
				&& Double.valueOf(stepCountData.get(0).getValue()) < 120) {

			intensity = "65";
			currentHR += Double.valueOf(heartRateData.get(0).getValue());
			numberHR++;
			stepCountData.remove(stepCountData.get(0));
			heartRateData.remove(heartRateData.get(0));
		}

		if (!intensity.equalsIgnoreCase("")) {

			List<Double> means = segmentsIntensityList.get(intensity);
			if (means == null) {
				means = new ArrayList<Double>();
			}
			means.add(currentHR / numberHR);

			segmentsIntensityList.put(intensity, means);
			intensity = "";
			numberHR = 0;
		}

		while (!stepCountData.isEmpty()
				&& Double.valueOf(stepCountData.get(0).getValue()) >= 120
				&& Double.valueOf(stepCountData.get(0).getValue()) < 130) {

			intensity = "70";
			currentHR += Double.valueOf(heartRateData.get(0).getValue());
			numberHR++;
			stepCountData.remove(stepCountData.get(0));
			heartRateData.remove(heartRateData.get(0));
		}

		if (!intensity.equalsIgnoreCase("")) {

			List<Double> means = segmentsIntensityList.get(intensity);
			if (means == null) {
				means = new ArrayList<Double>();
			}
			means.add(currentHR / numberHR);

			segmentsIntensityList.put(intensity, means);
			intensity = "";
			numberHR = 0;
		}

		while (!stepCountData.isEmpty()
				&& Double.valueOf(stepCountData.get(0).getValue()) >= 130
				&& Double.valueOf(stepCountData.get(0).getValue()) < 140) {

			intensity = "75";
			currentHR += Double.valueOf(heartRateData.get(0).getValue());
			numberHR++;
			stepCountData.remove(stepCountData.get(0));
			heartRateData.remove(heartRateData.get(0));
		}

		if (!intensity.equalsIgnoreCase("")) {

			List<Double> means = segmentsIntensityList.get(intensity);
			if (means == null) {
				means = new ArrayList<Double>();
			}
			means.add(currentHR / numberHR);

			segmentsIntensityList.put(intensity, means);
			intensity = "";
			numberHR = 0;
		}

		while (!stepCountData.isEmpty()
				&& Double.valueOf(stepCountData.get(0).getValue()) >= 140
				&& Double.valueOf(stepCountData.get(0).getValue()) < 150) {

			intensity = "80";
			currentHR += Double.valueOf(heartRateData.get(0).getValue());
			numberHR++;
			stepCountData.remove(stepCountData.get(0));
			heartRateData.remove(heartRateData.get(0));
		}

		if (!intensity.equalsIgnoreCase("")) {

			List<Double> means = segmentsIntensityList.get(intensity);
			if (means == null) {
				means = new ArrayList<Double>();
			}
			means.add(currentHR / numberHR);

			segmentsIntensityList.put(intensity, means);
			intensity = "";
			numberHR = 0;
		}

		while (!stepCountData.isEmpty()
				&& Double.valueOf(stepCountData.get(0).getValue()) >= 150
				&& Double.valueOf(stepCountData.get(0).getValue()) < 160) {

			intensity = "90";
			currentHR += Double.valueOf(heartRateData.get(0).getValue());
			numberHR++;
			stepCountData.remove(stepCountData.get(0));
			heartRateData.remove(heartRateData.get(0));
		}

		if (!intensity.equalsIgnoreCase("")) {

			List<Double> means = segmentsIntensityList.get(intensity);
			if (means == null) {
				means = new ArrayList<Double>();
			}
			means.add(currentHR / numberHR);

			segmentsIntensityList.put(intensity, means);
			intensity = "";
			numberHR = 0;
		}

		stepCountData.remove(stepCountData.get(0));
		heartRateData.remove(heartRateData.get(0));
	}

	int size = segmentsIntensityList.size();
	System.out.println(segmentsIntensityList.get("10").size());
	intensity = "";
	currentHR = 0;
	numberHR = 0; */
}
