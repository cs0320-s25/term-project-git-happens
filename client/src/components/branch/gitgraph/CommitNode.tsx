import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../../styles/branch.css";
import "../../../styles/main.css";
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

export function CommitNode({
  commit,
  onToggleSelect,
  isFocused,
  onArrowNavigate,
}: CommitNodeProps & {
  isFocused: boolean;
  onArrowNavigate: (dir: "up" | "down" | "left" | "right") => void;
}) {
  const ref = useRef<SVGGElement>(null);

  useEffect(() => {
    if (isFocused && ref.current) {
      ref.current.focus();
    }
  }, [isFocused]);

  const handleSelect = (clickX: number, clickY: number) => {
    onToggleSelect(commit, clickX, clickY);
  };

  const ariaLabel = `Commit ${commit.commit.commit_hash.slice(
    0,
    7
  )}, message: ${commit.commit.message}`;

  return (
    <g
      ref={ref}
      className="commit-node"
      tabIndex={0}
      role="button"
      aria-label={ariaLabel}
      onKeyDown={(e) => {
        if (e.key === "ArrowUp") onArrowNavigate("up");
        else if (e.key === "ArrowDown") onArrowNavigate("down");
        else if (e.key === "ArrowLeft") onArrowNavigate("left");
        else if (e.key === "ArrowRight") onArrowNavigate("right");
        else if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          onToggleSelect(commit, commit.x, commit.y);
        }
      }}
      onClick={(e) => {
        const svgRect = e.target.ownerSVGElement?.getBoundingClientRect();
        const clickX = e.clientX - (svgRect?.left || 0);
        const clickY = e.clientY - (svgRect?.top || 0);
        handleSelect(clickX, clickY);
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
      >
        {commit.commit.message.slice(0, 20)}...
      </text>
    </g>
  );
}
