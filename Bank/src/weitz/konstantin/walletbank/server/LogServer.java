package weitz.konstantin.walletbank.server;

import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;

import java.util.Calendar;

public class LogServer extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) {		
		try  {
		resp.getWriter().append("Start");
			
			
		Objectify ofy = ObjectifyService.begin();
		Query<CashTransaction> query = ofy.query(CashTransaction.class);
		
		for(CashTransaction ct : query){
			resp.getWriter().append("Item:");
			resp.getWriter().append(ct.id);
		}
		
		resp.getWriter().append("End");
		
		} catch(IOException e){
		}
	}
}