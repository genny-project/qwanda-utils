package life.genny;

import static java.lang.System.out;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwandautils.GennySheets;

public class QwandaTest {

  @Test
  public void getData() {
    GennySheets sheets = new GennySheets(
        "{\"installed\":{\"client_id\":\"260075856207-9d7a02ekmujr2bh7i53dro28n132iqhe.apps.googleusercontent.com\",\"project_id\":\"genny-sheets-181905\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://accounts.google.com/o/oauth2/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"vgXEFRgQvh3_t_e5Hj-eb6IX\",\"redirect_uris\":[\"urn:ietf:wg:oauth:2.0:oob\",\"http://localhost\"]}}",
        "1VSXJUn8_BHG1aW0DQrFDnvLjx_jxcNiD33QzqO5D-jc", new File(System.getProperty("user.home"),
            ".credentials/sheets.googleapis.com-java-quickstart"));
    List<BaseEntity> bes = null;
    try {
      bes = sheets.getBeans(BaseEntity.class);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    bes.forEach(out::println);
  }
}
