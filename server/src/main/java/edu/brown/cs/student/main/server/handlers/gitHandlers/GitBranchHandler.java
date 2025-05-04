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
    final String sessionId = request.session().attribute("session_id");
    final String branchRequest = request.queryParams("branch_request");
    if (branchRequest == null || sessionId == null) {
      returnErrorResponse("error_bad_request", "null parameter(s)", branchRequest==null? "branch_request" : "" + sessionId==null? ", session_id" : "");
    }
    try {
      // TODO: if branchRequest == "":
        // Fetch and return List of local branch names
      if (branchRequest.equals("")) {
        List<String> branchNames = storage.getAllBranches(sessionId);
        responseMap.put("session_id", sessionId);
        responseMap.put("branch_names", branchNames);
        responseMap.put("action", "list local branches");
      }
      // TODO: if branchRequest == "-d" + ...:
        // Fetch and delete specified branch
      else if (branchRequest.equals("-d")) {
        String branchToDelete = request.queryParams("branch_id");
        if (branchToDelete == null) {
          returnErrorResponse("error_bad_request", "null parameter(s)", "branch");
        }
        storage.deleteBranch(sessionId, branchToDelete);
        responseMap.put("session_id", sessionId);
        responseMap.put("action", "delete local branch");
        responseMap.put("branch_id", branchToDelete);
      } else {
        //create branch with given string name
        String newBranch = request.queryParams("new_branch_id");
        String currentProjectState = request.queryParams("file_map_json");
        if (newBranch == null || currentProjectState == null) {
          returnErrorResponse("error_bad_request", "null parameter(s)", newBranch==null? "new_branch_id" : "" + currentProjectState==null? ", file_map_json" : "");
        }
        storage.addBranch(sessionId, newBranch, currentProjectState);
        responseMap.put("session_id", sessionId);
        responseMap.put("action", "add branch");
        responseMap.put("branch_id", newBranch);
      }
    } catch (Exception e) {
      return returnErrorResponse("error_database", "branch request failed: " + e.getMessage());
    }
    return returnSuccessResponse();
  }
}
