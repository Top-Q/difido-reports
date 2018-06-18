package il.co.topq.difido.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {

	public static String extractRegexGroup(String text, String regex, int groupIndex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		matcher.find();
		String groupText = matcher.group(groupIndex);
		return groupText;
	}
}
