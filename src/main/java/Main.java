import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class Main {

    public static void main(String[] args) {

        Parser parser = new Parser();
        try {
            parser.initHTTPSConnection();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        try {
            //parser.getPriceAuto("kia", "ceed", "2014");

            parser.calculationAVGCost();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
