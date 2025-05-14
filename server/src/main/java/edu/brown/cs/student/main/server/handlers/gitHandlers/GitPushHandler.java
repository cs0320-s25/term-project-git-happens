package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;

public class GitPushHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitPushHandler(final StorageInterface storage) {
    this.storage = storage;
  }

  @Override
  public Object handle(final Request request, final Response response) throws Exception {

    // unique session id
    final String sessionId = request.queryParams("session_id");
    // unique user id
    final String userId = request.queryParams("user_id");
    // id of currently checked out branch
    final String currentBranch = request.queryParams("branch_id");

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
    if (currentBranch == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "current_branch_id");
    } else {
      responseMap.put("branch_id", currentBranch);
    }

    try {

      // check that there are local staged commits
      List<Map<String, Object>> stagedCommits =
          storage.getStagedCommits(sessionId, userId, currentBranch);

      // if there is nothing to commit, return message for terminal display
      if (stagedCommits.isEmpty()) {
        responseMap.put("message", "Already up to date.");
        return returnSuccessResponse();
      }

      // check that local and remote head are the same
      Map<String, Object> localLatestCommit =
          storage.getLatestLocalCommit(sessionId, userId, currentBranch);
      Map<String, Object> remoteLatestCommit =
          storage.getLatestRemoteCommit(sessionId, currentBranch);
      String localLatestCommitId = (String) localLatestCommit.get("commit_id");
      String remoteLatestCommitId = (String) remoteLatestCommit.get("commit_id");

      // if user needs to pull remote changes, return message for terminal display
      if (!localLatestCommitId.equals(remoteLatestCommitId)) {
        responseMap.put(
            "message",
            "hint: Updates were rejected because the remote contains work that you do"
                + " not have locally. This is usually caused by another repository pushing "
                + "to the same remote branch. You may want to first integrate the remote changes "
                + "(e.g., 'git pull') before pushing again.");
        return returnErrorResponse("error_database", "Error: failed to push to origin/" + currentBranch);
      }

      // otherwise, push staged commit(s) to remote
      storage.pushCommit(sessionId, userId, currentBranch);
      responseMap.put("old_head_id", remoteLatestCommitId);
      responseMap.put("new_head_id", localLatestCommitId);
      responseMap.put("action", "push");
      responseMap.put(
          "message",
          remoteLatestCommitId
              + ".."
              + localLatestCommitId
              + " "
              + currentBranch
              + " -> "
              + "origin/"
              + currentBranch
              + " Successfully pushed.");

    } catch (Exception e) {
      return returnErrorResponse("error_database", "push_failed: " + e.getMessage());
    }

    return returnSuccessResponse();
  }
}
