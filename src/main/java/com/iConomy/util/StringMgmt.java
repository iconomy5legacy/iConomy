package com.iConomy.util;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class StringMgmt {
	public static String[] remFirstArg(String[] arr) {

		return remArgs(arr, 1);
	}

	public static String[] remLastArg(String[] arr) {

		return subArray(arr, 0, arr.length - 1);
	}

	public static String[] remArgs(String[] arr, int startFromIndex) {

		if (arr.length == 0)
			return arr;
		else if (arr.length < startFromIndex)
			return new String[0];
		else {
			String[] newSplit = new String[arr.length - startFromIndex];
			System.arraycopy(arr, startFromIndex, newSplit, 0, arr.length - startFromIndex);
			return newSplit;
		}
	}

	public static String[] subArray(String[] arr, int start, int end) {

		if (arr.length == 0)
			return arr;
		else if (end < start)
			return new String[0];
		else {
			int length = end - start;
			String[] newSplit = new String[length];
			System.arraycopy(arr, start, newSplit, 0, length);
			return newSplit;
		}
	}

	/**
	 * Returns strings that start with a string
	 *
	 * @param list strings to check
	 * @param startingWith string to check with list
	 * @return strings from list that start with startingWith
	 */
	public static List<String> filterByStart(List<String> list, String startingWith) {
		if (list == null || startingWith == null) {
			return Collections.emptyList();
		}
		return list.stream().filter(name -> name.toLowerCase(Locale.ROOT).startsWith(startingWith.toLowerCase(Locale.ROOT))).collect(Collectors.toList());
	}
}
