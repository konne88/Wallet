package weitz.konstantin.android.wallet;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

// setup usb connection
// http://developer.android.com/guide/developing/device.html#setting-up

public class EditTransaction extends Activity implements DatePickerDialog.OnDateSetListener,
	TimePickerDialog.OnTimeSetListener
{
	private enum ShowDialog {
		PICK_TIME {
			public Dialog create(EditTransaction activity) {
				return new TimePickerDialog(activity, activity,
						activity.eventTime.hour,
						activity.eventTime.minute,
						DateFormat.is24HourFormat(activity));
			}
		},
		PICK_DATE {
			public Dialog create(EditTransaction activity) {
				return new DatePickerDialog(activity, activity,
						activity.eventTime.year,
						activity.eventTime.month,
						activity.eventTime.monthDay);
			}
		};
		
		public abstract Dialog create(EditTransaction context);
	}
	
    private ArrayAdapter<CharSequence> categoriesAdapter;
    private ArrayAdapter<CharSequence> payerAdapter;
    private ArrayAdapter<CharSequence> receiverAdapter;
    private CharSequence expense;
    private CharSequence revenue;
    private CharSequence receiver;
    private CharSequence payer;
    
    private Button inout;
    private AutoCompleteTextView category; 
    private AutoCompleteTextView partner;
    private EditText money;
    private Button date;
    private Button time;
    private Button clear;
    private EditText comment;
    private Button create;
    
    private Time eventTime = new Time();
	
    private String formatTime(){
    	return eventTime.format(DateFormat.is24HourFormat(this)?
    		"%H:%M":"%I:%M%p"
    	);
    }

    private String formatDate(){
    	return eventTime.format("%a, %b %e, %Y");
    }
        
  /*  private void assertAccount() {
	    // Only try looking for an account if this is the first launch.
	    if (icicle == null) {
	        Account[] accounts = AccountManager.get(this).getAccounts();
	        if(accounts.length > 0) {
	            // If the only account is an account that can't use Calendar we let the user into
	            // Calendar, but they can't create any events until they add an account with a
	            // Calendar.
	            launchCalendarView();
	        } else {
	            // If we failed to find a valid Calendar, bounce the user to the account settings
	            // screen. Using the Calendar authority has the added benefit of only showing
	            // account types that use Calendar when you enter the add account screen from here.
	            final Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
	            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
	            intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {
	                Calendar.AUTHORITY
	            });
	            startActivityForResult(intent, 0);
	        }
	    }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    Account[] accounts = AccountManager.get(this).getAccounts();
	    if(accounts.length > 0) {
	        // If the only account is an account that can't use Calendar we let the user into
	        // Calendar, but they can't create any events until they add an account with a
	        // Calendar.
	        launchCalendarView();
	    } else {
	        finish();
	    }
	}*/
    
    private void clearFields() {
    	eventTime.setToNow();

    	partner.setAdapter(receiverAdapter);
		inout.setText(expense);
		partner.setHint(receiver);
        
		partner.setText("");
		category.setText("");
		money.setText("");
        comment.setText("");
        
        date.setText(formatDate());
        time.setText(formatTime());
        
        money.requestFocus();
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        expense = getString(R.string.expense);
        revenue = getString(R.string.revenue);
        receiver = getString(R.string.receiver);
        payer = getString(R.string.payer);

        money       = (EditText) findViewById(R.id.money);
        inout       = (Button) findViewById(R.id.in_out);
        category    = (AutoCompleteTextView) findViewById(R.id.category);
        partner     = (AutoCompleteTextView) findViewById(R.id.partner);
        date        = (Button) findViewById(R.id.date);
        time        = (Button) findViewById(R.id.time);
        clear       = (Button) findViewById(R.id.clear);
        comment     = (EditText) findViewById(R.id.comment);
        create      = (Button) findViewById(R.id.create);

    	InputFilter[] filterArray = new InputFilter[1];	
        filterArray[0] = new MoneyValueFilter();
        money.setFilters(filterArray);
        
        categoriesAdapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_dropdown_item_1line);
        payerAdapter      = ArrayAdapter.createFromResource(this, R.array.payer, android.R.layout.simple_dropdown_item_1line);
        receiverAdapter   = ArrayAdapter.createFromResource(this, R.array.receiver, android.R.layout.simple_dropdown_item_1line);   

        category.setAdapter(categoriesAdapter);
        
        clearFields();
        
        date.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(ShowDialog.PICK_DATE.ordinal());
			}
		});
        
        time.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(ShowDialog.PICK_TIME.ordinal());
			}
		});
        
        inout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Toggle expense/revenue
				
				if(inout.getText().equals(revenue)) {
					partner.setAdapter(receiverAdapter);
					inout.setText(expense);
					partner.setHint(receiver);
				} else {
					partner.setAdapter(payerAdapter);
					inout.setText(revenue);
					partner.setHint(payer);
				}
			}
		});
        
        clear.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				clearFields();
			}
		});
        
        create.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sendTransaction();
			}
		});
    }
    
    private void sendTransaction() {
    	AccountManager accountManager = AccountManager.get(getApplicationContext());
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account = accounts[0];
        Intent intent = new Intent(this, SendTransaction.class);
        
        Bundle when = new Bundle();
        when.putInt("second", eventTime.second);
        when.putInt("hour", eventTime.hour);
        when.putInt("monthDay", eventTime.monthDay);
        when.putInt("month", eventTime.month);
        when.putInt("year", eventTime.year);

        Bundle transaction = new Bundle();
		transaction.putString("amount",money.getText().toString());
		transaction.putString("memo",comment.getText().toString());
		transaction.putString("partner",partner.getText().toString());
		transaction.putString("wallet","0");
		transaction.putBundle("when",when);
        
        intent.putExtra("transaction", transaction);
        intent.putExtra("account", account);
        startActivity(intent);
    
        finish();

//	    Account[] mAccounts = acm.getAccountsByType("Google");
	//    AccountManagerFuture<options> response = acm.getAuthToken(mAccounts[0],
	  //  		type, options, activity, mCallback, mHandler); 
	    // define callback and handler yourself for where to retur
    }
	    
    protected Dialog onCreateDialog(int id) {
        return ShowDialog.values()[id].create(this);
    }
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		eventTime.year = year;
		eventTime.monthDay = dayOfMonth;
		eventTime.month = monthOfYear;
		date.setText(formatDate());
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		eventTime.minute = minute;
		eventTime.hour = hourOfDay;		
		time.setText(formatTime());
	}

	

        
       // TextView tv = new TextView(this);
       // tv.setText("Hello, Android");
       // setContentView(tv);
}