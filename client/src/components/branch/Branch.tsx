import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../styles/main.css";
import "../../styles/branch.css";
import { GitGraph } from "./gitgraph/GitGraph";
import type { CommitData, BranchData } from "../App";

interface BranchProps {
  branchData: {
    commits: CommitData[];
    branches: BranchData[];
  };
  visibleBranches: string[];
}

export function Branch(props: BranchProps) {
  // assumption for now that main will always be in focus, otherwise need to default add main
  const [branchList, setBranchList] = useState<string[]>([]);
  const [branchFocusList, setBranchFocusList] = useState<string[]>([
    "b1",
    "b2",
  ]);

  return (
    <div className="branch">
      <p>Branch</p>
      <div className="branch-vis">
        <GitGraph
          branchData={props.branchData}
          visibleBranches={props.visibleBranches}
        />
      </div>
    </div>
  );
}
