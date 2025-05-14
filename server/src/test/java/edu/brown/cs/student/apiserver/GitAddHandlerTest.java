package edu.brown.cs.student.apiserver;

import static org.junit.jupiter.api.Assertions.*;

import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import java.net.HttpURLConnection;
import java.util.*;
import org.junit.jupiter.api.Test;

public class GitAddHandlerTest extends BaseEndpointTest {

  private final String sessionId = "add-test";
  private final String userId = "test-user";
  private final String branchId = "main";

  @Test
  public void testMissingSessionId() {
    try {
      HttpURLConnection connection =
          tryRequest("gitadd?user_id=" + userId + "&branch_id=" + branchId + "&file_map_json={}");
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
              "gitadd?session_id=" + sessionId + "&branch_id=" + branchId + "&file_map_json={}");
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
          tryRequest("gitadd?session_id=" + sessionId + "&user_id=" + userId + "&file_map_json={}");
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_bad_request", response.get("response"));
      assertEquals("null parameter", response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testMissingFileMapJson() {
    try {
      HttpURLConnection connection =
          tryRequest(
              "gitadd?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_bad_request", response.get("response"));
      assertEquals("null parameter", response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testSuccessfulAdd() {
    try {
      List<MockFileObject> fileList = List.of(new MockFileObject("ingredient1", "ingredient1"));
      Map<String, List<MockFileObject>> fileMap = new HashMap<>();
      fileMap.put("file1", fileList);
      String fileMapJson = serializeFileMap(fileMap);

      // create session first
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
              "gitadd?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&branch_id="
                  + branchId
                  + "&file_map_json="
                  + fileMapJson);
      Map<String, Object> response = deserializeResponse(connection);

      assertEquals("success", response.get("response"));
      assertEquals("add -A", response.get("action"));
    } catch (Exception e) {
      fail(e);
    }
  }
}
