import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;


public class Weather
{
    public static void doHttpGet()
    {
        //http://dataservice.accuweather.com/forecasts/v1/daily/5day/274663?apikey=<ApiKey>

        String url = "http://dataservice.accuweather.com/forecasts/v1/daily/5day/274663?apikey=" + ApiKey.getApikey();

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response = null;
        try
        {
            response = client.execute(get);
            HttpEntity entity = response.getEntity();
            System.out.println(EntityUtils.toString(entity));
        }
        catch (IOException e) { System.err.println("Something went wrong getting the weather: "); e.printStackTrace();}
        catch (Exception e) {System.err.println("Error: "); e.printStackTrace();}

    }
}

//GnVrkjfrIyp0aAtmUR6qJiseCY7Fzhyp - apikey
//274663 - warsaw code
