package com.template.states;

import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType;
import com.template.contracts.HouseTokenContract;
import com.template.contracts.HouseTokenContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@BelongsToContract(HouseTokenContract.class)
public class HouseTokenState extends EvolvableTokenType {

    private final BigDecimal valuation;
    private final UniqueIdentifier uid;
    private final String symbol;
    private final Party maintainer;
    private final int digit;

    public HouseTokenState(BigDecimal valuation, UniqueIdentifier uid, String symbol, Party maintainer, int digit) {
        this.valuation = valuation;
        this.uid = uid;
        this.symbol = symbol;
        this.maintainer = maintainer;
        this.digit = digit;
    }

    public BigDecimal getValuation() {
        return valuation;
    }

    public String getSymbol() {
        return symbol;
    }

    public Party getMaintainer() {
        return maintainer;
    }

    @Override
    public int getFractionDigits() {
        return this.digit;
    }

    @NotNull
    @Override
    public List<Party> getMaintainers() {
        return Arrays.asList(maintainer);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.uid;
    }
    public UniqueIdentifier getUniqueIdentifier() {
        return uid;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj ==null || getClass() != obj.getClass()) return false;
        HouseTokenState state = (HouseTokenState) obj;
        return getFractionDigits() == state.getFractionDigits() &&
                                      getValuation().equals(state.getValuation()) &&
                                      getMaintainer().equals(state.getMaintainer()) &&
                                      uid.equals(state.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValuation(),getUniqueIdentifier(),getMaintainer(),getFractionDigits());
    }
}
