package ApiData;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Warning: mamy max 50 wykorzystań 1 klucza dziennie, wię mając 150 wykorzystań, uważajmy
// dla testow mozna pozapisywac dane jako pliki .json, lub nawet .txt, zeby nie marnować odpaleń
public class AcuWeatherApi {
//     private static final String apiKey = "KpFoV3MGxJ0yX8PZkMgYHZe89j4pkD4n"; // kiddo key
    private static final String apiKey = "AGwmg1rTWzUvFPmRt4ZHPAUM0xZDw9QM"; // florini key
//    private static final String apiKey = "GnVrkjfrIyp0aAtmUR6qJiseCY7Fzhyp"; // mata key
//    private static final String apiKey = "j0Uk3VetIpAnxq5F9GVyO4tkMhDLM7uw";
//private static final String apiKey = "nfrn9irmm7AD7vHy3D38pGomXJf3gpWw";

    private Gson gson = new Gson();

    private String getLocationKey(String location) {
        // pobranie klucza lokacji z nazwy miejscowosci

        try {
            String apiUrl = "http://dataservice.accuweather.com/locations/v1/PL/search?apikey=" + apiKey + "&q=" + location;
            String jsonResponse = sendHttpGetRequest(apiUrl);
            JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class); // jak jsony

            if (jsonArray.size() > 0) {
                JsonObject weatherData = jsonArray.get(0).getAsJsonObject();
                return weatherData.get("Key").getAsString(); // wyrzuc klucz jako String
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    private List<Map<String, Object>> getDailyForecasts(String location_key, boolean isMetric){ // max 5 days
        // Dostajemy pobrane dane z naszego url i przerabiamy je na Liste ze słownikami, kazdy element listy odpowiada
        // innemu dniu, w mapie dane jak na accu

        try {
            String apiUrl = "http://dataservice.accuweather.com/forecasts/v1/daily/5day/" + location_key + "?apikey=" + apiKey + "&metric=" + isMetric;
            String jsonResponse = sendHttpGetRequest(apiUrl);
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
            JsonArray dailyForecastsArray = jsonObject.getAsJsonArray("DailyForecasts");
            List<Map<String, Object>> dailyForecastsList = new ArrayList<>();
            for (JsonElement element : dailyForecastsArray) {
                JsonObject dailyForecastObject = element.getAsJsonObject();
                dailyForecastObject.remove("Headline");

                Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
                Map<String, Object> dailyForecastMap = gson.fromJson(dailyForecastObject, mapType);
                dailyForecastsList.add(dailyForecastMap);
            }
            return dailyForecastsList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private List<Map<String, Object>> getHistory(String location_key, boolean isMetric){ // max 24h
        // tak samo ale 24 komorki, pogoda 24h wstecz jaka była
        try {
            String apiUrl = "http://dataservice.accuweather.com/currentconditions/v1/" + location_key + "/historical/24?apikey=" + apiKey + "&metric=" + isMetric;
            String jsonResponse = sendHttpGetRequest(apiUrl);
            Type listType = new TypeToken<ArrayList<Map<String, Object>>>() {}.getType();
            return gson.fromJson(jsonResponse, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private List<Map<String, Object>> getHourlyForecast(String location_key, boolean isMetric){ // max 12h
        // godzinowa, miejsce na liscie - rozne godziny
        try {
            String apiUrl = "http://dataservice.accuweather.com/forecasts/v1/hourly/12hour/" + location_key + "?apikey=" + apiKey + "&metric=" + isMetric;
            String jsonResponse = sendHttpGetRequest(apiUrl);
            Type listType = new TypeToken<ArrayList<Map<String, Object>>>() {}.getType();
            return gson.fromJson(jsonResponse, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private List<Map<String, Object>> getIndices(String location_key, boolean isMetric){ // zajebiste pogody dla joggingu itd
        // rozne typy aktywnosci outdorowych i ocena czy dobra pogoda na to czy zła
        // 1 element listy to jakas pojedyncza aktywnosc w danym dniu, w srodku slowniki
        try {
            String apiUrl = "http://dataservice.accuweather.com/indices/v1/daily/5day/"+location_key+"?apikey=" + apiKey + "&metric=" + isMetric;
            String jsonResponse = sendHttpGetRequest(apiUrl);
            //JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
           // JsonArray forecastsArray = jsonObject.getAsJsonArray("DailyForecasts");
            JsonArray forecastsArray = gson.fromJson(jsonResponse, JsonArray.class);

            List<Map<String, Object>> forecastsList = new ArrayList<>();

            for (JsonElement element : forecastsArray) {
                JsonObject forecastObject = element.getAsJsonObject();
                Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
                Map<String, Object> forecastMap = gson.fromJson(forecastObject, mapType);
                forecastsList.add(forecastMap);
            }

            return forecastsList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private List<Map<String, Object>> getCurrent(String location_key, boolean isMetric){ //current weather
        // sam slownik, obecna pogoda tylko
        try {
            String apiUrl = "http://dataservice.accuweather.com/currentconditions/v1/" + location_key + "?apikey=" + apiKey + "&metric=" + isMetric;
            String jsonResponse = sendHttpGetRequest(apiUrl);

            Type listType = new TypeToken<ArrayList<Map<String, Object>>>() {}.getType();
            return gson.fromJson(jsonResponse, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static String sendHttpGetRequest(String url) throws IOException {
        // pobieranie danych z serwera i konwertowanie odpowiedzi na stringa przy dobrym status codzie
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        HttpResponse httpResponse = httpClient.execute(httpGet);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            return EntityUtils.toString(httpResponse.getEntity());
        } else {
            throw new IOException("Failed to fetch data. HTTP status code: " + statusCode);
        }
    }
    public List<List<Map<String,Object>>> Executor(String location, String unit){
        // Executor, pobiera lokalizacje i List<> z wyborami jakie komendy chcemy odpalic
        boolean isMetric = (Objects.equals(unit, "metric"));
        // zwraca liste z utworzonymi obiektami (max size 5)
        List<List<Map<String,Object>>> executed = new ArrayList<>();

        String location_key = getLocationKey(location);

        List<Thread> threads = new ArrayList<>();

        Thread currentConditionThread = new Thread(() -> {
            List<Map<String, Object>> currentCondition = getCurrent(location_key, isMetric);
            executed.add(currentCondition);
        });
        threads.add(currentConditionThread);

        Thread hourlyThread = new Thread(() -> {
            List<Map<String, Object>> hourlyData = getHourlyForecast(location_key, isMetric);
            executed.add(hourlyData);
        });
        threads.add(hourlyThread);

        Thread dailyThread = new Thread(() -> {
            List<Map<String, Object>> daily = getDailyForecasts(location_key, isMetric);
            executed.add(daily);
        });
        threads.add(dailyThread);

        Thread past24hThread = new Thread(() -> {
            List<Map<String, Object>> past24h = getHistory(location_key, isMetric);
            executed.add(past24h);
        });
        threads.add(past24hThread);

        Thread indicesThread = new Thread(() -> {
            List<Map<String, Object>> indices = getIndices(location_key, isMetric);
            executed.add(indices);
        });
        threads.add(indicesThread);

          for (Thread thread : threads) {
            try {
                thread.start();
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return executed;
    }
    public void saveExecuted(List<List<Map<String,Object>>> executed){
        try (PrintWriter writer = new PrintWriter("executed.txt")) {
            writer.println("[");
            for (List<Map<String, Object>> sublist : executed) {
                writer.println("[");
                for (Map<String, Object> dictionary : sublist) {
                    writer.println(dictionary);
                }
                writer.println("],");
                writer.println();
            }
            writer.println("]");
            System.out.println("Zapisano do pliku.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
//TODO:
// GUI z mozliwoscią wyborów jakie chcemy mieć dane, można ograniczać je czasowo, ja pobieram maxymalne jakie mamy dostepne
// cleaning - zostawienie tylko potrzebnych nam informacji z listy powstałej po Execute - pytanie czy nie wydłuzy to czasu
// połączenie tego w całość
// dokumentacja
// komentarze do kodu
