package edu.brown.cs.student.apiserver;

import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GitStashHandlerTest extends BaseEndpointTest {
  private final String sessionId = "stash-session";
  private final String userId = "stash-user";
  private final String branchId = "main";

  private Map<String, List<MockFileObject>> buildFileMap(String suffix) {
    return Map.of(
        "file1", List.of(new MockFileObject("content1-" + suffix, "name1-" + suffix)),
        "file2", List.of(new MockFileObject("content2-" + suffix, "name2-" + suffix))
    );
  }

  @Test
  public void testMissingParams() throws Exception {
    HttpURLConnection conn = tryRequest("gitstash?user_id=" + userId + "&branch_id=" + branchId);
    Map<String, Object> response = deserializeResponse(conn);
    assertEquals("error_bad_request", response.get("response"));
    assertEquals("null parameter", response.get("error_cause"));
  }

  @Test
  public void testAddAndListStash() throws Exception {
    String baseJson = serializeFileMap(buildFileMap("v0"));
    HttpURLConnection conn = tryRequest("createsession?session_id=" + sessionId + "&user_id=" + userId + "&file_map_json=" + baseJson);
    assertEquals("success", deserializeResponse(conn).get("response"));

    String stashJson = serializeFileMap(buildFileMap("stash1"));
    conn = tryRequest("gitstash?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId + "&stash_request=&file_map_json=" + stashJson);
    Map<String, Object> stashResponse = deserializeResponse(conn);
    assertEquals("success", stashResponse.get("response"));
    assertEquals("add stash", stashResponse.get("action"));

    conn = tryRequest("gitstash?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId + "&stash_request=list&file_map_json=" + stashJson);
    Map<String, Object> listResponse = deserializeResponse(conn);
    assertEquals("success", listResponse.get("response"));
    assertEquals("list stashes", listResponse.get("action"));
    List<?> stashes = (List<?>) listResponse.get("stashes");
    assertEquals(1, stashes.size());
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

      String v2 = serializeFileMap(buildFileMap("v1")); // Keep the same version as commit1 to avoid conflict
      conn = tryRequest("gitadd?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId + "&file_map_json=" + v2);
      assertEquals("success", deserializeResponse(conn).get("response"));
      Map<String, Object> commit2 = deserializeResponse(tryRequest("gitcommit?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId + "&commit_message=two"));
      String commitId2 = (String) commit2.get("commit_id");

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



  @Test
  public void testPopInvalidIndex() throws Exception {
    String stashJson = serializeFileMap(buildFileMap("any"));
    HttpURLConnection conn = tryRequest("gitstash?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId + "&stash_request=pop&stash_index=10&file_map_json=" + stashJson);
    Map<String, Object> response = deserializeResponse(conn);
    assertEquals("error_database", response.get("response"));
    assertTrue(response.get("error_cause").toString().contains("stash@{10} not found"));
  }

  @Test
  public void testUnsupportedCommand() throws Exception {
    String stashJson = serializeFileMap(buildFileMap("bad"));
    HttpURLConnection conn = tryRequest("gitstash?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId + "&stash_request=unsupported&file_map_json=" + stashJson);
    Map<String, Object> response = deserializeResponse(conn);
    assertEquals("error_database", response.get("response"));
    assertEquals("Command not supported.", response.get("error_cause"));
  }
}

