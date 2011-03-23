package weitz.konstantin.walletbank.server;

import java.util.Date;

import javax.persistence.Id;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Unindexed;
import java.math.BigDecimal;

public class CashTransaction {
	public @Id String id;
	public String user;
	public String account;
	public Date when;
	public @Unindexed BigDecimal amount;
	public @Unindexed String partner;
	public @Unindexed String memo;
	
	static {
		ObjectifyService.register(CashTransaction.class);
	}
}
