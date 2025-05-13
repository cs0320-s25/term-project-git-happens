package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import spark.Request;
import spark.Response;

public class GitBranchHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitBranchHandler(final StorageInterface storage) {
    this.storage = storage;
  }

  @Override
  public Object handle(final Request request, final Response response) throws Exception {
    responseMap = new HashMap<>();

    final String branchRequest = request.queryParams("branch_request");

    try {

      // TODO: if branchRequest == "":
        // Fetch and return List of local branch names
      // TODO: if branchRequest == "-a":
        // Fetch and return List of local and remote branch names
      // TODO: if branchRequest == "-d" + ...:
        // Fetch and delete specified branch
      // TODO: else:
        // Create branch with given string name

    } catch (Exception e) {
      return returnErrorResponse("", "");
    }

    return returnSuccessResponse();
  }
}
