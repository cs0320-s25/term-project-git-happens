package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import spark.Request;
import spark.Response;

public class GitCommitHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitCommitHandler(final StorageInterface storage) {
    this.storage = storage;
  }

  @Override
  public Object handle(final Request request, final Response response) throws Exception {
    responseMap = new HashMap<>();

    final String branchName = request.queryParams("branch_name");
    final String commitMessage = request.queryParams("commit_message");
//    final String changeHash = request.queryParams("change_hash"); // Instead of storing by
//    changeHash, store staged files in an array and for commit, pull the top item

    try {

      // Code

    } catch (Exception e) {
      return returnErrorResponse("", "");
    }

    return returnSuccessResponse();
  }
}
