package curfew.scan;

import java.util.List;

/**
 * Origem da fila. A implementação devolve metadado, nunca o valor de um segredo.
 */
public interface AccessSource {

    List<RawAccess> list();
}
