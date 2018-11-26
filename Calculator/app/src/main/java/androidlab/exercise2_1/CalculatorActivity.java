package androidlab.exercise2_1;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//the following imports are from the external library exp4j-0.3.10.jar for the calculation
import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;

public class CalculatorActivity extends Activity {

	
	private BroadcastReceiver buttonPressedReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String button = intent.getStringExtra("button");
			String formula = tvFormula.getText().toString();
			String lastChar = "";
			if (formula.length() > 0) {
				lastChar = formula.substring(formula.length() - 1);
			}
			
			//Define what happens when one of the buttons "+", "-", "*", "/" is clicked
			if (button.equals("+") || button.equals("-") || button.equals("*") || button.equals("/")) {
				
				//There should be no double operators like ++, +-, *-...
				if (lastChar.equals("+") || lastChar.equals("-") || lastChar.equals("*") 
						|| lastChar.equals("/")) {
					tvFormula.setText(formula.substring(0, formula.length() - 1) + button);
					return;
				}
			}
			
			//When button "=" is clicked calculate the formula and show the result
			if (button.equals("=")) {
				
				//Formula with operator at the end can't be calculated --> delete operator at the end
				if (lastChar.equals("+") || lastChar.equals("-") || lastChar.equals("*") 
						|| lastChar.equals("/")) {
					formula = formula.substring(0, formula.length() - 1);
				}
				tvFormula.setText(calculate(formula));
				return;
			}
			
			//When button "DEL" is clicked the last char should be deleted
			if (button.equals("del")) {
				if (formula.length() > 0) {
					tvFormula.setText(formula.substring(0, formula.length() - 1));
				}
				return;
			}
			
			
			//When button "C" is clicked the whole formula should be deleted
			if (button.equals("C")) {
				tvFormula.setText("");
				return;
			}
			
			//Define what happens when button "." is clicked
			if (button.equals(".")) {
				
				//There should be only one point per number
				int indexPoint = formula.lastIndexOf(".");
				int indexPlus = formula.lastIndexOf("+");
				int indexMinus = formula.lastIndexOf("-");
				int indexMulti = formula.lastIndexOf("*");
				int indexDiv = formula.lastIndexOf("/");
				
				if (indexPoint > indexPlus && indexPoint > indexMinus && 
						indexPoint > indexMulti && indexPoint > indexDiv){
					return;
				}
				
				//IF there is an operator directly before the point, add a zero between them
				if (lastChar.equals("+") || lastChar.equals("-") || lastChar.equals("*") 
						|| lastChar.equals("/")){
					formula = formula + "0";
				}
			}
			
			
			tvFormula.setText(formula + button);
		}
	};
	
	private TextView tvFormula;
	private Button bOne;
	private Button bTwo;
	private Button bThree;
	private Button bFour;
	private Button bFive;
	private Button bSix;
	private Button bSeven;
	private Button bEight;
	private Button bNine;
	private Button bZero;
	private Button bPlus;
	private Button bMinus;
	private Button bMulti;
	private Button bDiv;
	private Button bEquals;
	private Button bDel;
	private Button bC;
	private Button bPoint;
	
	
	
	public void sendCalculatorBroadcast(String button) {
		Intent intent = new Intent("buttonPressed");
		intent.putExtra("button", button);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//connect Objects from xml with java objects
		tvFormula = (TextView) findViewById(R.id.tvFormula);
		bOne = (Button) findViewById(R.id.bOne);
		bTwo = (Button) findViewById(R.id.bTwo);
		bThree = (Button) findViewById(R.id.bThree);
		bFour = (Button) findViewById(R.id.bFour);
		bFive = (Button) findViewById(R.id.bFive);
		bSix = (Button) findViewById(R.id.bSix);
		bSeven  = (Button) findViewById(R.id.bSeven);
		bEight = (Button) findViewById(R.id.bEight);
		bNine = (Button) findViewById(R.id.bNine);
		bZero = (Button) findViewById(R.id.bZero);
		bPlus = (Button) findViewById(R.id.bPlus);
		bMinus = (Button) findViewById(R.id.bMinus);
		bMulti = (Button) findViewById(R.id.bMulti);
		bDiv = (Button) findViewById(R.id.bDiv);
		bEquals = (Button) findViewById(R.id.bEquals);
		bDel = (Button) findViewById(R.id.bDel);
		bC = (Button) findViewById(R.id.bC);
		bPoint = (Button) findViewById(R.id.bPoint);
		
		//already writen data should be writen again in the Formula
		if (savedInstanceState != null){
			tvFormula.setText(savedInstanceState.getString("tvFormula"));
		}
		
		
		bOne.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("1");
			}
		});
		
		bTwo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("2");
			}
		});
		
		bThree.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("3");
			}
		});
		
		bFour.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("4");
			}
		});
		
		bFive.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("5");
			}
		});
		
		bSix.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("6");
			}
		});
		
		bSeven.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("7");
			}
		});
		
		bEight.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("8");
			}
		});
		
		bNine.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("9");
			}
		});
		
		bZero.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("0");
			}
		});
		
		bPlus.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("+");
			}
		});
		
		bMinus.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("-");
			}
		});
		
		bMulti.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("*");
			}
		});
		
		bDiv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("/");
			}
		});
		
		bEquals.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("=");
			}
		});
		
		bDel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("del");
			}
		});
		
		bC.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast("C");
			}
		});
		
		bPoint.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCalculatorBroadcast(".");
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onPause() {
		// Receiver should not receive anything when activiti is paused
		LocalBroadcastManager.getInstance(this).unregisterReceiver(buttonPressedReceiver);
		super.onPause();
	} 
	
	protected void onResume(){
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(buttonPressedReceiver
				, new IntentFilter("buttonPressed"));
	}
	
	/**
	 * provides the formula after a display rotation
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState){
		//save the writen data of tvFormula
		outState.putString("tvFormula", tvFormula.getText().toString());
		super.onSaveInstanceState(outState);
    }
	
	/**
	 * calculates a string formula and returns the result also in a string
	 */
	public String calculate(String formula){
		
		try {
			//calculate the String formula
			Calculable calculable = new ExpressionBuilder(formula).build();
			String ret = calculable.calculate() + "";
			return ret;
		} catch (UnknownFunctionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnparsableExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//if we reach this part of code return an error message
		return "Something went wrong";
	}
}
