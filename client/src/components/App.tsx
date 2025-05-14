import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../styles/App.css";
import { Game } from "./game/Game";
import { Branch } from "./branch/Branch";
import { IngredientImage } from "./game/Game";
import {
  fancy_patty,
  fries,
  ketchup,
  lettuce,
  mayo,
  moldy_patty,
  mustard,
  onion,
  patty,
  pretzel_bottom,
  pretzel_top,
  sesame_bottom,
  sesame_top,
  tomato,
  plate,
  cheese,
} from "../assets/images";
import { createSession } from "../datasource/sessionCreation";
import { getAllCommits, getBranchNames } from "../datasource/setupHelpers";

export interface fileCommit {
  fileName: string;
  fileContents: IngredientImage[];
}

export interface CommitData {
  commit_hash: string;
  message: string;
  branch: string;
  parent_commits: string[];
  contents: fileCommit[];
}

export interface BranchData {
  name: string;
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
  const [currentBranch, setCurrentBranch] = useState<string>("main");

  const sampleData = {
    commits: [
      {
        commit_hash: "a",
        message: "Initial commit",
        branch: "main",
        parent_commits: [],
        contents: [
          {
            fileName: "file1",
            fileContents: [
              { imgStr: sesame_top, imgName: "burger" },
              { imgStr: mayo, imgName: "burger" },
              { imgStr: cheese, imgName: "burger" },
              { imgStr: patty, imgName: "burger" },
              { imgStr: sesame_bottom, imgName: "burger" },
            ],
          },
          {
            fileName: "file2",
            fileContents: [
              { imgStr: sesame_top, imgName: "burger" },
              { imgStr: onion, imgName: "burger" },
              { imgStr: lettuce, imgName: "burger" },
              { imgStr: tomato, imgName: "burger" },
              { imgStr: sesame_bottom, imgName: "burger" },
            ],
          },
          {
            fileName: "file3",
            fileContents: [
              { imgStr: ketchup, imgName: "burger" },
              { imgStr: fries, imgName: "burger" },
            ],
          },
        ],
      },
      {
        commit_hash: "b",
        message: "Add feature",
        branch: "feature",
        parent_commits: ["a"],
        contents: [
          {
            fileName: "file1",
            fileContents: [
              { imgStr: sesame_top, imgName: "burger" },
              { imgStr: mayo, imgName: "burger" },
              { imgStr: cheese, imgName: "burger" },
              { imgStr: patty, imgName: "burger" },
              { imgStr: sesame_bottom, imgName: "burger" },
            ],
          },
        ],
      },
      {
        commit_hash: "c",
        message: "Fix bug",
        branch: "main",
        parent_commits: ["a"],
        contents: [
          {
            fileName: "file1",
            fileContents: [
              { imgStr: sesame_top, imgName: "burger" },
              { imgStr: mayo, imgName: "burger" },
              { imgStr: cheese, imgName: "burger" },
              { imgStr: patty, imgName: "burger" },
              { imgStr: sesame_bottom, imgName: "burger" },
            ],
          },
        ],
      },
      {
        commit_hash: "d",
        message: "Merge feature",
        branch: "main",
        parent_commits: ["f", "h"],
        contents: [
          {
            fileName: "file1",
            fileContents: [
              { imgStr: sesame_top, imgName: "burger" },
              { imgStr: mayo, imgName: "burger" },
              { imgStr: cheese, imgName: "burger" },
              { imgStr: patty, imgName: "burger" },
              { imgStr: sesame_bottom, imgName: "burger" },
            ],
          },
        ],
      },
      {
        commit_hash: "e",
        message: "new branch :p",
        branch: "side",
        parent_commits: ["a"],
        contents: [
          {
            fileName: "file1",
            fileContents: [
              { imgStr: sesame_top, imgName: "burger" },
              { imgStr: mayo, imgName: "burger" },
              { imgStr: cheese, imgName: "burger" },
              { imgStr: patty, imgName: "burger" },
              { imgStr: sesame_bottom, imgName: "burger" },
            ],
          },
        ],
      },
      {
        commit_hash: "f",
        message:
          "merge side to featureaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        branch: "feature",
        parent_commits: ["e", "b"],
        contents: [
          {
            fileName: "file1",
            fileContents: [
              { imgStr: sesame_top, imgName: "burger" },
              { imgStr: mayo, imgName: "burger" },
              { imgStr: cheese, imgName: "burger" },
              { imgStr: patty, imgName: "burger" },
              { imgStr: sesame_bottom, imgName: "burger" },
            ],
          },
        ],
      },
      {
        commit_hash: "g",
        message: "external create",
        branch: "external",
        parent_commits: ["a"],
        contents: [
          {
            fileName: "file1",
            fileContents: [
              { imgStr: sesame_top, imgName: "burger" },
              { imgStr: mayo, imgName: "burger" },
              { imgStr: cheese, imgName: "burger" },
              { imgStr: patty, imgName: "burger" },
              { imgStr: sesame_bottom, imgName: "burger" },
            ],
          },
        ],
      },
      {
        commit_hash: "h",
        message: "external merge",
        branch: "main",
        parent_commits: ["g", "c"],
        contents: [
          {
            fileName: "file1",
            fileContents: [
              { imgStr: sesame_top, imgName: "burger" },
              { imgStr: mayo, imgName: "burger" },
              { imgStr: cheese, imgName: "burger" },
              { imgStr: patty, imgName: "burger" },
              { imgStr: sesame_bottom, imgName: "burger" },
            ],
          },
        ],
      },
      {
        commit_hash: "i",
        message: "more",
        branch: "main",
        parent_commits: ["d"],
        contents: [
          {
            fileName: "file1",
            fileContents: [
              { imgStr: sesame_top, imgName: "burger" },
              { imgStr: mayo, imgName: "burger" },
              { imgStr: cheese, imgName: "burger" },
              { imgStr: patty, imgName: "burger" },
              { imgStr: sesame_bottom, imgName: "burger" },
            ],
          },
        ],
      },
      {
        commit_hash: "j",
        message: "more",
        branch: "main",
        parent_commits: ["i"],
        contents: [
          {
            fileName: "file1",
            fileContents: [
              { imgStr: sesame_top, imgName: "burger" },
              { imgStr: mayo, imgName: "burger" },
              { imgStr: cheese, imgName: "burger" },
              { imgStr: patty, imgName: "burger" },
              { imgStr: sesame_bottom, imgName: "burger" },
            ],
          },
        ],
      },
      {
        commit_hash: "k",
        message: "more",
        branch: "main",
        parent_commits: ["j"],
        contents: [
          {
            fileName: "file1",
            fileContents: [
              { imgStr: sesame_top, imgName: "burger" },
              { imgStr: mayo, imgName: "burger" },
              { imgStr: cheese, imgName: "burger" },
              { imgStr: patty, imgName: "burger" },
              { imgStr: sesame_bottom, imgName: "burger" },
            ],
          },
        ],
      },
      {
        commit_hash: "l",
        message: "more",
        branch: "main",
        parent_commits: ["k"],
        contents: [
          {
            fileName: "file1",
            fileContents: [
              { imgStr: sesame_top, imgName: "burger" },
              { imgStr: mayo, imgName: "burger" },
              { imgStr: cheese, imgName: "burger" },
              { imgStr: patty, imgName: "burger" },
              { imgStr: sesame_bottom, imgName: "burger" },
            ],
          },
        ],
      },
      {
        commit_hash: "m",
        message: "other branch",
        branch: "other",
        parent_commits: ["l"],
        contents: [
          {
            fileName: "file1",
            fileContents: [
              { imgStr: sesame_top, imgName: "burger" },
              { imgStr: mayo, imgName: "burger" },
              { imgStr: cheese, imgName: "burger" },
              { imgStr: patty, imgName: "burger" },
              { imgStr: sesame_bottom, imgName: "burger" },
            ],
          },
        ],
      },
    ],
    branches: [
      { name: "main" },
      { name: "feature" },
      { name: "side" },
      { name: "other" },
    ],
  };

  const sampleVisibleBranches = ["side", "main", "feature"];

  const [sessionID, setSessionID] = useState("");
  const [userID, setUserID] = useState("");
  const [submitted, setSubmitted] = useState(false);
  const [mostRecentCommit, setMostRecentCommit] = useState<fileCommit[]>([]);

  useEffect(() => {
    const loadBranchData = async () => {
      try {
        const branchNames = await getBranchNames(
          sessionID,
          userID,
          currentBranch
        );
        const allCommits = await getAllCommits(sessionID, userID, branchNames);
        setBranchData({ commits: allCommits, branches: branchNames });
        if (allCommits.length !== 0) {
          setMostRecentCommit(allCommits[0].contents)
        }
      } catch (error) {
        console.error("Error loading branch data:", error);
      }
    };
    if (!submitted) {
      return;
    }

    loadBranchData();
    // setBranchData(sampleData);
    setVisibleBranches(["main", "new-branch"]);
    // setVisibleBranches(sampleVisibleBranches);
  }, [sessionID, submitted]);

  return (
    <div className="App">
      {!submitted ? (
        <div className="session-form">
          <h2>Enter Session Details</h2>
          <input
            type="text"
            placeholder="Session ID"
            value={sessionID}
            onChange={(e) => setSessionID(e.target.value)}
          />
          <input
            type="text"
            placeholder="User ID"
            value={userID}
            onChange={(e) => setUserID(e.target.value)}
          />
          <button
            onClick={() => {
              if (sessionID && userID) {
                createSession({
                  session_id: sessionID,
                  user_id: userID,
                  file_map_json: "{}",
                }).then((response) => {
                  if (!response[0]) {
                    console.log(response[1].error_response!);
                  }
                });
                setSubmitted(true);
              }
            }}
          >
            Enter
          </button>
        </div>
      ) : (
        <div className="game-branch-container">
          <Game
            branchData={branchData}
            setBranchData={setBranchData}
            currentBranch={currentBranch}
            setCurrentBranch={setCurrentBranch}
            sessionID={sessionID}
            userID={userID}
            startingState={mostRecentCommit}
          />
          <Branch
            currentBranch={currentBranch}
            setCurrentBranch={setCurrentBranch}
            branchData={branchData}
            visibleBranches={visibleBranches}
            setVisibleBranches={setVisibleBranches}
          />
        </div>
      )}
    </div>
  );
}

export default App;
