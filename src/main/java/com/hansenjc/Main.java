package com.hansenjc;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.jtransforms.fft.DoubleFFT_1D;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.stream.IntStream;

public class Main {
    public static final String INPUT = "input.txt";

    public static final double f_c = 20.d;      // center/carrier frequency
    public static final double f_s = 100.d;     // sampling frequency
    public static final double t_s = (1 / f_s); // sampling interval

    public static final int N = 3000; // samples

    public static final List<Integer> h = List.of(1); // channel impulse response, no multipath so only 1 element

    public static void main(String[] args) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(INPUT));

        DMatrixRMaj vec_input = new DMatrixRMaj(reader.lines()
                .mapToDouble(Double::parseDouble)
                .toArray());

        DMatrixRMaj vec_raw_I = new DMatrixRMaj(N);
        DMatrixRMaj vec_raw_Q = new DMatrixRMaj(N);

        downConvert(vec_input, vec_raw_I, vec_raw_Q);

        double[] filtered_I_Q = filter(vec_raw_I, vec_raw_Q);

        // 4. Perform the Inverse FFT to get back to the time domain
//        fft.complexInverse(fftData, true); // 'true' scales the result correctly

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

        CommonOps_DDRM.elementMult(vec_input, vec_I, vec_raw_I);
        CommonOps_DDRM.elementMult(vec_input, vec_Q, vec_raw_Q);
    }

    private static double[] filter(DMatrixRMaj vec_raw_I, DMatrixRMaj vec_raw_Q) {
        double[] vec_raw_I_Q = new double[2 * N];
        for (int i = 0; i < N; i++) {
            vec_raw_I_Q[2 * i] = vec_raw_I.get(i);
            vec_raw_I_Q[2 * i + 1] = vec_raw_Q.get(i);
        }

        // DFT
        DoubleFFT_1D fft = new DoubleFFT_1D(N);
        fft.complexForward(vec_raw_I_Q);

        // zero out frequencies outside +/- 5.1 Hz
        for (int i = 0; i < N; i++) {
            double freq = (i <= N / 2 ? i : i - N) * f_s / N;
            if (Math.abs(freq) > 5.1) {
                vec_raw_I_Q[2 * i] = 0;
                vec_raw_I_Q[2 * i + 1] = 0;
            }
        }

        return vec_raw_I_Q;
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