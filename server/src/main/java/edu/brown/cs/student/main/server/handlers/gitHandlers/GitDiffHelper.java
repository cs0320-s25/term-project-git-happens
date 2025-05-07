package edu.brown.cs.student.main.server.handlers.gitHandlers;

import java.util.List;
import java.util.Map;

public class GitDiffHelper {

  public GitDiffHelper() {}
  public boolean differenceDetected(Map<String, List<Object>> localState, Map<String, List<Object>> incomingState) {
    if (localState.size() != incomingState.size()) {
      return true;
    }
  }

}
