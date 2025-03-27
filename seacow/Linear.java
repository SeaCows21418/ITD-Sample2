package seacow;

public class Linear {

    public double m;    // slope
    public double b;    // y-intercept

    public Double function(double x) {
        return m*x + b;
    }

    public Linear(double m, double b) {
        this.m = m;
        this.b = b;
    }

    public Linear() {
        this.m = 1.0;
        this.b = 0.0;
    }

    public Linear(Linear corr) {
        this.m = corr.m;
        this.b = corr.b;
    }

}
