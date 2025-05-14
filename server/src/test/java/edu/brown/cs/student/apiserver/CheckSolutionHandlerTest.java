package edu.brown.cs.student.apiserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CheckSolutionHandlerTest extends BaseEndpointTest {

  static final Map<String, List<MockFileObject>> solutionMap = new HashMap<>();
  static final String testSessionId = "check-test";
  static final String testUserId = "test-user1";
  static final String testBranchId = "main";

  public void setSolution() {

    try {
      Map<String, List<MockFileObject>> originalFileMap = new HashMap<>();
      String originalFileMapJson = serializeFileMap(originalFileMap);
      HttpURLConnection connection = tryRequest("createsession?session_id=" + testSessionId
          + "&user_id=" + testUserId + "&file_map_json=" + originalFileMapJson);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));
      List<MockFileObject> solFile1 = new ArrayList<>();
      List<MockFileObject> solFile2 = new ArrayList<>();

      solFile1.add(new MockFileObject("ingredient1", "ingredient1"));
      solFile1.add(new MockFileObject("ingredient2", "ingredient2"));
      solFile2.add(new MockFileObject("ingredient3", "ingredient3"));
      solFile2.add(new MockFileObject("ingredient4", "ingredient4"));

      solutionMap.put("file1", solFile1);
      solutionMap.put("file2", solFile2);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
//TODO: everywhere getLatestLocalCommit is called, get map by adding .get("head")
  @Test
  public void testMissingParameter() {
    try {
      setSolution();
      HttpURLConnection connection = tryRequest("checksolution?");
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_bad_request", response.get("response"));

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  @Test
  public void testCorrectSolution() {
    try {
      Map<String, List<MockFileObject>> userMap = new HashMap<>(solutionMap);
      String userFileMapJson = serializeFileMap(userMap);

      HttpURLConnection connection = tryRequest("gitadd?session_id=" + testSessionId
          + "&user_id=" + testUserId + "&branch_id=" + testBranchId + "&file_map_json="
          + userFileMapJson);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));

      connection = tryRequest("gitcommit?session_id=" + testSessionId
          + "&user_id=" + testUserId + "&branch_id=" + testBranchId + "&commit_message=test");
      response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));
//      System.out.flush();
//      System.out.println(response.get("error_cause"));

      connection = tryRequest("gitpush?session_id=" + testSessionId
          + "&user_id=" + testUserId + "&branch_id=" + testBranchId);
      response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));
      System.out.flush();
      System.out.println(response.get("error_cause"));
      System.out.print(response);

      connection = tryRequest("checksolution?session_id=" + testSessionId
          + "&user_id=" + testUserId + "&solution_branch_id=" + testBranchId
          + "&solution_file_map_json=" + serializeFileMap(solutionMap));
      response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));
      assertEquals(true, response.get("solution_correct"));

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testIncorrectSolution() {}
}
