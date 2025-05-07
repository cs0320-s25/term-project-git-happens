package edu.brown.cs.student.main.server.mergeHelpers;

public class MockFileObject extends Object {
private String imgStr;
private String imgName;
public MockFileObject(String imgStr, String imgName) {
  this.imgStr = imgStr;
  this.imgName = imgName;
}

public String getImgStr() {
  String imgStrCopy = imgStr;
  return imgStrCopy;
}

public String getImgName() {
  String imgNameCopy = imgName;
  return imgNameCopy;
}

  @Override
  public boolean equals(Object obj) {
  if (obj == null) {
    return false;
  }
  return (((MockFileObject) obj).imgStr.equals(imgStr) && ((MockFileObject) obj).imgName.equals(imgName));
  }

  @Override
  public int hashCode() {
  return imgStr.hashCode() * 31 + imgName.hashCode();
  }
}
