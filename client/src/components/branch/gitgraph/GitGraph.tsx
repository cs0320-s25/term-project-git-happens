import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../../styles/branch.css";
import type { CommitData, BranchData } from "../../App";
import { CommitPopup } from "../commitpopup/CommitPopup";
import { BranchHeaders } from "./BranchHeaders";
import { BranchEdges } from "./BranchEdges";
import { ExternalEdges } from "./ExternalEdges";
import { CommitNode } from "./CommitNode";

// Define the types of the props this component will receive
interface GitGraphProps {
  branchData: {
    commits: CommitData[]; // List of commit data objects
    branches: BranchData[]; // List of branch data objects
  };
  visibleBranches: string[]; // Array of visible branch names
  setVisibleBranches: Dispatch<SetStateAction<string[]>>; // Function to update the visible branches state
}

export function GitGraph(props: GitGraphProps) {
  // Destructure data and visibleBranches from props
  const data = props.branchData;
  const visibleBranches = props.visibleBranches;

  // State to track selected commits and their positions
  const [selectedCommits, setSelectedCommits] = useState<
    { commit: CommitData; x: number; y: number }[] // An array of selected commits and their positions
  >([]);

  // 1. Filter to only include commits from visible branches
  const visibleCommits = data.commits.filter((c) =>
    visibleBranches.includes(c.branch)
  );

  // 2. Track index of branches for layout (used to space out branches)
  const branchTrack: Record<string, number> = Object.fromEntries(
    visibleBranches.map((name, idx) => [name, idx])
  );

  // 3. Create a commit map where commit_hash is the key for fast lookup
  const commitMap = Object.fromEntries(
    data.commits.map((c) => [c.commit_hash, c])
  );

  /**
   * Recursively calculates the depth of a commit in the commit graph.
   * @param commit The current commit being processed
   * @param memo A memoization object to store depths of previously computed commits
   * @returns The depth of the current commit
   */
  function getDepth(commit: CommitData, memo: Record<string, number>): number {
    if (memo[commit.commit_hash] !== undefined) return memo[commit.commit_hash]; // Return memoized depth if available
    if (commit.parent_commits.length === 0)
      return (memo[commit.commit_hash] = 0); // Root commit has depth 0

    // Calculate depth for parent commits
    const parentDepths = commit.parent_commits.map((ph) => {
      const parent = commitMap[ph];
      return parent ? getDepth(parent, memo) : 0;
    });

    // Memoize and return the depth for the current commit
    return (memo[commit.commit_hash] = Math.max(...parentDepths) + 1);
  }

  // Compute depths for each visible commit
  const depthMemo: Record<string, number> = {};
  const commitDepths = visibleCommits.map((c) => ({
    ...c,
    depth: getDepth(c, depthMemo),
  }));

  // 4. Position commits on the graph using their branch and depth
  const branchSpacing = 150; // Horizontal spacing between branches
  const depthSpacing = 150; // Vertical spacing between commit depths
  const baseX = 50; // Starting position for the x-axis

  // Map commits to positions (x, y) based on their branch and depth
  const commitPositions = commitDepths.map((commit) => ({
    commit: commit,
    x: baseX + branchTrack[commit.branch] * branchSpacing, // Horizontal position based on branch
    y: 100 + commit.depth * depthSpacing, // Vertical position based on depth
    hasExternalParent: commit.parent_commits.some((ph) => {
      const parent = data.commits.find((c) => c.commit_hash === ph);
      return parent && !visibleBranches.includes(parent.branch); // Check for external parent
    }),
  }));

  const margin = 20; // Margin to prevent nodes from being too close to edges
  const maxWidth = branchSpacing - margin; // Max width for commit node popups
  const maxHeight = depthSpacing - margin; // Max height for commit node popups

  // Utility to find position by commit hash
  const getPos = (hash: string) =>
    commitPositions.find((c) => c.commit.commit_hash === hash);

  // Ref for the SVG element to convert coordinates
  const svgRef = useRef<SVGSVGElement | null>(null);

  /**
   * Converts SVG coordinates to screen coordinates.
   * @param svgElement The SVG element
   * @param commitX The x-coordinate in the SVG
   * @param commitY The y-coordinate in the SVG
   * @returns The x and y coordinates in screen space
   */
  function svgToScreenCoords(
    svgElement: SVGSVGElement,
    commitX: number,
    commitY: number
  ) {
    const container = svgElement.parentElement!;
    const svgRect = svgElement.getBoundingClientRect();
    const containerRect = container.getBoundingClientRect();

    const scrollLeft = container.scrollLeft;
    const scrollTop = container.scrollTop;

    const offsetX = svgRect.left - containerRect.left + scrollLeft;
    const offsetY = svgRect.top - containerRect.top + scrollTop;

    return {
      x: commitX + offsetX,
      y: commitY + offsetY,
    };
  }

  // State for tracking the currently focused commit
  const [focusedCommitHash, setFocusedCommitHash] = useState<string | null>(
    null
  );

  /**
   * Handles navigation between commits using the arrow keys.
   * @param current The current commit position being focused
   * @param direction The direction of navigation ("up", "down", "left", "right")
   */
  function handleArrowNavigate(
    current: (typeof commitPositions)[0],
    direction: "up" | "down" | "left" | "right"
  ) {
    const branchCommits = commitPositions.filter(
      (c) => c.commit.branch === current.commit.branch
    );

    const sortByY = (
      a: (typeof commitPositions)[0],
      b: (typeof commitPositions)[0]
    ) => a.y - b.y;
    const sortByX = (
      a: (typeof commitPositions)[0],
      b: (typeof commitPositions)[0]
    ) => a.x - b.x;

    let next;

    // Find the next commit based on the direction of navigation
    if (direction === "up") {
      next = branchCommits
        .filter((c) => c.y < current.y)
        .sort(sortByY)
        .pop();
    } else if (direction === "down") {
      next = branchCommits.filter((c) => c.y > current.y).sort(sortByY)[0];
    } else if (direction === "left") {
      next = commitPositions
        .filter((c) => c.x < current.x && c.y === current.y)
        .sort(sortByX)
        .pop();
    } else if (direction === "right") {
      next = commitPositions
        .filter((c) => c.x > current.x && c.y === current.y)
        .sort(sortByX)[0];
    }

    // Update focused commit if a valid next commit is found
    if (next) {
      setFocusedCommitHash(next.commit.commit_hash);
      // Optional: scroll into view or apply visual highlight
    }
  }

  return (
    <div className="svg-container">
      <svg
        className="svg-vis"
        ref={svgRef}
        width={visibleBranches.length * branchSpacing + 100} // Calculate SVG width based on visible branches
        height={Math.max(...commitPositions.map((c) => c.y)) + 100} // Calculate SVG height based on commit positions
        style={{
          pointerEvents: "auto",
          zIndex: 10,
        }}
      >
        {/* Render branch headers */}
        <BranchHeaders
          branches={data.branches}
          visibleBranches={visibleBranches}
          setVisibleBranches={props.setVisibleBranches}
          branchTrack={branchTrack}
          branchSpacing={branchSpacing}
          baseX={baseX}
        />

        {/* Render edges connecting commits in the same branch */}
        <BranchEdges commitPositions={commitPositions} getPos={getPos} />

        {/* Render external edges (connections to commits from outside visible branches) */}
        <ExternalEdges
          commitPositions={commitPositions}
          allCommits={data.commits}
          visibleBranches={visibleBranches}
        />

        {/* Render commit nodes */}
        {commitPositions.map((commit) => (
          <CommitNode
            key={commit.commit.commit_hash}
            commit={commit}
            onToggleSelect={(c) => {
              // Handle commit selection/deselection
              setSelectedCommits((prev) => {
                const alreadySelected = prev.find(
                  (sel) => sel.commit.commit_hash === c.commit.commit_hash
                );
                if (alreadySelected) {
                  return prev.filter(
                    (sel) => sel.commit.commit_hash !== c.commit.commit_hash
                  );
                }
                return [...prev, c];
              });
            }}
            isFocused={focusedCommitHash === commit.commit.commit_hash}
            onArrowNavigate={(direction) =>
              handleArrowNavigate(commit, direction)
            }
          />
        ))}
      </svg>

      {/* Popups for selected commits */}
      {selectedCommits.map(({ commit, x, y }) => {
        // Ensure you're using the correct reference for the SVG element
        const coords = svgRef.current
          ? svgToScreenCoords(svgRef.current, x, y)
          : { x, y }; // If no SVG reference, fall back to default

        return (
          <CommitPopup
            key={commit.commit_hash}
            commit={commit}
            x={coords.x}
            y={coords.y}
            maxWidth={maxWidth}
            maxHeight={maxHeight}
            onClose={(hash) =>
              setSelectedCommits((prev) =>
                prev.filter((c) => c.commit.commit_hash !== hash)
              )
            }
          />
        );
      })}
    </div>
  );
}
