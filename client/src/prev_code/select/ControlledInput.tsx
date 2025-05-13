// taken from REPL gearup

import "../../styles/main.css";
import { Dispatch, SetStateAction } from "react";

/**
 * Interface for the properties of the ControlledInput component.
 */
/**
 * @param {string} value - The current value of the input.
 * @param {Dispatch<SetStateAction<string>>} setValue - A function to update the value of the input.
 * @param {string} ariaLabel - The aria-label for accessibility.
 * @param {string} placeholder - The placeholder text for the input.
 * @param {string} [className] - Optional CSS class name for styling.
 * @param {string} [id] - Optional ID for the input element.
 */
/**
 * A controlled input component that manages its own state.
 *
 * @component
 * @param {ControlledInputProps} props - The properties for the ControlledInput component.
 * @returns {JSX.Element} A controlled input element.
 */

// Remember that parameter names don't necessarily need to overlap;
// I could use different variable names in the actual function.

interface ControlledInputProps {
  value: string;
  // This type comes from React+TypeScript. VSCode can suggest these.
  //   Concretely, this means "a function that sets a state containing a string"
  setValue: Dispatch<SetStateAction<string>>;
  ariaLabel: string;
  placeholder: string;
  className?: string;
  id?: string;
  tabIndex?: number;
}

// Input boxes contain state. We want to make sure React is managing that state,
//   so we have a special component that wraps the input box.
export function ControlledInput({
  value,
  setValue,
  ariaLabel,
  placeholder,
  className,
  id,
  tabIndex,
}: ControlledInputProps) {
  return (
    <input
      type="text"
      className={className ? className : "default-command-box"}
      value={value}
      placeholder={placeholder}
      onChange={(ev) => setValue(ev.target.value)}
      aria-label={ariaLabel}
      id={id}
      tabIndex={tabIndex}
    ></input>
  );
}