package curfew.store;

import java.util.List;
import java.util.Optional;

import curfew.scan.ClassifiedAccess;
import curfew.scan.HumanDecision;

public interface AccessStore {

    List<ClassifiedAccess> list();

    Optional<ClassifiedAccess> find(String id);

    void replace(List<ClassifiedAccess> items);

    void remember(HumanDecision decision);

    List<HumanDecision> decisions();

    void forgetDecisions();

    void rememberTarget(String id, String target);

    Optional<String> target(String id);
}
