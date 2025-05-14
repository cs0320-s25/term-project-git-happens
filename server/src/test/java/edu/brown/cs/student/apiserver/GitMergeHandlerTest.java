package edu.brown.cs.student.apiserver;

import static edu.brown.cs.student.apiserver.BaseEndpointTest.*;
import static org.junit.jupiter.api.Assertions.*;

import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import java.net.HttpURLConnection;
import java.util.*;
import org.junit.jupiter.api.Test;

public class GitMergeHandlerTest extends BaseEndpointTest {

  private final String sessionId = "merge-test";
  private final String userId = "test-user";
  private final String currentBranch = "main";
  private final String mergeBranch = "feature";

  private String serializeSimpleFile(String value) {
    return serializeFileMap(Map.of("file1", List.of(new MockFileObject(value, value))));
  }

  @Test
  public void testMissingParameters() {
    try {
      HttpURLConnection connection =
          tryRequest(
              "gitmerge?user_id="
                  + userId
                  + "&current_branch_id="
                  + currentBranch
                  + "&merge_branch_id="
                  + mergeBranch
                  + "&file_map_json={}");
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_bad_request", response.get("response"));
      assertEquals("null parameter", response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testSameBranchMerge() {
    try {
      String fileMapJson =
          serializeFileMap(Map.of("file1", List.of(new MockFileObject("$1", "$1"))));
      HttpURLConnection connection =
          tryRequest(
              "createsession?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&file_map_json="
                  + fileMapJson);
      assertEquals("success", deserializeResponse(connection).get("response"));

      connection =
          tryRequest(
              "gitmerge?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&current_branch_id="
                  + currentBranch
                  + "&merge_branch_id="
                  + currentBranch
                  + "&file_map_json="
                  + fileMapJson);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));
      assertEquals("Already up to date", response.get("message"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testNonexistentMergeBranch() {
    try {
      String fileMapJson =
          serializeFileMap(Map.of("file1", List.of(new MockFileObject("$1", "$1"))));
      HttpURLConnection connection =
          tryRequest(
              "createsession?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&file_map_json="
                  + fileMapJson);
      assertEquals("success", deserializeResponse(connection).get("response"));

      connection =
          tryRequest(
              "gitmerge?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&current_branch_id="
                  + currentBranch
                  + "&merge_branch_id=ghost&file_map_json="
                  + fileMapJson);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_database", response.get("response"));
      assertEquals("Merge: ghost - not something we can merge.", response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testUncommittedChangesBlockMerge() {
    try {
      String originalJson = serializeSimpleFile("original");
      String modifiedJson =
          serializeFileMap(Map.of("file1", List.of(new MockFileObject("$1", "$1"))));

      HttpURLConnection connection =
          tryRequest(
              "createsession?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&file_map_json="
                  + originalJson);
      assertEquals("success", deserializeResponse(connection).get("response"));

      // add a new branch
      HttpURLConnection branchResponse =
          tryRequest(
              "gitbranch?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&branch_request="
                  + mergeBranch
                  + "&current_branch_id="
                  + currentBranch
                  + "&file_map_json="
                  + originalJson);
      Map<String, Object> branchJson = deserializeResponse(branchResponse);
      assertEquals("success", branchJson.get("response"));

      // now simulate modified, unstaged changes
      connection =
          tryRequest(
              "gitmerge?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&current_branch_id="
                  + currentBranch
                  + "&merge_branch_id="
                  + mergeBranch
                  + "&file_map_json="
                  + modifiedJson);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_database", response.get("response"));
      assertTrue((Boolean) response.get("difference_detected"));
      assertTrue(response.containsKey("files_with_differences"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testSuccessfulAutoMerge() {
    try {
      String baseJson =
          serializeFileMap(Map.of("file1", List.of(new MockFileObject("base", "base"))));

      HttpURLConnection connection =
          tryRequest(
              "createsession?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&file_map_json="
                  + baseJson);
      assertEquals("success", deserializeResponse(connection).get("response"));

      // Create merge branch
      HttpURLConnection branchResponse =
          tryRequest(
              "gitbranch?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&branch_request="
                  + mergeBranch
                  + "&current_branch_id="
                  + currentBranch
                  + "&file_map_json="
                  + baseJson);
      Map<String, Object> branchJson = deserializeResponse(branchResponse);
      assertEquals("success", branchJson.get("response"));

      // Modify merge branch and commit
      String mergeModified =
          serializeFileMap(
              Map.of(
                  "file1",
                  List.of(new MockFileObject("base", "base"), new MockFileObject("new", "new"))));
      tryRequest(
          "gitadd?session_id="
              + sessionId
              + "&user_id="
              + userId
              + "&branch_id="
              + mergeBranch
              + "&file_map_json="
              + mergeModified);
      tryRequest(
          "gitcommit?session_id="
              + sessionId
              + "&user_id="
              + userId
              + "&branch_id="
              + mergeBranch
              + "&commit_message=extend");

      // Merge into base
      connection =
          tryRequest(
              "gitmerge?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&current_branch_id="
                  + currentBranch
                  + "&merge_branch_id="
                  + mergeBranch
                  + "&file_map_json="
                  + baseJson);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));
      assertTrue(response.get("action") == null); // merge doesn't return action on success
      assertTrue(response.get("merged_files") instanceof Map);
    } catch (Exception e) {
      fail(e);
    }
  }
}
