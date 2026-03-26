import { existsSync } from "node:fs";
import { spawn } from "node:child_process";
import path from "node:path";
import process from "node:process";

const [, , binName, ...args] = process.argv;

if (!binName) {
  console.error("Missing binary name. Example: node ./scripts/run-local-bin.mjs vite");
  process.exit(1);
}

const binDir = path.resolve(process.cwd(), "node_modules", ".bin");
const isWindows = process.platform === "win32";
const localBin = path.join(binDir, isWindows ? `${binName}.cmd` : binName);

if (!existsSync(localBin)) {
  console.error(`\nCannot find local binary: ${binName}`);
  console.error("Reason: frontend dependencies are not installed in the current project.");
  console.error(`Expected file: ${localBin}`);
  console.error("\nFix steps:");
  console.error("1. Open a terminal in the frontend directory");
  console.error("2. Run: npm install");
  console.error(`3. Then run: npm run ${binName === "vite" && args[0] ? args[0] : "dev"}`);
  console.error("\nIf npm install fails, check network / registry settings first.");
  process.exit(1);
}

const child = spawn(localBin, args, {
  stdio: "inherit",
  shell: isWindows,
});

child.on("exit", (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal);
    return;
  }
  process.exit(code ?? 0);
});

