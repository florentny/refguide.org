package us.florent;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import java.util.logging.Logger;


import static org.junit.jupiter.api.Assertions.*;

class genReef35Test {

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n");
    }
    private final static Logger log = Logger.getLogger(genReef35Test.class.getName());
    genReef35 r;

    @BeforeAll
    static void setup() {
        log.info("@BeforeAll - executes once before all test methods in this class");
    }

    @BeforeEach
    void init() {
        r = new genReef35();
        log.info("@BeforeEach - executes before each test method in this class");
    }

    @Test
    void buildDB() {
        assertTrue(true);
    }

    @Test
    void depthmetric() {
        int m = r.depthmetric(100);
        assertEquals(30, m);
        assertEquals(18, r.depthmetric(60));
    }
}