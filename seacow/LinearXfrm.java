package seacow;

public class LinearXfrm {

    public Linear eqn;      // Linear equation of slope, y-intercept
    public double value;    // associated value


    public LinearXfrm(double value, double m, double b) {
        this.value = value;
        this.eqn = new Linear(m,b);
    }

}
