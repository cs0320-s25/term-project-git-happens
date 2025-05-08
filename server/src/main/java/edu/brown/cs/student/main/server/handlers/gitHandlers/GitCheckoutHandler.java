package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.mergeHelpers.GitDiffHelper;
import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    GitDiffHelper diffHelper = new GitDiffHelper();
    // unique session id
    final String sessionId = request.queryParams("session_id");
    // unique user id
    final String userId = request.queryParams("user_id");
    // id of currently checked out branch
    final String currentBranch = request.queryParams("current_branch_id");
    // id of branch user would like to check out
    final String newBranch = request.queryParams("new_branch_id");
    // file map of current state of project on current branch (should include any changes that
    // haven't been staged)
    final String fileMapJson = request.queryParams("file_map_json");

    if (sessionId == null) {
      returnErrorResponse("error_bad_request", "null parameter", "session_id");
    } else {
      responseMap.put("session_id", sessionId);
    }
    if (userId == null) {
      returnErrorResponse("error_bad_request", "null parameter", "user_id");
    } else {
      responseMap.put("user_id", userId);
    }
    if (currentBranch == null) {
      returnErrorResponse("error_bad_request", "null parameter", "current_branch_id");
    } else {
      responseMap.put("current_branch_id", currentBranch);
    }
    if (newBranch == null) {
      returnErrorResponse("error_bad_request", "null parameter", "new_branch_id");
    } else {
      responseMap.put("new_branch_id", newBranch);
    }
    if (fileMapJson == null) {
      returnErrorResponse("error_bad_request", "null parameter", "file_map_json");
    } else {
      responseMap.put("file_map_json", fileMapJson);
    }

    try {
      // check for any changes between user's last local commit and current files state
      Map<String, Object> latestCommit =
          storage.getLatestLocalCommit(sessionId, userId, currentBranch);
      Map<String, List<MockFileObject>> commitedFileMap =
          deserializeFileMap((String) latestCommit.get("file_map_json"));
      Map<String, List<MockFileObject>> currentFileMap = deserializeFileMap(fileMapJson);
      Set<String> filesWithDifferences =
          diffHelper.differenceDetected(currentFileMap, commitedFileMap);
      // if there are differences, return error message for terminal display
      if (!filesWithDifferences.isEmpty()) {
        responseMap.put("difference_detected", true);
        responseMap.put("files_with_differences", filesWithDifferences);
        responseMap.put(
            "instructions", "Please commit your changes or stash them before you switch branches.");
        returnErrorResponse(
            "error_untracked_changes",
            "Your local changes to the following files would be overwritten by checkout:");
      } else {
        responseMap.put("difference_detected", false);
        // check that desired branch exists
        List<String> allRemoteBranches = storage.getAllRemoteBranches(sessionId);
        if (!allRemoteBranches.contains(newBranch)) {}
      }

    } catch (Exception e) {
      return returnErrorResponse("", "");
    }

    return returnSuccessResponse();
  }
}
