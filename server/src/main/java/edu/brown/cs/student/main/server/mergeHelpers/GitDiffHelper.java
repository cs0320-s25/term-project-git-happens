package edu.brown.cs.student.main.server.mergeHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GitDiffHelper {

  //list of filenames which are on the local repository but not on the remote repository
  private List<String> newLocalFiles = new ArrayList<>();
  //list of filenames which are on the remote repository but not on the local repository
  private List<String> newIncomingFiles = new ArrayList<>();
  //map of filenames to indexes where conflicts were detected between the local and remote file list
  private Map<String, List<Integer>> fileConflictIndexes = new HashMap<>();

  public GitDiffHelper() {}

  /**
   * Return any local files that aren't stored in the incoming filemap
   * @return - list of new local file names
   */
  public List<String> getNewLocalFiles() {
    List<String> newLocalFilesCopy = new ArrayList<>(newLocalFiles);
    return newLocalFilesCopy;
  }

  /**
   * Return any incoming files that aren't stored in the local filemap
   * @return - list of new incoming file names
   */
  public List<String> getNewIncomingFiles() {
    List<String> newIncomingFilesCopy = new ArrayList<>(newIncomingFiles);
    return newIncomingFilesCopy;
  }

  /**
   * Return map of files that have conflicts between local and incoming information, mapped to indexes
   * of conflicting objects
   * @return - map of filenames to indexes of conflicts
   */
  public Map<String, List<Integer>> getFileConflictIndexes() {
    Map<String, List<Integer>> fileConflictIndexesCopy = new HashMap<>(fileConflictIndexes);
    return fileConflictIndexesCopy;
  }

  /**
   * Method that determines if there are local files that are not stored remotely or remote files that
   * the user does not have locally, and adds these file names to respective lists.
   * @param localState - filemap representing the user's locally stored files
   * @param incomingState - filemap representing the incoming filemap
   */
  public void detectNewFiles(Map<String, List<MockFileObject>> localState, Map<String, List<MockFileObject>> incomingState) {

    List<String> localFileNames = new ArrayList<>(localState.keySet());
    List<String> incomingFileNames = new ArrayList<>(incomingState.keySet());

    //find local filenames not present in the incoming filemap
    for (int i = 0; i < localFileNames.size(); i++) {
      if (!incomingFileNames.contains(localFileNames.get(i))) {
        newLocalFiles.add(localFileNames.get(i));
      }
    }
    //find incoming filenames not present in the local filemap
    for (int i = 0; i < incomingFileNames.size(); i++) {
      if (!localFileNames.contains(incomingFileNames.get(i))) {
        newIncomingFiles.add(incomingFileNames.get(i));
      }
    }
  }

  public Set<String> differenceDetected(Map<String, List<MockFileObject>> storedState, Map<String, List<MockFileObject>> currentState) {
    //files are different if stored or current state have different files
    detectNewFiles(storedState, currentState);
    Set<String> filesWithDifferences = new HashSet<>();
    filesWithDifferences.addAll(newIncomingFiles);
    //check for any difference in file objects list in corresponding files between stored and current state
    for (Map.Entry<String, List<MockFileObject>> storedFile : storedState.entrySet()) {
      String fileName = storedFile.getKey();
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
    //return set of filenames
    return filesWithDifferences;
  }

  private boolean isSubsequence(String fileName, List<MockFileObject> smallerFile, List<MockFileObject> largerFile) {
    //track length of subsequence
    int i = 0;
    for (int j = 0; j < largerFile.size(); j++) {
      if (i < smallerFile.size()) {
        if (largerFile.get(j).equals(smallerFile.get(i))) {
          i++;
        } else {
          List<Integer> conflictIndexes = fileConflictIndexes.get(fileName);
          conflictIndexes.add(j);
          fileConflictIndexes.put(fileName, conflictIndexes);
        }
      }
      }
    //if all of smaller file's objects were a subsequence in larger file's objects
    return i == smallerFile.size();
  }

  public List<MockFileObject> autoMergeIfPossible(String fileName, List<MockFileObject> file1,
      List<MockFileObject> file2) {

    //create new entry in fileConflictIndexes for filename
    fileConflictIndexes.put("filename", new ArrayList<>());
    //if files are equal, return first file by default
    if (file1.equals(file2)) {
      return file1;
    }

    //if one file is a subsequence of another, return the larger file
    if (isSubsequence(fileName, file1, file2)) {
      return file1;
    }
    if (isSubsequence(fileName, file2, file1)) {
      return file2;
    }
    //otherwise, return null to indicate a merge conflict
    return null;
  }
}
