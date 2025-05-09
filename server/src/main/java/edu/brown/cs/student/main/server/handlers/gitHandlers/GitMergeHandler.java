package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.mergeHelpers.GitDiffHelper;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import spark.Request;
import spark.Response;

public class GitMergeHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitMergeHandler(final StorageInterface storage) {
    this.storage = storage;
  }

  @Override
  public Object handle(final Request request, final Response response) throws Exception {
    responseMap = new HashMap<>();
    GitDiffHelper diffHelper = new GitDiffHelper();

    //unique session id
    final String session_id = request.queryParams("session_id");
    //unique user id
    final String user_id = request.queryParams("user_id");
    //id of currently checked out branch
    final String currentBranch = request.queryParams("current_branch_id");
    //id of branch to merge with
    final String mergeBranch = request.queryParams("merge_branch_id");

    if (session_id == null) {
      returnErrorResponse("error_bad_request", "null parameter", "session_id");
    } else {
      responseMap.put("session_id", session_id);
    }
    if (user_id == null) {
      returnErrorResponse("error_bad_request", "null parameter", "user_id");
    } else {
      responseMap.put("user_id", user_id);
    }
    if (currentBranch == null) {
      returnErrorResponse("error_bad_request", "null parameter", "current_branch_id");
    } else {
      responseMap.put("current_branch_id", currentBranch);
    }
    if (mergeBranch == null) {
      returnErrorResponse("error_bad_request", "null parameter", "merge_branch_id");
    } else {
      responseMap.put("merge_branch_id", mergeBranch);
    }

    try {

      // TODO: compare current branch to fetched branch branchName
      //  create helper class to compare current branch state with pulled branch,
      //  if there are no conflicts, add pulled branch to response map
      //  if there are conflicts, return error response for merging

    } catch (Exception e) {
      return returnErrorResponse("", "");
    }

    return returnSuccessResponse();
  }
}
