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

  const [sessionID, setSessionID] = useState("");
  const [userID, setUserID] = useState("");
  const [submitted, setSubmitted] = useState(false);
  const [mostRecentCommit, setMostRecentCommit] = useState<fileCommit[]>([]);

  useEffect(() => {
    const loadBranchData = async () => {
      try {
        await new Promise((resolve) => setTimeout(resolve, 3000));
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
    setVisibleBranches(["main"]);
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
                  file_map_json: JSON.stringify({file1: [], file2: [], file3: []}),
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
