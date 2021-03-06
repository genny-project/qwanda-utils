package life.genny.qwandautils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

public class DriveListener {
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	
  /** Application name. */
  private static final String APPLICATION_NAME = "Drive API Java Quickstart";

  /** Directory to store user credentials for this application. */
  private static final java.io.File DATA_STORE_DIR =
      new java.io.File(System.getProperty("user.home"), ".credentials/drive-java-quickstart");

  /** Global instance of the {@link FileDataStoreFactory}. */
  private static FileDataStoreFactory DATA_STORE_FACTORY;

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  /** Global instance of the HTTP transport. */
  private static HttpTransport HTTP_TRANSPORT;

  /**
   * Global instance of the scopes required by this quickstart.
   *
   * If modifying these scopes, delete your previously saved credentials at
   * ~/.credentials/drive-java-quickstart
   */
  private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_METADATA_READONLY);

  static {
    try {
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Creates an authorized Credential object.
   * 
   * @return an authorized Credential object.
   * @throws IOException
   */
  public static Credential authorize() throws IOException {
    // Load client secrets.

    String clientSecret = "{\"installed\":{\"client_id\":\"1024578714028-vk6ssq5j0d1rui2evvasj73h1ta7s6q3.apps.googleusercontent.com\",\"project_id\":\"genny-sheets\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://accounts.google.com/o/oauth2/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"CMoPWWPgmQxkThsPrKAti2Lx\",\"redirect_uris\":[\"urn:ietf:wg:oauth:2.0:oob\",\"http://localhost\"]}}";
    final InputStream in = IOUtils.toInputStream(clientSecret, "UTF-8");
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
    Credential credential = null;
    try {
      credential =
          new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    log.info("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
    return credential;
  }

  /**
   * Build and return an authorized Drive client service.
   * 
   * @return an authorized Drive client service
   * @throws IOException
   */
  public static Drive getDriveService() throws IOException {
    Credential credential = authorize();
    return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
        .setApplicationName(APPLICATION_NAME).build();
  }

  public static void main(String[] args) throws IOException {
    // Build a new authorized API client service.
    Drive service = getDriveService();

    // Print the names and IDs for up to 10 files.
    FileList result = service.files().list().setPageSize(10)
        .setFields("nextPageToken, files(id, name)").execute();
    List<File> files = result.getFiles();
    if (files == null || files.size() == 0) {
      log.info("No files found.");
    } else {
      log.info("Files:");
      for (File file : files) {
        System.out.printf("%s (%s)\n", file.getName(), file.getId());
      }
    }
  }

}
