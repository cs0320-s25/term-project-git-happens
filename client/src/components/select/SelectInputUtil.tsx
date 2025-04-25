import { Dispatch, SetStateAction, useState, useEffect } from "react";

export interface SelectInputProps {
  table: string[][] | null;
  setTable: Dispatch<SetStateAction<string[][] | null>>;
  displayType: string;
  setDisplayType: Dispatch<SetStateAction<string>>;
  hasHeader: boolean;
  setHasHeader: Dispatch<SetStateAction<boolean>>;
}

export function useErrorAndHelpHooks() {
  const [errorMessage, setErrorMessage] = useState<string>("");
  const [errorTrigger, setErrorTrigger] = useState<number>(0);
  const [helpMessage, setHelpMessage] = useState<string>("");
  const [helpTrigger, setHelpTrigger] = useState<number>(0);

  useEffect(() => {
    if (errorMessage) {
      const errorElement = document.getElementById("error-message");
      errorElement?.focus();
    }
  }, [errorTrigger]);

  useEffect(() => {
    if (helpMessage) {
      const helpElement = document.getElementById("help-message");
      helpElement?.focus();
    }
  }, [helpTrigger]);

  function outputError(message: string) {
    setErrorMessage(message);
    setErrorTrigger((prev) => prev + 1);
  }

  function outputHelp(message: string) {
    setHelpMessage(message);
    setHelpTrigger((prev) => prev + 1);
  }

  function removeError() {
    setErrorMessage("");
  }

  function removeHelp() {
    setHelpMessage("");
  }

  return {
    errorMessage,
    helpMessage,
    outputError,
    outputHelp,
    removeError,
    removeHelp,
  };
}

export function validateDataForGraph(data: string[][] | null): boolean {
  if (!data) return false;
  return data
    .slice(1)
    .every((row) => row.length === 2 && !isNaN(Number(row[1])));
}

export function isTableFormat(value: any): value is string[][] {
  return (
    Array.isArray(value) &&
    value.length > 0 &&
    value.every(
      (row) =>
        Array.isArray(row) && row.every((cell) => typeof cell === "string")
    )
  );
}
