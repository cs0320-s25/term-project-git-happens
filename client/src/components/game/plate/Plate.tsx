import { Dispatch, SetStateAction, useState, useEffect } from "react";
import "../../../styles/game.css";
import { tray } from "../../../assets/images";
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
      <p>Plate</p>
      <div className="tray-background">
        <div className="plate-section plate1">
          {props.plate1Items.map((ing, i) => (
            <img
              key={ing.imgName}
              src={ing.imgStr}
              style={{
                zIndex: props.plate1Items.length - i,
                position: "relative",
              }}
            />
          ))}
        </div>
        <div className="plate-section plate2">
          {props.plate2Items.map((ing, i) => (
            <img
              key={ing.imgName}
              src={ing.imgStr}
              style={{
                zIndex: props.plate2Items.length - i,
                position: "relative",
              }}
            />
          ))}
        </div>
        <div className="plate-section plate3">
          {props.plate3Items.map((ing, i) => (
            <img
              key={ing.imgName}
              src={ing.imgStr}
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
