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

    //unique session id
    final String session_id = request.queryParams("session_id");
    //unique user id
    final String user_id = request.queryParams("user_id");
    //id of currently checked out branch
    final String branchId = request.queryParams("branch_id");
    //message to accompany commit
    final String commitMessage = request.queryParams("commit_message");

    if (session_id == null) {
      return returnErrorResponse("error_bad_request", "null_parameter", "session_id");
    } else {
      responseMap.put("session_id", session_id);
    }
    if (user_id == null) {
      return returnErrorResponse("error_bad_request", "null_parameter", "user_id");
    } else {
      responseMap.put("user_id", user_id);
    }
    if (branchId == null) {
      return returnErrorResponse("error_bad_request", "null_parameter", "branch_id");
    } else {
      responseMap.put("branch_id", branchId);
    }
    if (commitMessage == null) {
      return returnErrorResponse("error_bad_request", "null_parameter", "commit_message");
    } else {
      responseMap.put("commit_message", commitMessage);
    }
    try {
      storage.commitChange(session_id, user_id, branchId, commitMessage);
      responseMap.put("action", "commit -m");
    } catch (Exception e) {
      return returnErrorResponse("error_database", "commit_failed: " + e.getMessage());
    }

    return returnSuccessResponse();
  }
}
