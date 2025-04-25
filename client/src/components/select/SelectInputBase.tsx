import { Dispatch, SetStateAction, useEffect, useRef, useState } from "react";
import "../../styles/main.css";
import {
  validateDataForGraph,
  isTableFormat,
  useErrorAndHelpHooks,
} from "./SelectInputUtil";
import { getAvailableDataTable } from "../table/TableRetriever";

interface SelectInputBaseProps {
  fetchData: () => Promise<[string[][] | string, boolean]> | null;
  inputFields: React.ReactNode;
  table: string[][] | null;
  setTable: Dispatch<SetStateAction<string[][] | null>>;
  displayType: string;
  setDisplayType: Dispatch<SetStateAction<string>>;
  hasHeader: boolean;
  setHasHeader: Dispatch<SetStateAction<boolean>>;
  internalHasHeader: boolean;
}

const MESSAGES = {
  badTable: "Error: Requested table is not valid",
  ungraphable:
    "Error: Data is not valid for graph. Graphable data must be pairs of labels and a number to graph. Dataset being displayed as table",
  noFilePath:
    "Error: Please submit the filepath for the data you would like to display",
  keyboardInstructions: "Accessibility Instructions",
};

/**
 * A React component that renders a base select input with error and help messages.
 *
 * @param {SelectInputBaseProps} props - The properties for the SelectInputBase component.
 * @param {() => Promise<[string[][] | string, boolean]>} props.fetchData - The function to fetch data.
 * @param {React.ReactNode} props.inputFields - The input fields for the select input.
 * @param {string[][] | null} props.table - The table data to display.
 * @param {Dispatch<SetStateAction<string[][] | null>>} props.setTable - The function to set the table data.
 * @param {string} props.displayType - The display type for the table data.
 * @param {Dispatch<SetStateAction<string>>} props.setDisplayType - The function to set the display type.
 * @param {boolean} props.hasHeader - A boolean indicating whether the table has a header row.
 * @param {Dispatch<SetStateAction<boolean>>} props.setHasHeader - The function to set the header row.
 * @param {boolean} props.internalHasHeader - A boolean indicating whether the table has a header row internally.
 *
 * @returns {JSX.Element} A React select input component with error and help messages.
 */
export function SelectInputBase(props: SelectInputBaseProps) {
  const {
    errorMessage,
    helpMessage,
    outputError,
    outputHelp,
    removeError,
    removeHelp,
  } = useErrorAndHelpHooks();
  const selectRef = useRef<HTMLSelectElement>(null);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  /**
   * Handles the submit button click event to fetch and display data.
   * If the data is not in table format, an error message is displayed.
   * If the data is not graphable, the display type is set to "Table" and an error message is displayed.
   * Otherwise, the table and display type are set based on the selected display type.
   *
   * @param {void} - The event object.
   * @returns {void}
   */
  function handleSubmit(): void {
    removeHelp();

    const filepathInput = document.getElementById(
      "filepath input"
    ) as HTMLInputElement;
    if (filepathInput.value === "") {
      outputError(MESSAGES.noFilePath);
      return;
    }

    // props.fetchData().then(([data, success]) => {
    //   if (!success) {
    //     outputError("ERROR: " + data);
    //     return;
    //   }

    //   if (!isTableFormat(data)) {
    //     outputError(MESSAGES.badTable);
    //   } else {
    //     props.setHasHeader(props.internalHasHeader);
    //     props.setTable(data);
    //     const selectedDisplayType = (
    //       document.getElementById("display-type-dropdown") as HTMLSelectElement
    //     )?.value;
    //     if (selectedDisplayType !== "Table" && !validateDataForGraph(data)) {
    //       props.setDisplayType("Table");
    //       outputError(MESSAGES.ungraphable);
    //     } else {
    //       props.setDisplayType(selectedDisplayType);
    //       removeError();
    //     }
    //   }
    // });
  }

  /**
   * Handles keyboard navigation for the dropdown menu.
   *
   * @param {React.KeyboardEvent} event - The keyboard event.
   */
  const handleSelectKeyDown = (event: React.KeyboardEvent) => {
    const select = selectRef.current;
    if (!select) return;

    switch (event.key) {
      case "ArrowDown":
        event.preventDefault();
        if (!isDropdownOpen) {
          // Open dropdown on first arrow down
          setIsDropdownOpen(true);
          select.size = Math.min(select.options.length, 5); // Show up to 5 options
        } else if (select.selectedIndex < select.options.length - 1) {
          // Navigate down through options
          select.selectedIndex += 1;
        }
        break;

      case "ArrowUp":
        event.preventDefault();
        if (isDropdownOpen && select.selectedIndex > 0) {
          // Navigate up through options
          select.selectedIndex -= 1;
        }
        break;

      case "Enter":
      case " ": // Space key
        event.preventDefault();
        if (!isDropdownOpen) {
          // Open dropdown
          setIsDropdownOpen(true);
          select.size = Math.min(select.options.length, 5);
        } else {
          // Select current option and close dropdown
          setIsDropdownOpen(false);
          const submitButton = document.getElementById("submit-button");
          if (submitButton) {
            submitButton.focus();
          }
          select.size = 1;
        }
        break;

      case "Escape":
        if (isDropdownOpen) {
          event.preventDefault();
          // Close dropdown without changing selection
          setIsDropdownOpen(false);
          select.size = 1;
        }
        break;

      default:
        return;
    }
  };

  /**
   * Renders a select input component with error and help messages.
   */
  return (
    <div className="dropdown-container">
      <fieldset>
        <legend>Enter Search Parameters:</legend>
        {props.inputFields}

        <div className="parameter-row">
          <p>Change display type:</p>
          <select
            id="display-type-dropdown"
            className="dropdown"
            aria-label="Display type selection"
            tabIndex={6}
            onKeyDown={handleSelectKeyDown}
            ref={selectRef}
          >
            <option
              aria-label="table option"
              style={{ fontSize: "16px !important" }}
            >
              Table
            </option>
            <option aria-label="bar option">Bar Graph</option>
            <option aria-label="line option">Line Graph</option>
          </select>
          <button
            onClick={handleSubmit}
            aria-label="submit"
            tabIndex={7}
            className="submit-button"
            id="submit-button"
          >
            Submit
          </button>
        </div>

        <div className="parameter-row" tabIndex={-1}>
          <button
            aria-label="Display CSV Table Options"
            onClick={() => {
              props.setDisplayType("Table");
              props.setHasHeader(true);
              props.setTable(getAvailableDataTable());
            }}
            tabIndex={8}
          >
            Display Available CSV Dataset Options
          </button>

          <button
            aria-label="Display Keyboard Use Instructions"
            onClick={() => {
              outputHelp(MESSAGES.keyboardInstructions);
            }}
            tabIndex={9}
          >
            Display Keyboard Use Instructions
          </button>
        </div>
      </fieldset>

      {errorMessage && (
        <p
          id="error-message"
          className="error-message"
          aria-label={errorMessage}
        >
          {errorMessage}
        </p>
      )}
      {helpMessage && (
        <div
          className="help-message"
          id="help-message"
          aria-label="Accessibility instructions"
        >
          <p>
            - To activate keyboard navigation in search parameters, press tab.
          </p>
          <p>- To focus on bar graph or line graph, press tab.</p>
          <p>
            - To submit specified parameters with submit button, press space or
            enter after focusing on submit button with tab.
          </p>
          <p>
            {" "}
            - To turn header on or off, press space or enter after focusing on
            header button with tab{" "}
          </p>
          <p>
            - To activate keyboard navigation in Table, press Control+T. Press W
            to go up a cell, D to move to left of a cell, S to move down a cell,
            and A to move right of a cell. Press ESC before switching to another
            dataset.
          </p>
          <p>
            - To navigate through the 'Change display type' dropdown menu, press
            the down arrow key and it will open. Up and down arrow keys can be
            used to navigate through the options. Press enter or escape when you
            have chosen an option.
          </p>
        </div>
      )}
    </div>
  );
}
