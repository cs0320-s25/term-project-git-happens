import "../../../styles/branch.css";
import type { CommitData, BranchData } from "../../App";

/**
 * Props for the BranchEdges component.
 */
interface BranchEdgesProps {
  /** An array of commit positions including the commit data and its x, y coordinates. */
  commitPositions: { commit: CommitData; x: number; y: number }[];
  /** A function to get the position (x, y) of a commit by its hash. Returns undefined if not found. */
  getPos: (hash: string) => { x: number; y: number } | undefined;
}

/**
 * Renders the edges (lines) connecting parent commits to child commits on the branch.
 * Each edge is drawn as a line from a parent commit to its child commit.
 *
 * @param {BranchEdgesProps} props - The props for the BranchEdges component.
 * @returns {JSX.Element} The SVG element containing the branch edges.
 */
export function BranchEdges({ commitPositions, getPos }: BranchEdgesProps) {
  return (
    <g className="branch-edges">
      {commitPositions.flatMap((commit) =>
        commit.commit.parent_commits.map((parentHash) => {
          // Retrieve the position of the parent commit
          const parent = getPos(parentHash);
          if (!parent) return null; // If parent position is not found, return null

          return (
            <line
              key={`${commit.commit.commit_hash}-${parentHash}`}
              x1={parent.x} // Starting x coordinate (parent commit)
              y1={parent.y} // Starting y coordinate (parent commit)
              x2={commit.x} // Ending x coordinate (child commit)
              y2={commit.y} // Ending y coordinate (child commit)
              stroke="#999" // Stroke color for the line
              strokeWidth={2} // Stroke width for the line
            />
          );
        })
      )}
    </g>
  );
}
