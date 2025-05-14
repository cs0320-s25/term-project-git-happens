package edu.brown.cs.student.apiserver;

import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GitResetHandlerTest extends BaseEndpointTest {

  private final String sessionId = "reset-test";
  private final String userId = "test-user";
  private final String branchId = "main";

  private Map<String, List<MockFileObject>> buildFileMap(String suffix) {
    return Map.of(
        "file1", List.of(new MockFileObject("ingredient1-" + suffix, "ingredient1-" + suffix)),
        "file2", List.of(new MockFileObject("ingredient2-" + suffix, "ingredient2-" + suffix))
    );
  }

  @Test
  public void testMissingParams() {
    try {
      HttpURLConnection conn = tryRequest("gitreset?user_id=" + userId + "&branch_id=" + branchId);
      Map<String, Object> response = deserializeResponse(conn);
      assertEquals("error_bad_request", response.get("response"));
      assertEquals("null parameter", response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testResetToStagedCommit() {
    try {
      String base = serializeFileMap(buildFileMap("v0"));
      HttpURLConnection conn = tryRequest("createsession?session_id=" + sessionId + "&user_id=" + userId + "&file_map_json=" + base);
      assertEquals("success", deserializeResponse(conn).get("response"));

      String v1 = serializeFileMap(buildFileMap("v1"));
      conn = tryRequest("gitadd?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId + "&file_map_json=" + v1);
      assertEquals("success", deserializeResponse(conn).get("response"));
      Map<String, Object> commit1 = deserializeResponse(tryRequest("gitcommit?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId + "&commit_message=one"));
      String commitId1 = (String) commit1.get("commit_id");
      // Push the first commit to move it to pushed commits list
      conn = tryRequest("gitpush?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId);
      assertEquals("success", deserializeResponse(conn).get("response"));

      String v2 = serializeFileMap(buildFileMap("v2"));
      conn = tryRequest("gitadd?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId + "&file_map_json=" + v2);
      assertEquals("success", deserializeResponse(conn).get("response"));
      Map<String, Object> commit2 = deserializeResponse(tryRequest("gitcommit?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId + "&commit_message=two"));
      String commitId2 = (String) commit2.get("commit_id");
      System.out.println(commitId1);
      conn = tryRequest("gitreset?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId + "&reset_commit_id=" + commitId1);
      Map<String, Object> reset = deserializeResponse(conn);
      assertEquals("success", reset.get("response"));
      assertEquals("reset", reset.get("action"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testResetToInvalidCommit() {
    try {
      String base = serializeFileMap(buildFileMap("base"));
      HttpURLConnection conn = tryRequest("createsession?session_id=" + sessionId + "&user_id=" + userId + "&file_map_json=" + base);
      assertEquals("success", deserializeResponse(conn).get("response"));

      HttpURLConnection reset = tryRequest("gitreset?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId + "&reset_commit_id=BAD123");
      Map<String, Object> response = deserializeResponse(reset);
      assertEquals("error_database", response.get("response"));
      assertTrue(response.get("message").toString().contains("Commit 'BAD123' not found"));
    } catch (Exception e) {
      fail(e);
    }
  }
}
