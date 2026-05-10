import java.net.URL;
import javax.json.*;
import java.io.*;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

public class t39_40 {

    static String perusUrl = "https://www.compass-group.fi/menuapi/feed/json";
    static String ravintola = "costNumber=0436";
    static String paivaus = "" ;
    static String kieli = "&language=fi";



    public static void main(String[] args) {


        URL url = null;
        HttpsURLConnection https = null;
        try {

            String u = perusUrl + "?" + ravintola + paivaus + kieli;

            System.out.println("url|"+u+"|\n");
            url = new URL(u);

            https = (HttpsURLConnection) url.openConnection();
            https.connect();

        } catch (Exception e) {
            System.out.println("Poikkeus URL + " + e);
        };

        try {
            InputStream is = https.getInputStream();
            JsonReader rdr = Json.createReader(is);
            JsonObject obj = rdr.readObject(); // koko roska

            JsonArray mdays = obj.getJsonArray("MenusForDays");

            for (JsonObject day : mdays.getValuesAs(JsonObject.class)) {
                JsonString pvm = day.getJsonString("Date");
                System.out.println("\n" + pvm);

                JsonArray menus = day.getJsonArray("SetMenus");
                for (JsonObject menu : menus.getValuesAs(JsonObject.class)) {
                    String hinta = "";
                    if (! menu.isNull("Price")) // hinta voi olla null josta ei tule JsonString:iÃ¤
                        menu.getJsonString("Price").toString();

                    JsonArray meals = menu.getJsonArray("Components");
                    for (JsonString meal : meals.getValuesAs(JsonString.class)) {
                        if (meal.toString().contains("Veg")){
                        System.out.println(meal.toString() + ":" + hinta);}
                    }

                }

            }


        } catch (Exception e) {
            System.out.println("Exception: + " + e);
        };

    }


} // class
