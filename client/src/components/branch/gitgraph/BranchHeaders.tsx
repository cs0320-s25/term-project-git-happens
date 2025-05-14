import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";

import "../../../styles/branch.css";
import "../../../styles/main.css";
import { BranchData } from "../../App";

interface BranchHeadersProps {
  branches: BranchData[];
  visibleBranches: string[];
  setVisibleBranches: Dispatch<SetStateAction<string[]>>;
  branchTrack: Record<string, number>;
  branchSpacing: number;
  baseX: number;
  topOffset?: number; // optional y offset
}

export function BranchHeaders({
  branches,
  visibleBranches,
  setVisibleBranches,
  branchTrack,
  branchSpacing,
  baseX,
  topOffset = 40,
}: BranchHeadersProps) {
  // List of all branch names for the dropdown
  const allBranchNames = branches.map((b) => b.name);

  const handleChange = (index: number, newBranch: string) => {
    setVisibleBranches((prev) => {
      const updated = [...prev];
      updated[index] = newBranch;
      return updated;
    });
  };

  useEffect(() => {
    console.log("VISIBLE BRANCHES UPDATED", visibleBranches);
  }, [visibleBranches]);

  return (
    <g className="branch-headers">
      {[0, 1, 2].map((i) => {
        const branchName = visibleBranches[i] ?? "";
        const branchIdx = branchTrack[branchName] ?? i;
        const x = baseX + branchIdx * branchSpacing;

        // Filter out the branches that are already selected in other dropdowns
        const availableBranches = allBranchNames.filter(
          (name) => !visibleBranches.includes(name) || name === branchName
        );

        return (
          <foreignObject
            key={i}
            x={x - 40}
            y={topOffset}
            width={80}
            height={30}
          >
            <div>
              <select
                value={branchName}
                onChange={(e) => handleChange(i, e.target.value)}
                style={{ fontFamily: "monospace", width: "100%" }}
              >
                <option value="">{`Select branch ${i + 1}`}</option>
                {availableBranches.map((name) => (
                  <option
                    key={name}
                    value={name}
                    disabled={
                      visibleBranches.includes(name) && name !== branchName
                    }
                  >
                    {name}
                  </option>
                ))}
              </select>
            </div>
          </foreignObject>
        );
      })}
    </g>
  );
}
