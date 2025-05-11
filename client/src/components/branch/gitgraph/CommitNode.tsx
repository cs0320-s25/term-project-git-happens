import "../../../styles/branch.css";
import type { CommitData, BranchData } from "../../App";

interface CommitNodeProps {
  commit: {
    commit: CommitData;
    x: number;
    y: number;
    hasExternalParent: boolean;
  };
  onToggleSelect: (
    commit: CommitNodeProps["commit"],
    clickX: number,
    clickY: number
  ) => void;
}

export function CommitNode({ commit, onToggleSelect }: CommitNodeProps) {
  const maxMessageLength = 20;
  const displayMessage =
    commit.commit.message.length > maxMessageLength
      ? commit.commit.message.slice(0, maxMessageLength) + "..."
      : commit.commit.message;

  return (
    <g
      className="commit-node"
      onClick={(e) => {
        const svgRect = e.target.ownerSVGElement?.getBoundingClientRect();
        const clickX = e.clientX - (svgRect?.left || 0);
        const clickY = e.clientY - (svgRect?.top || 0);
        onToggleSelect(commit, clickX, clickY);
      }}
    >
      <circle
        cx={commit.x}
        cy={commit.y}
        r={10}
        stroke={commit.hasExternalParent ? "#333" : "none"}
        strokeWidth={commit.hasExternalParent ? 2 : 0}
      />
      <text
        x={commit.x + 15}
        y={commit.y + 4}
        fontSize={12}
        fontFamily="monospace"
        fill="white"
      >
        {displayMessage}
      </text>
    </g>
  );
}
