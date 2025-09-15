/** @type {import('@commitlint/types').UserConfig} */
const config = {
  extends: ["@commitlint/config-conventional"],
  rules: {
    "type-enum": [
      2,
      "always",
      ["feat", "fix", "docs", "refactor", "perf", "test", "chore", "revert", "build", "ci", "wip"],
    ],
    "body-max-line-length": [
      2, // Set the maximum line length for the body to 100 characters
      "always",
      300, // Set the maximum line length for the body to 300 characters
    ],
  },
};

export default config;
