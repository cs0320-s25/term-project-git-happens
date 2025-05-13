import "../../../styles/branch.css";
import type { CommitData, BranchData } from "../../App";

interface ExternalEdgesProps {
  commitPositions: { commit: CommitData; x: number; y: number }[];
  allCommits: {
    commit_hash: string;
    branch: string;
  }[];
  visibleBranches: string[];
}

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
            const parent = allCommits.find((c) => c.commit_hash === parentHash);
            return parent && !visibleBranches.includes(parent.branch);
          })
          .map((externalParentHash, i) => (
            <line
              key={`external-${commit.commit.commit_hash}-${i}`}
              x1={commit.x - 20}
              y1={commit.y - 15 + i * 10}
              x2={commit.x}
              y2={commit.y}
              stroke="#bbb"
              strokeWidth={2}
              strokeDasharray="4 4"
            />
          ))
      )}
    </g>
  );
}
