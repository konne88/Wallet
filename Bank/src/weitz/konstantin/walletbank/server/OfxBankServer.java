package weitz.konstantin.walletbank.server;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import net.sf.ofx4j.domain.data.RequestEnvelope;
import net.sf.ofx4j.domain.data.RequestMessage;
import net.sf.ofx4j.domain.data.RequestMessageSet;
import net.sf.ofx4j.domain.data.ResponseEnvelope;
import net.sf.ofx4j.domain.data.ResponseMessageSet;
import net.sf.ofx4j.domain.data.banking.AccountType;
import net.sf.ofx4j.domain.data.banking.BankAccountDetails;
import net.sf.ofx4j.domain.data.banking.BankStatementRequestTransaction;
import net.sf.ofx4j.domain.data.banking.BankStatementResponse;
import net.sf.ofx4j.domain.data.banking.BankStatementResponseTransaction;
import net.sf.ofx4j.domain.data.banking.BankingRequestMessageSet;
import net.sf.ofx4j.domain.data.banking.BankingResponseMessageSet;
import net.sf.ofx4j.domain.data.common.Payee;
import net.sf.ofx4j.domain.data.common.Status;
import net.sf.ofx4j.domain.data.common.Status.Severity;
import net.sf.ofx4j.domain.data.common.StatusCode;
import net.sf.ofx4j.domain.data.common.Transaction;
import net.sf.ofx4j.domain.data.common.TransactionList;
import net.sf.ofx4j.domain.data.common.TransactionType;
import net.sf.ofx4j.domain.data.signon.SignonRequest;
import net.sf.ofx4j.domain.data.signon.SignonRequestMessageSet;
import net.sf.ofx4j.io.AggregateMarshaller;
import net.sf.ofx4j.io.v1.OFXV1Writer;
import net.sf.ofx4j.server.OFXServer;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;

public class OfxBankServer implements OFXServer {
    private static final Logger log =
        Logger.getLogger(OfxBankServer.class.getName());
    
    @Override
    public ResponseEnvelope getResponse(RequestEnvelope requestEnvelope) {
        String user = null;
        String password = null;
        String account = null;
        
        SortedSet<RequestMessageSet> set = requestEnvelope.getMessageSets();
        for(RequestMessageSet msgset : set){
            if(msgset instanceof SignonRequestMessageSet){
                SignonRequest signon = ((SignonRequestMessageSet)msgset)
                        .getSignonRequest();
                password = signon.getPassword();
                user = signon.getUserId();
            }
            else if(msgset instanceof BankingRequestMessageSet){
                List<RequestMessage> rqms = ((BankingRequestMessageSet)msgset)
                    .getRequestMessages();                
                for(RequestMessage rqm : rqms){
                    if(rqm instanceof BankStatementRequestTransaction){
                        account = ((BankStatementRequestTransaction) rqm)
                                        .getMessage()
                                        .getAccount()
                                        .getAccountNumber();
                    }
                }
            }
        }

        // not all parameters were specified
        if(user == null) {
            log.warning("No username specified");
            return null;
        }
        if(password == null) {
            log.warning("No password specified");
            return null;
        }
        if(account == null) {
            log.warning("No account specified");
            return null;
        }
        
        // TODO check reasonable sizes
        // TODO how many transactions to accept

        log.warning(password);
        log.warning(account);
        log.warning(user);
        
        ObjectifyService.register(WalletTransaction.class);
        Objectify ofy = ObjectifyService.begin();
        
        // The Query itself is Iterateble
        Query<WalletTransaction> query = ofy.query(WalletTransaction.class)
            .filter("user", user)
            .filter("account", account);

        SortedSet<ResponseMessageSet> response = 
            new TreeSet<ResponseMessageSet>();
            BankingResponseMessageSet brms = new BankingResponseMessageSet();
                List<BankStatementResponseTransaction> bsrts = 
                    new ArrayList<BankStatementResponseTransaction>();
                    BankStatementResponseTransaction bsrt = 
                        new BankStatementResponseTransaction();
                    bsrt.setUID("Test");        // <--

                    StatusCode mystatus = new StatusCode() {
                        @Override
                        public int getCode() {
                            return 0;
                        }

                        @Override
                        public Severity getDefaultSeverity() {
                            return Severity.INFO;
                        }

                        @Override
                        public String getMessage() {
                            return "SUCCESS";
                        }
                        
                        @Override
                        public String toString() {
                            return "0";
                        }
                    };
                    
                    status.setCode(mystatus);
//                    status.setSeverity(Severity.INFO);
                    bsrt.setStatus(status);
                        BankStatementResponse bsr = new BankStatementResponse();
                            BankAccountDetails bad = new BankAccountDetails();
                            bad.setBankId("WalletBank");
                            bad.setAccountNumber(account);
                            bad.setAccountType(AccountType.CHECKING);
                        bsr.setAccount(bad);
                            TransactionList trs = new TransactionList();
                                List<Transaction> transactions = 
                                    new ArrayList<Transaction>();
                                
                                Date finish = new Date(0);
                                Date start = new Date(Long.MAX_VALUE);
                                int i = 0;
                                
                                for(WalletTransaction ct : query) {
                                    Transaction t = new Transaction();
                                    t.setDatePosted(ct.when);
                                    t.setId(ct.id.toString());
                                    t.setTransactionType(TransactionType.CASH);
                                    t.setAmount(Double.parseDouble(ct.amount));
                                    t.setMemo(ct.memo);
                                    t.setPayeeId("Lidl");//ct.partner);
                                    t.setName("I AM A PARTNER NAME");
                                    
                                    /*Payee payee = new Payee();
                                    payee.setName("I AM A PARTNER COMPLEX NAME");
                                    payee.setAddress1("REGSTR");
                                    payee.setCity("STUTTGART");
                                    payee.setState("BAWUE");
                                    payee.setCountry("GERMANY");
                                    payee.setZip("70597");
                                    t.setPayee(payee);
                                        */                                
                                    transactions.add(t);
                                    if(start.after(ct.when)) {
                                        start = ct.when;
                                    }
                                    if(finish.before(ct.when)) {
                                        finish = ct.when;
                                    }
                                    ++i;
                                }
                                trs.setStart(start);
                                trs.setEnd(finish);
                            if(i > 0){
                                log.warning(i+" transactions send");
                                trs.setTransactions(transactions);
                            } else {
                                log.warning("No transactions send");
                            }        
                        bsr.setTransactionList(trs);
                    bsrt.setMessage(bsr);
                bsrts.add(bsrt);
            brms.setStatementResponses(bsrts);
        response.add(brms);
        
        ResponseEnvelope re = new ResponseEnvelope();
        re.setMessageSets(response);
        
        AggregateMarshaller marshaller = new AggregateMarshaller();

        StringWriter w = new StringWriter();
        try {
            marshaller.marshal(re, new OFXV1Writer(w));

            w.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        log.warning(w.getBuffer().toString());
        
        return re;
    }
}
