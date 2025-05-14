import "../../../styles/branch.css";
import type { CommitData, BranchData } from "../../App";

/**
 * Props for the ExternalEdges component.
 */
interface ExternalEdgesProps {
  /** List of commit positions (commit data and coordinates) for the nodes. */
  commitPositions: { commit: CommitData; x: number; y: number }[];
  /** List of all commits with their hash and associated branch. */
  allCommits: {
    commit_hash: string;
    branch: string;
  }[];
  /** List of branches that are currently visible. */
  visibleBranches: string[];
}

/**
 * Represents external edges that connect commits to their parents located in non-visible branches.
 * These edges are drawn as dashed lines.
 *
 * @param {ExternalEdgesProps} props - The props for the ExternalEdges component.
 * @returns {JSX.Element} The SVG group containing all the external edges.
 */
export function ExternalEdges({
  commitPositions,
  allCommits,
  visibleBranches,
}: ExternalEdgesProps) {
  return (
    <g className="external-edges">
      {commitPositions.flatMap((commit) =>
        commit.commit.parent_commits
          .filter((parentHash) => {
            // Find the parent commit by hash and filter out those in visible branches.
            const parent = allCommits.find((c) => c.commit_hash === parentHash);
            return parent && !visibleBranches.includes(parent.branch);
          })
          .map((externalParentHash, i) => (
            <line
              key={`external-${commit.commit.commit_hash}-${i}`}
              x1={commit.x - 20} // Position of the start of the line
              y1={commit.y - 15 + i * 10} // Y-position, offset by 'i' for visual separation of multiple external edges
              x2={commit.x} // Position of the end of the line (commit node's x-coordinate)
              y2={commit.y} // Position of the end of the line (commit node's y-coordinate)
              stroke="#bbb" // Light gray stroke for the edge
              strokeWidth={2} // Thickness of the edge
              strokeDasharray="4 4" // Dashed line style
            />
          ))
      )}
    </g>
  );
}
