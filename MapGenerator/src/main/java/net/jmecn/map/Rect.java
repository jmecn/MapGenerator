package net.jmecn.map;

public class Rect {
	public int x, y, width, height;

	public Rect() {
		x = y = width = height = 0;
	}

	public Rect(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	private boolean valueInRange(int value, int min, int max) {
		return (value <= max) && (value >= min);
	}

	public boolean contains(Point p) {
		return valueInRange(p.x, x, x + width) && valueInRange(p.y, y, y + height);
	}
	
	public boolean overlap(Rect B) {
		boolean xOverlap = valueInRange(x, B.x, B.x + B.width) || valueInRange(B.x, x, x + width);

		boolean yOverlap = valueInRange(y, B.y, B.y + B.height) || valueInRange(B.y, y, y + height);

		return xOverlap && yOverlap;
	}

    public int centerX(){
        return x + (width / 2);
    }

    public int centerY(){
        return y + (height / 2);
    }
}