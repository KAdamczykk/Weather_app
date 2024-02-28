package GUI;

import ApiData.AcuWeatherApi;

import java.util.List;
import java.util.Map;

public class test {
    public static void main(String[] args) {
        AcuWeatherApi acu = new AcuWeatherApi();
        List<List<Map<String,Object>>> downloaded_data = acu.Executor("Warsaw", "metric");
        acu.saveExecuted(downloaded_data);

    }
}
