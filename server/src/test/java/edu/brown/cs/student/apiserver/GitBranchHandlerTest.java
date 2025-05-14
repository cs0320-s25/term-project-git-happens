package edu.brown.cs.student.apiserver;

import static org.junit.jupiter.api.Assertions.*;

import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import java.net.HttpURLConnection;
import java.util.*;
import org.junit.jupiter.api.Test;

public class GitBranchHandlerTest extends BaseEndpointTest {

  private final String sessionId = "branch-test";
  private final String userId = "test-user";
  private final String branchId = "main";

  @Test
  public void testMissingSessionId() {
    try {
      HttpURLConnection connection =
          tryRequest(
              "gitbranch?user_id=" + userId + "&branch_request=&current_branch_id=" + branchId);
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
              "gitbranch?session_id="
                  + sessionId
                  + "&branch_request=&current_branch_id="
                  + branchId);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_bad_request", response.get("response"));
      assertEquals("null parameter", response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testMissingBranchRequest() {
    try {
      HttpURLConnection connection =
          tryRequest(
              "gitbranch?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&current_branch_id="
                  + branchId);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_bad_request", response.get("response"));
      assertEquals("null parameter", response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testMissingCurrentBranch() {
    try {
      HttpURLConnection connection =
          tryRequest(
              "gitbranch?session_id=" + sessionId + "&user_id=" + userId + "&branch_request=");
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_bad_request", response.get("response"));
      assertEquals("null parameter", response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testSuccessfulBranchList() {
    try {
      List<MockFileObject> fileList = List.of(new MockFileObject("fileA", "fileA"));
      Map<String, List<MockFileObject>> fileMap = Map.of("file1", fileList);
      String fileMapJson = serializeFileMap(fileMap);

      HttpURLConnection connection =
          tryRequest(
              "createsession?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&file_map_json="
                  + fileMapJson);
      Map<String, Object> createResponse = deserializeResponse(connection);
      assertEquals("success", createResponse.get("response"));

      connection =
          tryRequest(
              "gitbranch?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&branch_request=&current_branch_id="
                  + branchId);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));
      assertEquals("list local branches", response.get("action"));
      assertTrue(response.containsKey("local_branch_names"));
    } catch (Exception e) {
      fail(e);
    }
  }
}
