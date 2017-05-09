import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * 
 * @author Hubert PopioĹ‚kiewicz
 * @version 1.0
 */
public class MeasurementStationWorker
{   
    public static void main(String[] args) {
        MeasurementStationWorker msw = new MeasurementStationWorker();
        msw.process();
    }
    private void process() {
        String[] dhtMeasurements = readFromDHT22();
        String[] bmpMeasurements = readFromBMP180();
        boolean measurementsCorrect = checkMeasurementsCorrectness(dhtMeasurements, bmpMeasurements);
        if(measurementsCorrect)
            doRequest(dhtMeasurements, bmpMeasurements);
        else
            System.out.println("Something went wrong with measurements...");
    }
    private String[] readFromDHT22() {
        System.out.println("Reading from DHT22...");
        try {
            Runtime.getRuntime().exec("sudo pigpiod");
            Process process = Runtime.getRuntime().exec("python DHT22_custom.py");
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            
            String output = "";
            String tempReaderTarget = "";
            System.out.println("Standard input:");
            while((tempReaderTarget = stdInput.readLine()) != null) {
                System.out.println(tempReaderTarget);
                output += tempReaderTarget;
            }
            System.out.println("Error input:");
            while((tempReaderTarget = stdError.readLine()) != null) {
                System.out.println(tempReaderTarget);
            }
            return output.split("----");
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return new String[0];
        }
    }
    private String[] readFromBMP180() {
        System.out.println("Reading from BMP180...");
        try {
            Runtime.getRuntime().exec("sudo pigpiod");
            Process process = Runtime.getRuntime().exec("python BMP180_custom.py");
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            
            String output = "";
            String tempReaderTarget = "";
            System.out.println("Standard input:");
            while((tempReaderTarget = stdInput.readLine()) != null) {
                System.out.println(tempReaderTarget);
                output += tempReaderTarget;
            }
            System.out.println("Error input:");
            while((tempReaderTarget = stdError.readLine()) != null) {
                System.out.println(tempReaderTarget);
            }
            return output.split("----");
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return new String[0];
        }
    }
    private boolean checkMeasurementsCorrectness(String[] dhtMeasurements, String[] bmpMeasurements) {
        if(dhtMeasurements.length != 2 || bmpMeasurements.length != 2)
            return false;
        //Wykrycie sporadycznego bledu odczytu, 
        //spowodowanego nachodzeniem na siebie zadań z crontab.
        for(String dhtMeasurement : dhtMeasurements)
            if(dhtMeasurement.equals("-999.00"))
                return false;
        return true;
    }
    private void doRequest(String[] dhtMeasurements, String[] bmpMeasurements) {
        HttpURLConnection urlConnection;
        try {
            URL url = new URL("http://46.228.229.73:8080/MeasurementStation/rest/device/addMeasurements");
            URLConnection connection = url.openConnection();
            urlConnection = (HttpURLConnection) connection;
            urlConnection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization",
                "Basic aHViZXJ0X3BvcGlvbGtpZXdpY3o6M2NmNjJkZDA4YTc2MzFkMTMwMjdlZDQ2MTE3NzY3ODI=");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setUseCaches(true);
            connection.setDoOutput(true);
            String param = getJSON(dhtMeasurements, bmpMeasurements);
            System.out.println(param);
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            out.write(param);
            out.flush();
            out.close();
            connection.connect();
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line);
            urlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
        }
    }
    private String getJSON(String[] dhtMeasurements, String[] bmpMeasurements) {
        String measurementDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH).format(new Date());
        return "{" +
               "\"serialNumber\":\"2ABCB-RPI32\"," +
               "\"warning\":false," +
               "\"measurementTypeDTOs\":[" +
                  "{" +
                     "\"code\":\"DHTTMP\"," +
                     "\"measurementDTO\":{" +
                        "\"value\":" + dhtMeasurements[0] + "," +
                        "\"date\":\"" + measurementDate + "\"," + 
                        "\"warning\":false" +
                     "}," +
                     "\"measurementDTOs\":[" +
            
                     "]" +
                  "}," +
                  "{" +
                     "\"code\":\"WWP\"," +
                     "\"measurementDTO\":{" +
                        "\"value\":" + dhtMeasurements[1] + "," +
                        "\"date\":\"" + measurementDate + "\"," +
                        "\"warning\":false" +
                     "}," +
                     "\"measurementDTOs\":[" +
            
                     "]" +
                  "}," +
                  "{" +
                     "\"code\":\"BMPTMP\"," +
                     "\"measurementDTO\":{" +
                        "\"value\":" + bmpMeasurements[0] + "," +
                        "\"date\":\"" + measurementDate + "\"," + 
                        "\"warning\":false" +
                     "}," +
                     "\"measurementDTOs\":[" +
            
                     "]" +
                  "}," +
                  "{" +
                     "\"code\":\"CWP\"," +
                     "\"measurementDTO\":{" +
                        "\"value\":" + bmpMeasurements[1] + "," +
                        "\"date\":\"" + measurementDate + "\"," + 
                        "\"warning\":false" +
                     "}," +
                     "\"measurementDTOs\":[" +
            
                     "]" +
                  "}" +
               "]" +
            "}";
    }
}
