package life.genny.qwandautils;


import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.io.BufferedReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import org.w3c.dom.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import life.genny.qwanda.Answer;
import life.genny.qwanda.DateTimeDeserializer;
import life.genny.qwanda.Link;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.Person;



public class QwandaUtils {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	
	public static String apiGet(final String getUrl, final String authToken)
			throws ClientProtocolException, IOException {
		String retJson = "";
		log.debug("GET:" + getUrl + ":");
		final HttpClient client = new DefaultHttpClient();
		final HttpGet request = new HttpGet(getUrl);
		if (authToken != null) {
			request.addHeader("Authorization", "Bearer " + authToken); // Authorization": `Bearer
		}
		final HttpResponse response = client.execute(request);
		final BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			retJson += line;
			;
		}

		return retJson;
	}

	public static String apiPostEntity(final String postUrl, final String entityString, final String authToken)
			throws IOException {
		String retJson = "";
		final HttpClient client = new DefaultHttpClient();

		final HttpPost post = new HttpPost(postUrl);
		post.addHeader("Authorization", "Bearer " + authToken); // Authorization": `Bearer

		final StringEntity input = new StringEntity(entityString);
		input.setContentType("application/json");
		post.setEntity(input);
		final HttpResponse response = client.execute(post);
		final BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			retJson += line;
			;
		}
		return retJson;
	}

	public static String apiPost(final String postUrl, final ArrayList<BasicNameValuePair> nameValuePairs,
			final String authToken) throws IOException {
		String retJson = "";
		final HttpClient client = new DefaultHttpClient();
		final HttpPost post = new HttpPost(postUrl);
		post.addHeader("Authorization", "Bearer " + authToken); // Authorization": `Bearer

		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		final HttpResponse response = client.execute(post);
		final BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			retJson += line;
			;
		}
		return retJson;
	}

	public static BaseEntity createUserFromToken(final String qwandaUrl, final String serviceToken, final String userToken) throws IOException {
		JSONObject decodedToken = KeycloakUtils.getDecodedToken(userToken);
		
		String username = decodedToken.getString("preferred_username");
		String firstname = decodedToken.getString("given_name");
		String lastname =decodedToken.getString("family_name");
		String email = decodedToken.getString("email");
		
		BaseEntity be = createUser(qwandaUrl, serviceToken, username, firstname, lastname, email);
		
		return be;
	}
	
	public static BaseEntity postBaseEntity(final String qwandaUrl, final String token, final BaseEntity be
			) throws IOException
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = null;

		gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer());
		gson = gsonBuilder.create();
		String jsonBE = gson.toJson(be);
	
		
		QwandaUtils.apiPostEntity(qwandaUrl + "/qwanda/baseentitys", jsonBE,token);
		
		return be; 
	}
	public static Answer postAnswer(final String qwandaUrl, final String token, final Answer answer
			) throws IOException
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = null;

		gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer());
		gson = gsonBuilder.create();

		String id = QwandaUtils.apiPostEntity(qwandaUrl + "/qwanda/answers", gson.toJson(answer),token);
		// TODO check id is returned
		return answer;
	}

	public static Link postLink(final String qwandaUrl, final String token, final Link link
			) throws IOException
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = null;

		gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer());
		gson = gsonBuilder.create();

		QwandaUtils.apiPostEntity(qwandaUrl + "/qwanda/entityentitys", gson.toJson(link),token);

		return link;
	}	
	
	public static BaseEntity createUser(final String qwandaUrl, final String token, final String username, 
			final String firstname,
			final String lastname,
			final String email
			) throws IOException
	{

		String uname = username.replaceAll("\\&", "_AND_").replaceAll("@", "_AT_").replaceAll("\\.", "").toLowerCase();
		String code = "PER_" + username.toUpperCase();
		
		Person person = new Person(code, firstname + " " + lastname);
		
		postBaseEntity(qwandaUrl, token, person);
		postAnswer(qwandaUrl, token, new Answer(code,code,"PRI_USERNAME",uname));
		postAnswer(qwandaUrl, token, new Answer(code,code,"PRI_FIRSTNAME",firstname));
		postAnswer(qwandaUrl, token, new Answer(code,code,"PRI_LASTNAME",lastname));
		postAnswer(qwandaUrl, token, new Answer(code,code,"PRI_EMAIL",email));
		postAnswer(qwandaUrl, token, new Answer(code,code,"PRI_USERNAME",uname));
		
					
		postLink(qwandaUrl, token, new Link("GRP_USERS",code,"LNK_CORE"));
		postLink(qwandaUrl, token, new Link("GRP_PERSONS",code,"LNK_CORE"));
		
		return person;
	}
}
