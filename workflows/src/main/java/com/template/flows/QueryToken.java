package com.template.flows;

import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.workflows.utilities.QueryUtilitiesKt;
import com.template.states.HouseTokenState;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;

public class QueryToken {

    @InitiatingFlow
    @StartableByRPC
    public static class GetTokenBalance extends FlowLogic<String>{
        private final String symbol;

        public GetTokenBalance(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String call() throws FlowException {
            StateAndRef<HouseTokenState> stateandref = getServiceHub().getVaultService().queryBy(HouseTokenState.class)
                    .getStates().stream().filter(sf -> sf.getState().getData().getSymbol().equals(symbol)).findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Can't find the symbol"));
            HouseTokenState state = stateandref.getState().getData();
            TokenPointer<HouseTokenState> pointer = state.toPointer(HouseTokenState.class);
            Amount<TokenType> amt = QueryUtilitiesKt.tokenBalance(getServiceHub().getVaultService(),pointer);
            return "Currently have only"+amt.getQuantity()+" "+symbol+" tokens";
        }
    }
}
