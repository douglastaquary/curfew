package curfew.scan;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Linha de acompanhamento. Leva sistema, idade, dono e ação. Não leva segredo.
 */
public final class ScanLog {

    public static final ZoneId CLOCK = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(CLOCK);

    private ScanLog() {
    }

    public static String clock(java.time.Instant instant) {
        return TIME.format(instant);
    }

    public static String line(ClassifiedAccess item, String clock) {
        return clock + "  " + item.system()
                + " · " + item.kindLabel()
                + " · aberto há " + item.ageDays() + " dias"
                + " · " + item.owner()
                + " · " + item.action();
    }
}
