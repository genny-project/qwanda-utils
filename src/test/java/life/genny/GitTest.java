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

public class GitTest {

    private static final String REMOTE_URL = "https://github.com/genny-project/layouts.git";
    private static final String BRANCH = "master";


  private static final Logger log = org.apache.logging.log4j.LogManager
      .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

 // @Test
public void gitTest() throws MissingObjectException, IOException, InvalidRemoteException, TransportException, GitAPIException, BadDataException
{

	  List<BaseEntity> layouts = GitUtils.getLayoutBaseEntitys(REMOTE_URL, BRANCH,"internmatch");
	  
	  log.info("Layouts loaded = "+layouts.size());
	  for (BaseEntity layout : layouts) {
		  if (layout.getCode().equalsIgnoreCase("LAY_INTERNMATCH_-974673200")) {
			  log.info("found it LAY_INTERNMATCH_-974673200");
		  }
	  }
}

}
