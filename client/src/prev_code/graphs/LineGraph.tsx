import { Graph } from "./Graph";

interface LineGraphProps {
  data: string[][];
}

export function LineGraph(props: LineGraphProps) {
  return (
    <div
      aria-label="line graph"
      role="line graph"
      aria-description="Line graph showing data"
    >
      <Graph
        data={props.data}
        type="line"
        generateDataset={(values) => ({
          fill: true,
          borderColor: "rgb(53, 162, 235)",
          backgroundColor: "rgba(111, 191, 244, 0.5)",
          data: values,
        })}
      />
    </div>
  );
}
