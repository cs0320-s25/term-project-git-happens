package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;

public class GitResetHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitResetHandler(final StorageInterface storage) {
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
    //commit to reset to
    final String commitId = request.queryParams("reset_commit_id");

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
    if (commitId == null) {
      return returnErrorResponse("error_bad_request", "null_parameter", "reset_commit_id");
    } else {
      responseMap.put("reset_commit_id", commitId);
    }

    try {
      //get all local commits
      Map<String, List<Map<String, Object>>> allLocalCommits = storage.getAllLocalCommits(session_id, user_id, branchId);
      List<Map<String, Object>> stagedCommits = allLocalCommits.get("staged_commits");
      List<Map<String, Object>> pushedCommits = allLocalCommits.get("pushed_commits");

      List<Map<String, Object>> newStagedCommits = new ArrayList<>();
      List<Map<String, Object>> newPushedCommits = new ArrayList<>();
      Map<String, Object> resetCommit = null;
      // if desired commit is in staged commits, trim staged commits list
      for (Map<String, Object> commit : stagedCommits) {
        newStagedCommits.add(commit);
        if (commit.get("commit_id") == commitId) {
          resetCommit = commit;
          break;
        }
      }
      // if commit was not in staged commits list, discard all staged commits
      if (resetCommit == null) {
        newStagedCommits.clear();
        // if desired commit is in pushed commits, trim pushed commits list
        for (Map<String, Object> commit : pushedCommits) {
          newPushedCommits.add(commit);
          if (commit.get("commit_id") == commitId) {
            resetCommit = commit;
            break;
          }
        }
      }
      // if commit is not in local commit history, return error for terminal display
      if (resetCommit == null) {
        responseMap.put("message", "Commit '" + commitId + "' not found. Hint: use 'git log' to view a list of local commits.");
        returnErrorResponse("error_database", "Error: failed to reset.");
      }

      // if commit found, reset local commit history
      allLocalCommits.put("staged_commits", newStagedCommits);
      allLocalCommits.put("pushed_commits", newPushedCommits);
      storage.resetLocalCommits(session_id, user_id, branchId, allLocalCommits);

      // return new head's file_map_json so user's screen can be reset and message for terminal
      // display
      responseMap.put("file_map_json", resetCommit.get("file_map_json"));
      responseMap.put("action", "reset");
      responseMap.put("message", "HEAD is now at " + commitId + resetCommit.get("commit_message"));

    } catch (Exception e) {
      return returnErrorResponse("error_database", "reset_failed: " + e.getMessage());
    }

    return returnSuccessResponse();
  }
}
