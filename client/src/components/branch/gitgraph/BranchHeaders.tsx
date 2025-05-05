import "../../../styles/branch.css";

interface BranchHeadersProps {
  visibleBranches: string[];
  branchTrack: Record<string, number>;
  branchSpacing: number;
  baseX: number;
  topOffset?: number; // optional y offset
}

export function BranchHeaders({
  visibleBranches,
  branchTrack,
  branchSpacing,
  baseX,
  topOffset = 40,
}: BranchHeadersProps) {
  return (
    <g className="branch-headers">
      {visibleBranches.map((branchName) => {
        const branchIdx = branchTrack[branchName];
        const x = baseX + branchIdx * branchSpacing;

        return (
          <text
            key={branchName}
            x={x}
            y={topOffset}
            fontSize={14}
            fontWeight="bold"
            fontFamily="monospace"
            textAnchor="middle"
          >
            {branchName}
          </text>
        );
      })}
    </g>
  );
}
