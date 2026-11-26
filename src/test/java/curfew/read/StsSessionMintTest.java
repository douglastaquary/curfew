package curfew.read;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StsSessionMintTest {

    @Test
    void blankKeysDoNotCallTheAccount() {
        assertEquals(82800, StsSessionMint.seconds(23));
        assertEquals(3600, StsSessionMint.seconds(1));
        assertEquals(-1, StsSessionMint.seconds(24));
        StsSessionMint mint = new StsSessionMint();
        assertTrue(mint.mint("", "presente", 23).isEmpty());
        assertTrue(mint.mint("presente", "", 23).isEmpty());
        assertTrue(mint.mint("presente", "presente", 24).isEmpty());
    }
}
