package io.github.pellse.reactive.assembler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.pellse.reactive.assembler.MapFactory.defaultMapFactory;

public interface RuleContext<ID, IDC extends Collection<ID>, R, RRC> {

    Function<R, ID> idExtractor();

    Supplier<IDC> idCollectionFactory();

    MapFactory<ID, RRC> mapFactory();

    record DefaultRuleContext<ID, IDC extends Collection<ID>, R, RRC>(
            Function<R, ID> idExtractor,
            Supplier<IDC> idCollectionFactory,
            MapFactory<ID, RRC> mapFactory) implements RuleContext<ID, IDC, R, RRC> {
    }

    static <ID, R, RRC> RuleContext<ID, List<ID>, R, RRC> ruleContext(Function<R, ID> idExtractor) {
        return ruleContext(idExtractor, ArrayList::new);
    }

    static <ID, IDC extends Collection<ID>, R, RRC> RuleContext<ID, IDC, R, RRC> ruleContext(
            Function<R, ID> idExtractor,
            Supplier<IDC> idCollectionFactory) {
        return ruleContext(idExtractor, idCollectionFactory, defaultMapFactory());
    }

    static <ID, IDC extends Collection<ID>, R, RRC> RuleContext<ID, IDC, R, RRC> ruleContext(
            Function<R, ID> idExtractor,
            Supplier<IDC> idCollectionFactory,
            MapFactory<ID, RRC> mapFactory) {
        return new DefaultRuleContext<>(idExtractor, idCollectionFactory, mapFactory);
    }
}
