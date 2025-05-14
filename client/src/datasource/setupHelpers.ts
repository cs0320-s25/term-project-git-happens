import { gitBranch } from "./gitBranch";
import { gitLog, BackendCommit } from "./gitLog";
import { CommitData } from "../components/App";
import { BranchData } from "../components/App";
import { fileCommit } from "../components/App";
import { IngredientImage } from "../components/game/Game";

export async function getBranchNames(
  sessionID: string,
  userID: string,
  currentBranch: string
): Promise<BranchData[]> {
  try {
    const response = await gitBranch({
      session_id: sessionID,
      user_id: userID,
      branch_request: "",
      current_branch_id: currentBranch,
    });

    const [success, data] = response;

    if (!success) {
        console.log(data)
      console.error(
        "Error fetching available branches on startup:",
        data.error_response
      );
      return [];
    }

    const branchNames = data.local_branch_names ?? [];
    return branchNames.map((name: string) => ({ name }));
  } catch (err) {
    console.error("Unexpected error while fetching branches:", err);
    return [];
  }
}

export async function getAllCommits(
  sessionID: string,
  userID: string,
  branches: BranchData[]
): Promise<CommitData[]> {
  const allCommitsNested = await Promise.all(
    branches.map((branch) => getCommitsForBranch(sessionID, userID, branch))
  );

  // Flatten the array of arrays
  const allCommits = allCommitsNested.flat();

  // Deduplicate by commit_hash
  const uniqueCommitsMap = new Map<string, CommitData>();
  for (const commit of allCommits) {
    if (!uniqueCommitsMap.has(commit.commit_hash)) {
      uniqueCommitsMap.set(commit.commit_hash, commit);
    }
  }

  return Array.from(uniqueCommitsMap.values());
}

async function getCommitsForBranch(
  sessionID: string,
  userID: string,
  branch: BranchData
): Promise<CommitData[]> {
  try {
    const response = await gitLog({
      session_id: sessionID,
      user_id: userID,
      branch_id: branch.name,
      verbose: "true",
    });

    const [success, data] = response;

    if (!success) {
      console.error(
        `Error fetching commits for branch "${branch.name}":`,
        data.error_response
      );
      return [];
    }

    const unformattedCommits = data.commits ?? [];

    return unformattedCommits.map((commit: BackendCommit) =>
      convertBackendCommit(commit)
    );
  } catch (err) {
    console.error(
      `Unexpected error while fetching commits for branch "${branch.name}":`,
      err
    );
    return [];
  }
}

function convertBackendCommit(backendCommit: BackendCommit): CommitData {
  const files: Record<string, IngredientImage[]> = JSON.parse(backendCommit.file_map_json); 
  const contents: fileCommit[] = Object.entries(files).map(
    ([fileName, fileContents]) => ({
      fileName,
      fileContents,
    })
  );

  return {
    commit_hash: backendCommit.commit_id,
    message: backendCommit.commit_message,
    branch: backendCommit.branch_id,
    parent_commits: backendCommit.parent_commits,
    contents,
  };
}
