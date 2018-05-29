package com.zidoo.test.hdim.udp;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import com.zidoo.test.zidooutil.MyLog;

public class UdpTool {
	private Context					mContext			= null;
	private ParcelFileDescriptor	mReadPfd			= null;
	private ParcelFileDescriptor	mWritePfd			= null;
	private MulticastSocket			multicastSocket		= null;
	private FileOutputStream		mFileOutputStream	= null;
	private DatagramSocket			udpSocket			= null;
	private Handler					mHandler			= null;
	private final static int		ERROR				= 0;
	private final static int		ERRORTIME			= 1000;
	public final static String		IP					= "239.0.0.1";
	public final static int		PORT				= 7879;
	private boolean					isUdping			= false;

	public UdpTool(Context mContext) {
		super();
		this.mContext = mContext;
		initHandler();
	}

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case ERROR:
					int result = (Integer) msg.obj;
					if (result == 1) {
						Toast.makeText(mContext, "udp error...", Toast.LENGTH_SHORT).show();
					}
					break;

				default:
					break;
				}
			}
		};
	}

	public ParcelFileDescriptor prepareIO() {
		try {

			ParcelFileDescriptor[] mPipe = null;
			try {
				mPipe = ParcelFileDescriptor.createPipe();
				if (mPipe == null) {
					return null;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			mReadPfd = mPipe[0];
			mWritePfd = mPipe[1];
			InetAddress receiveAddress = InetAddress.getByName(IP);
			if (!receiveAddress.isMulticastAddress()) {// 测试是否为多播地址
				mHandler.removeMessages(ERROR);
				mHandler.sendMessageDelayed(mHandler.obtainMessage(ERROR, 1), ERRORTIME);
				return null;
			}
			try {
				multicastSocket = new MulticastSocket(PORT);
				multicastSocket.setBroadcast(true);
			} catch (Exception e) {
				e.printStackTrace();
				MyLog.v("e1 = " + e.getMessage());
				return null;
			}
			mFileOutputStream = new FileOutputStream(mReadPfd.getFileDescriptor());
			return mWritePfd;
			// writUdp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void stopUdp() {
		isUdping = false;
	}

	public void startUdp() {
		isUdping = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				InputStream ips = new ParcelFileDescriptor.AutoCloseInputStream(mReadPfd);
				byte[] buffer = new byte[4 * 1024];
				FileDescriptor fd = null;
				try {
					fd = mFileOutputStream.getFD();
				} catch (IOException e) {
					e.printStackTrace();
				}
				boolean isError = true;
				while (isUdping) {
					try {
						int data = ips.available();
						if (data > 0) {
							int readSize = ips.read(buffer);
							if (readSize > 0) {
								if (fd != null && fd.valid()) {
									DatagramPacket packet = new DatagramPacket(buffer, 0, readSize, InetAddress.getByName(IP), PORT);
									if (multicastSocket != null) {
										multicastSocket.send(packet);// 发送报文
									} else {
										udpSocket.send(packet);// 发送报文
									}
									isError = true;
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						if (isError) {
							MyLog.e("udp writUdp error = " + e.getMessage());
						}
						isError = false;
					}
				}
				try {
					fd = null;
					MyLog.v("stop writUdp --- ");
					mFileOutputStream.close();
					mWritePfd.close();
					mReadPfd.close();
					ips.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					if (multicastSocket != null) {
						multicastSocket.close();
						multicastSocket = null;
						MyLog.v("stop multicastSocket close --- ");
					}
					if (udpSocket != null) {
						// udpSocket.disconnect();
						udpSocket.close();
						udpSocket = null;
						MyLog.v("stop udpSocket close --- ");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

}
