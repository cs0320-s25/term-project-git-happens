import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../../styles/branch.css";
import type { CommitData, BranchData } from "../../App";
import { CommitPopup } from "../commitpopup/CommitPopup";
import { BranchHeaders } from "./BranchHeaders";
import { BranchEdges } from "./BranchEdges";
import { ExternalEdges } from "./ExternalEdges";
import { CommitNode } from "./CommitNode";

interface GitGraphProps {
  branchData: {
    commits: CommitData[];
    branches: BranchData[];
  };
  visibleBranches: string[];
  setVisibleBranches: Dispatch<SetStateAction<string[]>>;
}

export function GitGraph(props: GitGraphProps) {
  const data = props.branchData;
  const visibleBranches = props.visibleBranches;
  const [selectedCommits, setSelectedCommits] = useState<
    { commit: CommitData; x: number; y: number }[]
  >([]);

  // 1. Filter to only commits from visible branches
  const visibleCommits = data.commits.filter((c) =>
    visibleBranches.includes(c.branch)
  );

  // 2. Track index of branches for layout
  const branchTrack: Record<string, number> = Object.fromEntries(
    visibleBranches.map((name, idx) => [name, idx])
  );

  // 3. Map visible commits to positions and flags
  const commitMap = Object.fromEntries(
    data.commits.map((c) => [c.commit_hash, c])
  );

  function getDepth(commit: CommitData, memo: Record<string, number>): number {
    if (memo[commit.commit_hash] !== undefined) return memo[commit.commit_hash];
    if (commit.parent_commits.length === 0)
      return (memo[commit.commit_hash] = 0);

    const parentDepths = commit.parent_commits.map((ph) => {
      const parent = commitMap[ph];
      return parent ? getDepth(parent, memo) : 0;
    });

    return (memo[commit.commit_hash] = Math.max(...parentDepths) + 1);
  }

  // Compute depths
  const depthMemo: Record<string, number> = {};
  const commitDepths = visibleCommits.map((c) => ({
    ...c,
    depth: getDepth(c, depthMemo),
  }));

  // Position commits by branch and depth
  const branchSpacing = 150;
  const depthSpacing = 150;
  const baseX = 50; // Starting position for x-axis
  const commitPositions = commitDepths.map((commit) => ({
    commit: commit,
    x: baseX + branchTrack[commit.branch] * branchSpacing,
    y: 100 + commit.depth * depthSpacing,
    hasExternalParent: commit.parent_commits.some((ph) => {
      const parent = data.commits.find((c) => c.commit_hash === ph);
      return parent && !visibleBranches.includes(parent.branch);
    }),
  }));

  const margin = 20;
  const maxWidth = branchSpacing - margin;
  const maxHeight = depthSpacing - margin; // assuming depth spacing is 100

  // Utility to find position by commit hash
  const getPos = (hash: string) =>
    commitPositions.find((c) => c.commit.commit_hash === hash);

  const svgRef = useRef<SVGSVGElement | null>(null);

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

  const [focusedCommitHash, setFocusedCommitHash] = useState<string | null>(
    null
  );

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
        width={visibleBranches.length * branchSpacing + 100}
        height={Math.max(...commitPositions.map((c) => c.y)) + 100}
        style={{
          pointerEvents: "auto",
          zIndex: 10,
        }}
      >
        <BranchHeaders
          branches={data.branches}
          visibleBranches={visibleBranches}
          setVisibleBranches={props.setVisibleBranches}
          branchTrack={branchTrack}
          branchSpacing={branchSpacing}
          baseX={baseX}
        />

        <BranchEdges commitPositions={commitPositions} getPos={getPos} />

        <ExternalEdges
          commitPositions={commitPositions}
          allCommits={data.commits}
          visibleBranches={visibleBranches}
        />

        {commitPositions.map((commit) => (
          <CommitNode
            key={commit.commit.commit_hash}
            commit={commit}
            onToggleSelect={(c) => {
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
