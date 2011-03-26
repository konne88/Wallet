package weitz.konstantin.walletbank.server;

import java.util.Date;

import javax.persistence.Id;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Unindexed;

public class WalletTransaction {
	public @Id Long id;
	public String user;
	public String account;
	public Date when;
	public @Unindexed String amount;
	public @Unindexed String partner;
	public @Unindexed String memo;
}
