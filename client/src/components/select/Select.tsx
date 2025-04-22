import { useState } from "react";
import "../../styles/main.css";
import { SelectCsvInput } from "./SelectCsvInput";
import { Table } from "../table/Table";
import { BarGraph } from "../graphs/BarGraph";
import { LineGraph } from "../graphs/LineGraph";
import { SelectBroadbandInput } from "./SelectBroadbandInput";
import { SelectInputProps } from "./SelectInputUtil";

/**
 * Builds a Select component object that provides a dropdown to view current datasets available
 * and available data format styles
 *
 * @returns A JSX element that includes two dropdowns, after selection, display the dataset in selected form
 *
 */
export function Select() {
  const [table, setTable] = useState<string[][] | null>(null);
  const [hasHeader, setHasHeader] = useState<boolean>(true);
  const [displayType, setDisplayType] = useState<string>("Table");
  const [useBroadband, setUseBroadband] = useState<boolean>(false);
  const modeString = useBroadband ? "Broadband" : "CSV";

  const selectInputProps: SelectInputProps = {
    table: table,
    setTable: setTable,
    displayType: displayType,
    setDisplayType: setDisplayType,
    hasHeader: hasHeader,
    setHasHeader: setHasHeader,
  };

  return (
    <div className="select-layout">
      <div className="select-sidebar">
        <div className="mode-toggle">
          <button
            aria-label={`Swap Between CSV and Broadband Modes? Currently set to ${modeString} mode`}
            onClick={() => {
              setUseBroadband(!useBroadband);
              setTable(null);
            }}
            className="mode-toggle-button"
            tabIndex={1}
          >
            {useBroadband ? "Swap to CSV Mode" : "Swap to Broadband Mode"}
          </button>
        </div>

        {useBroadband ? (
          <SelectBroadbandInput {...selectInputProps} />
        ) : (
          <SelectCsvInput {...selectInputProps} />
        )}
      </div>

      <div className="select-display">
        <div className="select-container" aria-label="Select container">
          <h2>Data Display ({displayType})</h2>
          <pre>
            {
              {
                Table: table ? (
                  <Table data={table} hasHeader={hasHeader} />
                ) : null,
                "Bar Graph": table ? <BarGraph data={table} /> : null,
                "Line Graph": table ? <LineGraph data={table} /> : null,
              }[displayType]
            }
          </pre>
        </div>
      </div>
    </div>
  );
}
