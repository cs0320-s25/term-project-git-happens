import "../../../styles/branch.css";
import type { CommitData, BranchData } from "../../App";

interface BranchEdgesProps {
  commitPositions: { commit: CommitData; x: number; y: number }[];
  getPos: (hash: string) => { x: number; y: number } | undefined;
}

export function BranchEdges({ commitPositions, getPos }: BranchEdgesProps) {
  return (
    <g className="branch-edges">
      {commitPositions.flatMap((commit) =>
        commit.commit.parent_commits.map((parentHash) => {
          const parent = getPos(parentHash);
          if (!parent) return null;

          return (
            <line
              key={`${commit.commit.commit_hash}-${parentHash}`}
              x1={parent.x}
              y1={parent.y}
              x2={commit.x}
              y2={commit.y}
              stroke="#999"
              strokeWidth={2}
            />
          );
        })
      )}
    </g>
  );
}
