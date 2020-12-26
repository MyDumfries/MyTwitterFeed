package com.mydumfries.mytwitterfeed;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.util.Base64;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Demonstrates how to use a twitter application keys to access a user's timeline
 */
public class MainActivity extends ListActivity {
    static final int SEARCH_DIALOG_ID = 0;
    static final int TWITTER_PHONE_NO = 86444;
    private ListActivity activity;
    final static String ScreenName = "mydumfries";
    ArrayList<Tweet> tweets;
    String lasttweet;
    String lastfacebook;
    String previoustweet;
    static final int POST_DIALOG_ID = 1;
    private ConnectionDetector cd;
    int sweep = 0;
    int prevHasBeenUpdated = 0;
    int searching = 0;
    boolean isLongClick = false;
    AlertDialogManager alert = new AlertDialogManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        registerForContextMenu(getListView());
        tweets = new ArrayList<Tweet>();
        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar_item);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        Button prevButton = (Button) findViewById(R.id.previous_tweets);
        prevButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ReloadPreviousTweets(v);
            }
        });
        Button refreshButton = (Button) findViewById(R.id.refresh_tweets);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RefreshTweets(v);
            }
        });
        downloadTweets();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences Settings = getSharedPreferences("settings",
                Context.MODE_PRIVATE);
        Editor editor = Settings.edit();
        if (Settings.contains("last_tweet") && prevHasBeenUpdated == 0 && lasttweet != null) {
            prevHasBeenUpdated = 1;
            String temptweet = Settings.getString("last_tweet", "ERROR");
            editor.putString("previous_tweet", temptweet);
            editor.commit();
        }
        if (lasttweet != null) {
            editor.putString("last_tweet", lasttweet);
            editor.commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences Settings = getSharedPreferences("settings",
                Context.MODE_PRIVATE);
        Editor editor = Settings.edit();
        if (Settings.contains("last_tweet") && prevHasBeenUpdated == 0) {
            prevHasBeenUpdated = 1;
            editor.putString("previous_tweet", Settings.getString("last_tweet", "ERROR"));
            editor.commit();
        }
        editor.putString("last_tweet", lasttweet);
        editor.commit();
    }

    // download twitter timeline after first checking to see if there is a network connection
    public void downloadTweets() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String TwitterStreamURL = "https://api.twitter.com/1.1/lists/statuses.json?slug=allfollowing&count=200&page=2&";
            SharedPreferences Settings = getSharedPreferences("settings",
                    Context.MODE_PRIVATE);
            lasttweet = Settings.getString("last_tweet", "0");
            if (lasttweet == "") lasttweet = "0";
            previoustweet = Settings.getString("previous_tweet", "0");
            if (!lasttweet.equals("0")) {
                TwitterStreamURL = TwitterStreamURL + "since_id=" + lasttweet + "&";
            }
            TwitterStreamURL = TwitterStreamURL + "owner_screen_name=" + ScreenName + "&";
            TwitterStreamURL = TwitterStreamURL + "tweet_mode=extended";;
            new DownloadTwitterTaskPage2().execute(TwitterStreamURL);
            TwitterStreamURL = "https://api.twitter.com/1.1/lists/statuses.json?slug=allfollowing&count=200&";
            if (!lasttweet.equals("0")) {
                TwitterStreamURL = TwitterStreamURL + "since_id=" + lasttweet + "&";
            }
            TwitterStreamURL = TwitterStreamURL + "owner_screen_name=" + ScreenName + "&";
            TwitterStreamURL = TwitterStreamURL + "tweet_mode=extended";
            new DownloadTwitterTask().execute(TwitterStreamURL);
        } else {
            CharSequence text3 = "Would You Like to Post a Status Update Via SMS? Otherwise, Please establish a Network Connection and try again.";
            new AlertDialog.Builder(this)
                    .setTitle("No Network Connection.")
                    .setMessage(text3)
                    .setCancelable(false)
                    .setPositiveButton("Post SMS Status", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            PostSMS();
                            return;
                        }
                    })
                    .setNegativeButton("Close App", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            return;
                        }
                    })
                    .show();
        }
    }

    // Uses an AsyncTask to download a Twitter user's timeline
    private class DownloadTwitterTaskPage2 extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... screenNames) {
            String result = null;
            result = getTwitterStream(screenNames[0]);
            return result;
        }

        // onPostExecute convert the JSON results into a Twitter object (which is an Array list of tweets
        @Override
        protected void onPostExecute(String result) {
            tweets = jsonToTwitter(result);
        }
    }

    // Uses an AsyncTask to download a Twitter user's timeline
    private class DownloadTwitterTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... screenNames) {
            String result = null;
            result = getTwitterStream(screenNames[0]);
            return result;
        }

        // onPostExecute convert the JSON results into a Twitter object (which is an Array list of tweets
        @Override
        protected void onPostExecute(String result) {
            tweets = jsonToTwitter(result);
//			ArrayList<Tweet> debug=tweets;
            Collections.sort(tweets, new Comparator<Tweet>() {
                public int compare(Tweet o1, Tweet o2) {
                    return o1.uformdate.compareTo(o2.uformdate);
                }
            });
//			Collections.reverse(tweets);
            if (tweets.isEmpty())
            {
                Tweet tweet = new Tweet();
                tweet.content = "No new Tweets to display!";
                tweets.add(tweet);
            }
            TweetListAdaptor adaptor = new TweetListAdaptor(activity, R.layout.list_item, tweets);
            setListAdapter(adaptor);
            Button prevButton = (Button) findViewById(R.id.previous_tweets);
            prevButton.setText("PREVIOUS");
            Button refreshButton = (Button) findViewById(R.id.refresh_tweets);
            refreshButton.setText("REFRESH");
        }
    }

    // converts a string of JSON data into a Twitter object
    private ArrayList<Tweet> jsonToTwitter(String result) {
        Twitter2 twits = null;
        if (result != null && result.length() > 0) {
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = jsonArray.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject user = null;
                try {
                    user = jsonObject.getJSONObject("user");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Tweet tweet = new Tweet();
                try {
                    tweet.content = jsonObject.getString("full_text");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject retweet = null;
                try {
                    retweet =jsonObject.getJSONObject("retweeted_status");
                    int rt1=tweet.content.indexOf(":");
                    String rt2=tweet.content.substring(0,rt1+2);
                    tweet.content = rt2 + retweet.getString("full_text");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    tweet.idOfStatusToRetweet = jsonObject.getLong("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                tweet.content = tweet.content.replace("&amp;", "&");
                try {
                    tweet.author = user.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    tweet.screen = user.getString("screen_name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    tweet.image_url = user.getString("profile_image_url");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                            "EEE MMM dd HH:mm:ss ZZZZZ yyyy");
                    dateFormat.toString();
                    dateFormat.setLenient(true);
                    Date created = null;
                    try {
                        created = dateFormat.parse(jsonObject.getString("created_at"));
                    } catch (Exception e) {
                        System.out.println("Exception: " + e.getMessage());
                        return null;
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    tweet.uformdate = sdf.format(created);
                try {
                    tweet.date = twitterHumanFriendlyDate(jsonObject.getString("created_at"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if ("Stagecoach West Scotland".equals(tweet.author) || "Dumfries_First".equals(tweet.author))
                {
                    if (tweet.content.contains("Dumfries")) {
                        tweets.add(tweet);
                    }
                }
                else if (tweet.author.contains("Dumfries Ice Hockey News & Scores"))
                {
                    if (tweet.content.contains("sharks") || tweet.content.contains("Sharks")) {
                        tweets.add(tweet);
                    }
                }
                else {
                    tweets.add(tweet);
                }
                    if (i == 0) {
                        SharedPreferences Settings = getSharedPreferences("settings",
                                Context.MODE_PRIVATE);
                        lasttweet = Settings.getString("last_tweet", "0");
                        previoustweet = Settings.getString("previous_tweet", "0");
                        long l = 0;
                        try {
                            l = Long.parseLong(jsonObject.getString("id_str"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        l = l - 1;
                        lasttweet = String.valueOf(l);
                    }
                }
        }
        return tweets;
    }

    // convert a JSON authentication object into an Authenticated object
    private Authenticated jsonToAuthenticated(String rawAuthorization) {
        Authenticated auth = null;
        if (rawAuthorization != null && rawAuthorization.length() > 0) {
            try {
                Gson gson = new Gson();
                auth = gson.fromJson(rawAuthorization, Authenticated.class);
            } catch (IllegalStateException ex) {
                // just eat the exception
            }
        }
        return auth;
    }

    private String getResponseBody(HttpRequestBase request) {
        StringBuilder sb = new StringBuilder();
        try {

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String reason = response.getStatusLine().getReasonPhrase();

            if (statusCode == 200) {

                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();

                BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                String line = null;
                while ((line = bReader.readLine()) != null) {
                    sb.append(line);
                }
            } else {
                sb.append(reason);
            }
        } catch (UnsupportedEncodingException ex) {
        } catch (ClientProtocolException ex1) {
        } catch (IOException ex2) {
        }
        return sb.toString();
    }

    private String getTwitterStream(String screenName) {
        String results = null;
        final String CONSUMER_KEY = "cKquCndTeDdHN8tHzzT55A";
        final String CONSUMER_SECRET = "Ci8jutTahMQXhUNiFYCyr6MRFfsams2cjakZrmSU";
        final String TwitterTokenURL = "https://api.twitter.com/oauth2/token";
        // Step 1: Encode consumer key and secret
        try {
            // URL encode the consumer key and secret
            String urlApiKey = URLEncoder.encode(CONSUMER_KEY, "UTF-8");
            String urlApiSecret = URLEncoder.encode(CONSUMER_SECRET, "UTF-8");

            // Concatenate the encoded consumer key, a colon character, and the
            // encoded consumer secret
            String combined = urlApiKey + ":" + urlApiSecret;

            // Base64 encode the string
            String base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);

            // Step 2: Obtain a bearer token
            HttpPost httpPost = new HttpPost(TwitterTokenURL);
            httpPost.setHeader("Authorization", "Basic " + base64Encoded);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            httpPost.setEntity(new StringEntity("grant_type=client_credentials"));
            String rawAuthorization = getResponseBody(httpPost);
            Authenticated auth = jsonToAuthenticated(rawAuthorization);

            // Applications should verify that the value associated with the
            // token_type key of the returned object is bearer
            if (auth != null && auth.token_type.equals("bearer")) {

                // Step 3: Authenticate API requests with bearer token
                HttpGet httpGet = new HttpGet(screenName);

                // construct a normal HTTPS request and include an Authorization
                // header with the value of Bearer <>
                httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
                httpGet.setHeader("Content-Type", "application/json");
                // update the results with the body of the response
                results = getResponseBody(httpGet);
            }
        } catch (UnsupportedEncodingException ex) {
        } catch (IllegalStateException ex1) {
        }
        return results;
    }

    public class Tweet {
        String author;
        String screen;
        String content;
        String date;
        String uformdate;
        String image_url;
        long idOfStatusToRetweet;
    }

    private class TweetListAdaptor extends ArrayAdapter<Tweet> {

        private ArrayList<Tweet> tweets;

        public TweetListAdaptor(Context context,
                                int textViewResourceId,
                                ArrayList<Tweet> items) {
            super(context, textViewResourceId, items);
            this.tweets = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item, null);
            }
            Tweet o = tweets.get(position);
            TextView tt = (TextView) v.findViewById(R.id.toptext);
            TextView bt = (TextView) v.findViewById(R.id.bottomtext);
            TextView ft = (TextView) v.findViewById(R.id.footertext);
            ImageView image = (ImageView) v.findViewById(R.id.avatar);
            bt.setText(o.content);
            tt.setText(o.author + " (@" + o.screen + ")");
            ft.setText(o.date);
//                new DisplayAvator().execute(o.image_url);
            TransformFilter filter = new TransformFilter() {
                public final String transformUrl(final Matcher match, String url) {
                    return match.group();
                }
            };

            Pattern mentionPattern = Pattern.compile("@([A-Za-z0-9_-]+)");
            String mentionScheme = "http://www.twitter.com/";
            Linkify.addLinks(tt, mentionPattern, mentionScheme, null, filter);
            Linkify.addLinks(bt, mentionPattern, mentionScheme, null, filter);

            Pattern hashtagPattern = Pattern.compile("#([A-Za-z0-9_-]+)");
            String hashtagScheme = "http://www.twitter.com/search/";
            Linkify.addLinks(bt, hashtagPattern, hashtagScheme, null, filter);

            Pattern urlPattern = Patterns.WEB_URL;
            Linkify.addLinks(bt, urlPattern, null, null, filter);

            bt.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    isLongClick = true;
                    return false;
                }
            });

            bt.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP && isLongClick) {
                        isLongClick = false;
                        return true;
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        isLongClick = false;
                    }
                    return v.onTouchEvent(event);
                }
            });

            return v;
        }
    }

    public static String twitterHumanFriendlyDate(String dateStr) {
        // parse Twitter date

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        dateFormat.toString();
        dateFormat.setLenient(true);
        Date created = null;
        try {

            created = dateFormat.parse(dateStr);

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return null;
        }

        // today

        Date today = new Date();
        System.out.println("<<<<<<" + today);
        // how much time since (ms)
        Long duration = today.getTime() - created.getTime();
        System.out.println(">>>>>>>>>>>>>>>" + duration + ">>>>>" + today.getTime() + ">>>>>" + created.getTime());
        int second = 1000;
        int minute = second * 60;
        int hour = minute * 60;
        int day = hour * 24;

        if (duration < second * 7) {

            System.out.println("right now");
            return "right now";
        }

        if (duration < minute) {
            int n = (int) Math.floor(duration / second);
            System.out.println("seconds ago");
            return n + " seconds ago";
        }

        if (duration < minute * 2) {
            System.out.println("about 1 minute ago");
            return "about 1 minute ago";
        }

        if (duration < hour) {
            int n = (int) Math.floor(duration / minute);
            System.out.println(" minutes ago");
            return n + " minutes ago";
        }

        if (duration < hour * 2) {
            System.out.println("about 1 hour ago");
            return "about 1 hour ago";
        }

        if (duration < day) {
            int n = (int) Math.floor(duration / hour);
            System.out.println(" hours ago");
            return n + " hours ago";
        }
        if (duration > day && duration < day * 2) {
            System.out.println(" yesterday");
            return "yesterday";
        }

        if (duration < day * 365) {
            int n = (int) Math.floor(duration / day);
            return n + " days ago";
        } else {
            System.out.println(" over a year ago");
            return "over a year ago";
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            return;
        }
        long id = getListAdapter().getItemId(info.position);
        menu.setHeaderTitle("Tweets Menu");
        menu.add(0, v.getId(), 0, "Update MyDumfries");
        menu.add(0, v.getId(), 0, "Update QOSfan");
        menu.add(0, v.getId(), 0, "Retweet by SMS");
        menu.add(0, v.getId(), 0, "Retweet by WIFI");
        menu.add(0, v.getId(), 0, "Save as To-Do in MyDiary");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
                .getMenuInfo();
        long id = getListAdapter().getItemId(menuInfo.position);
        if (item.getTitle().equals("Update MyDumfries")) {
            UpdateMyDumfries(id);
        }
        if (item.getTitle().equals("Update QOSfan")) {
            UpdateQOSfan(id);
        }
        if (item.getTitle().equals("Retweet by WIFI")) {
            RetweetWifi(id);
        }
        if (item.getTitle().equals("Retweet by SMS")) {
            RetweetSMS(id);
        }
        if (item.getTitle().equals("Save as To-Do in MyDiary")) {
            SaveToDo(id);
        }
        return true;
    }

    void RefreshTweets(View v) {   //called from actionbar_item.xml
        if (searching == 1) {
            searching = 0;
            TweetListAdaptor adaptor = new TweetListAdaptor(activity, R.layout.list_item, tweets);
            setListAdapter(adaptor);
        } else {
            Button refreshButton = (Button) findViewById(R.id.refresh_tweets);
            refreshButton.setText("Loading");
            tweets.clear();
            downloadTweets();
        }
    }

    void ReloadPreviousTweets(View v) {    //called from actionbar_item.xml
//		if (v == null) {
//            LayoutInflater vi = (LayoutInflater) getSystemService
//                    (Context.LAYOUT_INFLATER_SERVICE);
//            v = vi.inflate(R.layout.list_item, null);
//        }
        if (searching == 1) {
            searching = 0;
            TweetListAdaptor adaptor = new TweetListAdaptor(activity, R.layout.list_item, tweets);
            setListAdapter(adaptor);
        } else {
            Button prevButton = (Button) findViewById(R.id.previous_tweets);
            prevButton.setText("Loading");
            String TwitterStreamURL = "https://api.twitter.com/1.1/lists/statuses.json?slug=allfollowing&count=200&";
            SharedPreferences Settings = getSharedPreferences("settings",
                    Context.MODE_PRIVATE);
            tweets.clear();
            if (Settings.contains("previous_tweet")) {
                TwitterStreamURL = TwitterStreamURL + "since_id=" + Settings.getString("previous_tweet", "0") + "&";
            }
            TwitterStreamURL = TwitterStreamURL + "owner_screen_name=" + ScreenName + "&";
            TwitterStreamURL = TwitterStreamURL + "tweet_mode=extended";
            new DownloadTwitterTask().execute(TwitterStreamURL);
        }
    }

    void Reload100Tweets() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        tweets.clear();
        if (networkInfo != null && networkInfo.isConnected()) {
            String TwitterStreamURL = "https://api.twitter.com/1.1/lists/statuses.json?slug=allfollowing&count=100&";
            TwitterStreamURL = TwitterStreamURL + "owner_screen_name=" + ScreenName + "&";
            TwitterStreamURL = TwitterStreamURL + "tweet_mode=extended";
            Button prevButton = (Button) findViewById(R.id.previous_tweets);
            prevButton.setText("Loading");
            new DownloadTwitterTask().execute(TwitterStreamURL);
        } else {
            CharSequence text3 = "Please establish a Network Connection and try again.";
            AlertDialog alertDialog3 = new AlertDialog.Builder(
                    MainActivity.this).create();
            alertDialog3.setTitle("No Network Connection");
            alertDialog3.setMessage(text3);
            alertDialog3.setButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(
                                DialogInterface dialog,
                                int which) {
                            MainActivity.this.finish();
                        }
                    });
            alertDialog3.show();
        }
    }

    void UpdateMyDumfries(long id) {
        final int id2 = (int) id;
        Tweet o = tweets.get((int) id);
        String content = o.content;
        String author = o.author + " (@" + o.screen + ")";
        String date = o.uformdate;
        String image = o.image_url;
        try {
            content = URLEncoder.encode(content, "utf-8");
            author = URLEncoder.encode(author, "utf-8");
            date = URLEncoder.encode(date, "utf-8");
            image = URLEncoder.encode(image, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            Toast.makeText(getBaseContext(), "Error 1.",
                    Toast.LENGTH_LONG).show();
        }
        String url = "http://www.mydumfries.com/RemoteUpdate.php";
        url=url+"?username="+author+"&message="+content+"&date="+date+"&image_url="+image;


        com.android.volley.RequestQueue queue = Volley.newRequestQueue(this);
        final String finalAuthor = author;
        final String finalContent = content;
        final String finalDate = date;
        final String finalImage = image;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("MyDumfries Has Been Updated.")
                                .setMessage("Would You Also Like To Retweet This Status?")
                                .setCancelable(false)
                                .setPositiveButton("Yes, Please", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        RetweetWifi(id2);
                                        return;
                                    }
                                })
                                .setNegativeButton("No, Thanks", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        return;
                                    }
                                })
                                .show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("username", finalAuthor);
                params.put("message", finalContent);
                params.put("date", finalDate);
                params.put("image_url", finalImage);
                return params;
            }
        };

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    void UpdateQOSfan(long id) {
        final int id2 = (int) id;
        Tweet o = tweets.get((int) id);
        String content = o.content;
        String author = o.author + " (@" + o.screen + ")";
        String date = o.uformdate;
        String image = o.image_url;
        try {
            content = URLEncoder.encode(content, "utf-8");
            author = URLEncoder.encode(author, "utf-8");
            date = URLEncoder.encode(date, "utf-8");
            image = URLEncoder.encode(image, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            Toast.makeText(getBaseContext(), "Error 1.",
                    Toast.LENGTH_LONG).show();
        }
        String url = "http://www.qosfan.co.uk/RemoteUpdate.php";
        url=url+"?username="+author+"&message="+content+"&date="+date+"&image_url="+image;
//        String url ="http://www.google.com";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("QOSfan Has Been Updated.")
                                .setMessage("Would You Also Like To Retweet This Status?")
                                .setCancelable(false)
                                .setPositiveButton("Yes, Please", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        RetweetWifi(id2);
                                        return;
                                    }
                                })
                                .setNegativeButton("No, Thanks", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        return;
                                    }
                                })
                                .show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private static class MyTaskParams {
        String httpaddress;
        String username;
        String message;
        String date;
        String image_url;
        long id2;

        MyTaskParams(String httpaddress,String username, String message, String date, String image_url, long id2) {
            this.httpaddress = httpaddress;
            this.username = username;
            this.message = message;
            this.date = date;
            this.image_url = image_url;
            this.id2 = id2;
        }
    }

    private class UpdateSite extends AsyncTask<MyTaskParams, Void, Long>{
        protected Long doInBackground(MyTaskParams... parms){
            String string = parms[0].httpaddress;
            String username=parms[0].username;
            String message=parms[0].message;
            String date=parms[0].date;
            String image_url=parms[0].image_url;
            long id2 = parms[0].id2;
            string=string+"?username="+username+"&message="+message+"&date="+date+"&image_url="+image_url;
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(string);
            try {
                // Execute HTTP Get Request
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity e=response.getEntity();
                String data= EntityUtils.toString(e);
                data.length();
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
                try {
                    wait();
                } catch (InterruptedException e1) {

                }
            }
            return id2;
        }
        @Override
        protected void onPostExecute(final Long id2) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("MyDumfries or QOSfan Has Been Updated.")
                    .setMessage("Would You Also Like To Retweet This Status?")
                    .setCancelable(false)
                    .setPositiveButton("Yes, Please", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            RetweetWifi(id2);
                            return;
                        }
                    })
                    .setNegativeButton("No, Thanks", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            return;
                        }
                    })
                    .show();
        }
    }

    private class DisplayAvator extends AsyncTask<String, Void, Boolean> {
        URL url = null;
        Bitmap avt;

        protected Boolean doInBackground(String... strings) {
            try {
                url = new URL(strings[0]);
            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try {
                avt = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    ImageView image = (ImageView) findViewById(R.id.avatar);
                    image.setImageBitmap(avt);
                }
            });
            return null;
        }
    }

    void PostSMS() {
        Intent newIntent = new Intent(MainActivity.this,
                PostTweet.class);
        newIntent.putExtra("source", 1);
        startActivity(newIntent);
    }

    void PostWifi() {
        Intent newIntent = new Intent(MainActivity.this,
                PostTweet.class);
        newIntent.putExtra("source", 2);
        startActivity(newIntent);
    }

    void RetweetWifi(long id) {
        Tweet o = tweets.get((int) id);
        long idOfStatusToRetweet = o.idOfStatusToRetweet;
        Intent newIntent = new Intent(MainActivity.this,
                PostTweet.class);
        newIntent.putExtra("source", 3);
        newIntent.putExtra("idOfStatusToRetweet", idOfStatusToRetweet);
        startActivity(newIntent);
    }

    void RetweetSMS(long id) {
        Tweet o = tweets.get((int) id);
        String ReTweet = "";
        ReTweet = "RT @" + o.screen + ": " + o.content;
        ReTweet = ReTweet.substring(0, 140);
        Intent newIntent = new Intent(MainActivity.this,
                PostTweet.class);
        newIntent.putExtra("source", 4);
        newIntent.putExtra("ReTweet", ReTweet);
        startActivity(newIntent);
    }

    void SaveToDo(long id) {
        EventDataSQLHelper booksData;
        booksData = new EventDataSQLHelper(this);
        final int id2 = (int) id;
        Tweet o = tweets.get((int) id);
        String content = o.content;
        String author = o.author + " (@" + o.screen + ")";
        String date = o.uformdate;

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = content + " : " + author;
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));

    /*final String pathtosdcard = Environment.getExternalStorageDirectory().getPath();
    final String sdpath = pathtosdcard + "/mydiary/";
    File dbfile = new File(sdpath + "/librarybooks.db");

    SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
    ContentValues values = new ContentValues();
        values.put(EventDataSQLHelper.TODO, content + " : " + author);
        values.put(EventDataSQLHelper.TIMESTAMP, date);
    String sortorder = "_id DESC";
    String sql = "create table if not exists "
            + EventDataSQLHelper.TABLE3 + "( " + BaseColumns._ID
            + " integer primary key, " + EventDataSQLHelper.TIMESTAMP
            + " date default CURRENT_DATE, " + EventDataSQLHelper.TODO
            + " text, " + EventDataSQLHelper.FLAG
            + " integer default 0);";
        Log.d("EventsData", "onCreate: " + sql);
        db.execSQL(sql);
    Cursor cursor = db.query(EventDataSQLHelper.TABLE3, null,
            BaseColumns._ID + " < ?", new String[]{"9999"}, null,
            null, sortorder, "1");
    startManagingCursor(cursor);
    int mid = 0;
        while (cursor.moveToNext()) {
        mid = cursor.getInt(0);
    }
    mid = mid + 1;
        values.put(BaseColumns._ID, mid);
        db.insert(EventDataSQLHelper.TABLE3, null, values);

        new AlertDialog.Builder(this)
                .setTitle("Tweet Added.")
                .setMessage("This Tweet has been added as a To-Do in MyDiary.")
                .setCancelable(false)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int id) {
            return;
        }
    })
            .show();
*/}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        uiHelper.onActivityResult(requestCode, resultCode, data); 
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        //      uiHelper.onSaveInstanceState(savedState);
    }

    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            for (String url : urls) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(
                            new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
//			textView.setText(Html.fromHtml(result));
        }
    }

    public void readWebpage() {
        DownloadWebPageTask task = new DownloadWebPageTask();
        task.execute("https://www.facebook.com/?ref=tn_tnmn");

    }

    public void SearchTweets() {
        showDialog(SEARCH_DIALOG_ID);
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SEARCH_DIALOG_ID:
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout = inflater.inflate(R.layout.search_dialog,
                        (ViewGroup) findViewById(R.id.root));
                final EditText search = (EditText) layout
                        .findViewById(R.id.EditText_IP);

                // ... other required overrides do nothing
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(layout);
                // Now configure the AlertDialog
                builder.setTitle("Search Tweets");
                builder.setMessage("Enter Search Term (Case Sensitive)");
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                MainActivity.this.removeDialog(SEARCH_DIALOG_ID);
                            }
                        });
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String searchterm = search.getText().toString();
                                SearchTweets2(searchterm);
                                MainActivity.this.removeDialog(SEARCH_DIALOG_ID);
                            }
                        });

                // Create the AlertDialog and return it
                AlertDialog searchDialog = builder.create();
                return searchDialog;
        }
        return null;
    }

    public void SearchTweets2(String searchterm) {
        searching = 1;
        ArrayList<Tweet> listClone = new ArrayList<Tweet>();
        for (Tweet tweet : tweets) {
            if (tweet.content.contains(searchterm) || tweet.author.contains(searchterm)) {
                listClone.add(tweet);
            }
        }
        TweetListAdaptor adaptor = new TweetListAdaptor(activity, R.layout.list_item, listClone);
        setListAdapter(adaptor);
        Button prevButton = (Button) findViewById(R.id.previous_tweets);
        prevButton.setText("PREVIOUS");
        Button refreshButton = (Button) findViewById(R.id.refresh_tweets);
        refreshButton.setText("REFRESH");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menuoptions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.last100_menu_item:
                Reload100Tweets();
                return true;

            case R.id.smstweet_menu_item:
                PostSMS();
                return true;

            case R.id.wifitweet_menu_item:
                PostWifi();
                return true;

            case R.id.searchtweet_menu_item:
                SearchTweets();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}