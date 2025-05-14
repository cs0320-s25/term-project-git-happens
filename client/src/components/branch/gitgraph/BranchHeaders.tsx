import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";

import "../../../styles/branch.css";
import "../../../styles/main.css";
import { BranchData } from "../../App";

/**
 * Props for the BranchHeaders component.
 */
interface BranchHeadersProps {
  /** List of all branches data. */
  branches: BranchData[];
  /** List of currently visible branches in dropdowns. */
  visibleBranches: string[];
  /** Function to update visible branches. */
  setVisibleBranches: Dispatch<SetStateAction<string[]>>;
  /** A record tracking the index of each branch. */
  branchTrack: Record<string, number>;
  /** The spacing between each branch dropdown in the UI. */
  branchSpacing: number;
  /** The base X coordinate for positioning the branches. */
  baseX: number;
  /** Optional Y offset for positioning (default is 40). */
  topOffset?: number;
}

/**
 * Renders the branch headers with dropdowns for selecting branches.
 * Each dropdown allows the user to select a branch to display in a specific position.
 * The dropdowns prevent selecting the same branch in multiple positions.
 *
 * @param {BranchHeadersProps} props - The props for the BranchHeaders component.
 * @returns {JSX.Element} The SVG group element containing the branch headers.
 */
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

  /**
   * Handles the change in selected branch from the dropdown.
   * Updates the corresponding index in the visibleBranches array.
   *
   * @param {number} index - The index of the dropdown being updated.
   * @param {string} newBranch - The new branch selected from the dropdown.
   */
  const handleChange = (index: number, newBranch: string) => {
    setVisibleBranches((prev) => {
      const updated = [...prev];
      updated[index] = newBranch;
      return updated;
    });
  };

  // Log the updated visible branches for debugging
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
