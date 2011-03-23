package weitz.konstantin.walletbank.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
import net.sf.ofx4j.domain.data.common.Status;
import net.sf.ofx4j.domain.data.common.Status.Severity;
import net.sf.ofx4j.domain.data.common.Transaction;
import net.sf.ofx4j.domain.data.common.TransactionList;
import net.sf.ofx4j.domain.data.common.TransactionType;
import net.sf.ofx4j.domain.data.signon.SignonRequest;
import net.sf.ofx4j.domain.data.signon.SignonRequestMessageSet;
import net.sf.ofx4j.server.OFXServer;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;

public class OfxBankServer implements OFXServer {
	@Override
	public ResponseEnvelope getResponse(RequestEnvelope requestEnvelope) {
		String user = null;
		String password = null;
		String account = null;
		
		SortedSet<RequestMessageSet> set = requestEnvelope.getMessageSets();
		for(RequestMessageSet msgset : set){
			if(msgset instanceof SignonRequestMessageSet){
				SignonRequest signon = ((SignonRequestMessageSet)msgset).getSignonRequest();
				password = signon.getPassword();
				user = signon.getUserId();
			}
			else if(msgset instanceof BankingRequestMessageSet){
				List<RequestMessage> rqms = ((BankingRequestMessageSet)msgset).getRequestMessages();				
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
		if(user == null || password ==null || account != null){
			return null;
		}
		
		// TODO check reasonable sizes
		// TODO how many transactions to accept
		
		Objectify ofy = ObjectifyService.begin();
		
		// The Query itself is Iterateble
		Query<CashTransaction> query = ofy.query(CashTransaction.class)
			.filter("user", user)
			.filter("account", account);

		SortedSet<ResponseMessageSet> response = new TreeSet<ResponseMessageSet>();
			BankingResponseMessageSet brms = new BankingResponseMessageSet();
				List<BankStatementResponseTransaction> bsrts = new ArrayList<BankStatementResponseTransaction>();
					BankStatementResponseTransaction bsrt = new BankStatementResponseTransaction();
					bsrt.setUID("Test");		// <--
					Status status = new Status();
					status.setSeverity(Severity.INFO);
					bsrt.setStatus(status);
						BankStatementResponse bsr = new BankStatementResponse();
							BankAccountDetails bad = new BankAccountDetails();
							bad.setBankId("WalletBank");
							bad.setAccountNumber(account);
							bad.setAccountType(AccountType.CHECKING);
						bsr.setAccount(bad);
							TransactionList trs = new TransactionList();
								List<Transaction> transactions = new ArrayList<Transaction>();
								
								Date finish = new Date(0);
								Date start = new Date(Long.MAX_VALUE);
								
								for(CashTransaction ct : query) {
									Transaction t = new Transaction();
									t.setDatePosted(ct.when);
									t.setId(ct.id);
									t.setTransactionType(TransactionType.CASH);
									t.setAmount(ct.amount.doubleValue());
									t.setMemo(ct.memo);
									t.setPayeeId(ct.partner);
									transactions.add(t);
									if(start.after(ct.when)) {
										start = ct.when;
									}
									if(finish.before(ct.when)) {
										finish = ct.when;
									}
								}
								trs.setStart(start);
								trs.setEnd(finish);
							trs.setTransactions(transactions);
						bsr.setTransactionList(trs);
					bsrt.setMessage(bsr);
				bsrts.add(bsrt);
			brms.setStatementResponses(bsrts);
		response.add(brms);
		
		ResponseEnvelope re = new ResponseEnvelope();
		re.setMessageSets(response);
		
		return re;
	}
}
