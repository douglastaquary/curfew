package curfew.learn;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class DeployGuideTest {

    @Test
    void accessKeyProfileIsUsedAsIs() throws Exception {
        String guide = Files.readString(Path.of("docs/deploy.md"));
        String skill = Files.readString(Path.of(".cursor/skills/curfew-deploy/SKILL.md"));
        String config = Files.readString(Path.of("samconfig.toml"));
        String combined = guide + "\n" + skill;

        assertTrue(combined.contains("Profile 'default' is already configured with Access Key credentials"));
        assertTrue(combined.contains("Não apague a credencial do perfil default"));
        assertTrue(combined.contains("aws sts get-caller-identity"));
        assertTrue(skill.contains("Não rode `aws login`"));
        assertTrue(skill.contains("curfew-leitura"));
        assertTrue(skill.contains("iam:ListAccessKeys"));
        assertFalse(config.contains("GateSecret"));
        assertFalse(guide.contains("é `aws login`"));
    }
}
