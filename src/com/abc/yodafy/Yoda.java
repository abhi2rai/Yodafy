package com.abc.yodafy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;


public class Yoda extends Activity {
	
	String URL="https://yoda.p.mashape.com/yoda?sentence=";
	private ProgressDialog progress;
	private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoda);
        
        if(android.os.Build.VERSION.SDK_INT == 19){
        	setStatusBarColor("#7AA42F");
        }
        
        setViewBackgroundColor(this.getWindow().getDecorView(),"#7AA42F");
        
        Typeface fontLight = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Light.ttf");
        Typeface fontReg = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Regular.ttf");
        
        setActionBarFont(fontReg,25);
        
        EditText input = (EditText)findViewById(R.id.inputtext);
        input.setTypeface(fontLight);
        
        EditText output = (EditText)findViewById(R.id.outputtext);
        output.setTypeface(fontLight);
        
        Button yoda = (Button)findViewById(R.id.yoda);
        yoda.setTypeface(fontReg);
    }
    
    private void setViewBackgroundColor(View view, String color){
    	view.setBackgroundColor(Color.parseColor(color));
    }
    
    private void setActionBarFont(Typeface type, int size){
    	int actionBarTitle = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarTitleView = (TextView) getWindow().findViewById(actionBarTitle);
        if(actionBarTitleView != null){
            actionBarTitleView.setTypeface(type);
            actionBarTitleView.setTextSize(size);
        }
    }
    
    private void setStatusBarColor(String color){
    	SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintColor(Color.parseColor(color));
    }
    
    private void hideKeyboard(EditText myEditText){
    	InputMethodManager imm = (InputMethodManager)getSystemService(
      	      Context.INPUT_METHOD_SERVICE);
      	imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_share_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        return true;
    }
    
    protected Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        EditText output = (EditText)findViewById(R.id.outputtext);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, output.getText().toString());
        return shareIntent;
    }
    
    public void yodafy(View view){
    	if(isConnected()){  
    		EditText myEditText = (EditText) findViewById(R.id.inputtext);
        	hideKeyboard(myEditText);
        	progress = new ProgressDialog(this);
        	progress.setMessage("Yodafying....");
        	progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        	progress.setIndeterminate(true);
        	progress.show();
        	progress.setCancelable(false);
        	
        	String inputString = myEditText.getText().toString();
        	
        	if(myEditText.getText().toString().equals("")){
        		progress.hide();
        		hideKeyboard((EditText) findViewById(R.id.inputtext));
    			showToast("Please enter some text");
        	}
        	else{
        		new RetrieveFeedTask().execute(inputString);
        	}
        }
		else{
			hideKeyboard((EditText) findViewById(R.id.inputtext));
			showToast("You need to be connected");
		}
    }
    
    private boolean isConnected(){
    	ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
    	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    	    if (networkInfo != null && networkInfo.isConnected()) 
    	    	return true;
    	    else
    	    	return false;	
    }
    
    
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
    
    private void showToast(String input){
    	Context context = getApplicationContext();
		CharSequence text = input;
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
    }
    
    class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        String result="";
    	InputStream inputStream = null;
		private Exception exception;

        protected String doInBackground(String... q) {
            try {
            	HttpClient httpclient = new DefaultHttpClient();
    			HttpGet request = new HttpGet(URL + URLEncoder.encode(q[0]));
    			request.addHeader("X-Mashape-Key","vBZ1RjhxfRmshasaahoedrfb7nEfp1WuIchjsnjFtj1EseuZ6O");
    			// make GET request to the given URL
    			
    			HttpParams httpParameters = new BasicHttpParams();
    			int timeoutConnection = 15000;
    			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
    			int timeoutSocket = 15000;
    			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
    			request.setParams(httpParameters);
    			
    			HttpResponse httpResponse = httpclient.execute(request);

    			// receive response as inputStream
    			inputStream = httpResponse.getEntity().getContent();
    			int status = httpResponse.getStatusLine().getStatusCode();
    			// convert inputstream to string
    			if(inputStream != null && status == 200)
    				result = convertInputStreamToString(inputStream);
    			else
    				result = "";
    			
    			return result;
            } catch (Exception e) {
                this.exception = e;
                result = "";
                return result;
            }
        }

        protected void onPostExecute(String feed) {
        	if(feed.equals("")){
                progress.hide();
        		hideKeyboard((EditText) findViewById(R.id.inputtext));
        		showToast("Something went wrong. May the force be with you");
        	}else{
        		EditText output = (EditText)findViewById(R.id.outputtext);
            	output.setText(feed);
            	mShareActionProvider.setShareIntent(createShareIntent());
            	progress.hide();
        	}
            // TODO: check this.exception 
            // TODO: do something with the feed
        }
    }
}
