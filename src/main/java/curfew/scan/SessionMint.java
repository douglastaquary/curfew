package curfew.scan;

import java.util.Optional;

@FunctionalInterface
public interface SessionMint {

    Optional<Minted> mint(int hours);
}
