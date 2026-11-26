package curfew.read;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import curfew.scan.Minted;
import curfew.scan.TaskLease;

/**
 * Corpo único da API. A página não usa este texto.
 */
public final class SessionJson {

    private static final ObjectMapper JSON = new ObjectMapper();

    private SessionJson() {
    }

    public static String body(TaskLease lease, Minted minted) {
        if (lease == null) {
            return "";
        }
        ObjectNode node = JSON.createObjectNode();
        node.put("tarefa", lease.task());
        node.put("sistema", lease.system());
        node.put("horas", lease.hours());
        node.put("conta", minted == null ? "nao" : "sim");
        if (!lease.tail().isBlank()) {
            node.put("finais", lease.tail());
        }
        if (minted != null) {
            node.put("accessKeyId", minted.accessKeyId());
            node.put("secretAccessKey", minted.secretAccessKey());
            node.put("sessionToken", minted.sessionToken());
        }
        try {
            return JSON.writeValueAsString(node);
        } catch (JsonProcessingException exception) {
            return "";
        }
    }
}
