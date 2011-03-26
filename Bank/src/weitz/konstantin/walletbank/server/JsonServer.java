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
		try {
			if (req.getUserPrincipal() == null){
				log.warning("User not signed in");
				resp.setStatus(401);
				return;
			}

			log.warning("Logged in");
			
			WalletTransaction ct = new WalletTransaction();
			ct.user = req.getUserPrincipal().getName();
			
			JSONObject jo = (JSONObject)(new JSONTokener(req.getReader()).nextValue());
			
			new BigDecimal(jo.getString("amount"));
			
			ct.amount   = jo.getString("amount");
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
			
			// store the transaction
			ObjectifyService.register(WalletTransaction.class);
			Objectify ofy = ObjectifyService.begin();
			ofy.put(ct);
			
			return;
		} catch (NumberFormatException e) {
			log.warning("Amount is not a number");
		} catch (ClassCastException e) {
			log.warning("Invalid JSON Format");
		} catch (JSONException e) {
			log.warning("Invalid JSON Format");
		} catch (IllegalArgumentException e) {
			log.warning("Invalid Date");
		} catch (IOException e) {
			log.warning("Some sort of internal IOException");
			resp.setStatus(500);			
			return;
		}
		
		resp.setStatus(400);
	}
}