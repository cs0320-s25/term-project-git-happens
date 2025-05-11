package edu.brown.cs.student.main.server.storage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface StorageInterface {

  /**
   * Method that creates origin/main branch on the remote repository if it has not already been
   * created and adds the first commit to setup the original state of files for every user. Then, a
   * local repository is created for the user, which reflects the initial state of main.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id ("user1" or "user2")
   * @param file_map_json - json of filemap representing initial state of game
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  void addSession(String session_id, String user_id, String file_map_json)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Method that adds set of changed files to stashes list in user's local store
   *
   * @param session_id - unique session_id for current game
   * @param user_id - unique user id
   * @param file_map_json - json string of map of filenames to file contents
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  void addStash(String session_id, String user_id, String file_map_json)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Method for adding a branch to user's local and remote store, which uses the file state of
   * current branch for setting up the new branch. Both the current and new branch have the same
   * commit history and head.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param current_branch_id - branch the user currently has checked out
   * @param new_branch_id - name for the new branch
   * @param file_map_json - json string of local state of files in currently checked out branch
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  void addBranch(
      String session_id,
      String user_id,
      String current_branch_id,
      String new_branch_id,
      String file_map_json)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Method for deleting a branch locally for the specified user.
   *
   * @param session_id - unique session id of current game
   * @param user_id - unique user id
   * @param branch_id - name of branch to be deleted
   * @throws IllegalArgumentException - if any parameters are null
   */
  void deleteBranch(String session_id, String user_id, String branch_id)
      throws IllegalArgumentException;

  /**
   * Method for returning a list of all branch IDs on the remote repository.
   *
   * @param session_id - unique session id for current game
   * @return - a list of remote branch names
   * @throws IllegalArgumentException - if any parameters are null
   */
  List<String> getAllRemoteBranches(String session_id) throws IllegalArgumentException;

  /**
   * Method that returns all local branches on a user's local repository
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @return - a list of local branch names
   * @throws IllegalArgumentException - if any parameters are null
   */
  List<String> getAllLocalBranches(String session_id, String user_id)
      throws IllegalArgumentException;

  /**
   * Method for adding a changed filemap to the local working directory so changes can be committed.
   * This is used for git add, git rm, and staging changes that result from merging.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - branch id for currently checked out branch
   * @param file_map_json - json string of all files the user would like to track changes for on Git
   * @throws IllegalArgumentException - if any parameters are null
   */
  void addChange(String session_id, String user_id, String branch_id, String file_map_json)
      throws IllegalArgumentException;

  /**
   * Method for getting the last staged or pushed commit for a specific user on a specific local
   * branch.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - branch id for currently checked out branch
   * @return - map of commit data representing user's most recently committed changes to files
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  Map<String, Object> getLatestLocalCommit(String session_id, String user_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Method that returns all local staged commits.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - currently checked out branch
   * @return - list of staged commit data maps
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
 List<Map<String, Object>> getStagedCommits(
      String session_id, String user_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Method that returns all local pushed commits.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - currently checked out branch
   * @return - list of pushed commit data maps
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  List<Map<String, Object>> getLocalPushedCommits(
      String session_id, String user_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Method that returns the head commit for a specific branch stored in the remote repository.
   *
   * @param session_id - unique session id
   * @param branch_id - name of local branch that pushes to the remote repository
   * @return - map of commit data representing the remote branch's head
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  Map<String, Object> getLatestRemoteCommit(String session_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Method that returns all remote pushed commits.
   *
   * @param session_id - unique session id
   * @param branch_id - currently checked out branch
   * @return - list of staged commit data maps
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  List<Map<String, Object>> getRemotePushedCommits(
      String session_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Method that returns the file map json of the last staged changes that remain uncommitted; null
   * if there are no staged changes.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - currently checked out branch
   * @return - json string of file map containing changed files
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  String getLatestLocalChanges(String session_id, String user_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Method for commiting most recent changes. Moves changed filemap to the local staged-commits
   * list and clears the changes document.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - branch id for currently checked out branch
   * @param commit_message - corresponding message for commit
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  String commitChange(String session_id, String user_id, String branch_id, String commit_message)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Method for push command, which moves all local staged commits to local pushed commits and
   * remote pushed commits, then clears the staged commits list. The most recently staged commit
   * will now be the last commit in the pushed-commits list.
   *
   * @param session_id - unique session id
   * @param branch_id - branch id for currently checked out branch
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  void pushCommit(String session_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Method for returning a map of all unstaged and pushed commits in a user's local repository.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - id of currently checked out branch
   * @return a map that contains a list of unstaged commits and a list of pushed commits
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  Map<String, List<Map<String, Object>>> getAllLocalCommits(
      String session_id, String user_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Method that returns all pushed commits for a branch on the remote repository.
   *
   * @param session_id - unique session id
   * @param branch_id - id of currently checked out branch
   * @return - a list of pushed commits
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  List<Map<String, Object>> getAllRemoteCommits(String session_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Method that pulls full list of pushed commits from the remote branch and adds them to the local
   * commit history. Now, the local pushed commits reflect the remote pushed commits
   * @param session_id
   * @param user_id
   * @param branch_id
   * @throws IllegalArgumentException
   * @throws ExecutionException
   * @throws InterruptedException
   */
  void pullRemoteCommits(String session_id, String user_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Method that resets local commit history to inputted commits list for the given local branch.
   * Staged changes and staged commits are deleted, used for git reset.
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - id of currently checked out branch
   * @param commits - list of commit map data, representing new local commit history
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  void resetLocalCommits(
      String session_id, String user_id, String branch_id, List<Map<String, Object>> commits)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Method for returning the data for a specified commit on a user's local repository. Used for git
   * reset.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - branch id for branch currently checked out
   * @param commit_id - commit id to search for
   * @return null if commit_id does not exist, otherwise a map of stored commit data for specified
   *     commit
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  Map<String, Object> getCommit(
      String session_id, String user_id, String branch_id, String commit_id)
      throws ExecutionException, InterruptedException;

  /**
   * Method that returns any updates to the branch that are stored remotely but not on the user's
   * local repository (new branches and latest commits).
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - branch id for currently checked out branch
   * @return - a map of newly added branches info and head commit updates
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  Map<String, Object> fetch(String session_id, String user_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException;

  /**
   * Returns a list of all stored session IDs
   *
   * @return list of session ID strings
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  List<String> getAllSessions() throws ExecutionException, InterruptedException;

  /**
   * Deletes all stored information for a session, which can be used when users finish the game so
   * session IDs can be reused.
   *
   * @param session_id - unique session id
   * @throws IllegalArgumentException - if session_id is null
   */
  void deleteSession(String session_id) throws IllegalArgumentException;
}
