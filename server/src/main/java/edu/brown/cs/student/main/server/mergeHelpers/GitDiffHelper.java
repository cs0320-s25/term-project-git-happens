package edu.brown.cs.student.main.server.mergeHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitDiffHelper {

  //list of filenames which are on the local repository but not on the remote repository
  private List<String> newLocalFiles = new ArrayList<>();
  //list of filenames which are on the remote repository but not on the local repository
  private List<String> newRemoteFiles = new ArrayList<>();
  //map of filenames to indexes where conflicts were detected between the local and remote file list
  private Map<String, List<Integer>> fileConflictIndexes = new HashMap<>();

  public GitDiffHelper() {}

  /**
   * Method that determines if there are local files that are not stored remotely or remote files that
   * the user does not have locally, and adds these file names to respective lists.
   * @param localState - filemap representing the user's locally stored files
   * @param incomingState - filemap representing the incoming filemap
   */
  public void detectNewFiles(Map<String, List<Object>> localState, Map<String, List<Object>> incomingState) {

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
        newRemoteFiles.add(incomingFileNames.get(i));
      }
    }
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

public List<MockFileObject> autoMergeIfPossible(String fileName, List<MockFileObject> file1, List<MockFileObject> file2) {

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
