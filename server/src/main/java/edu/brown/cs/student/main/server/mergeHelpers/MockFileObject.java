package edu.brown.cs.student.main.server.mergeHelpers;

import java.util.Objects;

public record MockFileObject(String imgStr, String imgName) {
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    MockFileObject other = (MockFileObject) obj;
    return imgStr.equals(other.imgStr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(imgStr, imgName);
  }
}
