package weitz.konstantin.walletbank.server;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

public class JsonServer extends HttpServlet {
	private static final Logger log = Logger.getLogger(JsonServer.class.getName());
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) {
		log.warning("Was called");
		
		try {
			if (req.getUserPrincipal() == null){
				log.warning("Not signed in");
				
				//return;
			}

			log.warning("Logged in");
			
			CashTransaction ct = new CashTransaction();
			ct.user = req.getUserPrincipal().getName();
			
			JSONObject jo = (JSONObject)(new JSONTokener(req.getReader()).nextValue());
			
			ct.amount   = new BigDecimal(jo.getString("amount"));
			ct.partner  = jo.getString("partner");
			ct.memo     = jo.getString("memo");
			ct.account  = jo.getString("wallet");
			
			JSONObject time = jo.getJSONObject("when");
			Calendar cal = Calendar.getInstance();
			cal.setLenient(false);
			cal.set(time.getInt("year"), 
					time.getInt("month"),
					time.getInt("monthDay"),
					time.getInt("hour"),
					time.getInt("second"));
			ct.when = cal.getTime();
			
			log.warning("Parsed");

			ct.id       = "123";
			ct.amount   = new BigDecimal(100);
			ct.partner  = "partner";
			ct.memo     = "memo";
			ct.account  = "wallet";
			ct.when     = new Date();
			
			log.warning("Parsed");

			// store the transaction
			Objectify ofy = ObjectifyService.begin();
			ofy.put(ct);
			
			log.warning("Put");
			
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}