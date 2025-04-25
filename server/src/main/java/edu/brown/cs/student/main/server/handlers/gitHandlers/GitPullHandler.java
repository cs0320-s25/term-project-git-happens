package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import spark.Request;
import spark.Response;

public class GitPullHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitPullHandler(final StorageInterface storage) {
    this.storage = storage;
  }

  @Override
  public Object handle(final Request request, final Response response) throws Exception {
    responseMap = new HashMap<>();

    final String branchName = request.queryParams("branch_name");
    // TODO: request.queryParams for current branch state

    try {

      // TODO: check if branchName exists,
      //  fetch branch JSON data,
      //  create helper class to compare current branch state with pulled branch,
        //  if there are no conflicts, add pulled branch to response map
        //  if there are conflicts, return error response for merging

    } catch (Exception e) {
      return returnErrorResponse("", "");
    }

    return returnSuccessResponse();
  }
}
