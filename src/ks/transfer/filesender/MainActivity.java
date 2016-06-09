package ks.transfer.filesender;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import tistory.whdghks913.fileexplorer.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SdCardPath")
public class MainActivity extends Activity {

	static int kill = 0;

	// Socket / IP / PORT
	private String ip = "203.241.228.122";
	private int port = 9999;

	private static final int GALLERY_INTENT_CALLED = 302;
	private TextView tv;
	private Button sendbtn;
	private Button listbtn;
	
	// Prograss Dialog 변수
	public Activity act = this;
	private volatile Thread theProgressBarThread1;
	public int CurrentPosition2= 0;
	private volatile Thread theProgressBarThread2;
	ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		startActivity(new Intent(this, splashActivity.class));
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 상단바 없애기
		setContentView(R.layout.activity_main);

		tv = (TextView) findViewById(R.id.TextView01);
		sendbtn = (Button) findViewById(R.id.sendbtn);
		listbtn = (Button) findViewById(R.id.listbtn);
		
		sendbtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if (tv.getText().toString() == null	|| tv.getText().toString().equals("")) {
					Toast.makeText(getApplicationContext(), "파일을 선택해주세요.",
							Toast.LENGTH_LONG).show();
				}
				else
				{
					//startProgressbarThread2();
					
					new Thread(new Runnable() {
						public void run() {
							try {
								Socket socket = new Socket(ip, port);
								//OutputStream os = socket.getOutputStream();
								PrintWriter out = new PrintWriter(new BufferedWriter(new
										OutputStreamWriter(socket.getOutputStream())), true);
								
								out.println(array[6]);
								out.flush();
								
								File file_path = new File(selected_filePath);
								FileInputStream fis = new FileInputStream(file_path);
								//BufferedInputStream bis = new BufferedInputStream(fis);
								
								DataInputStream dis = new DataInputStream(new
										FileInputStream(new File(selected_filePath)));
										               DataOutputStream dos = new
										DataOutputStream(socket.getOutputStream());
								byte[] buffer = new byte[4096];
								int readBytes;
								
								//while ((readBytes = bis.read(buffer)) > 0) {
								while(dis.read(buffer)>0) {
									if (out == null) {
										Log.d("Test", "OutputStream is null");
									}
									//os.write(buffer, 0, readBytes);
									dos.write(buffer);
									dos.flush();
								}

								//os.flush();
								//os.close();
								dos.close();

							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}).start();
					Toast.makeText(getApplicationContext(), "파일전송이 완료되었습니다.",
							Toast.LENGTH_LONG).show();

					tv.setText("");
				}
			}
		});

		listbtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("*/*");
				//String[] mimetypes = { "image/*", "video/*", "audio/*" };
				String[] mimetypes = { "image/*", "video/*"};
				intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
				startActivityForResult(intent, GALLERY_INTENT_CALLED);
			}
		});
	}

	// File Path / File Name 메서드
	static String selected_filePath = null;
	static String selected_filename = null;
	static String[] array;

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == GALLERY_INTENT_CALLED) {
				if (null == data)
					return;

				Uri selectedImageUri = data.getData();

				// MEDIA GALLERY
				selected_filePath = ImageFilePath.getPath(
						getApplicationContext(), selectedImageUri);

				selected_filename = selected_filePath.split("/")[6];
				
				array = selected_filePath.split("/");

				Log.d("Test", "File path : " + selected_filePath);
				Log.d("Test", "File name : " + array[6]);

				tv.setText(selected_filename);
			}
		}
	}

	public void _finish() {
		moveTaskToBack(true);
		finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) { // 뒤로가기 키를 눌렀을때
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (kill == 0) {
				kill++;
				Toast.makeText(this, "종료하시려면 한번더 누르세요", Toast.LENGTH_SHORT)
						.show();
			} else
				_finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	// progress Dialog
	public synchronized void startProgressbarThread2() {
    	if(theProgressBarThread2==null) {
    		theProgressBarThread2 = new Thread(null,backgroundThread2,"startProgressBarThread2");
    		CurrentPosition2 = 0;
    		theProgressBarThread2.start();
    		
    		progressDialog =new ProgressDialog(act);
    		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    		progressDialog.setMessage(". . .");
    		progressDialog.incrementProgressBy(1);
    		progressDialog.setMax(100);
    		progressDialog.setProgress(0);
    		progressDialog.show();
    	}
    }
    public synchronized void stopProgressBarThread2() {
    	if(theProgressBarThread2 != null) {
    		Thread tmpThread = theProgressBarThread2;
    		theProgressBarThread2 = null;
    		tmpThread.interrupt();
    	}
    	if(progressDialog !=null) {
    		progressDialog.dismiss();
    	}
    }
    
    private Runnable backgroundThread2 =new Runnable() {
    	public void run() {
    		if(Thread.currentThread() == theProgressBarThread2) {
    			CurrentPosition2=0;
    			final int total=100;
    			
    			while(CurrentPosition2<total) {
    				try {
    					progressBarHandle2.sendMessage(progressBarHandle2.obtainMessage());
    					Thread.sleep(100);
    				}
    				catch(final InterruptedException e) {
    					return;
    				}
    				catch(final Exception e){
    					return;
    				}
    			}
    		}
    	}
    	
        Handler progressBarHandle2 = new Handler() {
        	public void handleMessage(Message msg) {
        		CurrentPosition2++;
        		progressDialog.setProgress(CurrentPosition2);
        		progressDialog.setMessage(selected_filename + " 를 전송중입니다.");
        		if(CurrentPosition2 == 100) {
        			stopProgressBarThread2();
        		}
        	}
        };
    };
}