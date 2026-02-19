package com.hansenjc;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.stream.IntStream;

public class Main {
    public static final String INPUT = "input.txt";

    public static final double f_c = 20.d;  // center/carrier frequency
    public static final double f_s = 100.d; // signal frequency
    public static final double t_s = (1 / f_s); // sampling interval

    public static final int N = 3000; // samples

    public static final List<Integer> h = List.of(1); // channel impulse response, no multipath so only 1 element

    public static void main(String[] args) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(INPUT));

        DMatrixRMaj vec_input = new DMatrixRMaj(reader.lines()
                .mapToDouble(Double::parseDouble)
                .toArray());

        DMatrixRMaj vec_raw_I = new DMatrixRMaj();
        DMatrixRMaj vec_raw_Q = new DMatrixRMaj();
        downConvert(vec_input, vec_raw_I, vec_raw_Q);

//        System.out.println(vec_raw_I);
//        System.out.println(vec_raw_Q);

        filter();
        downSample();
        correlate();
        demodulate();
        asciiToText();
        errorCorrection();
    }

    private static double sin(int n) {
        return Math.sin(2 * Math.PI * f_c * t_s * n);
    }

    private static double cos(int n) {
        return Math.cos(2 * Math.PI * f_c * t_s * n);
    }

    private static void downConvert(DMatrixRMaj vec_input, DMatrixRMaj vec_raw_I, DMatrixRMaj vec_raw_Q) {
        // in-phase
        DMatrixRMaj vec_I = new DMatrixRMaj(IntStream.range(0, N)
                .mapToDouble(Main::cos)
                .toArray());

        // quadrature
        DMatrixRMaj vec_Q = new DMatrixRMaj(IntStream.range(0, N)
                .mapToDouble(Main::sin)
                .toArray());

        CommonOps_DDRM.elementMult(vec_input, vec_I, vec_raw_Q);
        CommonOps_DDRM.elementMult(vec_input, vec_Q, vec_raw_Q);
    }

    private static void filter() {

    }

    private static void downSample() {

    }

    private static void correlate() {

    }

    private static void demodulate() {

    }

    private static void asciiToText() {
    }

    private static void errorCorrection() {

    }
}