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
        <div>
          <h1>Level 1</h1>
          <p className="instructions">
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce sed
            ornare neque. Aenean non metus sit amet nisi fermentum pharetra.
            Aliquam egestas porta enim, sit amet commodo dui tempor sed.
            Pellentesque et commodo arcu, ut vehicula quam. Nulla tristique
            lacus a felis scelerisque suscipit. Mauris auctor in tortor quis
            maximus. Vestibulum fermentum varius malesuada. Maecenas vestibulum
            sagittis lorem, non dictum massa malesuada elementum. Aliquam
            consequat elit nec turpis pharetra imperdiet. Cras quis elementum
            elit, non porttitor orci. Nam volutpat quam id arcu hendrerit
            porttitor. Aenean vitae leo ex. Nunc molestie non erat ac sodales.
            Mauris consequat, ex quis bibendum pretium, erat augue dapibus
            ligula, aliquam sollicitudin enim est at velit. Vivamus sed posuere
            mi, a eleifend erat. Curabitur dapibus mollis purus at ultrices.
            Vestibulum ornare, orci vitae congue dignissim, augue mauris
            consectetur arcu, id tincidunt mauris diam vel est. Pellentesque eu
            sollicitudin neque, vel scelerisque justo. Donec fermentum elementum
            mauris, nec lobortis massa molestie vitae. Morbi sollicitudin
            eleifend mi, id mollis enim fringilla et. Sed nec tortor dolor.
            Vestibulum condimentum fringilla dui non aliquam. Vestibulum ante
            ipsum primis in faucibus orci luctus et ultrices posuere cubilia
            curae; Nam accumsan dapibus facilisis. Nullam dictum venenatis
            maximus. Duis quis turpis diam. Aenean ac eleifend nulla. Aliquam in
            odio efficitur, eleifend est quis, varius ante. Aliquam et ipsum non
            mi viverra tempor id at nulla. Mauris malesuada sollicitudin
            bibendum. Suspendisse pharetra eu risus quis maximus. Integer
            euismod mollis libero, eu maximus nunc. Cras ultrices, massa sed
            viverra lacinia, diam nulla luctus nulla, efficitur mollis arcu nunc
            quis nunc. Etiam viverra, nisl in maximus tempus, ex ex faucibus
            ante, ac cursus lacus mi nec neque. Cras in odio sed est tempor
            posuere. Aliquam hendrerit arcu elit, non fermentum leo auctor id.
            Pellentesque a justo et est sodales congue vel non leo. Cras
            sagittis enim at venenatis pretium. Phasellus fringilla vehicula
            est, a tincidunt massa congue vel. Phasellus vel massa mi.
            Suspendisse sit amet neque quam. Cras eu nibh semper, porta mauris
            non, placerat velit. Morbi dolor dolor, consectetur quis ultricies
            sit amet, scelerisque vel augue. Maecenas turpis neque, dictum sed
            dolor eget, lacinia porttitor ipsum. Quisque leo est, ultrices quis
            est ut, rhoncus feugiat urna. Nam eget feugiat nisi. In scelerisque
            at quam sit amet pharetra. Phasellus feugiat purus et euismod
            auctor. Suspendisse pellentesque velit sed lacus vehicula, sit amet
            sagittis massa ornare. In in venenatis turpis. Sed id molestie
            metus. Aliquam et cursus tortor. Aenean porta nisi malesuada erat
            molestie rhoncus. Praesent sed tristique purus. Proin dui risus,
            ultrices id sem sit amet, eleifend consectetur orci. Nulla facilisi.
            Sed imperdiet diam aliquam aliquam facilisis. Donec ac euismod urna,
            ac dapibus nulla. In convallis turpis arcu, vel bibendum nisl cursus
            vel. Pellentesque eu sagittis tortor, in tincidunt odio.
          </p>
        </div>
        <Game branchData={branchData} setBranchData={setBranchData} />
        <Branch branchData={branchData} visibleBranches={visibleBranches} />
      </div>
    </div>
  );
}

export default App;
