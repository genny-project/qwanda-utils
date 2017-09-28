package life.genny.qwandautils;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class QwandaUtils {
  public static String apiGet(final String getUrl, final String authToken)
      throws ClientProtocolException, IOException {
    String retJson = "";
    if (authToken != null) {
      System.out.println("Auth:" + authToken);;
    }
    System.out.println("GET:" + getUrl + ":");
    final HttpClient client = new DefaultHttpClient();
    final HttpGet request = new HttpGet(getUrl);
    if (authToken != null) {
      request.addHeader("Authorization", "Bearer " + authToken); // Authorization": `Bearer
    }
    final HttpResponse response = client.execute(request);
    final BufferedReader rd =
        new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    String line = "";
    while ((line = rd.readLine()) != null) {
      retJson += line;;
    }

    return retJson;
  }

  public static String apiPostEntity(final String postUrl, final String entityString,
      final String authToken) throws IOException {
    String retJson = "";
    final HttpClient client = new DefaultHttpClient();

    final HttpPost post = new HttpPost(postUrl);
    post.addHeader("Authorization", "Bearer " + authToken); // Authorization": `Bearer

    final StringEntity input = new StringEntity(entityString);
    input.setContentType("application/json");
    post.setEntity(input);
    final HttpResponse response = client.execute(post);
    final BufferedReader rd =
        new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    String line = "";
    while ((line = rd.readLine()) != null) {
      retJson += line;;
    }
    return retJson;
  }

  public static String apiPost(final String postUrl,
      final ArrayList<BasicNameValuePair> nameValuePairs, final String authToken)
      throws IOException {
    String retJson = "";
    final HttpClient client = new DefaultHttpClient();
    final HttpPost post = new HttpPost(postUrl);
    post.addHeader("Authorization", "Bearer " + authToken); // Authorization": `Bearer

    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    final HttpResponse response = client.execute(post);
    final BufferedReader rd =
        new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    String line = "";
    while ((line = rd.readLine()) != null) {
      retJson += line;;
    }
    return retJson;
  }

}
