package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.contracts.utilities.TransactionUtilitiesKt;
import com.r3.corda.lib.tokens.workflows.flows.rpc.*;
import com.template.states.HouseTokenState;
import kotlin.Unit;
import net.corda.core.contracts.*;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;

import java.math.BigDecimal;
import java.util.Arrays;

public class HouseTokenFlow {

    private HouseTokenFlow() {
        //Instantiation not allowed
    }

    /**
     *  Issue Non Fungible Token using IssueTokens flow
     */
    @StartableByRPC
    public static class CreateHouseTokenFlow extends FlowLogic<SignedTransaction>{

        private final String symbol;
        private final BigDecimal valuation;

        public CreateHouseTokenFlow(String symbol, BigDecimal valuation) {
            this.symbol = symbol;
            this.valuation = valuation;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            HouseTokenState state = new HouseTokenState(valuation,new UniqueIdentifier(),symbol,getOurIdentity(),0);
            TransactionState tstate = new TransactionState(state,notary);
            return subFlow(new CreateEvolvableTokens(tstate));
        }
    }
    /**
     *  Issue Fungible Token against an evolvable house asset on ledger
     */
    @StartableByRPC
    public static class IssueHouseTokenFlow extends FlowLogic<SignedTransaction> {

        private final String symbol;
        private final int quantity;
        private final Party holder;

        public IssueHouseTokenFlow(String symbol, int quantity, Party holder) {
            this.symbol = symbol;
            this.quantity = quantity;
            this.holder = holder;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            StateAndRef<HouseTokenState> stateref = getServiceHub().getVaultService().
                    queryBy(HouseTokenState.class).getStates().stream()
                    .filter(sf -> sf.getState().getData().getSymbol().equals(symbol)).findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Token synbol of type " + this.symbol + "not found"));
HouseTokenState hstate = stateref.getState().getData();
            TokenPointer pointer = hstate.toPointer(hstate.getClass());
            IssuedTokenType tokenType =new IssuedTokenType(getOurIdentity(),pointer);
            Amount<IssuedTokenType> amt =new Amount<>(quantity,tokenType);
            FungibleToken ftoken = new FungibleToken(amt,holder, TransactionUtilitiesKt.getAttachmentIdForGenericParam(pointer));

            return subFlow(new IssueTokens(Arrays.asList(ftoken)));
        }
    }
    @StartableByRPC
    @InitiatingFlow
    public static class MoveHouseTokenFlow extends FlowLogic<SignedTransaction> {

        private final String symbol;
        private final int quantity;
        private final Party holder;

        public MoveHouseTokenFlow(String symbol, int quantity, Party holder) {
            this.symbol = symbol;
            this.quantity = quantity;
            this.holder = holder;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            StateAndRef<HouseTokenState> stateref = getServiceHub().getVaultService().
                    queryBy(HouseTokenState.class).getStates().stream()
                    .filter(sf -> sf.getState().getData().getSymbol().equals(symbol)).findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Token synbol of type " + this.symbol + "not found"));
            HouseTokenState hstate = stateref.getState().getData();
            TokenPointer<HouseTokenState> pointer = hstate.toPointer(HouseTokenState.class);
            Amount<TokenType> amt =new Amount(quantity,pointer);

            return subFlow(new MoveFungibleTokens(amt,holder));
        }
    }

    @InitiatedBy(MoveHouseTokenFlow.class)
public static class MoveHouseTokenFlowResponder extends FlowLogic<Unit>{
        private FlowSession session;

        public MoveHouseTokenFlowResponder(FlowSession session) {
            this.session = session;
        }

        @Override
        public Unit call() throws FlowException {
            return subFlow(new MoveFungibleTokensHandler(session));
        }
    }
    /**
     *  Holder Redeems fungible token issued by issuer. The code below is a demonstration for how to redeem a toke.
     *
     *  Or we have to define an issuance celling for the fungible token,
     *  and you can redeem for the non-fungible asset, the house in this case, when you have all the fungible tokens.
     */
//    @StartableByRPC
//    public static class RedeemHouseFungibleTokenFlow extends FlowLogic<SignedTransaction> {
//
//        private final String symbol;
//        private final Party issuer;
//        private final int quantity;
//
//        public RedeemHouseFungibleTokenFlow(String symbol, Party issuer, int quantity) {
//            this.symbol = symbol;
//            this.issuer = issuer;
//            this.quantity = quantity;
//        }
//
//        @Override
//        @Suspendable
//        public SignedTransaction call() throws FlowException {
//            //get house states on ledger with uuid as input tokenId
//            StateAndRef<FungibleHouseTokenState> stateAndRef = getServiceHub().getVaultService().
//                    queryBy(FungibleHouseTokenState.class).getStates().stream()
//                    .filter(sf->sf.getState().getData().getSymbol().equals(symbol)).findAny()
//                    .orElseThrow(()-> new IllegalArgumentException("StockState symbol=\""+symbol+"\" not found from vault"));
//
//            //get the RealEstateEvolvableTokenType object
//            FungibleHouseTokenState evolvableTokenType = stateAndRef.getState().getData();
//
//            //get the pointer pointer to the house
//            TokenPointer tokenPointer = evolvableTokenType.toPointer(evolvableTokenType.getClass());
//
//            //specify how much amount quantity of tokens of type token parameter
//            Amount amount = new Amount(quantity, tokenPointer);
//
//            //call built in redeem flow to redeem tokens with issuer
//            return subFlow(new RedeemFungibleTokens(amount, issuer));
//        }
//    }
}

