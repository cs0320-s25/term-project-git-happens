package edu.brown.cs.student.apiserver;

import edu.brown.cs.student.main.server.mergeHelpers.GitDiffHelper;
import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

  public class GitDiffHelperTest {
    private GitDiffHelper diffHelper;

    private MockFileObject fileObj(String name) {
      return new MockFileObject(name, name);
    }

    @BeforeEach
    public void setup() {
      diffHelper = new GitDiffHelper();
    }

    @Test
    public void testDetectNewFiles() {
      Map<String, List<MockFileObject>> local = Map.of("file1", List.of(fileObj("a")));
      Map<String, List<MockFileObject>> incoming = Map.of("file2", List.of(fileObj("a")));

      diffHelper.detectNewFiles(local, incoming);

      assertEquals(List.of("file1"), diffHelper.getNewLocalFiles());
      assertEquals(List.of("file2"), diffHelper.getNewIncomingFiles());
    }

    @Test
    public void testDifferenceDetectedWithSameFiles() {
      Map<String, List<MockFileObject>> local = Map.of("file1", List.of(fileObj("a"), fileObj("b")));
      Map<String, List<MockFileObject>> incoming = Map.of("file1", List.of(fileObj("a"), fileObj("b")));

      Set<String> diff = diffHelper.differenceDetected(local, incoming);
      assertTrue(diff.isEmpty());
    }

    @Test
    public void testDifferenceDetectedWithDifferentContents() {
      Map<String, List<MockFileObject>> local = Map.of("file1", List.of(fileObj("a"), fileObj("x")));
      Map<String, List<MockFileObject>> incoming = Map.of("file1", List.of(fileObj("a"), fileObj("b")));

      Set<String> diff = diffHelper.differenceDetected(local, incoming);
      assertEquals(Set.of("file1"), diff);
    }

    @Test
    public void testAutoMergeIdenticalFiles() {
      List<MockFileObject> list = List.of(fileObj("x"), fileObj("y"));
      List<MockFileObject> result = diffHelper.autoMergeIfPossible("file1", list, new ArrayList<>(list));
      assertEquals(list, result);
    }

    @Test
    public void testAutoMergeSubsequence() {
      List<MockFileObject> local = List.of(fileObj("a"));
      List<MockFileObject> incoming = List.of(fileObj("a"), fileObj("b"));

      List<MockFileObject> result = diffHelper.autoMergeIfPossible("file1", local, incoming);
      assertEquals(incoming, result);
    }

    @Test
    public void testAutoMergeConflict() {
      List<MockFileObject> local = List.of(fileObj("a"), fileObj("b"));
      List<MockFileObject> incoming = List.of(fileObj("b"), fileObj("a"));

      List<MockFileObject> result = diffHelper.autoMergeIfPossible("file1", local, incoming);
      assertNull(result);

      Map<String, Map<String, List<MockFileObject>>> conflicts = diffHelper.getFileConflicts();
      assertTrue(conflicts.containsKey("file1"));
      assertEquals(local, conflicts.get("file1").get("local"));
      assertEquals(incoming, conflicts.get("file1").get("incoming"));
    }

    @Test
    public void testDetectNewFilesNoDifference() {
      Map<String, List<MockFileObject>> local = Map.of("file1", List.of(fileObj("x")));
      Map<String, List<MockFileObject>> incoming = Map.of("file1", List.of(fileObj("x")));

      diffHelper.detectNewFiles(local, incoming);
      assertTrue(diffHelper.getNewLocalFiles().isEmpty());
      assertTrue(diffHelper.getNewIncomingFiles().isEmpty());
    }
  }

