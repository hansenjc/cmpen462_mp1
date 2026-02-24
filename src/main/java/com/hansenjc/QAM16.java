package com.hansenjc;

import java.util.List;

public class QAM16 {
    public static final QAM16 _0000 = new QAM16(3.D, 3.D, (byte) 0);
    public static final QAM16 _0001 = new QAM16(1.D, 3.D, (byte) 1);
    public static final QAM16 _0010 = new QAM16(-3.D, 3.D, (byte) 2);
    public static final QAM16 _0011 = new QAM16(-1.D, 3.D, (byte) 3);
    public static final QAM16 _0100 = new QAM16(3.D, 1.D, (byte) 4);
    public static final QAM16 _0101 = new QAM16(1.D, 1.D, (byte) 5);
    public static final QAM16 _0110 = new QAM16(-3.D, 1.D, (byte) 6);
    public static final QAM16 _0111 = new QAM16(-1.D, 1.D, (byte) 7);
    public static final QAM16 _1000 = new QAM16(3.D, -3.D, (byte) 8);
    public static final QAM16 _1001 = new QAM16(1.D, -3.D, (byte) 9);
    public static final QAM16 _1010 = new QAM16(-3.D, -3.D, (byte) 10);
    public static final QAM16 _1011 = new QAM16(-1.D, -3.D, (byte) 11);
    public static final QAM16 _1100 = new QAM16(3.D, -1.D, (byte) 12);
    public static final QAM16 _1101 = new QAM16(1.D, -1.D, (byte) 13);
    public static final QAM16 _1110 = new QAM16(-3.D, -1.D, (byte) 14);
    public static final QAM16 _1111 = new QAM16(-1.D, -1.D, (byte) 15);
    public static final List<QAM16> CONSTELLATION = List.of(_0000, _0001, _0010, _0011, _0100, _0101, _0110, _0111, _1000, _1001, _1010, _1011, _1100, _1101, _1110, _1111);

    double i;
    double q;
    byte b = 0;

    public QAM16(double i, double q) {
        this.i = i;
        this.q = q;
    }

    public QAM16(double i, double q, byte b) {
        this.i = i;
        this.q = q;
        this.b = b;
    }

    public double distance_squared(QAM16 other) {
        return Math.pow(other.i - this.i, 2) + Math.pow(other.q - this.q, 2);
    }

    public byte demodulate() {
        double min = 1000000000.D;
        QAM16 closest = null;
        for (QAM16 star : CONSTELLATION) {
            double d = this.distance_squared(star);
            if (d < min) {
                min = d;
                closest = star;
            }
        }
        assert closest != null;
        return closest.b;
    }

    @Override
    public String toString() {
        return String.format("[i:%.2f q:%.2f]: %s", i, q, Integer.toBinaryString(b));
    }
}
