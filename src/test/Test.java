/**
 * hujiachun
 */
package test;

import com.netease.qa.emmagee.utils.MemoryInfo;

import android.test.AndroidTestCase;
import android.util.Log;

/**
 * @author hujiachun
 */
public class Test extends AndroidTestCase{
	private String TAG = "Test";
	
	public void test001(){
    String[][] info = MemoryInfo.parseMeminfo(23665);
		Log.e(TAG, info.toString());
		Log.e(TAG, "haha");
	}
	
	

}
