package curfew.scan;

import java.util.ArrayList;
import java.util.List;

/**
 * A noite anota a ação já decidida pela regra. Produção pede um humano. Nada é fechado.
 */
public final class NightlyClose {

    public List<NightlyNote> note(List<ClassifiedAccess> items) {
        List<NightlyNote> notes = new ArrayList<>();
        for (ClassifiedAccess item : items) {
            notes.add(new NightlyNote(item.id(), item.action(), item.needsHuman()));
        }
        return List.copyOf(notes);
    }
}
