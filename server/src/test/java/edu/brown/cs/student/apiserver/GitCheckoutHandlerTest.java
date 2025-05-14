package edu.brown.cs.student.apiserver;

import static org.junit.jupiter.api.Assertions.*;

import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import java.net.HttpURLConnection;
import java.util.*;
import org.junit.jupiter.api.Test;

public class GitCheckoutHandlerTest extends BaseEndpointTest {

  private final String sessionId = "checkout-test";
  private final String userId = "test-user";
  private final String branchId = "main";
  private final String newBranch = "feature";

  @Test
  public void testMissingParameters() {
    try {
      HttpURLConnection connection =
          tryRequest(
              "gitcheckout?user_id="
                  + userId
                  + "&current_branch_id="
                  + branchId
                  + "&new_branch_id="
                  + newBranch
                  + "&file_map_json={}");
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_bad_request", response.get("response"));
      assertEquals("null parameter", response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testDetectUncommittedChanges() {
    try {
      Map<String, List<MockFileObject>> fileMap =
          Map.of("file1", List.of(new MockFileObject("original", "original")));
      String fileMapJson = serializeFileMap(fileMap);

      // Create session
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

      // Modify file map
      Map<String, List<MockFileObject>> modifiedFileMap =
          Map.of("file1", List.of(new MockFileObject("changed", "changed")));
      String modifiedJson = serializeFileMap(modifiedFileMap);

      connection =
          tryRequest(
              "gitcheckout?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&current_branch_id="
                  + branchId
                  + "&new_branch_id="
                  + branchId
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
  public void testCheckoutCreatesLocalBranch() {
    try {
      Map<String, List<MockFileObject>> fileMap =
          Map.of("file1", List.of(new MockFileObject("content", "content")));
      String fileMapJson = serializeFileMap(fileMap);

      // Create session
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

      // Add a new remote branch manually via addBranch
      connection =
          tryRequest(
              "gitbranch?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&branch_request="
                  + newBranch
                  + "&current_branch_id="
                  + branchId
                  + "&file_map_json="
                  + fileMapJson);
      assertEquals("success", deserializeResponse(connection).get("response"));
      // Now checkout the new branch
      connection =
          tryRequest(
              "gitcheckout?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&current_branch_id="
                  + branchId
                  + "&new_branch_id="
                  + newBranch
                  + "&file_map_json="
                  + fileMapJson);
      Map<String, Object> response = deserializeResponse(connection);

      assertEquals("success", response.get("response"));
      assertEquals("switched to branch '" + newBranch + "'", response.get("action"));
      assertTrue(response.containsKey("file_map_json"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testCheckoutNonexistentBranch() {
    try {
      Map<String, List<MockFileObject>> fileMap =
          Map.of("file1", List.of(new MockFileObject("content", "content")));
      String fileMapJson = serializeFileMap(fileMap);

      // Create session
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

      // Attempt to checkout a branch that doesn't exist remotely
      connection =
          tryRequest(
              "gitcheckout?session_id="
                  + sessionId
                  + "&user_id="
                  + userId
                  + "&current_branch_id="
                  + branchId
                  + "&new_branch_id=doesNotExist&file_map_json="
                  + fileMapJson);
      Map<String, Object> response = deserializeResponse(connection);

      assertEquals("error_database", response.get("response"));
      assertEquals(
          "pathspec 'doesNotExist' did not match any branches known to git",
          response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }
}
