package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.mergeHelpers.GitDiffHelper;
import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    GitDiffHelper diffHelper = new GitDiffHelper();

    // unique session id
    final String session_id = request.queryParams("session_id");
    // unique user id
    final String user_id = request.queryParams("user_id");
    // id of currently checked out branch
    final String branchId = request.queryParams("branch_id");
    // message to accompany commit
    final String commitMessage = request.queryParams("commit_message");

    // optional parent commit ids (for merge that was halted by conflicts)
    final String localCommitId = request.queryParams("local_commit_id");
    if (localCommitId != null) {
      responseMap.put("local_commit_id", localCommitId);
    }
    final String incomingCommitId = request.queryParams("incoming_commit_id");
    if (incomingCommitId != null) {
      responseMap.put("incoming_commit_id", incomingCommitId);
    }

    if (session_id == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "session_id");
    } else {
      responseMap.put("session_id", session_id);
    }
    if (user_id == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "user_id");
    } else {
      responseMap.put("user_id", user_id);
    }
    if (branchId == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "branch_id");
    } else {
      responseMap.put("branch_id", branchId);
    }
    if (commitMessage == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "commit_message");
    } else {
      responseMap.put("commit_message", commitMessage);
    }
    if ((localCommitId == null) != (incomingCommitId == null)) {
      return returnErrorResponse(
          "error_bad_request",
          "both local_commit_id and incoming_commit_id must be either null or included");
    }
    try {
      System.out.println("got commit");
      // get staged changes
      String newFileMapJson = storage.getLatestLocalChanges(session_id, user_id, branchId);
      // if there are no staged changes, return error for terminal display
      if (newFileMapJson == null) {
       System.out.println("no file found");
        return returnErrorResponse(
            "error_database", "No changes added to commit (use 'git add -A')");
      }

      System.out.println("got new file map");
      // get last latest local commit and deserialize file map
      Map<String, Object> latestLocalCommit =
          storage.getLatestLocalCommit(session_id, user_id, branchId);
      Map<String, List<MockFileObject>> latestFileMap =
          deserializeFileMap((String) latestLocalCommit.get("file_map_json"));

      List<String> parentCommitIdList =
          localCommitId == null
              ? Collections.singletonList(latestLocalCommit.get("commit_id").toString())
              : List.of(localCommitId, incomingCommitId);

      System.out.println("got latest file map");
      // check that there is a difference between the last commit and staged changes

      Map<String, List<MockFileObject>> newFileMap = deserializeFileMap((newFileMapJson));
      Set<String> filesWithDifferences = diffHelper.differenceDetected(latestFileMap, newFileMap);

      System.out.println("difference about to be detected");
      // if the filemap has not changed since last commit, return message for terminal display
      if (filesWithDifferences.isEmpty()) {
        return returnErrorResponse("error_database", "Nothing to commit, working tree clean");
      }

      System.out.println("difference detected, committing");
      // make new commit, populate response map for terminal display
      Map<String, Object> newCommit =
          storage.commitChange(session_id, user_id, branchId, commitMessage, parentCommitIdList);
      String commitId = newCommit.get("commit_id").toString();
      responseMap.put("new_commit", newCommit);
      responseMap.put("commit_id", commitId);
      responseMap.put("commit_message", commitMessage);
      responseMap.put("num_files_changed", filesWithDifferences.size());
      responseMap.put("action", "commit -m");
    } catch (Exception e) {
      System.out.println("exception exists");
      return returnErrorResponse("error_database", "commit_failed: " + e.getMessage());
    }

    return returnSuccessResponse();
  }
}
