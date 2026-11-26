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

    @Test
    void packageRunsOnJava21() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"));
        String guide = Files.readString(Path.of("docs/deploy.md"));
        String skill = Files.readString(Path.of(".cursor/skills/curfew-deploy/SKILL.md"));

        assertTrue(pom.contains("<version>[21,)</version>"));
        assertTrue(guide.contains("NoSuchElementException"));
        assertTrue(guide.contains("./mvnw -version"));
        assertTrue(guide.contains("jbr-17"));
        assertTrue(guide.contains("temurin@21"));
        assertTrue(skill.contains("NoSuchElementException"));
        assertTrue(skill.contains("jbr-17"));
    }

    @Test
    void samNeedsPygments() throws Exception {
        String guide = Files.readString(Path.of("docs/deploy.md"));
        String skill = Files.readString(Path.of(".cursor/skills/curfew-deploy/SKILL.md"));

        assertTrue(guide.contains("No module named 'pygments'"));
        assertTrue(guide.contains("python3.12 -m pip install --break-system-packages pygments"));
        assertTrue(guide.contains("externally-managed-environment"));
        assertTrue(guide.contains("não rode `brew reinstall aws-sam-cli`"));
        assertTrue(guide.contains("SAM CLI, version 1.132.0"));
        assertTrue(guide.contains("https://f3nqvezln1.execute-api.us-east-1.amazonaws.com"));
        assertTrue(guide.contains("Internal Server Error"));
        assertTrue(skill.contains("sam logs -n CurfewFunction --stack-name curfew --region us-east-1"));
        assertTrue(skill.contains("No module named 'pygments'"));
        assertTrue(skill.contains("não rode `brew reinstall aws-sam-cli`"));
    }
}
