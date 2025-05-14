package edu.brown.cs.student.apiserver;

import static org.junit.jupiter.api.Assertions.*;

import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class GitPushHandlerTest extends BaseEndpointTest {

  private final String sessionId = "push-test";
  private final String userA = "user-a";
  private final String userB = "user-b";
  private final String branchId = "main";

  private String serializeFile(String val) {
    return serializeFileMap(Map.of("file1", List.of(new MockFileObject(val, val))));
  }

  @Test
  public void testMissingParameter() {
    try {
      HttpURLConnection connection =
          tryRequest("gitpush?user_id=" + userA + "&branch_id=" + branchId);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_bad_request", response.get("response"));
      assertEquals("null parameter", response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testAlreadyUpToDate() {
    try {
      String fileMapJson = serializeFile("a");
      HttpURLConnection connection =
          tryRequest(
              "createsession?session_id="
                  + sessionId
                  + "&user_id="
                  + userA
                  + "&file_map_json="
                  + fileMapJson);
      assertEquals("success", deserializeResponse(connection).get("response"));

      connection =
          tryRequest(
              "gitpush?session_id=" + sessionId + "&user_id=" + userA + "&branch_id=" + branchId);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));
      assertEquals("Already up to date.", response.get("message"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testPushWithStagedCommits() {
    try {
      String fileMapJson = serializeFile("a");

      // Both users start with the same session state
      HttpURLConnection conn =
          tryRequest(
              "createsession?session_id="
                  + sessionId
                  + "&user_id="
                  + userA
                  + "&file_map_json="
                  + fileMapJson);
      assertEquals("success", deserializeResponse(conn).get("response"));

      conn =
          tryRequest(
              "createsession?session_id="
                  + sessionId
                  + "&user_id="
                  + userB
                  + "&file_map_json="
                  + fileMapJson);
      assertEquals("success", deserializeResponse(conn).get("response"));

      // User A makes and stages a change
      String updatedFileMap = serializeFile("a+1");
      conn =
          tryRequest(
              "gitadd?session_id="
                  + sessionId
                  + "&user_id="
                  + userA
                  + "&branch_id="
                  + branchId
                  + "&file_map_json="
                  + updatedFileMap);
      assertEquals("success", deserializeResponse(conn).get("response"));

      conn =
          tryRequest(
              "gitcommit?session_id="
                  + sessionId
                  + "&user_id="
                  + userA
                  + "&branch_id="
                  + branchId
                  + "&commit_message=AddA1");
      assertEquals("success", deserializeResponse(conn).get("response"));

      // User A pushes
      conn =
          tryRequest(
              "gitpush?session_id=" + sessionId + "&user_id=" + userA + "&branch_id=" + branchId);
      Map<String, Object> response = deserializeResponse(conn);
      assertEquals("success", response.get("response"));
      assertTrue(response.get("message").toString().contains("Successfully pushed"));
      assertEquals("push", response.get("action"));
      assertNotNull(response.get("old_head_id"));
      assertNotNull(response.get("new_head_id"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testPushRejectedDueToRemoteAhead() {
    try {
      String fileMapJson = serializeFile("a");

      // Both users start with same state
      HttpURLConnection conn =
          tryRequest(
              "createsession?session_id="
                  + sessionId
                  + "&user_id="
                  + userA
                  + "&file_map_json="
                  + fileMapJson);
      assertEquals("success", deserializeResponse(conn).get("response"));
      conn =
          tryRequest(
              "createsession?session_id="
                  + sessionId
                  + "&user_id="
                  + userB
                  + "&file_map_json="
                  + fileMapJson);
      assertEquals("success", deserializeResponse(conn).get("response"));

      // userB pushes first
      String update = serializeFile("a+1");
      conn =
          tryRequest(
              "gitadd?session_id="
                  + sessionId
                  + "&user_id="
                  + userB
                  + "&branch_id="
                  + branchId
                  + "&file_map_json="
                  + update);
      assertEquals("success", deserializeResponse(conn).get("response"));
      conn =
          tryRequest(
              "gitcommit?session_id="
                  + sessionId
                  + "&user_id="
                  + userB
                  + "&branch_id="
                  + branchId
                  + "&commit_message=Bupdate");
      assertEquals("success", deserializeResponse(conn).get("response"));
      conn =
          tryRequest(
              "gitpush?session_id=" + sessionId + "&user_id=" + userB + "&branch_id=" + branchId);
      assertEquals("success", deserializeResponse(conn).get("response"));

      // userA commits based on outdated state
      String staleUpdate = serializeFile("a+2");
      conn =
          tryRequest(
              "gitadd?session_id="
                  + sessionId
                  + "&user_id="
                  + userA
                  + "&branch_id="
                  + branchId
                  + "&file_map_json="
                  + staleUpdate);
      assertEquals("success", deserializeResponse(conn).get("response"));
      conn =
          tryRequest(
              "gitcommit?session_id="
                  + sessionId
                  + "&user_id="
                  + userA
                  + "&branch_id="
                  + branchId
                  + "&commit_message=Aupdate");
      assertEquals("success", deserializeResponse(conn).get("response"));

      // Now userA tries to push, which should fail due to remote being ahead
      conn =
          tryRequest(
              "gitpush?session_id=" + sessionId + "&user_id=" + userA + "&branch_id=" + branchId);
      Map<String, Object> response = deserializeResponse(conn);
      assertEquals("error_database", response.get("response"));
      assertTrue(response.get("message").toString().contains("Updates were rejected"));
    } catch (Exception e) {
      fail(e);
    }
  }
}
