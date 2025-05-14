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
  setVisibleBranches: Dispatch<SetStateAction<string[]>>;
}

export function Branch(props: BranchProps) {
  const [isOpen, setIsOpen] = useState(false);
  const branchHeaderRef = useRef<HTMLParagraphElement>(null);

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

      if (e.shiftKey && e.key === "N") {
        console.log("PRESS Ns");
        if (isOpen && branchHeaderRef.current) {
          console.log("PRESS Ns inside");
          e.preventDefault();
          branchHeaderRef.current.focus();
        }
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen]);

  return (
    <div className={`sidebar ${isOpen ? "open" : "collapsed"}`}>
      <button className="toggle-btn" onClick={() => setIsOpen(!isOpen)}>
        {isOpen ? "⮜" : "⮞"}
      </button>
      {isOpen && (
        <div className="branch">
          <p className="section-text" ref={branchHeaderRef} tabIndex={-1}>
            Branch
          </p>
          <div className="branch-vis">
            <GitGraph
              branchData={props.branchData}
              visibleBranches={props.visibleBranches}
              setVisibleBranches={props.setVisibleBranches}
            />
          </div>
        </div>
      )}
    </div>
  );
}
