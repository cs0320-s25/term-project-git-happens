package edu.brown.cs.student.apiserver;

import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GitStatusHandlerTest extends BaseEndpointTest {

  private final String sessionId = "status-test";
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
      HttpURLConnection conn = tryRequest("gitstatus?user_id=" + userId + "&branch_id=" + branchId);
      Map<String, Object> response = deserializeResponse(conn);
      assertEquals("error_bad_request", response.get("response"));
      assertEquals("null parameter", response.get("error_cause"));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testStatusWithStagedAndUnstagedChanges() {
    try {
      String base = serializeFileMap(buildFileMap("v0"));
      HttpURLConnection conn = tryRequest("createsession?session_id=" + sessionId + "&user_id=" + userId + "&file_map_json=" + base);
      assertEquals("success", deserializeResponse(conn).get("response"));

      String v1 = serializeFileMap(buildFileMap("v1"));
      conn = tryRequest("gitadd?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId + "&file_map_json=" + v1);
      assertEquals("success", deserializeResponse(conn).get("response"));

      String v2 = serializeFileMap(buildFileMap("v2"));
      conn = tryRequest("gitstatus?session_id=" + sessionId + "&user_id=" + userId + "&branch_id=" + branchId + "&file_map_json=" + v2);
      Map<String, Object> response = deserializeResponse(conn);

      assertEquals("success", response.get("response"));
      assertEquals("Changes to be committed: ", response.get("staged_changes_message"));
      assertEquals("Changes not staged for commit: ", response.get("unstaged_changes_message"));
      assertTrue(((List<?>) response.get("staged_changes")).contains("file1"));
      assertTrue(((List<?>) response.get("unstaged_changes")).contains("file1"));
    } catch (Exception e) {
      fail(e);
    }
  }
}

