package edu.brown.cs.student.main.server.storage;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class MockStorage implements StorageInterface {

  private final Map<String, Set<String>> sessions = new HashMap<>();
  private final Map<String, Map<String, Map<String, List<Map<String, Object>>>>> localCommits = new HashMap<>();
  private final Map<String, Map<String, List<Map<String, Object>>>> remoteCommits = new HashMap<>();
  private final Map<String, Map<String, Map<String, Object>>> localHeads = new HashMap<>();
  private final Map<String, Map<String, Object>> remoteHeads = new HashMap<>();
  private final Map<String, Map<String, Map<String, List<Map<String, Object>>>>> stagedCommits = new HashMap<>();
  private final Map<String, Map<String, List<Map<String, Object>>>> stashes = new HashMap<>();

  @Override
  public void addSession(String sessionId, String userId, String fileMapJson) {
    sessions.computeIfAbsent(sessionId, k -> new HashSet<>()).add(userId);

    Map<String, Object> initialCommit = Map.of(
        "commit_id", "initial",
        "file_map_json", fileMapJson
    );

    // Remote
    remoteCommits.computeIfAbsent(sessionId, k -> new HashMap<>())
        .computeIfAbsent("main", k -> new ArrayList<>())
        .add(initialCommit);
    remoteHeads.computeIfAbsent(sessionId, k -> new HashMap<>()).put("main", initialCommit);

    // Local
    localCommits.computeIfAbsent(sessionId, k -> new HashMap<>())
        .computeIfAbsent(userId, k -> new HashMap<>())
        .computeIfAbsent("main", k -> new ArrayList<>())
        .add(initialCommit);
    localHeads.computeIfAbsent(sessionId, k -> new HashMap<>())
        .computeIfAbsent(userId, k -> new HashMap<>())
        .put("main", initialCommit);
  }

  @Override
  public Map<String, Object> getLatestRemoteCommit(String sessionId, String branchId) {
    return (Map<String, Object>) remoteHeads.getOrDefault(sessionId, Map.of())
        .getOrDefault(branchId, Map.of());
  }

  @Override
  public Map<String, Object> getLatestLocalCommit(String sessionId, String userId,
      String branchId) {
    return (Map<String, Object>) localHeads.getOrDefault(sessionId, Map.of())
        .getOrDefault(userId, Map.of())
        .getOrDefault(branchId, Map.of());
  }

  @Override
  public List<Map<String, Object>> getStagedCommits(String sessionId, String userId,
      String branchId) {
    return stagedCommits.getOrDefault(sessionId, Map.of())
        .getOrDefault(userId, Map.of())
        .getOrDefault(branchId, List.of());
  }

  @Override
  public void addChange(String sessionId, String userId, String branchId, String fileMapJson) {
    Map<String, Object> staged = Map.of(
        "commit_id", UUID.randomUUID().toString().substring(0, 6),
        "file_map_json", fileMapJson
    );
    stagedCommits.computeIfAbsent(sessionId, k -> new HashMap<>())
        .computeIfAbsent(userId, k -> new HashMap<>())
        .computeIfAbsent(branchId, k -> new ArrayList<>())
        .add(staged);
  }

  @Override
  public String commitChange(String sessionId, String userId, String branchId, String message,
      List<String> parents) {
    // just pass staged to staging area for simplicity
    Map<String, Object> staged = new HashMap<String, Object>();
    String commitId = String.valueOf(stagedCommits.get(sessionId).get(userId).get(branchId).size());

    staged.put("commit_id", commitId);
    staged.put("message", message);
    staged.put("file_map_json", "{}");
    stagedCommits.get(sessionId).get(userId).get(branchId).add(staged);
    return commitId;
  }

  @Override
  public void pushCommit(String sessionId, String userId, String branchId) {
    List<Map<String, Object>> staged = getStagedCommits(sessionId, userId, branchId);
    if (staged.isEmpty())
      return;

    localCommits.get(sessionId).get(userId).get(branchId).addAll(staged);
    remoteCommits.get(sessionId).get(branchId).addAll(staged);

    Map<String, Object> latest = staged.get(staged.size() - 1);
    localHeads.get(sessionId).get(userId).put(branchId, latest);
    remoteHeads.get(sessionId).put(branchId, latest);

    stagedCommits.get(sessionId).get(userId).put(branchId, new ArrayList<>());
  }

  @Override
  public String addStash(String sessionId, String userId, String branchId, String fileMapJson) {
    Map<String, Object> stash = new HashMap<>();
    stash.put("file_map_json", fileMapJson);
    stash.put("stash_message", "Auto stash for branch: " + branchId);
    stashes.computeIfAbsent(sessionId, k -> new HashMap<>())
        .computeIfAbsent(userId, k -> new ArrayList<>())
        .add(stash);
    return "Stash added";
  }

  @Override
  public List<Map<String, Object>> getStashes(String sessionId, String userId) {
    return stashes.getOrDefault(sessionId, Collections.emptyMap())
        .getOrDefault(userId, Collections.emptyList());
  }

  @Override
  public Map<String, Object> popStash(String sessionId, String userId, int index) {
    List<Map<String, Object>> stashList = getStashes(sessionId, userId);
    if (index < 0 || index >= stashList.size())
      return null;
    return stashList.remove(index);
  }

  @Override
  public void addBranch(String sessionId, String userId, String currentBranchId, String newBranchId,
      String fileMapJson) {
    List<Map<String, Object>> currentCommits = localCommits.get(sessionId).get(userId)
        .get(currentBranchId);
    localCommits.get(sessionId).get(userId).put(newBranchId, new ArrayList<>(currentCommits));
    localHeads.get(sessionId).get(userId)
        .put(newBranchId, localHeads.get(sessionId).get(userId).get(currentBranchId));
    remoteCommits.get(sessionId)
        .put(newBranchId, new ArrayList<>(remoteCommits.get(sessionId).get(currentBranchId)));
    remoteHeads.get(sessionId).put(newBranchId, remoteHeads.get(sessionId).get(currentBranchId));
  }

  @Override
  public void deleteBranch(String sessionId, String userId, String branchId) {
    localCommits.getOrDefault(sessionId, Collections.emptyMap())
        .getOrDefault(userId, Collections.emptyMap()).remove(branchId);
    localHeads.getOrDefault(sessionId, Collections.emptyMap())
        .getOrDefault(userId, Collections.emptyMap()).remove(branchId);
  }

  @Override
  public List<String> getAllRemoteBranches(String sessionId) {
    return new ArrayList<>(remoteCommits.getOrDefault(sessionId, Collections.emptyMap()).keySet());
  }

  @Override
  public List<String> getAllLocalBranches(String sessionId, String userId) {
    return new ArrayList<>(localCommits.getOrDefault(sessionId, Collections.emptyMap())
        .getOrDefault(userId, Collections.emptyMap())
        .keySet());
  }

  @Override
  public List<Map<String, Object>> getLocalPushedCommits(String sessionId, String userId,
      String branchId) {
    return localCommits.getOrDefault(sessionId, Collections.emptyMap())
        .getOrDefault(userId, Collections.emptyMap())
        .getOrDefault(branchId, Collections.emptyList());
  }

  @Override
  public List<Map<String, Object>> getRemotePushedCommits(String sessionId, String branchId) {
    return remoteCommits.getOrDefault(sessionId, Collections.emptyMap())
        .getOrDefault(branchId, Collections.emptyList());
  }

  @Override
  public String getLatestLocalChanges(String sessionId, String userId, String branchId) {
    List<Map<String, Object>> staged = stagedCommits.getOrDefault(sessionId, Collections.emptyMap())
        .getOrDefault(userId, Collections.emptyMap())
        .getOrDefault(branchId, Collections.emptyList());
    if (staged.isEmpty())
      return null;
    return (String) staged.get(staged.size() - 1).get("file_map_json");
  }

  @Override
  public Map<String, List<Map<String, Object>>> getAllLocalCommits(String sessionId, String userId, String branchId) {
    Map<String, List<Map<String, Object>>> result = new HashMap<>();
    result.put("pushed_commits", new ArrayList<>(getLocalPushedCommits(sessionId, userId, branchId)));
    result.put("staged_commits", new ArrayList<>(getStagedCommits(sessionId, userId, branchId)));
    return result;
  }
  @Override
  public List<Map<String, Object>> getAllRemoteCommits(String sessionId, String branchId) {
    return getRemotePushedCommits(sessionId, branchId);
  }

  @Override
  public List<Map<String, Object>> getAllCommits(String sessionId, String userId, String branchId) {
    List<Map<String, Object>> all = new ArrayList<>();
    all.addAll(getStagedCommits(sessionId, userId, branchId));
    all.addAll(getRemotePushedCommits(sessionId, branchId));
    return all;
  }

  @Override
  public void pullRemoteCommits(String sessionId, String userId, String branchId) {
    List<Map<String, Object>> remote = getRemotePushedCommits(sessionId, branchId);
    localCommits.get(sessionId).get(userId).put(branchId, new ArrayList<>(remote));
    localHeads.get(sessionId).get(userId).put(branchId, remote.get(remote.size() - 1));
  }

  @Override
  public void resetLocalCommits(String sessionId, String userId, String branchId,
      Map<String, List<Map<String, Object>>> commits) {
    localCommits.get(sessionId).get(userId)
        .put(branchId, commits.getOrDefault("pushed_commits", new ArrayList<>()));
    stagedCommits.get(sessionId).get(userId).put(branchId, new ArrayList<>());
  }

  @Override
  public Map<String, Object> getCommit(String sessionId, String userId, String branchId,
      String commitId) {
    for (Map<String, Object> commit : localCommits.getOrDefault(sessionId, Collections.emptyMap())
        .getOrDefault(userId, Collections.emptyMap())
        .getOrDefault(branchId, Collections.emptyList())) {
      if (commitId.equals(commit.get("commit_id")))
        return commit;
    }
    return null;
  }

  @Override
  public Map<String, Object> fetch(String sessionId, String userId, String branchId) {
    Map<String, Object> result = new HashMap<>();
    result.put("remote_head", getLatestRemoteCommit(sessionId, branchId));
    result.put("new_branches", getAllRemoteBranches(sessionId));
    return result;
  }

  @Override
  public List<String> getAllSessions() throws ExecutionException, InterruptedException {
    return new ArrayList<>(sessions.keySet());
  }

  @Override
  public void deleteSession(String session_id) throws IllegalArgumentException {
    sessions.remove(session_id);
  }
}

