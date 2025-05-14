package edu.brown.cs.student.apiserver;

import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GitPullHandlerTest extends BaseEndpointTest {

  private final String sessionId = "pull-test";
  private final String userId = "test-user";
  private final String branchId = "main";

  private String serializeFile(String val) {
    return serializeFileMap(Map.of("file1", List.of(new MockFileObject(val, val))));
  }

  @Test
  public void testMissingParameter() {
    try {
      HttpURLConnection connection = tryRequest("gitpull?user_id=" + userId + "&branch_id=" + branchId + "&file_map_json={}");
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
      HttpURLConnection connection = tryRequest("createsession?session_id=" + sessionId + "&user_id=" + userId + "&file_map_json=" + fileMapJson);
      assertEquals("success", deserializeResponse(connection).get("response"));

      connection = tryRequest("gitpull?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId + "&file_map_json=" + fileMapJson);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));
      assertEquals("Already up to date.", response.get("message"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testPullWithRemoteAhead() {
    try {
      String baseJson = serializeFileMap(Map.of(
          "file1", List.of(new MockFileObject("base", "base"))
      ));
      String userA = "user-a";
      String userB = "user-b";

      // Both users join session at the same base state
      HttpURLConnection conn = tryRequest("createsession?session_id=" + sessionId + "&user_id=" + userA + "&file_map_json=" + baseJson);
      assertEquals("success", deserializeResponse(conn).get("response"));

      conn = tryRequest("createsession?session_id=" + sessionId + "&user_id=" + userB + "&file_map_json=" + baseJson);
      assertEquals("success", deserializeResponse(conn).get("response"));

      // User A pushes a new commit
      conn = tryRequest("createsession?session_id=" + sessionId + "&user_id=" + userA + "&file_map_json=" + baseJson);
      assertEquals("success", deserializeResponse(conn).get("response"));

      String updatedJson = serializeFileMap(Map.of(
          "file1", List.of(new MockFileObject("base", "base"), new MockFileObject("new", "new"))
      ));
      conn = tryRequest("gitadd?session_id=" + sessionId + "&user_id=" + userA + "&branch_id=" + branchId + "&file_map_json=" + updatedJson);
      assertEquals("success", deserializeResponse(conn).get("response"));
      conn = tryRequest("gitcommit?session_id=" + sessionId + "&user_id=" + userA + "&branch_id=" + branchId + "&commit_message=remoteUpdate");
      assertEquals("success", deserializeResponse(conn).get("response"));
      conn = tryRequest("gitpush?session_id=" + sessionId + "&user_id=" + userA + "&branch_id=" + branchId);
      assertEquals("success", deserializeResponse(conn).get("response"));

      // User B pulls
      conn = tryRequest("gitpull?session_id=" + sessionId + "&user_id=" + userB + "&branch_id=" + branchId + "&file_map_json=" + baseJson);
      Map<String, Object> response = deserializeResponse(conn);
      assertEquals("success", response.get("response"));
      assertTrue(response.get("message").toString().contains("merged"));
      assertNotNull(response.get("merge_commit_id"));
    } catch (Exception e) {
      fail(e);
    }
  }
}
