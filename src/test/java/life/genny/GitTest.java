package life.genny;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.junit.Test;

import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwandautils.GitUtils;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.QwandaUtils;

public class GitTest {

    private static final String REMOTE_URL = "https://github.com/genny-project/layouts.git";
    private static final String BRANCH = "master";


  private static final Logger log = org.apache.logging.log4j.LogManager
      .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

 //@Test
public void gitTest() throws MissingObjectException, IOException, InvalidRemoteException, TransportException, GitAPIException, BadDataException
{

	//  List<BaseEntity> layouts = GitUtils.getLayoutBaseEntitys(REMOTE_URL, BRANCH,"internmatch","genny",false);
	  List<BaseEntity> layouts = GitUtils.getLayoutBaseEntitys(REMOTE_URL, BRANCH,System.getenv("TEST_REALM"),System.getenv("TEST_REALM")+"-new",true);

	  String url = "https://api-internmatch.outcome-hub.com/qwanda/baseentitys/";
	  
	  String token = KeycloakUtils.getAccessToken(System.getenv("TEST_KEYCLOAKURL"), System.getenv("TEST_REALM"), System.getenv("TEST_CLIENTID"), System.getenv("TEST_USER_SECRET"), System.getenv("TEST_USER"), System.getenv("TEST_USER_PASSWORD"));
	  
	  log.info("token="+token);
	  log.info("Layouts loaded = "+layouts.size());
	  for (BaseEntity layout : layouts) {
		 String layoutCode = layout.getCode();
		 
		 String json = null;
		 
		 try {
			json = QwandaUtils.apiGet(url+layoutCode, token);
			 BaseEntity be = JsonUtils.fromJson(json, BaseEntity.class);
			 isOk(layout,be);
		} catch (Exception e) {
			log.error(url+layoutCode+" not there");
		}
		
	  }
}

 private boolean isAttributeOk(final String attributeCode,BaseEntity layout, BaseEntity existing) {
	 boolean ok = existing.getValue(attributeCode, "").equals(layout.getValue(attributeCode, "!"));
	 return ok;
 }
 private boolean isOk(BaseEntity layout, BaseEntity existing) {
	 boolean ok = isAttributeOk("PRI_LAYOUT_NAME",layout,existing);
	 ok = ok & isAttributeOk("PRI_LAYOUT_URI",layout,existing);
	 ok = ok & isAttributeOk("PRI_LAYOUT_URL",layout,existing);
	 if (ok) {
		 log.info(layout.getCode()+" is ok");
	 } else {
		 log.info(layout.getCode()+" is NOT ok");
		 log.info("\t"+layout.getValue("PRI_LAYOUT_NAME", "")+" should be "+existing.getValue("PRI_LAYOUT_NAME", "BAD"));
		 log.info("\t"+layout.getValue("PRI_LAYOUT_URI", "")+" should be "+existing.getValue("PRI_LAYOUT_URI", "BAD"));
		 log.info("\t"+layout.getValue("PRI_LAYOUT_URL", "")+" should be "+existing.getValue("PRI_LAYOUT_URL", "BAD"));
	 }
	 return ok;
 }
 
}
