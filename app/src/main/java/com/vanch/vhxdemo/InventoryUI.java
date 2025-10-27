package com.vanch.vhxdemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import lab.sodino.language.util.Strings;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Vibrator;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vanch.vhxdemo.AccessUI.StatusChangeEvent;
import com.vanch.vhxdemo.helper.Utility;

import de.greenrobot.event.EventBus;

public class InventoryUI extends Fragment implements OnItemLongClickListener {

	MediaPlayer findEpcSound;
	AudioManager audioManager;
	StringBuilder builder;
	private Vibrator vibrator;
	long[] pattern = { 100, 400, 100, 400 }; // 停止   开启   停止   开启
	/**
	 * inventory terminal event
	 * @author liugang
	 */
	public static class InventoryTerminal {
	}

	private static final String TAG = "inventory";

	public static class TimeoutEvent {
	}

	/**
	 * an epc discovered
	 * @author liugang
	 */
	public static class EpcInventoryEvent {
	}

	/**
	 * inventory button clicked
	 * @author liugang
	 */
	public static class InventoryEvent {
	}

	public static InventoryUI me;

	ListView listView;
	Button btnInventory, btnStop, btnSave, btnSubmit;
	TextView txtCount;
	ListAdapter adapter;
	List<Epc> epcs = new ArrayList<Epc>();
	Map<String, Integer> epc2num = new ConcurrentHashMap<String, Integer>();
	ImageView statusOnImageView, statusTxImageView, statusRxImageView;
	Status on = Status.ON, tx = Status.BAD, rx = Status.BAD;

	ProgressDialog progressDialog;

	boolean stoped = false;
	int readCount = 0;

	boolean inventoring = false; // 发现按钮

	InventoryThread inventoryThread;
	Timer timer;

	public InventoryUI() {
	}

	class InventoryThread extends Thread {
		int len, addr, mem;
		Strings mask;

		public InventoryThread(int len, int addr, int mem, Strings mask) {
			this.len = len;
			this.addr = addr;
			this.mem = mem;
			this.mask = mask;
		}

		public void run() {
			try {
				LinkUi.currentDevice.listTagID(1, 0, 0);
				Log.i(TAG, "start read!!");
				LinkUi.currentDevice.getCmdResult();
				Log.i(TAG, "read ok!!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		me = this;
		builder = new StringBuilder();
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setCanceledOnTouchOutside(false);

		View view = inflater.inflate(R.layout.inventory, null);
		listView = (ListView) view.findViewById(R.id.list_rfid);

		statusOnImageView = (ImageView) view.findViewById(R.id.status_on);
		statusTxImageView = (ImageView) view.findViewById(R.id.status_tx);
		statusRxImageView = (ImageView) view.findViewById(R.id.status_rx);

		btnSave = (Button) view.findViewById(R.id.btn_save);
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String string2 = "=============================";
				Set<Entry<String, Integer>> entryseSet = epc2num.entrySet(); 
				for(Entry<String, Integer> entry:entryseSet){ 
					//System.out.println(entry.getKey()); 
					builder.append(entry.getKey()+"\n");	
				} 
				if(LinkUi.currentDevice != null)
				{
				/** 保存EPC */
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					File sdCardDir = Environment.getExternalStorageDirectory();//获取SDCard目录,2.2的时候为:/mnt/sdcart 2.1的时候为：/sdcard，所以使用静态方法得到路径会好一点。
					File saveFile = new File(sdCardDir, "abc.txt");
					FileOutputStream outStream;
					/** 追加时间 */
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
					/** new Date()为获取当前系统时间 */
					String date = df.format(new Date());
					date += "\r\n";
					if(!saveFile.exists())
					{
						try {
							saveFile.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					try {
						outStream = new FileOutputStream(saveFile,true);
						outStream.write("\r\n".getBytes());
						outStream.write(builder.toString().getBytes());
						outStream.write(date.getBytes());
						outStream.write("==========================".getBytes());
						outStream.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Toast.makeText(getActivity(), "保存成功", Toast.LENGTH_SHORT).show();
				}
				}
				else
				{
					Utility.WarningAlertDialg(getActivity(),
							Strings.getString(R.string.msg_waring),
							Strings.getString(R.string.msg_device_not_connect))
							.show();
				}
			}
		});
		
		btnSubmit = (Button) view.findViewById(R.id.btn_submit);
		btnSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement API submit functionality
				submitInventoryToServer();
			}
		});
		
		btnInventory = (Button) view.findViewById(R.id.btn_inventory);
		btnInventory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/* 这一部分注释掉才能读取数据
				 * This is a test!!!
				if (ConfigUI.getConfigCheckTest(getActivity())) {
					// progressDialog.setMessage(("test"));
					// progressDialog.show();
					for (Epc epc : epcs) {
						addEpcTest(epc.getId());
						txtCount.setText("" + epc.getCount());
					}
					refreshList();
					return;
				}*/

				if (LinkUi.currentDevice != null) {
					if (!inventoring) {
						inventoring = !inventoring;
						readCount = 0;
						/** 列表有多少行，即epc2num的长度是多少，就显示多少个 */
						txtCount.setText("" + epc2num.size());
						
						//txtCount.setText("" + readCount);  这一句不能准确统计条数
						
						btnInventory
								.setBackgroundResource(R.drawable.stop_btn_press);
						btnInventory.setText(Strings.getString(R.string.stop));
						clearList();
						EventBus.getDefault().post(new InventoryEvent());
						setRx(Status.ON);
						// progressDialog.setMessage(Strings.getString(R.string.inventory)+"......");
						// progressDialog.show();
					} else {
						inventoring = !inventoring;
						// setRx(Status.OFF);
						// btnInventory.setBackgroundResource(R.drawable.inventory_btn_press);
						// btnInventory.setText(Strings.getString(R.string.inventory));

						progressDialog.setMessage(Strings
								.getString(R.string.msg_inventory_stoping));
						;
						progressDialog.show();
					}
				} else {
					Utility.WarningAlertDialg(getActivity(),
							Strings.getString(R.string.msg_waring),
							Strings.getString(R.string.msg_device_not_connect))
							.show();
				}
			}
		});

		txtCount = (TextView) view.findViewById(R.id.txt_count);
		listView.setOnItemLongClickListener(this);

		updateLang();

		findEpcSound = new MediaPlayer();
		// mediaPlayer01.start();
		audioManager = (AudioManager) getActivity().getSystemService(
				Context.AUDIO_SERVICE);

		vibrator = (Vibrator) getActivity().getSystemService(
				Context.VIBRATOR_SERVICE);

		// vibrator.vibrate(pattern,-1); //重复两次上面的pattern 如果只想震动一次，index设为-1

		return view;
	}

	private void playFindEpcSound() {
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

		try {
			findEpcSound.setDataSource(getActivity(), alert);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
			findEpcSound.setAudioStreamType(AudioManager.STREAM_ALARM);
			findEpcSound.setLooping(false);
			try {
				findEpcSound.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			findEpcSound.start();
		}
	}

	private void shock() {
		vibrator.vibrate(pattern, -1);
	}

	private void freshStatus() {
		Map<ImageView, Status> map = new HashMap<ImageView, Status>();
		map.put(statusOnImageView, on);
		map.put(statusTxImageView, tx);
		map.put(statusRxImageView, rx);

		for (ImageView imageView : map.keySet()) {
			Status status = map.get(imageView);
			switch (status) {
			case ON:
				imageView.setImageResource(R.drawable.ic_on);
				break;
			// case OFF:
			// imageView.setImageResource(R.drawable.ic_off);
			// break;
			case BAD:
				imageView.setImageResource(R.drawable.ic_unnormal);
				break;
			case INCOMPLETE:
				imageView.setImageResource(R.drawable.ic_unnormal);
				break;
			default:
				break;
			}
		}
	}

	public Status getOn() {
		return on;
	}

	public void setOn(Status on) {
		this.on = on;
		EventBus.getDefault().post(new StatusChangeEvent());
	}

	public Status getTx() {
		return tx;
	}

	public void setTx(Status tx) {
		this.tx = tx;
		EventBus.getDefault().post(new StatusChangeEvent());
	}

	public Status getRx() {
		return rx;
	}

	public void setRx(Status rx) {
		this.rx = rx;
		EventBus.getDefault().post(new StatusChangeEvent());
	}

	/**
	 * clear id list
	 */
	private void clearList() {
		if (epc2num != null && epc2num.size() > 0) {
			epc2num.clear();
			refreshList();
		}
	}

	/**
	 * 响应inventory按钮
	 * 
	 * @param e
	 */
	public void onEventBackgroundThread(InventoryEvent e) {
		// 1.因为要跟新的VH75的一致，所以要加0B命令
		// 设置手机进入读写器模式，即模块电源打开，1--打开，0--关闭
		int i = 0;
		// while (i < 2) {
			try {
				//LinkUi.currentDevice.SetReaderMode((byte) 1);
				byte[] res = LinkUi.currentDevice.getCmdResultWithTimeout(3000);
				if (!VH73Device.checkSucc(res)) {
					// TODO show error message
				// if (i > ) {
						inventoring = false;
						EventBus.getDefault().post(new InventoryTerminal());
						return;
				// }
				// i++;
				// continue;
				// } else {
				// break;
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (TimeoutException e1) { // timeout
				Log.i(TAG, "Timeout!!@");
			}

		// }

		while (inventoring) {
			// if (inventoring) {//this is a test!!!
			long lnow = android.os.SystemClock.uptimeMillis(); // 起始时间
			doInventory();
			while (true) {
				long lnew = android.os.SystemClock.uptimeMillis(); // 结束时间
				if (lnew - lnow > 500) {
					break;
				}
			}
		}
		EventBus.getDefault().post(new InventoryTerminal());

		// 片断code开始 try { // 1.因为要跟新的VH75的一致，所以要加0B命令 //
		// 设置手机进入读写器模式，即模块电源打开，1--打开，0--关闭
		try {
			//LinkUi.currentDevice.SetReaderMode((byte) 1);
			byte[] ret = LinkUi.currentDevice.getCmdResultWithTimeout(3000);
			if (!VH73Device.checkSucc(ret)) { // TODO show error message //
				Log.i(TAG, "SetReaderMode Fail!"); // return;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (TimeoutException e1) { // timeout
			Log.i(TAG, "Timeout!!@");
		}
		// 片断code结束
	}

	private void doInventory() {
		try {
			LinkUi.currentDevice.listTagID(1, 0, 0);
			byte[] ret = LinkUi.currentDevice.getCmdResultWithTimeout(3000);
			if (!VH73Device.checkSucc(ret)) {
				// TODO show error message
				return;
			}
			VH73Device.ListTagIDResult listTagIDResult = VH73Device
					.parseListTagIDResult(ret);
			addEpc(listTagIDResult);
			EventBus.getDefault().post(new EpcInventoryEvent());
			// read the left id
			int left = listTagIDResult.totalSize - 8;
			/** 本项目中未使用到ED命令，这部分可注释掉 */
			while (left > 0) {
				if (left >= 8) {
					LinkUi.currentDevice.getListTagID(8, 8);
					left -= 8;
				} else {
					LinkUi.currentDevice.getListTagID(8, left);
					left = 0;
				}
				byte[] retLeft = LinkUi.currentDevice
						.getCmdResultWithTimeout(3000);
				if (!VH73Device.checkSucc(retLeft)) {
					Utility.showTostInNonUIThread(getActivity(),
							Strings.getString(R.string.msg_command_fail));
					continue;
				}
				VH73Device.ListTagIDResult listTagIDResultLeft = VH73Device
						.parseGetListTagIDResult(retLeft);
				addEpc(listTagIDResultLeft);
				EventBus.getDefault().post(new EpcInventoryEvent());
			}
			// EventBus.getDefault().post(new InventoryTerminal());
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (TimeoutException e1) { // timeout
			// e1.printStackTrace();
			// EventBus.getDefault().post(new TimeoutEvent());
			Log.i(TAG, "Timeout!!!");
		}
	}

	public void onEventMainThread(TimeoutEvent e) {
		progressDialog.dismiss();
		Utility.showDialogInNonUIThread(getActivity(),
				Strings.getString(R.string.msg_waring),
				Strings.getString(R.string.msg_timeout));
		inventoring = false;
		setRx(Status.BAD);
		btnInventory.setBackgroundResource(R.drawable.inventory_btn_press);
		btnInventory.setText(Strings.getString(R.string.inventory));
		EventBus.getDefault().post(new InventoryTerminal());
	}

	/**
	 * inventory 结束
	 * @param e
	 */
	public void onEventMainThread(InventoryTerminal e) {
		progressDialog.dismiss();
		inventoring = false;
		findEpcSound.stop(); // 当停止读卡的时候，铃声暂停，当重新读卡时则重新调用铃声
		setRx(Status.BAD);
		btnInventory.setBackgroundResource(R.drawable.inventory_btn_press);
		btnInventory.setText(Strings.getString(R.string.inventory));
	}

	private void addEpc(VH73Device.ListTagIDResult list) {
		ArrayList<byte[]> epcs = list.epcs;
		for (byte[] bs : epcs) {
			String string = Utility.bytes2HexString(bs);
			if (!ConfigUI.getConfigSkipsame(getActivity())) {
				if (epc2num.containsKey(string)) {
					epc2num.put(string, epc2num.get(string) + 1);
				} else {
					epc2num.put(string, 1);
				}
			} else {
				epc2num.put(string, 1);
			}
			// readCount++;
			// 改为下面表格有多少行，则为多少行显示,add by martrin 20131114
			readCount = epc2num.size();
		}
	}

	private void addEpcTest(String strEpc) {
		if (epc2num.containsKey(strEpc)) {
			epc2num.put(strEpc, epc2num.get(strEpc) + 1);
		} else {
			epc2num.put(strEpc, 1);
		}
		readCount = epc2num.size();
	}

	/**
	 * when inventory an epc, refresh the list
	 * 
	 * @param e
	 */
	public void onEventMainThread(EpcInventoryEvent e) {
		refreshList();
		txtCount.setText("" + readCount);
		//
		if (ConfigUI.getConfigCheckshock(getActivity()))
			shock();

		if (ConfigUI.getConfigChecksound(getActivity()))
			playFindEpcSound();
	}

	/**
	 * 状态改变
	 * 
	 * @param e
	 */
	public void onEventMainThread(StatusChangeEvent e) {
		freshStatus();
	}

	public void onEventMainThread(ConfigUI.LangChanged e) {
		updateLang();
	}

	/**
	 * 刷新列表
	 */
	private void refreshList() {
		adapter = new IdListAdaptor();
		listView.setAdapter(adapter);
		// listView.scrollTo(0, adapter.getCount());
		listView.setSelection(listView.getAdapter().getCount() - 1);
	}

	@Override
	public void onResume() {
		Log.i(TAG, "onResume");
		EventBus.getDefault().register(this);
		refreshList();
		if (LinkUi.currentDevice != null && LinkUi.currentDevice.isConnected())
			setOn(Status.ON);
		else
			setOn(Status.BAD);
		super.onResume();
	}

	private class IdListAdaptor extends BaseAdapter {
		@Override
		public int getCount() {
			return epc2num.size();
		}

		@Override
		public Object getItem(int position) {
			String[] ids = new String[epc2num.size()];
			epc2num.keySet().toArray(ids);
			return ids[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View view = inflater.inflate(R.layout.inventory_item_list, null);
			TextView rfidTextView = (TextView) view.findViewById(R.id.txt_rfid);
			TextView countTextView = (TextView) view
					.findViewById(R.id.txt_count_item);

			String id = (String) getItem(position);
			int count = epc2num.get(id);
			rfidTextView.setText(id);
			countTextView.setText("" + count);

			TextView textViewNoTitle = (TextView) view
					.findViewById(R.id.txt_no_title);
			textViewNoTitle.setText(Strings.getString(R.string.count_lable));
			return view;
		}
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		//findEpcSound.stop();
		findEpcSound.release();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	@Override
	public void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		String[] ids = new String[epc2num.size()];
		epc2num.keySet().toArray(ids);
		Epc epc = new Epc(ids[position], epc2num.get(ids[position]));
		EventBus.getDefault().postSticky(new AccessUI.EpcSelectedEvent(epc));
		Log.i(TAG, "epc selected " + epc);
		return true;
	}

	private void updateLang() {
		btnInventory.setText(Strings.getString(R.string.inventory));
		btnSave.setText(Strings.getString(R.string.save));
		refreshList();
	}

	public void onEventMainThread(VH73Device.GetCommandResultSuccess e) {
		if (e.isSuccess())
			setRxWithBlink(ConfigUI.cmd_timeout, Status.ON, Status.BAD);
		else
			setRx(Status.BAD);
	}

	public void onEventMainThread(VH73Device.SendCommandSuccess e) {
		if (e.isSuccess()) {
			setTxWithBlink(ConfigUI.cmd_timeout, Status.ON, Status.BAD);
		} else {
			setTx(Status.BAD);
		}
	}
	

	/**
	 * tx blink
	 * @param timeout
	 * @param from
	 * @param to
	 */
	private void setTxWithBlink(final long timeout, Status from, final Status to) {
		setTx(from);
		new Thread() {
			public void run() {
				try {
					sleep(timeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				new Thread() {
					public void run() {
						Looper.prepare();
						setTx(to);
					}
				}.start();
			}
		}.start();
	}

	private void setRxWithBlink(final long timeout, Status from, final Status to) {
		setRx(from);
		new Thread() {
			public void run() {
				try {
					sleep(timeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				new Thread() {
					public void run() {
						Looper.prepare();
						setRx(to);
					}
				}.start();
			}
		}.start();
	}
	
	/**
	 * Submit inventory data to server
	 * TODO: Implement API call functionality
	 */
	private void submitInventoryToServer() {
		if (epc2num.isEmpty()) {
			Toast.makeText(getActivity(), "No inventory data to submit", Toast.LENGTH_SHORT).show();
			return;
		}
		
		// TODO: Implement API call to submit inventory data
		// Data available: epc2num (Map<String, Integer>) contains EPC codes and counts
		
		Toast.makeText(getActivity(), "Submit functionality - To be implemented", Toast.LENGTH_SHORT).show();
		
		// Placeholder for future API implementation:
		// 1. Convert epc2num to JSON format
		// 2. Make HTTP POST request to server
		// 3. Handle response and show success/error message
	}
}
