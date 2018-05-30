package io.github.thepun.pq;

final class MathUtil {

    static int nextGreaterPrime(int value) {
        do {
            value++;
        } while (!isPrime(value));

        return value;
    }

    static boolean isPrime(int n) {
        if (n % 2 == 0) {
            return false;
        }

        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }

        return true;
    }
}
