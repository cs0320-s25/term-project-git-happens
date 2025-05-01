import { useState, useEffect } from "react";

const FONTSIZE_SCALE: number = 2.5;

export function useResponsiveSettings() {
  // Track font size for responsiveness
  const [fontSize, setFontSize] = useState(getFontSize());

  // Track dark mode state
  const [useDarkMode, setUseDarkMode] = useState(
    window.matchMedia("(prefers-color-scheme: dark)").matches
  );

  // based on https://stackoverflow.com/questions/19014250/rerender-view-on-browser-resize-with-react
  // Listen for window resizes
  useEffect(() => {
    function handleResize() {
      setFontSize(getFontSize()); // Update font size in state
    }

    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  // based on https://stackoverflow.com/questions/56393880/how-do-i-detect-dark-mode-using-javascript
  // Listen for dark mode changes
  useEffect(() => {
    function handleColorSchemeChange(event: MediaQueryListEvent) {
      setUseDarkMode(event.matches);
    }

    const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
    mediaQuery.addEventListener("change", handleColorSchemeChange);

    return () => {
      mediaQuery.removeEventListener("change", handleColorSchemeChange);
    };
  }, []);

  return { fontSize, useDarkMode };
}

// we use this to have fontsize here equivalent to using vh/vw in CSS
function getFontSize(): number {
  const vw: number = window.innerWidth / 100;
  const vh: number = window.innerHeight / 100;
  return Math.min(FONTSIZE_SCALE * vw, FONTSIZE_SCALE * vh);
}

export function generateBackgroundColors(barNumber: number): string[] {
  const colorOptions: string[] = ["red", "green", "blue"];
  let colors: string[] = colorOptions;
  while (colors.length < barNumber) {
    colors = colors.concat(colorOptions);
  }
  while (colors.length > barNumber) {
    colors.pop();
  }
  return colors;
}

export function makeOptions(
  fontSize: number,
  dataLabels: string[],
  xAxisName: string,
  yAxisName: string,
  useDarkMode: boolean
) {
  return {
    scales: {
      x: {
        ticks: {
          color: useDarkMode ? "white" : "black",
          font: { size: fontSize },
          // based on https://stackoverflow.com/questions/44396737/how-to-shorten-chart-js-label
          // truncate label lengths (full label still appears in tooltip on hover)
          callback: function (_value: string | number, index: number) {
            const label: string = dataLabels[index];
            if (label.length > 6) {
              return label.substring(0, 4) + "...";
            } else {
              return label;
            }
          },
          // notable design choice - up for consideration (when the table is big, do
          // we still want to show all of the labels?)
          autoSkip: false,
        },
        title: {
          display: true,
          text: xAxisName,
          color: useDarkMode ? "white" : "black",
          font: { size: fontSize },
        },
      },
      y: {
        ticks: {
          color: useDarkMode ? "white" : "black",
          font: { size: fontSize },
        },
        title: {
          display: true,
          text: yAxisName,
          color: useDarkMode ? "white" : "black",
          font: { size: fontSize },
        },
      },
    },
    plugins: {
      legend: {
        display: false,
      },
    },
  };
}
