import { SelectInputBase } from "./SelectInputBase";
import { ControlledInput } from "./ControlledInput";
import { useState } from "react";
import { SelectInputProps } from "./SelectInputUtil";

export function SelectCsvInput(props: SelectInputProps) {
  const [filePath, setFilePath] = useState<string>("");
  const [searchTerm, setSearchTerm] = useState<string>("");
  const [columnIdentifier, setColumnIdentifier] = useState<string>("");
  const [internalHasHeader, setInternalHasHeader] = useState<boolean>(true);

  const inputFields = (
    <>
      <div className="parameter-row" tabIndex={-1}>
        <p>File to search: </p>
        <ControlledInput
          value={filePath}
          setValue={setFilePath}
          placeholder="Enter filepath here!"
          ariaLabel="file path input"
          id={"filepath input"}
          tabIndex={2}
        />

        <button
          aria-label={"Use Header? Currently set to " + internalHasHeader}
          onClick={() => {
            setInternalHasHeader(!internalHasHeader);
          }}
          tabIndex={3}
        >
          {internalHasHeader ? "Header on" : "Header off"}
        </button>
      </div>

      <div className="parameter-row" tabIndex={-1}>
        <p>Optional search parameters: </p>
      </div>

      <div className="parameter-row" tabIndex={-1}>
        <ControlledInput
          value={searchTerm}
          setValue={setSearchTerm}
          ariaLabel={"search term input"}
          placeholder="Enter search term! e.g. Rhode Island "
          className={"optional-command-box"}
          tabIndex={4}
        />
      </div>

      <div className="parameter-row" tabIndex={-1}>
        <ControlledInput
          value={columnIdentifier}
          setValue={setColumnIdentifier}
          ariaLabel={"column identifier input"}
          placeholder="Enter column identifier! e.g. State"
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
      internalHasHeader={internalHasHeader}
      {...props}
    />
  );
}
