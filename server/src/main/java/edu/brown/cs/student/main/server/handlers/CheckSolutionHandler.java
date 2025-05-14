package edu.brown.cs.student.main.server.handlers;

import edu.brown.cs.student.main.server.mergeHelpers.GitDiffHelper;
import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import spark.Request;
import spark.Response;

public class CheckSolutionHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public CheckSolutionHandler(final StorageInterface storage) {
    this.storage = storage;
  }


  @Override
  public Object handle(Request request, Response response) throws Exception {
    responseMap = new HashMap<>();
    GitDiffHelper gitDiffHelper = new GitDiffHelper();

    // unique session id
    final String sessionId = request.queryParams("session_id");
    // unique user id
    final String userId = request.queryParams("user_id");
    // id of branch that the user's solution should be on
    final String branchId = request.queryParams("solution_branch_id");
    // json of level's solution file map
    final String solutionJson = request.queryParams("solution_file_map_json");

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
    if (branchId == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "branch_id");
    } else {
      responseMap.put("branch_id", branchId);
    }
    if (solutionJson == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "solution_file_map_json");
    } else {
      responseMap.put("solution_file_map_json", solutionJson);
    }
    try {
      Map<String, List<MockFileObject>> solutionFileMap = deserializeFileMap(solutionJson);

      // get latest pushed commit from the branch solution should be on
      Map<String, Object> latestRemoteCommit = storage.getLatestRemoteCommit(sessionId, branchId);
      Map<String, List<MockFileObject>> userFileMap = deserializeFileMap(
          (String) latestRemoteCommit.get("file_map_json"));

      // check for differences between user's commit and solution, return boolean
      Set<String> filesWithDifferences = gitDiffHelper.differenceDetected(solutionFileMap, userFileMap);
      responseMap.put("solution_correct", filesWithDifferences.isEmpty());
    } catch (Exception e) {
      return returnErrorResponse("error_database", "check_solution_failed: " + e.getMessage());
    }
    return returnSuccessResponse();
  }
}
