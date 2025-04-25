import { expect, test, defineConfig } from "@playwright/test";

// /**
//   The general shapes of tests in Playwright Test are:
//     1. Navigate to a URL
//     2. Interact with the page
//     3. Assert something about the page against your expectations
//   Look for this pattern in the tests below!
//  */

export default defineConfig({
  webServer: [
    {
      command: "npm run start",
      url: "http://localhost:8000/",
      timeout: 120 * 1000,
      reuseExistingServer: !process.env.CI,
    },
    {
      command: "npm run backend",
      url: "http://localhost:3232",
      timeout: 120 * 1000,
      reuseExistingServer: !process.env.CI,
    },
  ],
  use: {
    baseURL: "http://localhost:8000/",
  },
});

test.beforeEach(async ({ page }) => {
  await page.goto("http://localhost:8000/");
});