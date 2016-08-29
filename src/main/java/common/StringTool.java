package common;

public class StringTool {
	private static char[] takeArr = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
			'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	/**
	 * generate random String
	 * @param len 
	 * @return random String 随机字符串
	 */
	public static String getRandomString(int len) {
		char[] result = new char[len];
		int iTotalChars = takeArr.length;
		for (int i = 0; i < len; i++) {
			int take = (int) (Math.random() * iTotalChars);
			result[i] = takeArr[take];
		}
		return new String(result);
	}
}
