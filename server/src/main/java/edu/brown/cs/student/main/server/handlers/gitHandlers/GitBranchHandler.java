package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import java.util.List;
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
    //unique session id
    final String sessionId = request.queryParams("session_id");
    //user id
    final String userId = request.queryParams("user_id");
    //either "" for viewing all local branches, "-a" for viewing local and remote branches,
    // "-d" for deleting branch, or "<new branch id>" for adding branch
    final String branchRequest = request.queryParams("branch_request");
    //branch id of currently checked out branch
    final String currentBranch = request.queryParams("current_branch_id");

    if (sessionId == null) {
      returnErrorResponse("error_bad_request", "null parameter",
          "session_id");
    } else {
      responseMap.put("session_id", sessionId);
    }
    if (userId == null) {
      returnErrorResponse("error_bad_request", "null parameter",
          "user_id");
    } else {
      responseMap.put("user_id", userId);
    }
    if (branchRequest == null) {
      returnErrorResponse("error_bad_request", "null parameter",
          "branch_request");
    } else {
      responseMap.put("branch_request", branchRequest);
    }
    if (currentBranch == null) {
      returnErrorResponse("error_bad_request", "null parameter",
          "current_branch_id");
    } else {
      responseMap.put("current_branch_id", currentBranch);
    }
    try {
        // Fetch and return List of local branch names
      if (branchRequest.equals("")) {
        List<String> localBranchNames = storage.getAllLocalBranches(sessionId, userId);
        responseMap.put("local_branch_names", localBranchNames);
        responseMap.put("action", "list local branches");
      }
      else if (branchRequest.equals("-a")) {
        List<String> localBranchNames = storage.getAllLocalBranches(sessionId, userId);

      }
        // Fetch and delete specified branch
      else if (branchRequest.equals("-d")) {
        String branchToDelete = request.queryParams("delete_branch_id");
        if (branchToDelete == null) {
          returnErrorResponse("error_bad_request", "null parameter(s)", "branch");
        }
        if (branchToDelete.equals(currentBranch)) {
          returnErrorResponse("error_bad_request", "cannot delete current branch");
        }
        storage.deleteBranch(sessionId, branchToDelete);
        responseMap.put("session_id", sessionId);
        responseMap.put("action", "delete local branch");
        responseMap.put("branch_id", branchToDelete);
      } else {
        //create branch with given string name
        String currentFilemap = request.queryParams("file_map_json");
        if (currentFilemap == null) {
          return returnErrorResponse("error_bad_request", "null parameter(s)", "file_map_json");
        }
        storage.addBranch(sessionId, branchRequest, currentFilemap);
        responseMap.put("session_id", sessionId);
        responseMap.put("action", "add branch");
        responseMap.put("branch_id", branchRequest);
      }
    } catch (Exception e) {
      return returnErrorResponse("error_database", "branch request failed: " + e.getMessage());
    }
    return returnSuccessResponse();
  }
}
