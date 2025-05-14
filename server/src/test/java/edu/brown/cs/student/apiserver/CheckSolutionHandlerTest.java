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
  static final String testUserId = "test-user";
  static final String testBranchId = "main";


  public void setSolution() {

    try {
      Map<String, List<MockFileObject>> originalFileMap = new HashMap<>();
      String originalFileMapJson = serializeFileMap(originalFileMap);
      HttpURLConnection connection = tryRequest("createsession?session_id=" + testSessionId
          + "&user_id=" + testUserId + "&file_map_json=" + originalFileMapJson);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
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
      Map<String, List<MockFileObject>> userMap = new HashMap<>();
      Map<String, List<MockFileObject>> solutionMap = new HashMap<>();

      List<MockFileObject> solFile1 = List.of(
          new MockFileObject("ingredient1", "ingredient1"),
          new MockFileObject("ingredient2", "ingredient2")
      );

      List<MockFileObject> solFile2 = List.of(
          new MockFileObject("ingredient3", "ingredient3"),
          new MockFileObject("ingredient4", "ingredient4")
      );

      userMap.put("file1", new ArrayList<>(solFile1));
      userMap.put("file2", new ArrayList<>(solFile2));

      solutionMap.put("file1", new ArrayList<>(solFile1));
      solutionMap.put("file2", new ArrayList<>(solFile2));

      String userFileMapJson = serializeFileMap(userMap);

      setSolution();

      HttpURLConnection connection = tryRequest("gitadd?session_id=" + testSessionId
          + "&user_id=" + testUserId + "&branch_id=" + testBranchId + "&file_map_json="
          + userFileMapJson);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));

      connection = tryRequest("gitcommit?session_id=" + testSessionId
          + "&user_id=" + testUserId + "&branch_id=" + testBranchId + "&commit_message=test");
      response = deserializeResponse(connection);
      System.out.println(response.get("error_cause"));
      assertEquals("success", response.get("response"));

      connection = tryRequest("gitpush?session_id=" + testSessionId
          + "&user_id=" + testUserId + "&branch_id=" + testBranchId);
      response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));

      connection = tryRequest("checksolution?session_id=" + testSessionId
          + "&user_id=" + testUserId + "&solution_branch_id=" + testBranchId
          + "&solution_file_map_json=" + serializeFileMap(solutionMap));
      response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));
      assertEquals(true, response.get("solution_correct"));

      connection = tryRequest("deletesession?session_id=" + testSessionId);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testIncorrectSolution() {
    try {
      Map<String, List<MockFileObject>> userMap = new HashMap<>();
      Map<String, List<MockFileObject>> solutionMap = new HashMap<>();

      List<MockFileObject> solFile1 = List.of(
          new MockFileObject("ingredient1", "ingredient1"),
          new MockFileObject("ingredient2", "ingredient2")
      );

      List<MockFileObject> solFile2 = List.of(
          new MockFileObject("ingredient3", "ingredient3"),
          new MockFileObject("ingredient4", "ingredient4")
      );

      userMap.put("file1", new ArrayList<>(solFile1));

      solutionMap.put("file1", new ArrayList<>(solFile1));
      solutionMap.put("file2", new ArrayList<>(solFile2));

      String userFileMapJson = serializeFileMap(userMap);

      setSolution();

      HttpURLConnection connection = tryRequest("gitadd?session_id=" + testSessionId
          + "&user_id=" + testUserId + "&branch_id=" + testBranchId + "&file_map_json="
          + userFileMapJson);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));

      connection = tryRequest("gitcommit?session_id=" + testSessionId
          + "&user_id=" + testUserId + "&branch_id=" + testBranchId + "&commit_message=test");
      response = deserializeResponse(connection);
      System.out.println(response.get("error_cause"));
      assertEquals("success", response.get("response"));

      connection = tryRequest("gitpush?session_id=" + testSessionId
          + "&user_id=" + testUserId + "&branch_id=" + testBranchId);
      response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));

      connection = tryRequest("checksolution?session_id=" + testSessionId
          + "&user_id=" + testUserId + "&solution_branch_id=" + testBranchId
          + "&solution_file_map_json=" + serializeFileMap(solutionMap));
      response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));
      assertEquals(false, response.get("solution_correct"));

      connection = tryRequest("deletesession?session_id=" + testSessionId);

  } catch (Exception e) {
    e.printStackTrace();
  }
  }
}
