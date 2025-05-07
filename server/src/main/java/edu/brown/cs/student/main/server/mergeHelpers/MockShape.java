package edu.brown.cs.student.main.server.mergeHelpers;

public class MockShape extends Object {
private String type;
private String color;
public MockShape(String type, String color) {
  this.type = type;
  this.color = color;
}
  @Override
  public boolean equals(Object obj) {
  if (((MockShape) obj).type.equals(type) && ((MockShape) obj).color.equals(color)) {
    return true;
  }
  return false;
  }
}
