import { Dispatch, SetStateAction } from "react";
import "../../../styles/game.css";
import { IngredientImage } from "../Game";

interface PlateProps {
  plate1Items: IngredientImage[];
  setPlate1Items: Dispatch<SetStateAction<IngredientImage[]>>;
  plate2Items: IngredientImage[];
  setPlate2Items: Dispatch<SetStateAction<IngredientImage[]>>;
  plate3Items: IngredientImage[];
  setPlate3Items: Dispatch<SetStateAction<IngredientImage[]>>;
}

export function Plate(props: PlateProps) {
  return (
    <div className="plate-container">
      <p className="section-text">Plate</p>
      <div className="tray-background">
        <div
          className="plate-section plate1"
          role="list"
          aria-label="Plate 1 ingredients"
        >
          {props.plate1Items.map((ing, i) => (
            <img
              key={ing.imgName}
              src={ing.imgStr}
              alt={`${
                ing.imgStr
                  .split("/")
                  .pop()
                  ?.split("?")[0]
                  .replace(/\.\w+$/, "")
                  .replace(/[-_]/g, " ") || "ingredient"
              }`}
              role="listitem"
              style={{
                zIndex: props.plate1Items.length - i,
                position: "relative",
              }}
            />
          ))}
        </div>
        <div
          className="plate-section plate2"
          role="list"
          aria-label="Plate 2 ingredients"
        >
          {props.plate2Items.map((ing, i) => (
            <img
              key={ing.imgName}
              src={ing.imgStr}
              alt={`${
                ing.imgStr
                  .split("/")
                  .pop()
                  ?.split("?")[0]
                  .replace(/\.\w+$/, "")
                  .replace(/[-_]/g, " ") || "ingredient"
              }`}
              role="listitem"
              style={{
                zIndex: props.plate2Items.length - i,
                position: "relative",
              }}
            />
          ))}
        </div>
        <div
          className="plate-section plate3"
          role="list"
          aria-label="Plate 3 ingredients"
        >
          {props.plate3Items.map((ing, i) => (
            <img
              key={ing.imgName}
              src={ing.imgStr}
              alt={`${
                ing.imgStr
                  .split("/")
                  .pop()
                  ?.split("?")[0]
                  .replace(/\.\w+$/, "")
                  .replace(/[-_]/g, " ") || "ingredient"
              }`}
              role="listitem"
              style={{
                zIndex: props.plate3Items.length - i,
                position: "relative",
              }}
            />
          ))}
        </div>
      </div>
    </div>
  );
}
