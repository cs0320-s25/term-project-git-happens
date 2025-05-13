import { SelectInputBase } from "./SelectInputBase";
import { useState } from "react";
import { ControlledInput } from "./ControlledInput";
import { SelectInputProps } from "./SelectInputUtil";

/**
 * A component that provides a user interface for fetching broadband data based on specified parameters.
 * It includes input fields for state, county, custom parameters, and custom parameter names.
 * The component uses the `SelectInputBase` component to handle the data fetching and display.
 *
 * @param props - The properties passed to the `SelectBroadbandInput` component, extending `SelectInputProps`.
 * @returns A React functional component.
 */

export function SelectBroadbandInput(props: SelectInputProps) {
  const [state, setState] = useState<string>("");
  const [county, setCounty] = useState<string>("");
  const [customParameters, setCustomParameters] = useState<string>("");
  const [customParameterNames, setCustomParameterNames] = useState<string>("");

  const inputFields = (
    <>
      <div className="parameter-row" tabIndex={-1}>
        <p>State: </p>
        <ControlledInput
          value={state}
          setValue={setState}
          placeholder="Enter state here!"
          ariaLabel="state input"
          className={"optional-command-box"}
          id={"filepath input"}
          tabIndex={2}
        />
      </div>

      <div className="parameter-row" tabIndex={-1}>
        <p>County: </p>
        <ControlledInput
          value={county}
          setValue={setCounty}
          ariaLabel="county input"
          placeholder="Enter county here!"
          className={"optional-command-box"}
          tabIndex={3}
        />
      </div>

      <div className="short-parameter-row" tabIndex={-1}>
        <p style={{ fontSize: "small", margin: 0 }}>
          Custom parameters should be formatted as comma-separated lists (with
          no spaces) of ACS variable codes.
        </p>
        <p style={{ fontSize: "small", margin: 0 }}>
          Custom parameter names should be commas-separated list of names
          corresponding to the given ACS variables (in the same order)
        </p>
      </div>

      <div className="parameter-row" tabIndex={-1}>
        <p>Custom Parameters: </p>
        <ControlledInput
          value={customParameters}
          setValue={setCustomParameters}
          placeholder="Enter custom parameters here! e.g. S2704_C01_024E,S2801_C02_030E"
          ariaLabel="custom parameter input"
          className={"optional-command-box"}
          tabIndex={4}
        />

        <p>Custom Parameter Name Input: </p>
        <ControlledInput
          value={customParameterNames}
          setValue={setCustomParameterNames}
          ariaLabel={"custom parameter name input"}
          placeholder="Enter custom parameter names here! e.g. Insurance, Broadband"
          className={"optional-command-box"}
          tabIndex={5}
        />
      </div>
    </>
  );

  return (
    <SelectInputBase
      fetchData={() => null}
      inputFields={inputFields}
      internalHasHeader={true}
      {...props}
    />
  );
}
