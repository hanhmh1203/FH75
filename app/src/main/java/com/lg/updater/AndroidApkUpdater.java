package com.lg.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lab.sodino.language.util.Strings;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.vanch.vhxdemo.R;

public abstract class AndroidApkUpdater {
	protected abstract String getPackageName();
	protected abstract Version getServerVersion() throws Exception;
	
	Context context;
	Handler handler;
	ProgressDialog progressDialog;
	AlertDialog alertDialog;
	boolean stop = false;
	String filename;
	
	public AndroidApkUpdater(Context context) {
		this.context = context;
		handler = new Handler(context.getMainLooper());
		filename = "vh75demo.apk";
	}
	
	private int getVerCode() {
		int verCode = -1;
		try {
			verCode = context.getPackageManager().getPackageInfo(
					getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
		}
		return verCode;
	}

	private String getVerName() {
		String verName = "";
		try {
			verName = context.getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
		}
		return verName; 
	}
	
	private void update(long timeout) {
		ExecutorService executor = Executors.newSingleThreadExecutor();  
		FutureTask<Version> task = new FutureTask<AndroidApkUpdater.Version>(new Callable<Version>() {

			@Override
			public Version call() throws Exception {
				return getServerVersion();
			}
		});
		executor.execute(task);  
		try {
			Version serverVersion = task.get(timeout, TimeUnit.MILLISECONDS);
			if (serverVersion == null) {
				toast(Strings.getString(R.string.msg_check_update_fail));
				return;
			}
			
			Log.i("update", serverVersion.toString());
			
			if (serverVersion.verCode > getVerCode()) {
				startUpdate(serverVersion);
			} 
		} catch (InterruptedException e) {
			toast(Strings.getString(R.string.msg_check_update_fail));
		} catch (ExecutionException e) {
			toast(Strings.getString(R.string.msg_check_update_fail));
		} catch (TimeoutException e) {
			toast(Strings.getString(R.string.msg_check_update_fail));
		} finally {
			executor.shutdown();
		}
	}
	
	public void update() {
		new Thread() {
			@Override
			public void run() {
				update(5000);
			}
		}.start();
	}
	
	public static class Version implements Serializable {
		private static final long serialVersionUID = 5448272923900966062L;
		
		
		public Version(String verName, int verCode, String versionUrl) {
			super();
			this.verName = verName;
			this.verCode = verCode;
			this.versionUrl = versionUrl;
		}
		
		public String verName;
		public int verCode;
		public String versionUrl;


		@Override
		public String toString() {
			return "Version [verName=" + verName + ", verCode=" + verCode
					+ ", versionUrl=" + versionUrl + "]";
		}		
	}
	
    void install() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), filename)),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
    
    void downFile(final String url) {
		new Thread() {
			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				HttpResponse response;
				try {
					response = client.execute(get);
					HttpEntity entity = response.getEntity();
					long length = entity.getContentLength();
					InputStream is = entity.getContent();
					FileOutputStream fileOutputStream = null;
					if (is != null) {
						File file = new File(Environment.getExternalStorageDirectory(),
								filename);
						fileOutputStream = new FileOutputStream(file);
						byte[] buf = new byte[1024*10];
						int ch = -1;
						int count = 0;
						while ((ch = is.read(buf)) != -1 && !stop) {
							fileOutputStream.write(buf, 0, ch);
							
							count += ch;
							Log.i("update", "download " + count);
							if (length > 0) {
								int progress = (int) (count * 100/ length) ;
								setDownloadProgress(progress);
							}
						}
					}
					fileOutputStream.flush();
					if (fileOutputStream != null) {
						fileOutputStream.close();
					}
				} catch (ClientProtocolException e) {
					toast(Strings.getString(R.string.msg_download_apk_fail));
					stopProgressDialog();
				} catch (IOException e) {
					toast(Strings.getString(R.string.msg_download_apk_fail));
					stopProgressDialog();
				}
			}
		}.start();
	}
    
    private void setDownloadProgress(final int progress) {
    	handler.post(new Runnable() {
			@Override
			public void run() {
				progressDialog.setProgress(progress);
				Log.i("update", "progress " + progress);
				if (progress >= 100) {
					progressDialog.dismiss();
					install();
				}
			}
		});
    }
    
    private void startUpdate(final Version version) {
    	
    	handler.post(new Runnable() {
			
			@Override
			public void run() {
				createAlertDialog(version);
				alertDialog.show();
			}
		});
    }
    
    private void toast(final String msg) {
    	handler.post(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
		});
    }
    
    private void stopProgressDialog() {
    	handler.post(new Runnable() {
			
			@Override
			public void run() {
				progressDialog.dismiss();
			}
		});
    }
    
    private void createAlertDialog(final Version version) {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(Strings.getString(R.string.msg_cur_version))
    	.append(getVerName()).append(Strings.getString(R.string.msg_find_new_version)).append(version.verName);
    	alertDialog = new AlertDialog.Builder(context)
    	.setTitle(Strings.getString(R.string.msg_soft_update))
    	.setMessage(buffer.toString())  
    	.setPositiveButton(Strings.getString(R.string.msg_update), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				createProgressDialog();
				alertDialog.dismiss();
				progressDialog.show();
				downFile(version.versionUrl);
			}
		})
		.setNegativeButton(Strings.getString(R.string.msg_update_later), new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog,  
					int whichButton) {  
				alertDialog.dismiss();
			}  
		})
    	.create();
    }
    
    private void createProgressDialog() {
    	progressDialog = new ProgressDialog(context);
    	progressDialog.setIcon(android.R.drawable.ic_dialog_alert);
    	progressDialog.setTitle(Strings.getString(R.string.msg_soft_update));
    	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	progressDialog.setMax(100);
    	
    	progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                Strings.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	stop = true;
            	progressDialog.dismiss();
            }
        });
    }
}
