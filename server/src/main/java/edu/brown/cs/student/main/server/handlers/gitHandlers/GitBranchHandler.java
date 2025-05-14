package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.ArrayList;
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
    // unique session id
    final String sessionId = request.queryParams("session_id");
    // user id
    final String userId = request.queryParams("user_id");
    // either "" for viewing all local branches, "-a" for viewing local and remote branches,
    // "-d" for deleting branch, or "<new branch id>" for adding branch
    final String branchRequest = request.queryParams("branch_request");
    // branch id of currently checked out branch
    final String currentBranch = request.queryParams("current_branch_id");

    // optional parameter, only used in the case where branch is being added
    final String fileMapJson;
    if (sessionId == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "session_id");
    } else {
      responseMap.put("session_id", sessionId);
    }
    if (userId == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "user_id");
    } else {
      responseMap.put("user_id", userId);
    }
    if (branchRequest == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "branch_request");
    } else {
      responseMap.put("branch_request", branchRequest);
    }
    if (currentBranch == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "current_branch_id");
    } else {
      responseMap.put("current_branch_id", currentBranch);
    }
    try {
      // Fetch and return List of local branch names
      switch (branchRequest) {
        case "" -> {
          List<String> localBranchNames = storage.getAllLocalBranches(sessionId, userId);
          responseMap.put("local_branch_names", localBranchNames);
          responseMap.put("action", "list local branches");
        }

          // Fetch and return List of remote branch names
        case "-r" -> {
          List<String> remoteBranchNames = storage.getAllRemoteBranches(sessionId);
          List<String> updatedRemoteBranchNames = new ArrayList<>();
          for (String remoteBranchName : remoteBranchNames) {
            updatedRemoteBranchNames.add("origin/" + remoteBranchName);
          }
          responseMap.put("remote_branch_names", updatedRemoteBranchNames);
          responseMap.put("action", "list remote branches");
        }

          // Fetch and return all local and remote branch names
        case "-a" -> {
          List<String> localBranchNames = storage.getAllLocalBranches(sessionId, userId);
          List<String> remoteBranchNames = storage.getAllRemoteBranches(sessionId);
          List<String> updatedRemoteBranchNames = new ArrayList<>();
          for (String remoteBranchName : remoteBranchNames) {
            updatedRemoteBranchNames.add("origin/" + remoteBranchName);
          }
          responseMap.put("local_branch_names", localBranchNames);
          responseMap.put("remote_branch_names", updatedRemoteBranchNames);
          responseMap.put("action", "list remote and local branches");
        }

          // Fetch and delete specified branch
        case "-d" -> {
          String branchToDelete = request.queryParams("delete_branch_id");
          if (branchToDelete == null) {
            return returnErrorResponse("error_bad_request", "null parameter", "delete_branch_id");
          }
          if (branchToDelete.equals(currentBranch)) {
            return returnErrorResponse("error_bad_request", "cannot delete current branch");
          }
          storage.deleteBranch(sessionId, userId, branchToDelete);
          responseMap.put("action", "delete local branch");
          responseMap.put("delete_branch_id", branchToDelete);
        }
        default -> {
          // create branch with given string name
          fileMapJson = request.queryParams("file_map_json");
          if (fileMapJson == null) {
            return returnErrorResponse("error_bad_request", "null parameter", "file_map_json");
          }
          storage.addBranch(sessionId, userId, currentBranch, branchRequest, fileMapJson);
          responseMap.put("action", "add branch");
          responseMap.put("new_branch_id", branchRequest);
        }
      }
    } catch (Exception e) {
      return returnErrorResponse("error_database", "branch_request_failed: " + e.getMessage());
    }
    return returnSuccessResponse();
  }
}
