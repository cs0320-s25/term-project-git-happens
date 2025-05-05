import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../styles/App.css";
import { Game } from "./game/Game";
import { Branch } from "./branch/Branch";

export interface CommitData {
  commit_hash: string;
  message: string;
  branch: string;
  parent_commits: string[];
  contents: string[];
}

export interface BranchData {
  name: string;
  head_commit: string;
}

/**
 * This is the highest level of Mock which builds the component APP;
 *
 * @return JSX of the entire mock
 *  Note: if the user is loggedIn, the main interactive screen will show,
 *  else it will stay at the screen prompting for log in
 */
function App() {
  const [branchData, setBranchData] = useState<{
    commits: CommitData[];
    branches: BranchData[];
  }>({ commits: [], branches: [] });
  const [visibleBranches, setVisibleBranches] = useState<string[]>([]);

  const sampleData = {
    commits: [
      {
        commit_hash: "a",
        message: "Initial commit",
        branch: "main",
        parent_commits: [],
        contents: ["aaaaaa"],
      },
      {
        commit_hash: "b",
        message: "Add feature",
        branch: "feature",
        parent_commits: ["a"],
        contents: ["aaaaaa"],
      },
      {
        commit_hash: "c",
        message: "Fix bug",
        branch: "main",
        parent_commits: ["a"],
        contents: ["aaaaaa"],
      },
      {
        commit_hash: "d",
        message: "Merge feature",
        branch: "main",
        parent_commits: ["f", "h"],
        contents: [
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        ],
      },
      {
        commit_hash: "e",
        message: "new branch :p",
        branch: "side",
        parent_commits: ["a"],
        contents: ["aaaaaa"],
      },
      {
        commit_hash: "f",
        message:
          "merge side to featureaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        branch: "feature",
        parent_commits: ["e", "b"],
        contents: [
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        ],
      },
      {
        commit_hash: "g",
        message: "external create",
        branch: "external",
        parent_commits: ["a"],
        contents: [
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        ],
      },
      {
        commit_hash: "h",
        message: "external merge",
        branch: "main",
        parent_commits: ["g", "c"],
        contents: [
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        ],
      },
      {
        commit_hash: "i",
        message: "more",
        branch: "main",
        parent_commits: ["d"],
        contents: [
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        ],
      },
      {
        commit_hash: "j",
        message: "more",
        branch: "main",
        parent_commits: ["i"],
        contents: [
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        ],
      },
      {
        commit_hash: "k",
        message: "more",
        branch: "main",
        parent_commits: ["j"],
        contents: [
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        ],
      },
      {
        commit_hash: "l",
        message: "more",
        branch: "main",
        parent_commits: ["k"],
        contents: [
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        ],
      },
    ],
    branches: [
      { name: "main", head_commit: "d" },
      { name: "feature", head_commit: "b" },
      { name: "side", head_commit: "e" },
    ],
  };

  const sampleVisibleBranches = ["side", "main", "feature"];

  useEffect(() => {
    setBranchData(sampleData);
    setVisibleBranches(sampleVisibleBranches);
  }, []);

  return (
    <div className="App">
      <div className="flex-row">
        <Game branchData={branchData} setBranchData={setBranchData} />
        <Branch branchData={branchData} visibleBranches={visibleBranches} />
      </div>
    </div>
  );
}

export default App;
