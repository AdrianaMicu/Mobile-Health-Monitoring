package monitoring.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import monitoring.model.HealthData;
import monitoring.model.HealthDataFeatures;

public class ExportUtils {

	// Delimiter used in CSV file
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";

	// CSV file header
	private static final String FILE_HEADER = "timestamp,timestampStart,timestampEnd,type,value,user";
	private static final String FILE_HEADER_FEATURES = "timestamp,meanHR,stdDevHR,hrvEctopic,meanHRV,stdDevHRV,medianHRV,nn50,nn20,rmssd,sdsd,minNN,maxNN,hrvTriangularIndex,tinn,sd1,sd2,spEn,person";
	private static final String FILE_HEADER_FEATURES_NO_TIME = "meanHR,stdDevHR,hrvEctopic,meanHRV,stdDevHRV,medianHRV,nn50,nn20,rmssd,sdsd,minNN,maxNN,hrvTriangularIndex,tinn,sd1,sd2,spEn,person";
	private static final String FILE_HEADER_FEATURES_NO_TIME_LESS = "meanHR,stdDevHR,hrvEctopic,meanHRV,stdDevHRV,person";
	private static final String FILE_HEADER_FEATURES_NO_TIME_MORE = "meanHR,stdDevHR,hrvEct,meanHRV,stdDevHRV,medianHRV,nn50,nn20,rmssd,sdsd,minNN,maxNN,hrvTi,tinn,sd1,sd2,steps,spEn,person";

	public static String getDayPeriod(String timestamp) {

		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		LocalDateTime dateTime = LocalDateTime.parse(timestamp, formatter);

		if (dateTime.getHour() < 7) {
			return "0"; // sleep
		} else if (dateTime.getHour() < 12) {
			return "1"; // morning
		} else if (dateTime.getHour() < 18) {
			return "2"; // day
		} else {
			return "3"; // evening
		}
	}

	public static void writeToCSVNoTime(List<HealthData> f1,
			List<HealthData> f2, List<HealthData> f3, List<HealthData> f4,
			List<HealthData> f5, List<HealthData> f6, List<HealthData> f7,
			List<HealthData> f8, List<HealthData> f9, List<HealthData> f10,
			List<HealthData> f11, List<HealthData> f12, List<HealthData> f13,
			List<HealthData> f14, List<HealthData> f15, List<HealthData> f16,
			List<HealthData> f17, List<HealthData> f18, String name) {

		try (PrintWriter fileWriter = new PrintWriter(new BufferedWriter(
				new FileWriter("EXTRACTION-" + name + "-INT.csv", true)))) {

			// Write the CSV file header
			fileWriter.append(FILE_HEADER_FEATURES_NO_TIME_MORE.toString());

			// Add a new line separator after the header
			fileWriter.append(NEW_LINE_SEPARATOR);

			for (int i = 0; i < f17.size(); i++) {

				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f1.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f2.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f3.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f4.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f5.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f6.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f7.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f8.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f9.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f10.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f11.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f12.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f13.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f14.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f15.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f16.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f17.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##").format(Double
						.valueOf(f18.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("BPOS");

				fileWriter.append(NEW_LINE_SEPARATOR);
			}

			// Write a new object list to the CSV file
			// for (int i = 0; i < f17.size(); i++) {
			//
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f1.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f2.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f3.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f4.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f5.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f6.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f7.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f8.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f9.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f10.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f11.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f12.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f13.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f14.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f15.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f16.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f17.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append(new DecimalFormat("##.##").format(Double
			// .valueOf(f18.get(i).getValue())));
			// fileWriter.append(COMMA_DELIMITER);
			// fileWriter.append("BPOS");
			//
			// fileWriter.append(NEW_LINE_SEPARATOR);
			// }
		} catch (IOException e) {

			System.out.println("write exception");
		}

		// FileWriter fileWriter = null;
		//
		// try {
		// fileWriter = new FileWriter("EXTRACTION-" + name + ".csv");
		//
		// // Write the CSV file header
		// fileWriter.append(FILE_HEADER_FEATURES_NO_TIME.toString());
		//
		// // Add a new line separator after the header
		// fileWriter.append(NEW_LINE_SEPARATOR);
		//
		// // Write a new student object list to the CSV file
		// for (int i = 0; i < f1.size(); i++) {
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f1.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f2.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f3.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f4.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f5.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f6.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f7.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f8.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f9.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f10.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f11.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f12.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f13.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f14.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f15.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f16.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append(new DecimalFormat("##.##").format(Double
		// .valueOf(f17.get(i).getValue())));
		// fileWriter.append(COMMA_DELIMITER);
		// fileWriter.append("BPOS");
		//
		// fileWriter.append(NEW_LINE_SEPARATOR);
		// }
		//
		// System.out.println("CSV file was created successfully !!!");
		//
		// } catch (Exception e) {
		// System.out.println("Error in CsvFileWriter !!!");
		// e.printStackTrace();
		// } finally {
		//
		// try {
		// fileWriter.flush();
		// fileWriter.close();
		// } catch (IOException e) {
		// System.out
		// .println("Error while flushing/closing fileWriter !!!");
		// e.printStackTrace();
		// }
		//
		// }

	}

	public static void writeToCSV(List<HealthData> f1, List<HealthData> f2,
			List<HealthData> f3, List<HealthData> f4, List<HealthData> f5,
			List<HealthData> f6, List<HealthData> f7, List<HealthData> f8,
			List<HealthData> f9, List<HealthData> f10, List<HealthData> f11,
			List<HealthData> f12, List<HealthData> f13, List<HealthData> f14,
			List<HealthData> f15, List<HealthData> f16, List<HealthData> f17,
			String name) {

		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter("EXTRACTION-" + name + ".csv");

			// Write the CSV file header
			fileWriter.append(FILE_HEADER_FEATURES.toString());

			// Add a new line separator after the header
			fileWriter.append(NEW_LINE_SEPARATOR);

			// Write a new student object list to the CSV file
			for (int i = 0; i < f1.size(); i++) {

				fileWriter.append(String.valueOf(f1.get(i).getTimestamp()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f1.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f2.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f3.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f4.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f5.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f6.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f7.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f8.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f9.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f10.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f11.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f12.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f13.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f14.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f15.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f16.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new DecimalFormat("##.##").format(Double
						.valueOf(f17.get(i).getValue())));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("BPOS");

				fileWriter.append(NEW_LINE_SEPARATOR);
			}

			System.out.println("CSV file was created successfully !!!");

		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {

			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out
						.println("Error while flushing/closing fileWriter !!!");
				e.printStackTrace();
			}

		}

	}

	public static void writeToCSV(List<HealthData> listAll, String name) {

		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter(name + ".csv");

			// Write the CSV file header
			fileWriter.append(FILE_HEADER.toString());

			// Add a new line separator after the header
			fileWriter.append(NEW_LINE_SEPARATOR);

			// Write a new student object list to the CSV file
			for (HealthData healthData : listAll) {
				fileWriter.append(String.valueOf(healthData.getTimestamp()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter
						.append(String.valueOf(healthData.getTimestampStart()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(healthData.getTimestampEnd()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(healthData.getType());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(healthData.getValue());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(healthData.getUser());
				fileWriter.append(NEW_LINE_SEPARATOR);
			}

			System.out.println("CSV file was created successfully !!!");

		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {

			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out
						.println("Error while flushing/closing fileWriter !!!");
				e.printStackTrace();
			}

		}
	}

	public static List<HealthData> readFromCSV(String csvFile) {

		List<HealthData> healthDataList = new ArrayList<HealthData>();

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] healthDataValues = line.split(cvsSplitBy);

				HealthData healthData = new HealthData();

				healthData.setTimestamp(healthDataValues[0]);
				healthData.setTimestampStart(healthDataValues[1]);
				healthData.setTimestampEnd(healthDataValues[2]);
				healthData.setType(healthDataValues[3]);
				healthData.setValue(healthDataValues[4]);
				healthData.setUser(healthDataValues[5]);

				healthDataList.add(healthData);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return healthDataList;
	}
	
	public static List<HealthDataFeatures> readFromCSVFeatures(String csvFile) {

		List<HealthDataFeatures> healthDataFeaturesList = new ArrayList<HealthDataFeatures>();

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";

		try {

			br = new BufferedReader(new FileReader(csvFile));
			br.readLine();
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] healthDataValues = line.split(cvsSplitBy);

				HealthDataFeatures healthDataFeatures = new HealthDataFeatures();

				healthDataFeatures.setMeanHR(Integer.valueOf(healthDataValues[0]));
				healthDataFeatures.setStdDevHR(Integer.valueOf(healthDataValues[1]));
				healthDataFeatures.setHrvEct(Integer.valueOf(healthDataValues[2]));
				healthDataFeatures.setMeanHRV(Integer.valueOf(healthDataValues[3]));
				healthDataFeatures.setStdDevHRV(Integer.valueOf(healthDataValues[4]));
				healthDataFeatures.setMedianHRV(Integer.valueOf(healthDataValues[5]));
				healthDataFeatures.setNn50(Integer.valueOf(healthDataValues[6]));
				healthDataFeatures.setNn20(Integer.valueOf(healthDataValues[7]));
				healthDataFeatures.setRmssd(Integer.valueOf(healthDataValues[8]));
				healthDataFeatures.setSdsd(Integer.valueOf(healthDataValues[9]));
				healthDataFeatures.setMinNN(Integer.valueOf(healthDataValues[10]));
				healthDataFeatures.setMaxNN(Integer.valueOf(healthDataValues[11]));
				healthDataFeatures.setHrvTi(Integer.valueOf(healthDataValues[12]));
				healthDataFeatures.setTinn(Integer.valueOf(healthDataValues[13]));
				healthDataFeatures.setSd1(Integer.valueOf(healthDataValues[14]));
				healthDataFeatures.setSd2(Integer.valueOf(healthDataValues[15]));
				healthDataFeatures.setSteps(Integer.valueOf(healthDataValues[16]));
				healthDataFeatures.setSpEn(Integer.valueOf(healthDataValues[17]));

				healthDataFeaturesList.add(healthDataFeatures);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return healthDataFeaturesList;
	}
}
