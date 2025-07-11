package eu.efti.eftigate.testsupport;

import java.time.LocalDate;
import java.util.Random;

public class TestData {

    private static final long defaultSeed = LocalDate.now().getDayOfMonth() % 4;

    private static final Random random = new Random();

    public static long resetSeed() {
        random.setSeed(defaultSeed);
        return defaultSeed;
    }
}
