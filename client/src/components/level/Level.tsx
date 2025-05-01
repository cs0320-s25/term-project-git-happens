import React, { useState } from "react";

const ingredientsList = [
  { id: "top-bun", name: "Top Bun", type: "bun", variant: "sesame" },
  { id: "bottom-bun", name: "Bottom Bun", type: "bun", variant: "plain" },
  { id: "patty", name: "Patty", type: "meat" },
];

const desiredOrder = ["top-bun", "bottom-bun"];

export function Level() {
  const [workspace, setWorkspace] = useState<string[]>([]);
  const [plate, setPlate] = useState<string[]>([]);

  const addToWorkspace = (id: string) => {
    if (!workspace.includes(id)) {
      setWorkspace([...workspace, id]);
    }
  };

  const addToPlate = (id: string) => {
    if (workspace.includes(id) && !plate.includes(id)) {
      setPlate([...plate, id]);
    }
  };

  return (
    <div className="flex w-full h-screen bg-gray-100">
      {/* Left Panel - Game Area */}
      <div className="w-1/2 p-4 bg-white border-r border-gray-300 flex flex-col gap-4">
        <h1 className="text-2xl font-bold text-red-500">GitHappens</h1>

        <div className="border p-2">
          <h2 className="font-semibold">Desired Order</h2>
          <div className="flex gap-2">
            {desiredOrder.map((id) => (
              <div key={id} className="p-2 border rounded bg-yellow-100">
                {ingredientsList.find((i) => i.id === id)?.name}
              </div>
            ))}
          </div>
        </div>

        <div className="border p-2">
          <h2 className="font-semibold">Ingredients</h2>
          <div className="flex gap-2 overflow-x-auto">
            {ingredientsList.map((ingredient) => (
              <button
                key={ingredient.id}
                onClick={() => addToWorkspace(ingredient.id)}
                className="bg-green-200"
              >
                {ingredient.name}
              </button>
            ))}
          </div>
        </div>

        <div className="border p-2">
          <h2 className="font-semibold">Workspace</h2>
          <div className="flex gap-2">
            {workspace.map((id) => (
              <button
                key={id}
                onClick={() => addToPlate(id)}
                className="bg-blue-100"
              >
                {ingredientsList.find((i) => i.id === id)?.name}
              </button>
            ))}
          </div>
        </div>

        <div className="border p-2">
          <h2 className="font-semibold">Plate (Branch State)</h2>
          <div className="flex gap-2">
            {plate.map((id) => (
              <div key={id} className="p-2 border rounded bg-orange-100">
                {ingredientsList.find((i) => i.id === id)?.name}
              </div>
            ))}
          </div>
        </div>

        <div className="border p-2">
          <h2 className="font-semibold">Commands</h2>
          <code className="block whitespace-pre">
            git checkout main
            {"\n"}
            git merge branch-1 -m "mb1: add buns"
          </code>
        </div>

        <button className="bg-gray-300 text-black w-fit">Serve</button>
      </div>

      {/* Right Panel - Branch Visualization */}
      <div className="w-1/2 p-4 bg-gray-400 flex gap-4">
        {[
          { name: "Branch 1", events: ["branch created", "feat: add buns"] },
          { name: "Main", events: ["initial commit", "mb1: add buns"] },
          { name: "Branch 2", events: ["branch created"] },
        ].map((branch) => (
          <div key={branch.name} className="flex-1 bg-white p-2">
            <h3 className="text-xl font-bold text-center">{branch.name}</h3>
            <ul className="mt-2">
              {branch.events.map((event, i) => (
                <li key={i} className="border-b py-1 text-center">
                  {event}
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </div>
  );
}
