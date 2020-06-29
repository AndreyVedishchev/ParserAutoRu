import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.*;

public class Parser {

    private List<Integer> priceList = new ArrayList<>(); //лист ценников по модели и году

    int min, avg, max;

    /**
     * акцепт HTTPS соединения
     */
    void initHTTPSConnection() throws NoSuchAlgorithmException, KeyManagementException {

        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    /**
     * возвращает среднюю стоимость по заданным параметрам
     * @param mark - марка авто
     * @param model - модель
     * @param year - год выпуска
     */
    void getPriceAuto(String mark, String model, String year, String region) {

        int cntCars = 0; //кол-во найденных объявлений

        int cntPages = 1; //кол-во вкладок по запросу

        long cntCost = 0; //суммарная стоимость всех найденных автомобилий

        priceList.clear();
        for (int k = 1; k <= cntPages; k++) {

            String url = "https://auto.ru/" + region + "/cars/" + mark + "/" + model + "/" + year + "-year/all/?sort=fresh_relevance_1-desc&page=" + k;
            try {
                Document doc = Jsoup.connect(url).timeout(10000).get();

                Elements pages = doc.getElementsByAttributeValue("class", "Button__text");
                for (int i = 0; i < pages.size(); i++) {
                    if (pages.get(i).toString().contains("Предыдущая")) {
                        cntPages = Integer.parseInt(pages.get(i - 1).text());
                    }
                }

                Elements listNews = doc.getElementsByAttributeValue("class", "ListingItemPrice-module__content");
                for (Element listNew : listNews) {
                    System.out.println(listNew.text().replaceAll("\\D", ""));
                    priceList.add(Integer.parseInt(listNew.text().replaceAll("\\D", "")));
                    cntCost += Integer.parseInt(listNew.text().replaceAll("\\D", ""));
                    cntCars++;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("кол-во автомобилей по запросу " + cntCars);

            if (cntCars == 0) {
                cntCars = 1;
            }
        }

        Collections.sort(priceList);
        priceList.forEach(System.out::println);

        System.out.println("кол-во вкладок по запросу " + cntPages);
        System.out.println("суммарная стоимость " + cntCost);

        if (priceList.size() > 2) {

            List<Integer> subList = priceList.subList(0, (int) (priceList.size() - priceList.size() * 0.1));
            System.out.println("priceList.size отфильтрованный " + subList.size());
            System.out.println("результат " + subList.get(subList.size() - 1));
            //return subList.get(subList.size() - 1);

            min = priceList.get(0);
            avg = subList.get(subList.size() - 1);
            max = priceList.get(priceList.size() - 1);

        } else if (priceList.size() != 0) {
            System.out.println("priceList.size отфильтрованный " + priceList.size());
            System.out.println("результат " + (int) (cntCost / cntCars));
            //return (int) (cntCost / cntCars);

            min = priceList.get(0);
            avg = (int) (cntCost / cntCars);
            max = priceList.get(priceList.size() - 1);

        } else {
            System.out.println("priceList.size отфильтрованный " + priceList.size());
            System.out.println("результат " + 0);
            //return 0;

            min = 0;
            avg = 0;
            max = 0;
        }
    }

    void calculationAVGCost() throws IOException, SQLException, ClassNotFoundException {
        DBConnect connect = new DBConnect();
        connect.connectUDWH();
        connect.deleteData();

        List<String> listRegions = Files.readAllLines(Paths.get("regions.txt"));
        List<String> listCars = Files.readAllLines(Paths.get("cars.txt"));
        List<String> years = Arrays.asList("2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020");

        //String region;
        String mark;
        String model;

        for (String region : listRegions) {

            for (String car : listCars) {
                mark = car.substring(0, car.indexOf(" ")).toLowerCase();
                model = car.substring(car.indexOf(" ") + 1).toLowerCase();

                for (String year : years) {
                    getPriceAuto(mark, model, year, region);
                    System.out.println(mark + " " + model + " " + year + " -> " + min + " " + avg + " " + max + " " + region);
                    System.out.println("*******************************************");
                    connect.insertData(mark, model, year, min, avg, max, region);
                }
            }
        }
    }
}
