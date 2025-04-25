import { generateBackgroundColors } from "./GraphUtil";
import { Graph } from "./Graph";

interface BarGraphProps {
  data: string[][];
}

export function BarGraph(props: BarGraphProps) {
  return (
    <div
      aria-label="bar-graph"
      role="bar graph"
      aria-description="Bar graph showing data"
    >
      <Graph
        data={props.data}
        type="bar"
        generateDataset={(values) => ({
          backgroundColor: generateBackgroundColors(values.length),
          borderColor: "white",
          borderWidth: 1,
          data: values,
        })}
      />
    </div>
  );
}
