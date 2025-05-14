package edu.brown.cs.student.apiserver;

import static org.junit.jupiter.api.Assertions.*;

import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import java.net.HttpURLConnection;
import java.util.*;
import org.junit.jupiter.api.Test;

public class GitCommitHandlerTest extends BaseEndpointTest {

  private final String sessionId = "commit-test";
  private final String userId = "test-user";
  private final String branchId = "main";

  @Test
  public void testMissingSessionId() {
    try {
      HttpURLConnection connection =
          tryRequest(
              "gitcommit?user_id="
                  + userId
                  + "&branch_id="
                  + branchId
                  + "&commit_message=TestCommit");
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_bad_request", response.get("response"));
      assertEquals("null parameter", response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testMissingUserId() {
    try {
      HttpURLConnection connection =
          tryRequest(
              "gitcommit?session_id="
                  + sessionId
                  + "&branch_id="
                  + branchId
                  + "&commit_message=TestCommit");
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_bad_request", response.get("response"));
      assertEquals("null parameter", response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testMissingBranchId() {
    try {
      HttpURLConnection connection =
          tryRequest(
              "gitcommit?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&commit_message=TestCommit");
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_bad_request", response.get("response"));
      assertEquals("null parameter", response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testMissingCommitMessage() {
    try {
      HttpURLConnection connection =
          tryRequest(
              "gitcommit?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&branch_id="
                  + branchId);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_bad_request", response.get("response"));
      assertEquals("null parameter", response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testMismatchedParentCommitIds() {
    try {
      HttpURLConnection connection =
          tryRequest(
              "gitcommit?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&branch_id="
                  + branchId
                  + "&commit_message=TestCommit&local_commit_id=id1");
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_bad_request", response.get("response"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testCommitNoChanges() {
    try {
      Map<String, List<MockFileObject>> fileMap =
          Map.of("file1", List.of(new MockFileObject("data", "data")));
      String fileMapJson = serializeFileMap(fileMap);

      HttpURLConnection connection =
          tryRequest(
              "createsession?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&file_map_json="
                  + fileMapJson);
      Map<String, Object> sessionResponse = deserializeResponse(connection);
      assertEquals("success", sessionResponse.get("response"));

      connection =
          tryRequest(
              "gitadd?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&branch_id="
                  + branchId
                  + "&file_map_json="
                  + fileMapJson);
      Map<String, Object> addResponse = deserializeResponse(connection);
      assertEquals("success", addResponse.get("response"));

      connection =
          tryRequest(
              "gitcommit?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&branch_id="
                  + branchId
                  + "&commit_message=Initialcommit");
      Map<String, Object> commitResponse = deserializeResponse(connection);
      assertEquals("error_database", commitResponse.get("response"));
      assertEquals("Nothing to commit, working tree clean", commitResponse.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testSuccessfulCommit() {
    try {
      Map<String, List<MockFileObject>> fileMap =
          Map.of("file1", List.of(new MockFileObject("data1", "data1")));
      String fileMapJson = serializeFileMap(fileMap);

      HttpURLConnection connection =
          tryRequest(
              "createsession?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&file_map_json="
                  + fileMapJson);
      Map<String, Object> sessionResponse = deserializeResponse(connection);
      assertEquals("success", sessionResponse.get("response"));

      Map<String, List<MockFileObject>> modifiedMap =
          Map.of("file1", List.of(new MockFileObject("data2", "data2")));
      String modifiedJson = serializeFileMap(modifiedMap);

      connection =
          tryRequest(
              "gitadd?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&branch_id="
                  + branchId
                  + "&file_map_json="
                  + modifiedJson);
      Map<String, Object> addResponse = deserializeResponse(connection);
      assertEquals("success", addResponse.get("response"));

      connection =
          tryRequest(
              "gitcommit?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&branch_id="
                  + branchId
                  + "&commit_message=Updatefile");
      Map<String, Object> commitResponse = deserializeResponse(connection);
      assertEquals("success", commitResponse.get("response"));
      assertEquals("commit -m", commitResponse.get("action"));
    } catch (Exception e) {
      fail(e);
    }
  }
}
