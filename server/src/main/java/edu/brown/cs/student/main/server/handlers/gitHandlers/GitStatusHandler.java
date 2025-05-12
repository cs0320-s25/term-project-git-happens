package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.mergeHelpers.GitDiffHelper;
import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import spark.Request;
import spark.Response;

public class GitStatusHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitStatusHandler(final StorageInterface storage) {
    this.storage = storage;
  }

  @Override
  public Object handle(final Request request, final Response response) throws Exception {
    responseMap = new HashMap<>();
    // session id
    final String sessionId = request.queryParams("session_id");
    // user id
    final String userId = request.queryParams("user_id");
    // name of currently checked out branch
    final String branchId = request.queryParams("branch_id");
    // current file map json including unstaged changes
    final String fileMapJson = request.queryParams("file_map_json");

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
    if (fileMapJson == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "file_map_json");
    } else {
      responseMap.put("file_map_json", fileMapJson);
    }

    try {
      // deserialize current filemap
      Map<String, List<MockFileObject>> currentFileMap = deserializeFileMap(fileMapJson);

      // check if local and remote are ahead/behind to make branch info message for terminal display

      // get local and remote head for current branch
      Map<String, Object> latestLocalCommit = storage.getLatestLocalCommit(sessionId, userId, branchId);
      Map<String, Object> latestRemoteCommit = storage.getLatestRemoteCommit(sessionId, branchId);

      // if the heads are different, determine how
      if (!latestLocalCommit.get("commit_id").equals(latestRemoteCommit.get("commit_id"))) {
        List<Map<String, Object>> stagedCommits = storage.getStagedCommits(sessionId, userId, branchId);

        // local has commits that remote does not
        if (!stagedCommits.isEmpty()) {
          responseMap.put("branch_message", "On branch " + branchId + ". Your branch is ahead of 'origin/"
              + branchId + "' by " + stagedCommits.size() + " commits.");
        } else {
          // remote has commits that local does not
          int remoteAheadBy = storage.getRemotePushedCommits(sessionId, branchId).size()
              - storage.getLocalPushedCommits(sessionId, userId, branchId).size();
          responseMap.put("branch_message", "On branch " + branchId + ". Your branch is behind 'origin/'"
              + branchId + "' by " + remoteAheadBy + " commits, and can be fast-forwarded.");
        }
        // local and remote have the same head
      } else {
        responseMap.put("branch_message", "On branch " + branchId + ". Your branch is up to date with"
            + " 'origin/" + branchId + "'.");
      }

      // check for staged changes, add to changes to be committed if there are

      String stagedChangesJson = storage.getLatestLocalChanges(sessionId, userId, branchId);

      // send list of files with staged changes
      if (stagedChangesJson != null) {
        Map<String, List<MockFileObject>> stagedChangesFileMap = deserializeFileMap(stagedChangesJson);
        List<String> changedFiles = new ArrayList<>();
        for (String filename : stagedChangesFileMap.keySet()) {
          changedFiles.add(filename);
        }
        responseMap.put("staged_changes_message", "Changes to be committed: ");
        responseMap.put("staged_changes", changedFiles);

        // find difference between staged changes and current file map

        GitDiffHelper gitDiffHelper = new GitDiffHelper();
        Set<String> filesWithDifferences = gitDiffHelper.differenceDetected(stagedChangesFileMap, currentFileMap);

        // send set of files with unstaged changes
        if (!filesWithDifferences.isEmpty()) {
          responseMap.put("unstaged_changes_message", "Changes not staged for commit: ");
          responseMap.put("unstaged_changes", filesWithDifferences);
        } else {
          responseMap.put("unstaged_changes_message", "No unstaged changes detected.");
        }

      } else {
        responseMap.put("staged_changes_message", "No changes added to commit (use 'git add -A').");

        // find difference between last local commit and current filemap to find unstaged changes

        Map<String, List<MockFileObject>> localCommittedFileMap = deserializeFileMap(
            (String) latestLocalCommit.get("file_map_json"));
        GitDiffHelper gitDiffHelper = new GitDiffHelper();
        Set<String> filesWithDifferences = gitDiffHelper.differenceDetected(
            localCommittedFileMap, currentFileMap);

        // send set of files with unstaged changes
        if (!filesWithDifferences.isEmpty()) {
          responseMap.put("unstaged_changes_message", "Changes not staged for commit: ");
          responseMap.put("unstaged_changes", filesWithDifferences);
        } else {
          responseMap.put("unstaged_changes_message", "No unstaged changes detected.");
        }
      }

    } catch (Exception e) {
      return returnErrorResponse("error_database", "status_failed: " + e.getMessage());
    }

    return returnSuccessResponse();
  }
}
