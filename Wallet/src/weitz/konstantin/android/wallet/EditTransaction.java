package weitz.konstantin.android.wallet;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.format.DateFormat;
import android.text.format.Time;
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
    
    private Time eventTime = new Time();
	
    private String formatTime(){
    	return eventTime.format(DateFormat.is24HourFormat(this)?
    		"%H:%M":"%I:%M%p"
    	);
    }

    private String formatDate(){
    	return eventTime.format("%a, %b %e, %Y");
    }

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