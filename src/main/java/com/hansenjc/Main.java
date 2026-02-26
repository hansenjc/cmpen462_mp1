package com.hansenjc;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.jtransforms.fft.DoubleFFT_1D;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Main {
    public static final String INPUT = "input.txt";
    public static final String PREAMBLE = "preamble.txt";

    public static final double f_c = 20.d;      // center/carrier frequency
    public static final double f_s = 100.d;     // sampling frequency
    public static final double t_s = (1 / f_s); // sampling interval

    public static final int N = 3000; // samples

    public static void main(String[] args) throws FileNotFoundException {
        DMatrixRMaj vec_input = new DMatrixRMaj(new BufferedReader(new FileReader(INPUT))
                .lines()
                .mapToDouble(Double::parseDouble)
                .toArray());

        List<String> preamble = new BufferedReader(new FileReader(PREAMBLE)).lines().toList();

        Pattern pattern = Pattern.compile("(-?\\d\\.\\d{1,6})([-+]\\d\\.\\d{1,6})i");
        int i = 0;
        double[] vec_preamble = new double[2 * preamble.size()];
        for (String complex : preamble) {
            Matcher matcher = pattern.matcher(complex);
            if (matcher.find()) {
                vec_preamble[i] = Double.parseDouble(matcher.group(1));
                vec_preamble[i + 1] = Double.parseDouble(matcher.group(2));
            } else {
                throw new IllegalArgumentException("Invalid Complex number in preamble: " + complex);
            }
            i += 2;
        }

        DMatrixRMaj vec_raw_I = new DMatrixRMaj(N);
        DMatrixRMaj vec_raw_Q = new DMatrixRMaj(N);

        downConvert(vec_input, vec_raw_I, vec_raw_Q);

        double[] vec_filtered_I_Q = lowPassFilter(vec_raw_I, vec_raw_Q);

        double[] vec_downsampled_I_Q = downSample(vec_filtered_I_Q);

        int msg_start = crossCorrelate(vec_downsampled_I_Q, vec_preamble);
        System.out.printf("Message at: %d\n", msg_start);

        byte[] fourbits = demodulate(vec_downsampled_I_Q, msg_start);

        System.out.println(asciiToText(fourbits));
    }

    private static double cos(int n) {
        return Math.cos(2 * Math.PI * f_c * t_s * n);
    }

    private static double sin(int n) {
        return Math.sin(2 * Math.PI * f_c * t_s * n);
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

    private static double[] lowPassFilter(DMatrixRMaj vec_raw_I, DMatrixRMaj vec_raw_Q) {
        double[] vec_raw_I_Q = new double[2 * N];
        for (int i = 0; i < N; i++) {
            vec_raw_I_Q[2 * i] = vec_raw_I.get(i);
            vec_raw_I_Q[2 * i + 1] = vec_raw_Q.get(i);
        }

        // DFT separately
        DoubleFFT_1D fft = new DoubleFFT_1D(N);
        fft.complexForward(vec_raw_I_Q);

        // zero out frequencies outside +/- 5.1 Hz
        for (int i = 0; i < N; i++) {
            double freq = (i <= N / 2 ? i : i - N) * f_s / N; // frequency of the bin index i
            if (Math.abs(freq) > 5.1) {
                vec_raw_I_Q[2 * i] = 0;
                vec_raw_I_Q[2 * i + 1] = 0;
            }
        }

        // iFFT
        fft.complexInverse(vec_raw_I_Q, true);

        return vec_raw_I_Q;
    }

    private static double[] downSample(double[] vec) {
        final int symbol_rate = 10;
        final int n = vec.length / 2;
        assert (n % symbol_rate == 0);

        double[] down_sampled = new double[vec.length / symbol_rate];

        int j = 0;

        for (int i = 0; i < n; i += symbol_rate) {
            down_sampled[2 * j] = vec[2 * i];
            down_sampled[2 * j + 1] = vec[2 * i + 1];
            j++;
        }

        return down_sampled;
    }

    /**
     * Cross Correlate in the time domain
     * https://en.wikipedia.org/wiki/Cross-correlation#Cross-correlation_of_deterministic_signals
     */
    private static int crossCorrelate(double[] vec_data, double[] vec_preamble) {
        final int n = vec_data.length / 2;
        final int m = vec_preamble.length / 2;
        final int size = n - m + 1;

        double max_magnitude = -1;
        int msg_start = 0;

        for (int i = 0; i < size; i++) {
            double sum_real = 0;
            double sum_imaginary = 0;
            for (int j = 0; j < m; j++) {
                double data_real = vec_data[2 * (i + j)];
                double data_imaginary = vec_data[2 * (i + j) + 1];
                double preamble_real = vec_preamble[2 * j];
                double preamble_imaginary = vec_preamble[2 * j + 1];
                // complex conjugate of preamble negates i^2 = -1
                //                                    v
                sum_real += data_real * preamble_real + data_imaginary * preamble_imaginary;
                sum_imaginary += data_imaginary * preamble_real - data_real * preamble_imaginary;
            }

            double magnitude = sum_real * sum_real + sum_imaginary * sum_imaginary;
            if (magnitude > max_magnitude) {
                max_magnitude = magnitude;
                msg_start = i;
            }
        }

        return msg_start;
    }

    private static byte[] demodulate(double[] vec_data, int s) {
        final int n = vec_data.length / 2;

        byte[] fourbits = new byte[n - s];
        for (int i = s; i < n; i++) {
            fourbits[i - s] = new QAM16(vec_data[2 * i], vec_data[2 * i + 1]).demodulate();
        }

        return fourbits;
    }

    private static String asciiToText(byte[] fourbits) {
        // or 4 bits into a char
        StringBuilder string = new StringBuilder(fourbits.length / 2);
        for (int i = 0; i < fourbits.length; i += 2) {
            string.append((char) (0xFF & ((fourbits[i] & 0x0F) << 4 | (fourbits[i + 1] & 0x0F))));
        }

        return string.toString();
    }
}