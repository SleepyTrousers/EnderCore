package com.enderio.core.common.vecmath;

public class Vector2i {

  public int x;
  public int y;

  public Vector2i() {
    x = 0;
    y = 0;
  }

  public Vector2i(double x, double y) {
    this.x = (int) x;
    this.y = (int) y;
  }

  public Vector2i(float x, float y) {
    this.x = (int)x;
    this.y = (int)y;
  }
  
  public Vector2i(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Vector2i(Vector2d other) {
    this(other.x, other.y);
  }

  public Vector2i(Vector2f other) {
    this(other.x, other.y);
  }

  public void set(double x, double y) {
    this.x = (int) x;
    this.y = (int) y;
  }

  public void set(float x, float y) {
    this.x = (int)x;
    this.y = (int)y;
  }

  public void set(Vector2i vec) {
    x = vec.x;
    y = vec.y;
  }

  public void add(Vector2i vec) {
    x += vec.x;
    y += vec.y;
  }

  public void sub(Vector2i vec) {
    x -= vec.x;
    y -= vec.y;
  }

  public void negate() {
    x = -x;
    y = -y;
  }

  public void scale(int s) {
    x *= s;
    y *= s;
  } 

  public double lengthSquared() {
    return x * x + y * y;
  }

  public double length() {
    return Math.sqrt(lengthSquared());
  }

  public double distanceSquared(Vector2i v) {
    double dx, dy;
    dx = x - v.x;
    dy = y - v.y;
    return (dx * dx + dy * dy);
  }

  public double distance(Vector2i v) {
    return Math.sqrt(distanceSquared(v));
  }

  @Override
  public String toString() {
    return "Vector2i(" + x + ", " + y + ")";
  }

}
