package life.genny.qwandautils;

import org.apache.http.client.ClientProtocolException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class GitUtils {
  public static String gitGet(final String branch, final String project,
      final String repositoryName, final String layoutFilename) throws ClientProtocolException,
      IOException, InvalidRemoteException, TransportException, GitAPIException {
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
        System.out.println("Having tree: " + tree);

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


}
