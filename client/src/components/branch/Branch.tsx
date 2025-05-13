import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../styles/main.css";
import "../../styles/branch.css";
import { GitGraph } from "./gitgraph/GitGraph";
import type { CommitData, BranchData } from "../App";

interface BranchProps {
  currentBranch: string;
  setCurrentBranch: Dispatch<SetStateAction<string>>;
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

  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      const target = e.target as HTMLElement;

      const isTyping =
        target.tagName === "INPUT" ||
        target.tagName === "TEXTAREA" ||
        target.isContentEditable;

      if (isTyping) return;

      if (e.shiftKey && e.key === "B") {
        e.preventDefault(); // Prevent the default behavior
        setIsOpen((prev) => !prev); // Toggle the sidebar
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  return (
    <div className={`sidebar ${isOpen ? "open" : "collapsed"}`}>
      <button className="toggle-btn" onClick={() => setIsOpen(!isOpen)}>
        {isOpen ? "⮜" : "⮞"}
      </button>
      {isOpen && (
        <div className="branch">
          <p>Branch</p>
          <div className="branch-vis">
            <GitGraph
              branchData={props.branchData}
              visibleBranches={props.visibleBranches}
            />
          </div>
        </div>
      )}
    </div>
  );
}
