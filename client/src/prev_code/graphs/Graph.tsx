import { useResponsiveSettings, makeOptions } from "./GraphUtil";
import { Chart } from "react-chartjs-2";
import "chart.js/auto";

interface GraphProps {
  data: string[][];
  type: "bar" | "line";
  // we have to use the any type here because chart.js doesn't have a dataset type since it is
  // more js than ts
  generateDataset: (values: number[]) => any;
}

export function Graph({ data, type, generateDataset }: GraphProps) {
  const dataWithoutHeader: string[][] = data.slice(1);
  const dataLabels: string[] = dataWithoutHeader.map((row) => row[0]);
  const dataVals: number[] = dataWithoutHeader.map((row) => Number(row[1]));

  const { fontSize, useDarkMode } = useResponsiveSettings();

  return (
    <div
      className="graph-container"
      id="graph-container"
      aria-label={`${type} graph displaying the relationship between ${data[0][1]} and ${data[0][0]}`}
      role="bar"
      tabIndex={10}
      data-element-count={dataVals.length}
    >
      <Chart
        type={type}
        data={{
          labels: dataLabels,
          datasets: [generateDataset(dataVals)],
        }}
        options={makeOptions(fontSize, dataLabels, data[0][0], data[0][1], useDarkMode)}
      />
    </div>
  );
}
