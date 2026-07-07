package com.turing.model;

/**
 * Represents a 3-digit proposal code (Blue, Yellow, Purple) each ranging from 1 to 5.
 */
public class Code {
    private int blue;   // Triangle / A (1-5)
    private int yellow; // Square / B (1-5)
    private int purple; // Circle / C (1-5)

    public Code() {
    }

    public Code(int blue, int yellow, int purple) {
        setBlue(blue);
        setYellow(yellow);
        setPurple(purple);
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        if (blue < 1 || blue > 5) {
            throw new IllegalArgumentException("Blue digit must be between 1 and 5");
        }
        this.blue = blue;
    }

    public int getYellow() {
        return yellow;
    }

    public void setYellow(int yellow) {
        if (yellow < 1 || yellow > 5) {
            throw new IllegalArgumentException("Yellow digit must be between 1 and 5");
        }
        this.yellow = yellow;
    }

    public int getPurple() {
        return purple;
    }

    public void setPurple(int purple) {
        if (purple < 1 || purple > 5) {
            throw new IllegalArgumentException("Purple digit must be between 1 and 5");
        }
        this.purple = purple;
    }

    public int getDigit(char color) {
        switch (Character.toUpperCase(color)) {
            case 'B':
            case 'T': // Triangle
                return blue;
            case 'Y':
            case 'S': // Square
                return yellow;
            case 'P':
            case 'C': // Circle
                return purple;
            default:
                throw new IllegalArgumentException("Invalid digit color reference: " + color);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Code code = (Code) o;
        return blue == code.blue && yellow == code.yellow && purple == code.purple;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * blue + yellow) + purple;
    }

    @Override
    public String toString() {
        return "" + blue + yellow + purple;
    }
}
