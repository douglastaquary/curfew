package curfew.read;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import curfew.scan.RawAccess;

class MetadataInventoryTest {

    private static final Instant NOW = Instant.parse("2026-11-25T12:00:00Z");

    @Test
    void dropsUrlKeyMaterialAndSecretValues() {
        List<RawAccess> hooks = MetadataInventory.hooks("""
                [{"id":9,"created_at":"2026-11-15T12:00:00Z","events":["push"],
                  "config":{"url":"https://hooks.example/in?token=ghp_supersecret"}}]
                """, NOW);
        List<RawAccess> secrets = MetadataInventory.secretNames("""
                {"secrets":[
                  {"name":"DEPLOY_TOKEN","created_at":"2026-11-01T12:00:00Z"},
                  {"name":"ghp_supersecret","created_at":"2026-11-01T12:00:00Z"}]}
                """, NOW);
        List<RawAccess> keys = MetadataInventory.deployKeys("""
                [{"id":4,"title":"agente","read_only":false,"created_at":"2026-11-20T12:00:00Z",
                  "key":"ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIexample"}]
                """, NOW);
        List<RawAccess> aws = MetadataInventory.accessKeys("""
                {"AccessKeyMetadata":[
                  {"AccessKeyId":"AKIAIOSFODNN7EXAMPLE","Status":"Active",
                   "CreateDate":"2026-11-11T12:00:00Z","UserName":"agente"},
                  {"AccessKeyId":"AKIAIOSFODNN7EXAMPL2","Status":"Inactive",
                   "CreateDate":"2026-11-11T12:00:00Z","UserName":"velho"}]}
                """, NOW);

        String printed = hooks.toString() + secrets + keys + aws;
        assertFalse(printed.contains("ghp_"));
        assertFalse(printed.contains("AKIA"));
        assertFalse(printed.contains("ssh-ed25519"));
        assertFalse(printed.contains("hooks.example"));

        assertEquals("gh-hook-9", hooks.getFirst().id());
        assertEquals(10, hooks.getFirst().ageDays());
        assertEquals("DEPLOY_TOKEN", secrets.getFirst().owner());
        assertEquals(1, secrets.size());
        assertTrue(keys.getFirst().wideScope());
        assertEquals("aws-key-mple", aws.getFirst().id());
        assertEquals("agente", aws.getFirst().owner());
        assertEquals(1, aws.size());
    }
}
