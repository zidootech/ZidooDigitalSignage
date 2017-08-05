package com.zidoo.test.hdmi.record;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RecordUtil {

	public static boolean isHdcp() {
		try {
			String state = getRunTimeStr("/sys/class/switch/rx_hdcp/state");
			if (state.trim().equals("1")) {
				return true;
			}
			return isHdcp_nuplay();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean isHdcp_nuplay() {
		try {
			BufferedReader br = new BufferedReader(new FileReader("/sys/devices/platform/98037000.hdmirx/hdcp_status"));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("None")) {
					br.close();
					return false;
				}
			}
			br.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static String getRunTimeStr(String path) {
		String mac = null;
		try {
			Process p = Runtime.getRuntime().exec("cat " + path);
			InputStream is = p.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader bf = new BufferedReader(isr);
			String line = null;
			if ((line = bf.readLine()) != null) {
				mac = line;
			}
			bf.close();
			isr.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mac;
	}

}
