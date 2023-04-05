package com.iConomy.util;

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
}
