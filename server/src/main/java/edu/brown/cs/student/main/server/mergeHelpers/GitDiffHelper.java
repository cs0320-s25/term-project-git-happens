package edu.brown.cs.student.main.server.mergeHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GitDiffHelper {

  // list of filenames which are on the local repository but not on the remote repository
  private List<String> newLocalFiles = new ArrayList<>();
  // list of filenames which are on the remote repository but not on the local repository
  private List<String> newIncomingFiles = new ArrayList<>();
  // map of filenames to map containing the local and incoming version of the conflicting file
  private Map<String, Map<String, List<MockFileObject>>> fileConflicts = new HashMap<>();

  public GitDiffHelper() {}

  /**
   * Return any local files that aren't stored in the incoming filemap
   *
   * @return - list of new local file names
   */
  public List<String> getNewLocalFiles() {
    List<String> newLocalFilesCopy = new ArrayList<>(newLocalFiles);
    return newLocalFilesCopy;
  }

  /**
   * Return any incoming files that aren't stored in the local filemap
   *
   * @return - list of new incoming file names
   */
  public List<String> getNewIncomingFiles() {
    List<String> newIncomingFilesCopy = new ArrayList<>(newIncomingFiles);
    return newIncomingFilesCopy;
  }

  /**
   * Return map of files that have conflicts between local and incoming information, mapped to
   * indexes of conflicting objects
   *
   * @return - map of filenames to indexes of conflicts
   */
  public Map<String, Map<String, List<MockFileObject>>> getFileConflicts() {
    Map<String, Map<String, List<MockFileObject>>> fileConflictsCopy = new HashMap<>(fileConflicts);
    return fileConflictsCopy;
  }

  /**
   * Method that determines if there are local files that are not stored remotely or remote files
   * that the user does not have locally, and adds these file names to respective lists.
   *
   * @param localState - filemap representing the user's locally stored files
   * @param incomingState - filemap representing the incoming filemap
   */
  public void detectNewFiles(
      Map<String, List<MockFileObject>> localState,
      Map<String, List<MockFileObject>> incomingState) {

    newIncomingFiles = new ArrayList<>();
    newLocalFiles = new ArrayList<>();

    List<String> localFileNames = new ArrayList<>(localState.keySet());
    List<String> incomingFileNames = new ArrayList<>(incomingState.keySet());

    // find local filenames not present in the incoming filemap
    for (int i = 0; i < localFileNames.size(); i++) {
      if (!incomingFileNames.contains(localFileNames.get(i))) {
        newLocalFiles.add(localFileNames.get(i));
      }
    }
    // find incoming filenames not present in the local filemap
    for (int i = 0; i < incomingFileNames.size(); i++) {
      if (!localFileNames.contains(incomingFileNames.get(i))) {
        newIncomingFiles.add(incomingFileNames.get(i));
      }
    }
  }

  public Set<String> differenceDetected(
      Map<String, List<MockFileObject>> storedState,
      Map<String, List<MockFileObject>> currentState) {
    // files are different if stored or current state have different files
    System.out.println("stored keys: " + storedState.keySet());
    System.out.println("current keys: " + currentState.keySet());

    detectNewFiles(storedState, currentState);
    Set<String> filesWithDifferences = new HashSet<>();
    filesWithDifferences.addAll(newIncomingFiles);
    filesWithDifferences.addAll(newLocalFiles);
    // check for any difference in file objects list in corresponding files between stored and
    // current state
    for (Map.Entry<String, List<MockFileObject>> storedFile : storedState.entrySet()) {
      String fileName = storedFile.getKey();
      // if the file is not present in both the current and incoming states, no need to compare
      if (filesWithDifferences.contains(fileName)) {
        continue;
      }
      List<MockFileObject> currentFileContents = currentState.get(fileName);
      List<MockFileObject> storedFileContents = storedFile.getValue();
      if (currentFileContents.size() != storedFileContents.size()) {
        filesWithDifferences.add(fileName);
      } else {
        for (int i = 0; i < currentFileContents.size(); i++) {
          if (!currentFileContents.get(i).equals(storedFileContents.get(i))) {
            filesWithDifferences.add(fileName);
            break;
          }
        }
      }
    }
    // return set of filenames
    return filesWithDifferences;
  }

  private boolean isSubsequence(
      String fileName, List<MockFileObject> smallerFile, List<MockFileObject> largerFile) {
    // track length of subsequence
    int i = 0;
    for (int j = 0; j < largerFile.size(); j++) {
      if (i < smallerFile.size() && largerFile.get(i).equals(smallerFile.get(i))) {
        i++;
      }
    }
    // if all of smaller file's objects were a subsequence in larger file's objects
    return i == smallerFile.size();
  }

  public List<MockFileObject> autoMergeIfPossible(
      String fileName, List<MockFileObject> localFile, List<MockFileObject> incomingFile) {

    // if files are equal, return first file by default
    if (localFile.equals(incomingFile)) {
      return localFile;
    }

    // if one file is a subsequence of another, return the larger file
    if (isSubsequence(fileName, localFile, incomingFile)) {
      return incomingFile;
    }
    if (isSubsequence(fileName, incomingFile, localFile)) {
      return localFile;
    }
    // otherwise, return null to indicate a merge conflict
    Map<String, List<MockFileObject>> files = new HashMap<>();
    files.put("local", localFile);
    files.put("incoming", incomingFile);
    fileConflicts.put(fileName, files);
    return null;
  }
}
