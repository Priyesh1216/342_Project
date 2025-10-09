import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;

public class CSVReader {

    private String filepath;
    private ArrayList<ArrayList<String>> data;

    public CSVReader(String filepath) {
        this.filepath = filepath;
        this.data = new ArrayList<ArrayList<String>>();
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public ArrayList<ArrayList<String>> getData() {
        return this.data;
    }

    public void readData() {
        // Read in the contents of the CSV file into the data ArrayList

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8))) {
            System.out.println("Reading data");

            String line; // One line of the CSV file, read in as a string
            String[] splitLine; // The line string, split into an array
            ArrayList<String> lineArrayList; // The split array, converted into an ArrayList to be stored in 2D data
                                             // ArrayList

            while ((line = reader.readLine()) != null) {
                if (line.contains("\"")) {
                    /*
                     * Normally we would just split one line of the CSV file on the comma characters
                     * using line.split(",");
                     * However if the days of operation are not joined together, such as Mon-Fri,
                     * they will be listed separately in
                     * the format "Day1, Day2, ...". The quotation marks indicate that this should
                     * be treated as one field.
                     * 
                     * Just splitting the line on the comma characters will treat each of these days
                     * as a separate field,
                     * when instead we want to treat the list of days as one continuous string.
                     */

                    String[] lineSegments = line.split("\""); // Split on the quotation marks first

                    // The first segment includes everything before the DaysOfOperation field, so we
                    // just split on the commas normally
                    splitLine = lineSegments[0].split(",");
                    lineArrayList = new ArrayList<String>(Arrays.asList(splitLine)); // Conver to ArrayList

                    // The second segment is the DaysOfOperation field, which should be treated as
                    // one string
                    lineArrayList.add(lineSegments[1]);

                    // The last segement is the remainder, which should be split just as the first
                    // segment
                    lineSegments[2] = lineSegments[2].substring(1); // Remove leading comma
                    splitLine = lineSegments[2].split(",");

                    for (String item : splitLine) {
                        lineArrayList.add(item);
                    }
                } else {
                    /*
                     * In this case, DaysOfOperation is listed in the format "BeginningDay-EndDay",
                     * or just "Daily",
                     * so there are no extra commas to handle. We can just split the whole line on
                     * the commas, and turn it into
                     * an ArrayList.
                     */
                    splitLine = line.split(",");
                    lineArrayList = new ArrayList<String>(Arrays.asList(splitLine));
                }

                // Add this line to the overall data arrayList
                data.add(lineArrayList);
            }

        }

        catch (FileNotFoundException e) {
            System.out.println("Could not locate file.");
            System.exit(1);
        }

        catch (IOException e) {
            System.out.println("Something went wrong.");
            System.exit(1);
        }
    }

}
