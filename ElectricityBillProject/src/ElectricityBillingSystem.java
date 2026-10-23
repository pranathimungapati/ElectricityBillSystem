

		import java.io.*;
		import java.time.LocalDateTime;

		public class ElectricityBillingSystem {
		    public static void main(String[] args) {
		        File inputFile = new File("input/consumers.txt");
		        File outputDir = new File("output");
		        File reportFile = new File(outputDir, "bill_report.txt");
		        File errorFile = new File(outputDir, "errors.txt");

		        if (!outputDir.exists()) {
		            outputDir.mkdirs();
		        }

		        int recordsRead = 0;
		        int acceptedCount = 0;
		        int rejectedCount = 0;

		        ElectricityBill[] billArray = null;

		        // Write error log header
		        try (BufferedWriter errorBw = new BufferedWriter(new FileWriter(errorFile, true))) {
		            errorBw.write("=== Run Time: " + LocalDateTime.now() + " ===");
		            errorBw.newLine();
		        } catch (IOException e) {
		            System.out.println("Error initializing error log.");
		        }

		        try {
		            // Check input file validity
		            if (!inputFile.exists() || !inputFile.isFile() || !inputFile.canRead()) {
		                throw new FileNotFoundException("Input file missing or cannot be read.");
		            }
		            if (inputFile.length() == 0) {
		                throw new EmptyInputFileException("Input file is empty.");
		            }

		            try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
		                String firstLine = br.readLine();
		                if (firstLine == null || firstLine.trim().isEmpty()) {
		                    throw new EmptyInputFileException("Missing record count N in input file.");
		                }

		                int N;
		                try {
		                    N = Integer.parseInt(firstLine.trim());
		                } catch (NumberFormatException e) {
		                    throw new EmptyInputFileException("Invalid record count N format.");
		                }

		                if (N <= 0) {
		                    throw new EmptyInputFileException("Record count N is zero or negative.");
		                }

		                billArray = new ElectricityBill[N];
		                String line;
		                int lineNum = 1;

		                while ((line = br.readLine()) != null) {
		                    lineNum++;
		                    String trimmedLine = line.trim();
		                    if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
		                        continue;
		                    }

		                    recordsRead++;
		                    try {
		                        String[] fields = trimmedLine.split(",", -1);
		                        for (int i = 0; i < fields.length; i++) {
		                            fields[i] = fields[i].trim();
		                        }

		                        if (fields.length == 0) {
		                            throw new InvalidRecordException("Empty record line.");
		                        }

		                        String typeCode = fields[0];
		                        if (typeCode.equalsIgnoreCase("D")) {
		                            if (fields.length != 4) {
		                                throw new InvalidRecordException("Incorrect number of fields for Domestic record.");
		                            }
		                            String name = fields[1];
		                            double units = parseDouble(fields[2]);
		                            boolean includeGST = parseBoolean(fields[3]);

		                            ElectricityBill dBill = new ElectricityBill(name, units);
		                            dBill.calculateBill(includeGST);
		                            billArray[acceptedCount++] = dBill;

		                        } else if (typeCode.equalsIgnoreCase("C")) {
		                            if (fields.length != 6) {
		                                throw new InvalidRecordException("Incorrect number of fields for Commercial record.");
		                            }
		                            String name = fields[1];
		                            double units = parseDouble(fields[2]);
		                            double loadKW = parseDouble(fields[3]);
		                            boolean includeGST = parseBoolean(fields[4]);
		                            int daysLate = parseInt(fields[5]);

		                            CommercialBill cBill = new CommercialBill(name, units, loadKW);
		                            cBill.calculateBill(includeGST);
		                            cBill.calculatePenalty(daysLate);
		                            billArray[acceptedCount++] = cBill;

		                        } else {
		                            throw new InvalidRecordException("Unknown type code: " + typeCode);
		                        }

		                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
		                        rejectedCount++;
		                        logError(errorFile, lineNum, line, "InvalidRecordException", "Parsing error: " + e.getMessage());
		                    } catch (InvalidUnitsException | InvalidLoadException | InvalidDaysLateException | InvalidRecordException e) {
		                        rejectedCount++;
		                        logError(errorFile, lineNum, line, e.getClass().getSimpleName(), e.getMessage());
		                    }
		                }
		            }

		            // Write report
		            try (BufferedWriter reportBw = new BufferedWriter(new FileWriter(reportFile))) {
		                reportBw.write("=== ELECTRICITY BILL REPORT ===");
		                reportBw.newLine();

		                int validPrinted = 0;
		                ElectricityBill firstValid = null;
		                ElectricityBill secondValid = null;

		                for (ElectricityBill b : billArray) {
		                    if (b != null) {
		                        validPrinted++;
		                        if (validPrinted == 1) firstValid = b;
		                        else if (validPrinted == 2) secondValid = b;

		                        b.displayDetails(reportBw);
		                        if (b instanceof CommercialBill) {
		                            CommercialBill cb = (CommercialBill) b;
		                            reportBw.write("Late Penalty: " + String.format("%.2f", cb.calculatePenalty(3)));
		                            reportBw.newLine();
		                        }
		                        reportBw.write("-----------------------------------");
		                        reportBw.newLine();
		                    }
		                }

		                if (firstValid != null && secondValid != null) {
		                    reportBw.write("Comparison (First 2): " + firstValid.compareBill(secondValid));
		                    reportBw.newLine();
		                }

		                if (acceptedCount > 0) {
		                    reportBw.write("Highest Bill Consumer: " + ElectricityBill.findHighestBill(billArray));
		                    reportBw.newLine();
		                }

		                reportBw.write("Total Consumer Count: " + ElectricityBill.consumerCount);
		                reportBw.newLine();
		                reportBw.write("Records read: " + recordsRead + ", accepted: " + acceptedCount + ", rejected: " + rejectedCount);
		                reportBw.newLine();
		                reportBw.flush();
		            }

		            System.out.println("Processing complete. See output folder.");

		        } catch (FileNotFoundException e) {
		            System.out.println("Error: Input file not found. Make sure the 'input/consumers.txt' file exists inside your project folder.");
		        } catch (EmptyInputFileException e) {
		            System.out.println("Error: " + e.getMessage());
		        } catch (IOException e) {
		            System.out.println("I/O Error occurred: " + e.getMessage());
		        }
		    }

		    private static double parseDouble(String val) throws InvalidRecordException {
		        try {
		            return Double.parseDouble(val);
		        } catch (NumberFormatException e) {
		            throw new InvalidRecordException("Non-numeric value encountered", e);
		        }
		    }

		    private static int parseInt(String val) throws InvalidRecordException {
		        try {
		            return Integer.parseInt(val);
		        } catch (NumberFormatException e) {
		            throw new InvalidRecordException("Non-integer value encountered", e);
		        }
		    }

		    private static boolean parseBoolean(String val) throws InvalidRecordException {
		        if (val.equalsIgnoreCase("true")) return true;
		        if (val.equalsIgnoreCase("false")) return false;
		        throw new InvalidRecordException("Invalid boolean value: " + val);
		    }

		    private static void logError(File errorFile, int lineNum, String recordText, String exName, String message) {
		        try (BufferedWriter bw = new BufferedWriter(new FileWriter(errorFile, true))) {
		            bw.write("Line " + lineNum + " | Record: [" + recordText + "] | Exception: " + exName + " | Message: " + message);
		            bw.newLine();
		        } catch (IOException e) {
		            System.out.println("Failed to write to error log.");
		        }
		    }
		
	}


