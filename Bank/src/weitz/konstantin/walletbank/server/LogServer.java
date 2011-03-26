package weitz.konstantin.walletbank.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;

public class LogServer extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) {		
		try  {
		resp.getWriter().append("Start");
		
		ObjectifyService.register(WalletTransaction.class);
		Objectify ofy = ObjectifyService.begin();
		Query<WalletTransaction> query = ofy.query(WalletTransaction.class);
		
		for(WalletTransaction ct : query){
			resp.getWriter().append("Item:");
			resp.getWriter().append(ct.id.toString());
		}
		
		resp.getWriter().append("End");
		
		} catch(IOException e){
		}
	}
}