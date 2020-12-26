package com.mydumfries.mytwitterfeed;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PostTweet extends Activity {
	public static final String TWITTER_PHONE_NO = "86444";
	private Button SendTweetButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final int source = getIntent().getIntExtra("source", 1);
		final Long idOfStatusToRetweet=getIntent().getLongExtra("idOfStatusToRetweet", 0);
		final String ReTweet=getIntent().getStringExtra("ReTweet");
		if (source==3)
		{
			retweetwifi(idOfStatusToRetweet);
			finish();
		}
		if (source==4)
		{
			RetweetSms(TWITTER_PHONE_NO, ReTweet);
			finish();
		}
		setContentView(R.layout.post_tweet);
		final EditText TweetText = (EditText) findViewById(R.id.message);
		TweetText.addTextChangedListener(mTextEditorWatcher);
		SendTweetButton = (Button) findViewById(R.id.button_post);
		SendTweetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String Tweet = TweetText.getText().toString();
				if (source==1)
				{
					// now call the send text method
					composeSms(TWITTER_PHONE_NO, Tweet);
				}
				if (source==2)
				{
					composewifi(Tweet);
				}
				finish();
			}
		});
	}

	private boolean composeSms(String telefoonnr, String boodschap) {
		if (telefoonnr.equals("") || boodschap.equals("")) {
			// smsFail();
			return false;
		}
		boodschap=boodschap;
		try {
			SmsManager sm = SmsManager.getDefault();
			sm.sendTextMessage(telefoonnr, null, boodschap, null, null);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			// smsFail();
			return false;
		}
	}
	private final TextWatcher mTextEditorWatcher=new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			final TextView number_of_character = (TextView) findViewById(R.id.Remaining);
			number_of_character.setText(String.valueOf(140-s.length()));
		}
	};

	private void composewifi(final String Tweet)
	{
		final ConnectionDetector cd;
		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			new AlertDialog.Builder(this)
			.setTitle("No Internet Connection") 
			.setMessage("Would You Like To Post Via SMS Instead?") 
			.setCancelable(false) 
			.setPositiveButton("SMS", new DialogInterface.OnClickListener() 
			{ 
				@Override
				public void onClick(DialogInterface dialog, int id) 
				{	 
					composeSms(TWITTER_PHONE_NO, Tweet);
					return;
				}
			})	                	   
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { 
				@Override
				public void onClick(DialogInterface dialog, int id) { 
					return;
				} 
			})  
			.show(); 
		}
		else
		{
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					String CONSUMER_KEY = "cKquCndTeDdHN8tHzzT55A";
					String CONSUMER_SECRET = "Ci8jutTahMQXhUNiFYCyr6MRFfsams2cjakZrmSU";
					String ACCESS_SECRET = "aB1WZbjLl3ouaW5eRxBl7Wgfgt5Wcn84AaPUw2Aa6g8F8";
					String ACCESS_TOKEN = "367086496-JDUSbACNM5M8UmlWV4RJ7R9S36cCy7X9Bkfbzs75";

					// Consumer
					Twitter twitter = new TwitterFactory().getInstance();
					twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);

					// Access Token
					AccessToken accessToken = null;
					accessToken = new AccessToken(ACCESS_TOKEN, ACCESS_SECRET);
					twitter.setOAuthAccessToken(accessToken);

					// Posting Status
					Status status = null;
					try {
						status = twitter.updateStatus(Tweet);
					} catch (TwitterException e) {
						e.printStackTrace();
					}
				}	});
			thread.start();
		}
	}
	private void retweetwifi(final Long idOfStatusToRetweet)
	{
		final ConnectionDetector cd;
		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			new AlertDialog.Builder(this)
			.setTitle("No Internet Connection") 
			.setMessage("Please Select Retweet Via SMS Instead?") 
			.setCancelable(false)	                	   
			.setNegativeButton("OK", new DialogInterface.OnClickListener() { 
				@Override
				public void onClick(DialogInterface dialog, int id) { 
					return;
				} 
			})  
			.show(); 
		}
		else
		{
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					String CONSUMER_KEY = "cKquCndTeDdHN8tHzzT55A";
					String CONSUMER_SECRET = "Ci8jutTahMQXhUNiFYCyr6MRFfsams2cjakZrmSU";
					String ACCESS_SECRET = "aB1WZbjLl3ouaW5eRxBl7Wgfgt5Wcn84AaPUw2Aa6g8F8";
					String ACCESS_TOKEN = "367086496-JDUSbACNM5M8UmlWV4RJ7R9S36cCy7X9Bkfbzs75";

					// Consumer
					Twitter twitter = new TwitterFactory().getInstance();
					twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);

					// Access Token
					AccessToken accessToken = null;
					accessToken = new AccessToken(ACCESS_TOKEN, ACCESS_SECRET);
					twitter.setOAuthAccessToken(accessToken);

					// Posting Status
					Status status = null;
					try {
						status = twitter.retweetStatus(idOfStatusToRetweet);
					} catch (TwitterException e) {
						e.printStackTrace();
					}
				}	});
			thread.start();
		}
	}
	private boolean RetweetSms(String telefoonnr, String boodschap) {
		if (telefoonnr.equals("") || boodschap.equals("")) {
			// smsFail();
			return false;
		}
		boodschap=boodschap;
		try {
			SmsManager sm = SmsManager.getDefault();
			sm.sendTextMessage(telefoonnr, null, boodschap, null, null);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			// smsFail();
			return false;
		}
	}
}
