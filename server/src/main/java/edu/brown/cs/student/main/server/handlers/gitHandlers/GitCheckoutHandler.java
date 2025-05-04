package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;

public class GitCheckoutHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitCheckoutHandler(final StorageInterface storage) {
    this.storage = storage;
  }

  @Override
  public Object handle(final Request request, final Response response) throws Exception {
    responseMap = new HashMap<>();
    final String sessionId = request.queryParams("session_id");
    final String currentBranch = request.queryParams("current_branch_id");
    final String newBranch = request.queryParams("new_branch_id");
    final String fileMapJson = request.queryParams("file_map_json");

    if (sessionId == null || currentBranch == null || newBranch == null || fileMapJson == null) {
      String errorArg = sessionId==null?"session_id":"" + currentBranch==null?", current_branch_id":""
          + newBranch==null?", new_branch_id":"" + fileMapJson==null?", file_map_json":"" + fileMapJson;
      return returnErrorResponse("error_bad_request", "Null parameter(s)", errorArg);
    }
    try {
      Map<String, Object> currentBranchLatestCommit = storage.getLatestStagedCommit(sessionId, currentBranch);
      if (currentBranchLatestCommit == null) {
        currentBranchLatestCommit = storage.fetch(sessionId, newBranch);
      }
      //TODO: check if the latest committed changes match what the user has locally

      //TODO: if the user has uncommitted changes, tell them to commit them

    } catch (Exception e) {
      return returnErrorResponse("", "");
    }

    return returnSuccessResponse();
  }
}