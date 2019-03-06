package life.genny.qwandautils;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import life.genny.qwanda.Answer;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeDateTime;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.exception.BadDataException;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class GitUtils {
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

  
  public static final String GIT_PROP_EXTENSION = "-git.properties";
  
  public static String gitGet(final String branch, final String project,
      final String repositoryName, final String layoutFilename) throws IOException,
          GitAPIException {
    String retJson = "";

    final Git git = Git.cloneRepository()
        .setURI("https://github.com/" + project + "/" + repositoryName + ".git")
        .setDirectory(new File(".")).setBranchesToClone(Arrays.asList("refs/heads/" + branch))
        .setBranch("refs/heads/" + branch).call();

    try (Repository repository = git.getRepository()) {
      // find the HEAD
      final ObjectId lastCommitId = repository.resolve(Constants.HEAD);

      // a RevWalk allows to walk over commits based on some filtering that is defined
      try (RevWalk revWalk = new RevWalk(repository)) {
        final RevCommit commit = revWalk.parseCommit(lastCommitId);
        // and using commit's tree find the path
        final RevTree tree = commit.getTree();
        log.info("Having tree: " + tree);

        // now try to find a specific file
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
          treeWalk.addTree(tree);
          treeWalk.setRecursive(true);
          treeWalk.setFilter(PathFilter.create(layoutFilename));
          if (!treeWalk.next()) {
            throw new IllegalStateException("Did not find expected file '" + layoutFilename + "'");
          }

          final ObjectId objectId = treeWalk.getObjectId(0);
          final ObjectLoader loader = repository.open(objectId);

          // and then one can the loader to read the file
          retJson = new String(loader.getBytes());
          loader.copyTo(System.out);
        }

        revWalk.dispose();
      }
    }
    return retJson;
  }
  
  public static String getGitVersionString(String projectDependencies) throws IOException {
    Properties gitProperties;
    JsonObject response = new JsonObject();
    JsonArray versionArray = new JsonArray();
    Map<String,Object> versionMap;
    StringTokenizer st = new StringTokenizer(projectDependencies, ",");
    while (st.hasMoreElements()) {
      String projectName = st.nextToken();
      String gitPropertiesFileName = projectName + GIT_PROP_EXTENSION;
      URL gitPropertiesURL = Thread.currentThread().getContextClassLoader().getResource(gitPropertiesFileName);
      if(gitPropertiesURL != null && gitPropertiesURL.getFile() != "") {
        gitProperties = new Properties();
        gitProperties.load(gitPropertiesURL.openStream());
        versionMap = new HashMap<>();
        versionMap.put(projectName, new JsonObject(JsonUtils.toJson(gitProperties)));
        versionArray.add(versionMap);
      }
      response.put("version", versionArray);
    }
    return response.toString();
  }


  public static List<BaseEntity> getLayoutBaseEntitys(final String remoteUrl, final String branch,final String realm) throws BadDataException, InvalidRemoteException, TransportException, GitAPIException, RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException
  {

	  List<BaseEntity> layouts = new ArrayList<BaseEntity>();
	  
	  String gitFolder = ("genny".equalsIgnoreCase(realm))?realm:(realm+"-new");
	  
	//   Process pp = Runtime.getRuntime().exec("cd /tmp;git clone -b "+branch+" "+remoteUrl);

	  
//	  String tmpDir = "/tmp/git";
//	  try {
//          File directory = new File(tmpDir);
//
//          // Deletes a directory recursively. When deletion process is fail an
//          // IOException is thrown and that's why we catch the exception.
//          FileUtils.deleteDirectory(directory);
//      } catch (IOException e) {
//          e.printStackTrace();
//      }
	  
//	  Git git = Git.cloneRepository()
//	  
//			  .setURI( remoteUrl )
//			  .setDirectory( new File(tmpDir) )
//			  .setBranch(branch)
//			  .call();
	  
	  log.info("Set up Git");

//    git.fetch()
//            .setRemote(remoteUrl)
//            .setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"))
//            .call();
    
    
    
//    Repository repo = git.getRepository();
	  
      DfsRepositoryDescription repoDesc = new DfsRepositoryDescription();
      InMemoryRepository repo = new InMemoryRepository(repoDesc);
      Git git = new Git(repo);
      git.fetch()
              .setRemote(remoteUrl)
              .setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"))
              .call();
      repo.getObjectDatabase();
      
   //   Ref head = repo.getRef("HEAD");
      ObjectId lastCommitId = repo.resolve("refs/heads/" + branch);

      // a RevWalk allows to walk over commits based on some filtering that is defined
      RevWalk walk = new RevWalk(repo);

      RevCommit commit = walk.parseCommit(lastCommitId);
      RevTree tree = commit.getTree();
      log.info("Having tree: " + tree);

      // now use a TreeWalk to iterate over all files in the Tree recursively
      // you can set Filters to narrow down the results if needed
      TreeWalk treeWalk = new TreeWalk(repo);
      treeWalk.addTree(tree);
      treeWalk.setRecursive(true);
     // treeWalk.setFilter(AndTreeFilter.create(TreeFilter.ANY_DIFF, PathFilter.ANY_DIFF));
      String realmFilter= gitFolder+"/sublayouts";
      treeWalk.setFilter(AndTreeFilter.create(PathFilter.create(realmFilter),PathSuffixFilter.create(".json")));
      while (treeWalk.next()) {
          
          final ObjectId objectId = treeWalk.getObjectId(0);
          final ObjectLoader loader = repo.open(objectId);
          FileMode fileMode = treeWalk.getFileMode(0);
          // and then one can the loader to read the file
          String content = new String(loader.getBytes());
                          
          String fullpath = treeWalk.getPathString().substring(realmFilter.length()+1);  // get rid of realm+"-new/sublayouts/"
         
          
          Path p = Paths.get(fullpath);
          String filepath = p.getParent().toString();
          String name = fullpath.substring(filepath.length()+1).replaceFirst("[.][^.]+$", "");;
          filepath = filepath+"/"+name;
			String precode = String.valueOf(filepath.replaceAll("[^a-zA-Z0-9]", "").toUpperCase().hashCode());
			String layoutCode = ("LAY_" + realm + "_" + precode).toUpperCase();
          BaseEntity layout = new BaseEntity(layoutCode,name);
          layout.addAnswer(new Answer(layout,layout,new AttributeText("PRI_LAYOUT_DATA","Layout Data"),content));
          layout.addAnswer(new Answer(layout,layout,new AttributeText("PRI_LAYOUT_URI","Layout URI"),filepath));
          layout.addAnswer(new Answer(layout,layout,new AttributeText("PRI_LAYOUT_URL","Layout URL"),"http://layout-cache-service/"+realmFilter+"/"+fullpath));   
          layout.addAnswer(new Answer(layout,layout,new AttributeText("PRI_LAYOUT_NAME","Layout Name"),name));
          long secs = commit.getCommitTime();
          LocalDateTime commitDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(secs*1000), 
                  TimeZone.getDefault().toZoneId());  

          String lastCommitDateTimeString = commitDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
          layout.addAnswer(new Answer(layout,layout,new AttributeText("PRI_LAYOUT_MODIFIED_DATE","Modified"),lastCommitDateTimeString)); // if new
          layout.setRealm(realm);
          layout.setUpdated(commitDateTime);
          layouts.add(layout);
      }
      
      return layouts;

  }
  
}
